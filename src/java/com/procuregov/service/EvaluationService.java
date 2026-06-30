package com.procuregov.service;

import com.procuregov.dao.BidDAO;
import com.procuregov.dao.EvaluationDAO;
import com.procuregov.dao.EvaluatorDAO;
import com.procuregov.dao.TenderDAO;
import com.procuregov.dao.impl.BidDAOImpl;
import com.procuregov.dao.impl.EvaluationDAOImpl;
import com.procuregov.dao.impl.EvaluatorDAOImpl;
import com.procuregov.dao.impl.TenderDAOImpl;
import com.procuregov.model.Bid;
import com.procuregov.model.Evaluation;
import com.procuregov.model.Tender;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service class for bid evaluation calculations.
 * All score calculations must be performed here - not in Servlets or JSPs.
 * Implements weighted scoring: Price 40%, Technical 35%, Timeline 25%.
 */
public class EvaluationService {

    private static final Logger LOGGER = Logger.getLogger(EvaluationService.class.getName());
    private static final double PRICE_WEIGHT = 0.40;
    private static final double TECHNICAL_WEIGHT = 0.35;
    private static final double TIMELINE_WEIGHT = 0.25;

    private BidDAO bidDAO;
    private EvaluationDAO evaluationDAO;
    private EvaluatorDAO evaluatorDAO;
    private TenderDAO tenderDAO;

    public EvaluationService() {
        this.bidDAO = new BidDAOImpl();
        this.evaluationDAO = new EvaluationDAOImpl();
        this.evaluatorDAO = new EvaluatorDAOImpl();
        this.tenderDAO = new TenderDAOImpl();
    }

    /**
     * Calculates the Price Score for a bid.
     * Formula: (Lowest Bid Amount / This Bid Amount) x 100
     */
    public double calculatePriceScore(BigDecimal bidAmount, int tenderId) {
        List<Bid> bids = bidDAO.getByTenderId(tenderId);
        if (bids == null || bids.isEmpty()) return 0.0;

        BigDecimal lowest = null;
        for (Bid b : bids) {
            if (lowest == null || b.getAmount().compareTo(lowest) < 0) {
                lowest = b.getAmount();
            }
        }

        if (bidAmount.compareTo(BigDecimal.ZERO) == 0 || lowest == null) return 0.0;
        return lowest.divide(bidAmount, 6, RoundingMode.HALF_UP).doubleValue() * 100.0;
    }

    /**
     * Calculates the Delivery Timeline Score for a bid.
     * Formula: (Shortest Proposed Timeline / This Bid's Timeline) x 100
     */
    public double calculateTimelineScore(int proposedTimelineDays, int tenderId) {
        List<Bid> bids = bidDAO.getByTenderId(tenderId);
        if (bids == null || bids.isEmpty()) return 0.0;

        int shortest = Integer.MAX_VALUE;
        for (Bid b : bids) {
            if (b.getProposedTimelineDays() < shortest) {
                shortest = b.getProposedTimelineDays();
            }
        }

        if (proposedTimelineDays <= 0 || shortest == Integer.MAX_VALUE) return 0.0;
        return ((double) shortest / proposedTimelineDays) * 100.0;
    }

    /**
     * Calculates the Weighted Total Score for a bid.
     * Formula: (PriceScore x 0.40) + (TechnicalScore x 0.35) + (TimelineScore x 0.25)
     */
    public double calculateWeightedTotal(double priceScore, double technicalScore, double timelineScore) {
        return (priceScore * PRICE_WEIGHT) + (technicalScore * TECHNICAL_WEIGHT) + (timelineScore * TIMELINE_WEIGHT);
    }

    /**
     * Computes and returns a fully scored Evaluation object for a bid.
     * Price and Timeline scores are calculated automatically; technical score is provided by evaluator.
     */
    public Evaluation computeEvaluation(int bidId, int tenderId, int evaluatorId, double technicalScore) {
        Bid bid = bidDAO.getById(bidId);
        if (bid == null) return null;

        double priceScore = calculatePriceScore(bid.getAmount(), tenderId);
        double timelineScore = calculateTimelineScore(bid.getProposedTimelineDays(), tenderId);
        double weightedTotal = calculateWeightedTotal(priceScore, technicalScore, timelineScore);

        Evaluation eval = new Evaluation();
        eval.setBidId(bidId);
        eval.setTenderId(tenderId);
        eval.setEvaluatorId(evaluatorId);
        eval.setTechnicalScore(BigDecimal.valueOf(technicalScore));
        eval.setPriceScore(BigDecimal.valueOf(round2(priceScore)));
        eval.setTimelineScore(BigDecimal.valueOf(round2(timelineScore)));
        eval.setWeightedTotal(BigDecimal.valueOf(round2(weightedTotal)));
        eval.setBidAmount(bid.getAmount());
        eval.setSupplierName(bid.getCompanyName());
        eval.setTenderTitle(bid.getTenderTitle());
        eval.setTenderRefNumber(bid.getTenderRefNumber());

        return eval;
    }

    /**
     * Checks if all evaluators have scored all bids for a tender.
     * If so, automatically transitions the tender status to Evaluated.
     */
    public boolean checkAndTransitionToEvaluated(int tenderId) {
        try {
            Tender tender = tenderDAO.getById(tenderId);
            if (tender == null || !"Under Evaluation".equals(tender.getStatus())) return false;

            // Get all bids for this tender
            List<Bid> bids = bidDAO.getByTenderId(tenderId);
            if (bids == null || bids.isEmpty()) return false;

            // Get all evaluators assigned to this tender
            List<Integer> evaluatorIds = getAssignedEvaluators(tenderId);
            if (evaluatorIds.isEmpty()) return false;

            // Check if every evaluator has scored every bid
            for (int evaluatorId : evaluatorIds) {
                for (Bid bid : bids) {
                    boolean hasScored = hasEvaluatorScoredBid(evaluatorId, bid.getId());
                    if (!hasScored) return false; // Not all scored yet
                }
            }

            // All evaluators have scored all bids - transition to Evaluated
            tender.setStatus("Evaluated");
            return tenderDAO.update(tender);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error checking evaluation completion for tender " + tenderId, e);
            return false;
        }
    }

    /**
     * Gets the final averaged score for a bid across all evaluators.
     */
    public double getFinalScore(int bidId) {
        List<Evaluation> evals = evaluationDAO.getByBidId(bidId);
        if (evals == null || evals.isEmpty()) return 0.0;

        double total = 0.0;
        for (Evaluation e : evals) {
            total += e.getWeightedTotal().doubleValue();
        }
        return round2(total / evals.size());
    }

    private List<Integer> getAssignedEvaluators(int tenderId) {
        return evaluatorDAO.getAssignedEvaluatorIds(tenderId);
    }

    private boolean hasEvaluatorScoredBid(int evaluatorId, int bidId) {
        return evaluatorDAO.hasEvaluatorScoredBid(evaluatorId, bidId);
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
