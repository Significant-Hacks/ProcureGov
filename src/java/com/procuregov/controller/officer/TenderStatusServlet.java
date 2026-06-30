package com.procuregov.controller.officer;

import com.procuregov.dao.EvaluatorDAO;
import com.procuregov.dao.SupplierDAO;
import com.procuregov.dao.TenderDAO;
import com.procuregov.dao.impl.EvaluatorDAOImpl;
import com.procuregov.dao.impl.SupplierDAOImpl;
import com.procuregov.dao.impl.TenderDAOImpl;
import com.procuregov.model.Tender;
import com.procuregov.service.EmailService;
import com.procuregov.util.SessionUtil;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet handling tender status transitions by Procurement Officers.
 * Enforces the lifecycle: Draft → Open → Closed (automatic) → Under Evaluation → Evaluated (automatic) → Awarded.
 * Status cannot skip stages or move backwards.
 */
public class TenderStatusServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(TenderStatusServlet.class.getName());
    private TenderDAO tenderDAO;
    private SupplierDAO supplierDAO;
    private EvaluatorDAO evaluatorDAO;
    private EmailService emailService;

    @Override
    public void init() throws ServletException {
        tenderDAO = new TenderDAOImpl();
        supplierDAO = new SupplierDAOImpl();
        evaluatorDAO = new EvaluatorDAOImpl();
        try {
            String smtpHost = getServletContext().getInitParameter("smtpHost");
            String smtpPort = getServletContext().getInitParameter("smtpPort");
            String smtpUser = getServletContext().getInitParameter("smtpUser");
            String smtpPassword = getServletContext().getInitParameter("smtpPassword");
            if (smtpHost == null) smtpHost = "smtp.gmail.com";
            if (smtpPort == null) smtpPort = "587";
            emailService = new EmailService(smtpHost, smtpPort, smtpUser, smtpPassword);
        } catch (Throwable e) {
            LOGGER.log(Level.WARNING, "EmailService not available - email notifications disabled", e);
            emailService = null;
        }
    }

    /**
     * Displays the tender status management page.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.isLoggedIn(request) || !SessionUtil.hasRole(request, "OFFICER")) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String idStr = request.getParameter("id");
        if (idStr == null) {
            response.sendRedirect(request.getContextPath() + "/officer-dashboard");
            return;
        }

        int tenderId = Integer.parseInt(idStr);
        Tender tender = tenderDAO.getById(tenderId);
        if (tender == null) {
            response.sendRedirect(request.getContextPath() + "/officer-dashboard");
            return;
        }

        request.setAttribute("tender", tender);
        request.setAttribute("userName", SessionUtil.getUserName(request));
        request.getRequestDispatcher("/pages/officer/manage-tenders.jsp").forward(request, response);
    }

    /**
     * Processes status transition requests.
     * Validates that the transition is allowed before applying.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.isLoggedIn(request) || !SessionUtil.hasRole(request, "OFFICER")) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            int tenderId = Integer.parseInt(request.getParameter("id"));
            String action = request.getParameter("action");

            Tender tender = tenderDAO.getById(tenderId);
            if (tender == null) {
                response.sendRedirect(request.getContextPath() + "/officer-dashboard");
                return;
            }

            String currentStatus = tender.getStatus();
            String newStatus = null;

            // Enforce valid transitions only
            switch (action) {
                case "publish":
                    if ("Draft".equals(currentStatus)) newStatus = "Open";
                    break;
                case "startEvaluation":
                    if ("Closed".equals(currentStatus)) newStatus = "Under Evaluation";
                    break;
                case "award":
                    if ("Evaluated".equals(currentStatus)) newStatus = "Awarded";
                    break;
                default:
                    break;
            }

            if (newStatus != null) {
                tender.setStatus(newStatus);
                if (tenderDAO.update(tender)) {
                    request.setAttribute("success", "Tender status updated to " + newStatus);
                    // Auto-assign all evaluators when starting evaluation
                    if ("Under Evaluation".equals(newStatus)) {
                        int assigned = evaluatorDAO.assignAllEvaluatorsToTender(tenderId);
                        LOGGER.log(Level.INFO, "Assigned " + assigned + " evaluators to tender " + tenderId);
                    }
                    // Send email notification when tender is published
                    if ("Open".equals(newStatus) && emailService != null) {
                        try {
                            List<String> supplierEmails = supplierDAO.getAllActiveEmails();
                            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM yyyy HH:mm");
                            emailService.notifyTenderPublished(supplierEmails,
                                tender.getReferenceNumber(), tender.getTitle(),
                                sdf.format(tender.getDeadline()));
                        } catch (Exception ex) {
                            LOGGER.log(Level.WARNING, "Failed to send tender published emails", ex);
                        }
                    }
                } else {
                    request.setAttribute("error", "Failed to update tender status.");
                }
            } else {
                request.setAttribute("error", "Invalid status transition from " + currentStatus + " for action " + action);
            }

            request.setAttribute("tender", tenderDAO.getById(tenderId));
            request.setAttribute("userName", SessionUtil.getUserName(request));
            request.getRequestDispatcher("/pages/officer/manage-tenders.jsp").forward(request, response);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating tender status", e);
            response.sendRedirect(request.getContextPath() + "/officer-dashboard");
        }
    }

}
