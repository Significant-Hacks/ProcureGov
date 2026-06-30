package com.procuregov.service;

import com.procuregov.dao.BidDAO;
import com.procuregov.dao.EvaluatorDAO;
import com.procuregov.dao.TenderDAO;
import com.procuregov.dao.UserDAO;
import com.procuregov.dao.impl.BidDAOImpl;
import com.procuregov.dao.impl.EvaluatorDAOImpl;
import com.procuregov.dao.impl.TenderDAOImpl;
import com.procuregov.dao.impl.UserDAOImpl;
import com.procuregov.model.Tender;
import com.procuregov.util.OnlineUserTracker;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service class for evaluation notification business logic.
 * Handles three notification scenarios:
 * 1. Evaluation started: first evaluator submits a score → notify all other assigned evaluators
 * 2. Evaluation reminder: some evaluators have completed but others have not and are offline → email reminder
 * 3. Evaluation complete: all evaluators done, officers not online → email officers to award
 */
public class NotificationService {

    private static final Logger LOGGER = Logger.getLogger(NotificationService.class.getName());

    private EmailService emailService;
    private EvaluatorDAO evaluatorDAO;
    private TenderDAO tenderDAO;
    private UserDAO userDAO;
    private BidDAO bidDAO;

    public NotificationService(EmailService emailService) {
        this.emailService = emailService;
        this.evaluatorDAO = new EvaluatorDAOImpl();
        this.tenderDAO = new TenderDAOImpl();
        this.userDAO = new UserDAOImpl();
        this.bidDAO = new BidDAOImpl();
    }

    /**
     * Called when the first evaluation score is submitted for a tender.
     * Notifies all other assigned evaluators that evaluation has begun.
     * @param tenderId the tender being evaluated
     * @param submittingEvaluatorName name of the evaluator who submitted the first score
     * @param submittingUserId user ID of the evaluator who submitted (to exclude from notification)
     */
    public void notifyEvaluationStarted(int tenderId, String submittingEvaluatorName, int submittingUserId) {
        if (emailService == null || !emailService.isConfigured()) return;

        try {
            Tender tender = tenderDAO.getById(tenderId);
            if (tender == null) return;

            List<Object[]> evaluatorInfo = evaluatorDAO.getAssignedEvaluatorInfo(tenderId);
            for (Object[] info : evaluatorInfo) {
                int userId = (Integer) info[1];
                String email = (String) info[2];
                // Notify all evaluators except the one who just submitted
                if (userId != submittingUserId) {
                    emailService.notifyEvaluationStarted(email, tender.getReferenceNumber(),
                            tender.getTitle(), submittingEvaluatorName);
                }
            }
            LOGGER.info("Evaluation started notifications sent for tender " + tender.getReferenceNumber());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to send evaluation started notifications for tender " + tenderId, e);
        }
    }

    /**
     * Called after each score submission to check if any evaluators are lagging behind
     * and are not online. If so, sends them a reminder email.
     * @param tenderId the tender being evaluated
     */
    public void notifyOfflineEvaluatorsWithIncompleteEvaluations(int tenderId) {
        if (emailService == null || !emailService.isConfigured()) return;

        try {
            Tender tender = tenderDAO.getById(tenderId);
            if (tender == null) return;

            List<Object[]> evaluatorInfo = evaluatorDAO.getAssignedEvaluatorInfo(tenderId);
            int totalEvaluators = evaluatorInfo.size();
            if (totalEvaluators == 0) return;

            // Count how many evaluators have completed ALL their bids for this tender
            int completedCount = 0;
            for (Object[] info : evaluatorInfo) {
                int evaluatorId = (Integer) info[0];
                if (hasEvaluatorCompletedAllBids(evaluatorId, tenderId)) {
                    completedCount++;
                }
            }

            // Only send reminders if at least one evaluator has completed
            if (completedCount == 0) return;

            // Send reminders to evaluators who have NOT completed and are NOT online
            for (Object[] info : evaluatorInfo) {
                int evaluatorId = (Integer) info[0];
                int userId = (Integer) info[1];
                String email = (String) info[2];

                boolean completed = hasEvaluatorCompletedAllBids(evaluatorId, tenderId);
                boolean online = OnlineUserTracker.isOnline(userId);

                if (!completed && !online) {
                    emailService.notifyEvaluationReminder(email, tender.getReferenceNumber(),
                            tender.getTitle(), completedCount, totalEvaluators);
                    LOGGER.info("Evaluation reminder sent to " + email + " for tender " + tender.getReferenceNumber());
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to send evaluation reminder notifications for tender " + tenderId, e);
        }
    }

    /**
     * Called when all evaluations are complete for a tender (status transitions to Evaluated).
     * Notifies procurement officers who are not currently online.
     * @param tenderId the tender that has been fully evaluated
     */
    public void notifyOfficersEvaluationComplete(int tenderId) {
        if (emailService == null || !emailService.isConfigured()) return;

        try {
            Tender tender = tenderDAO.getById(tenderId);
            if (tender == null) return;

            List<Object[]> officers = userDAO.getOfficerEmails();
            for (Object[] officer : officers) {
                int userId = (Integer) officer[0];
                String email = (String) officer[1];

                if (!OnlineUserTracker.isOnline(userId)) {
                    emailService.notifyEvaluationComplete(email, tender.getReferenceNumber(), tender.getTitle());
                    LOGGER.info("Evaluation complete notification sent to officer " + email + " for tender " + tender.getReferenceNumber());
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to send evaluation complete notifications for tender " + tenderId, e);
        }
    }

    /**
     * Checks if an evaluator has scored all bids for a tender.
     */
    private boolean hasEvaluatorCompletedAllBids(int evaluatorId, int tenderId) {
        int totalBids = bidDAO.countByTenderId(tenderId);
        if (totalBids == 0) return true;
        int scoredBids = evaluatorDAO.countScoredBidsByEvaluatorAndTender(evaluatorId, tenderId);
        return scoredBids >= totalBids;
    }
}
