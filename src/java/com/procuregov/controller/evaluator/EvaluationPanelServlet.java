package com.procuregov.controller.evaluator;

import com.procuregov.dao.BidDAO;
import com.procuregov.dao.BidTechnicalCriterionDAO;
import com.procuregov.dao.EvaluationDAO;
import com.procuregov.dao.EvaluatorDAO;
import com.procuregov.dao.TenderDAO;
import com.procuregov.dao.impl.BidDAOImpl;
import com.procuregov.dao.impl.BidTechnicalCriterionDAOImpl;
import com.procuregov.dao.impl.EvaluationDAOImpl;
import com.procuregov.dao.impl.EvaluatorDAOImpl;
import com.procuregov.dao.impl.TenderDAOImpl;
import com.procuregov.model.Bid;
import com.procuregov.model.BidTechnicalCriterion;
import com.procuregov.model.Evaluation;
import com.procuregov.model.Tender;
import com.procuregov.service.EvaluationService;
import com.procuregov.util.SessionUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet handling the evaluation panel for evaluators and officers.
 * Under Evaluation: shows scoring form; after own submission, shows other evaluators' scores.
 * Evaluated/Awarded: shows final ranked leaderboard with individual evaluator scores.
 */
public class EvaluationPanelServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(EvaluationPanelServlet.class.getName());
    private BidDAO bidDAO;
    private TenderDAO tenderDAO;
    private EvaluationDAO evaluationDAO;
    private BidTechnicalCriterionDAO criterionDAO;
    private EvaluatorDAO evaluatorDAO;
    private EvaluationService evaluationService;

    @Override
    public void init() throws ServletException {
        bidDAO = new BidDAOImpl();
        tenderDAO = new TenderDAOImpl();
        evaluationDAO = new EvaluationDAOImpl();
        criterionDAO = new BidTechnicalCriterionDAOImpl();
        evaluatorDAO = new EvaluatorDAOImpl();
        evaluationService = new EvaluationService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String role = SessionUtil.getUserRole(request);
        if (!"EVALUATOR".equals(role) && !"OFFICER".equals(role)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            int tenderId = Integer.parseInt(request.getParameter("tenderId"));
            Tender tender = tenderDAO.getById(tenderId);
            if (tender == null) {
                String dashboard = "OFFICER".equals(role) ? "/officer-dashboard" : "/evaluator-dashboard";
                response.sendRedirect(request.getContextPath() + dashboard);
                return;
            }

            String status = tender.getStatus();
            if (!"Under Evaluation".equals(status) && !"Evaluated".equals(status) && !"Awarded".equals(status)) {
                String dashboard = "OFFICER".equals(role) ? "/officer-dashboard" : "/evaluator-dashboard";
                response.sendRedirect(request.getContextPath() + dashboard);
                return;
            }

            List<Bid> bids = bidDAO.getByTenderId(tenderId);

            // Load technical criteria for each bid
            Map<Integer, List<BidTechnicalCriterion>> criteriaMap = new HashMap<>();
            for (Bid bid : bids) {
                criteriaMap.put(bid.getId(), criterionDAO.getByBidId(bid.getId()));
            }
            request.setAttribute("criteriaMap", criteriaMap);

            request.setAttribute("tender", tender);
            request.setAttribute("bids", bids);

            // For Evaluated/Awarded tenders, compute ranked bids with individual evaluator scores
            if ("Evaluated".equals(status) || "Awarded".equals(status)) {
                List<RankedBid> rankedBids = new ArrayList<>();
                for (Bid b : bids) {
                    double finalScore = evaluationService.getFinalScore(b.getId());
                    List<Evaluation> allScores = evaluationDAO.getByBidId(b.getId());
                    rankedBids.add(new RankedBid(b, finalScore, allScores));
                }
                Collections.sort(rankedBids, new Comparator<RankedBid>() {
                    @Override
                    public int compare(RankedBid a, RankedBid b) {
                        return Double.compare(b.finalScore, a.finalScore);
                    }
                });
                request.setAttribute("rankedBids", rankedBids);
            }

            // For Under Evaluation, check which bids the current user has already scored
            if ("Under Evaluation".equals(status)) {
                int evaluatorId = evaluatorDAO.getEvaluatorIdByUserId(SessionUtil.getUserId(request));
                // Map of bidId -> list of other evaluators' scores (only for bids this evaluator has scored)
                Map<Integer, List<Evaluation>> otherScoresMap = new HashMap<>();
                for (Bid bid : bids) {
                    List<Evaluation> evals = evaluationDAO.getByBidId(bid.getId());
                    boolean alreadyScored = false;
                    for (Evaluation e : evals) {
                        if (e.getEvaluatorId() == evaluatorId) {
                            alreadyScored = true;
                            break;
                        }
                    }
                    request.setAttribute("scored_bid_" + bid.getId(), alreadyScored);

                    // If this evaluator has scored this bid, show other evaluators' scores
                    if (alreadyScored) {
                        List<Evaluation> otherScores = new ArrayList<>();
                        for (Evaluation e : evals) {
                            if (e.getEvaluatorId() != evaluatorId) {
                                otherScores.add(e);
                            }
                        }
                        otherScoresMap.put(bid.getId(), otherScores);
                    }
                }
                request.setAttribute("evaluatorId", evaluatorId);
                request.setAttribute("otherScoresMap", otherScoresMap);
            }

            request.setAttribute("userName", SessionUtil.getUserName(request));
            request.getRequestDispatcher("/pages/evaluator/evaluation-panel.jsp").forward(request, response);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error displaying evaluation panel", e);
            String dashboard = "OFFICER".equals(SessionUtil.getUserRole(request)) ? "/officer-dashboard" : "/evaluator-dashboard";
            response.sendRedirect(request.getContextPath() + dashboard);
        }
    }

    /**
     * Inner class to hold a bid with its final score and individual evaluator scores for ranking display.
     */
    public static class RankedBid {
        private Bid bid;
        private double finalScore;
        private List<Evaluation> evaluatorScores;

        public RankedBid(Bid bid, double finalScore, List<Evaluation> evaluatorScores) {
            this.bid = bid;
            this.finalScore = finalScore;
            this.evaluatorScores = evaluatorScores;
        }

        public Bid getBid() { return bid; }
        public double getFinalScore() { return finalScore; }
        public List<Evaluation> getEvaluatorScores() { return evaluatorScores; }
        public int getId() { return bid.getId(); }
        public String getCompanyName() { return bid.getCompanyName(); }
        public BigDecimal getAmount() { return bid.getAmount(); }
        public String getDocumentPath() { return bid.getDocumentPath(); }
        public int getProposedTimelineDays() { return bid.getProposedTimelineDays(); }
        public String getTechnicalCompliance() { return bid.getTechnicalCompliance(); }
        public int getSupplierId() { return bid.getSupplierId(); }
    }

}
