package com.procuregov.controller.evaluator;

import com.procuregov.dao.EvaluatorDAO;
import com.procuregov.dao.EvaluationDAO;
import com.procuregov.dao.impl.EvaluatorDAOImpl;
import com.procuregov.dao.impl.EvaluationDAOImpl;
import com.procuregov.model.Evaluation;
import com.procuregov.service.EmailService;
import com.procuregov.service.EvaluationService;
import com.procuregov.service.NotificationService;
import com.procuregov.util.SessionUtil;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet handling score submission by evaluators.
 * Uses EvaluationService for all score calculations (price, timeline, weighted total).
 * After each submission, checks if all evaluators have scored all bids
 * to trigger automatic Evaluated status transition.
 */
public class SubmitScoreServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(SubmitScoreServlet.class.getName());
    private EvaluationDAO evaluationDAO;
    private EvaluatorDAO evaluatorDAO;
    private EvaluationService evaluationService;
    private NotificationService notificationService;

    @Override
    public void init() throws ServletException {
        evaluationDAO = new EvaluationDAOImpl();
        evaluatorDAO = new EvaluatorDAOImpl();
        evaluationService = new EvaluationService();
        try {
            String smtpHost = getServletContext().getInitParameter("smtpHost");
            String smtpPort = getServletContext().getInitParameter("smtpPort");
            String smtpUser = getServletContext().getInitParameter("smtpUser");
            String smtpPassword = getServletContext().getInitParameter("smtpPassword");
            if (smtpHost == null) smtpHost = "smtp.gmail.com";
            if (smtpPort == null) smtpPort = "587";
            EmailService emailService = new EmailService(smtpHost, smtpPort, smtpUser, smtpPassword);
            notificationService = new NotificationService(emailService);
        } catch (Throwable e) {
            LOGGER.log(Level.WARNING, "NotificationService not available - email notifications disabled", e);
            notificationService = null;
        }
    }

    /**
     * Processes technical score submission for a bid.
     * Price and timeline scores are auto-calculated by EvaluationService.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
            int bidId = Integer.parseInt(request.getParameter("bidId"));
            int tenderId = Integer.parseInt(request.getParameter("tenderId"));
            double technicalScore = Double.parseDouble(request.getParameter("technicalScore"));

            // Validate technical score range 0-100
            if (technicalScore < 0 || technicalScore > 100) {
                request.setAttribute("error", "Technical score must be between 0 and 100.");
                response.sendRedirect(request.getContextPath() + "/evaluation-panel?tenderId=" + tenderId);
                return;
            }

            int evaluatorId = evaluatorDAO.getEvaluatorIdByUserId(SessionUtil.getUserId(request));

            // Check if already scored
            java.util.List<Evaluation> existing = evaluationDAO.getByBidId(bidId);
            for (Evaluation e : existing) {
                if (e.getEvaluatorId() == evaluatorId) {
                    request.setAttribute("error", "You have already scored this bid.");
                    response.sendRedirect(request.getContextPath() + "/evaluation-panel?tenderId=" + tenderId);
                    return;
                }
            }

            // Use EvaluationService to compute all scores
            Evaluation evaluation = evaluationService.computeEvaluation(bidId, tenderId, evaluatorId, technicalScore);
            if (evaluation == null) {
                request.setAttribute("error", "Error computing evaluation scores.");
                response.sendRedirect(request.getContextPath() + "/evaluation-panel?tenderId=" + tenderId);
                return;
            }

            // Check if this is the first evaluation score for this tender (before saving)
            boolean isFirstEvaluationForTender = !evaluatorDAO.hasAnyEvaluationForTender(tenderId);

            // Save evaluation
            if (evaluationDAO.score(evaluation)) {
                if (notificationService != null) {
                    try {
                        // Notify other evaluators that evaluation has started (first score for this tender)
                        if (isFirstEvaluationForTender) {
                            String evaluatorName = SessionUtil.getUserName(request);
                            int userId = SessionUtil.getUserId(request);
                            notificationService.notifyEvaluationStarted(tenderId, evaluatorName, userId);
                        }
                        // Remind offline evaluators who haven't completed
                        notificationService.notifyOfflineEvaluatorsWithIncompleteEvaluations(tenderId);
                    } catch (Exception ex) {
                        LOGGER.log(Level.WARNING, "Failed to send evaluation notifications", ex);
                    }
                }

                // Check if all evaluators have scored all bids - auto transition to Evaluated
                boolean transitioned = evaluationService.checkAndTransitionToEvaluated(tenderId);

                // If tender transitioned to Evaluated, notify officers who are not online
                if (transitioned && notificationService != null) {
                    try {
                        notificationService.notifyOfficersEvaluationComplete(tenderId);
                    } catch (Exception ex) {
                        LOGGER.log(Level.WARNING, "Failed to send officer evaluation complete notifications", ex);
                    }
                }
                response.sendRedirect(request.getContextPath() + "/evaluation-panel?tenderId=" + tenderId);
            } else {
                request.setAttribute("error", "Failed to save evaluation.");
                response.sendRedirect(request.getContextPath() + "/evaluation-panel?tenderId=" + tenderId);
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error submitting score", e);
            response.sendRedirect(request.getContextPath() + "/evaluator-dashboard");
        }
    }

}
