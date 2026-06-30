package com.procuregov.controller.supplier;

import com.procuregov.dao.BidDAO;
import com.procuregov.dao.BidTechnicalCriterionDAO;
import com.procuregov.dao.SupplierDAO;
import com.procuregov.dao.TenderDAO;
import com.procuregov.dao.impl.BidDAOImpl;
import com.procuregov.dao.impl.BidTechnicalCriterionDAOImpl;
import com.procuregov.dao.impl.SupplierDAOImpl;
import com.procuregov.dao.impl.TenderDAOImpl;
import com.procuregov.model.Bid;
import com.procuregov.model.BidTechnicalCriterion;
import com.procuregov.model.Tender;
import com.procuregov.service.EmailService;
import com.procuregov.util.SessionUtil;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

/**
 * Servlet handling bid submission by suppliers.
 * Enforces closing date server-side, one bid per tender, and file upload via Part API.
 * Uses POST-Redirect-GET pattern after successful submission.
 */
@MultipartConfig(maxFileSize = 10485760) // 10MB max
public class SubmitBidServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(SubmitBidServlet.class.getName());
    private TenderDAO tenderDAO;
    private BidDAO bidDAO;
    private BidTechnicalCriterionDAO criterionDAO;
    private SupplierDAO supplierDAO;
    private EmailService emailService;

    @Override
    public void init() throws ServletException {
        tenderDAO = new TenderDAOImpl();
        bidDAO = new BidDAOImpl();
        criterionDAO = new BidTechnicalCriterionDAOImpl();
        supplierDAO = new SupplierDAOImpl();
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
     * Displays the bid submission form for a given tender.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.isLoggedIn(request) || !SessionUtil.hasRole(request, "SUPPLIER")) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            int tenderId = Integer.parseInt(request.getParameter("tenderId"));
            Tender tender = tenderDAO.getById(tenderId);
            if (tender == null) {
                response.sendRedirect(request.getContextPath() + "/supplier-dashboard");
                return;
            }

            // Server-side closing date check
            if (!"Open".equals(tender.getStatus()) || new Date().after(tender.getDeadline())) {
                request.setAttribute("error", "This tender is no longer accepting bids.");
                request.setAttribute("tender", tender);
                request.getRequestDispatcher("/pages/supplier/tender-detail.jsp").forward(request, response);
                return;
            }

            // One-bid-per-tender check
            int supplierId = supplierDAO.getSupplierIdByUserId(SessionUtil.getUserId(request));
            if (hasExistingBid(supplierId, tenderId)) {
                request.setAttribute("error", "You have already submitted a bid for this tender.");
                request.setAttribute("tender", tender);
                request.setAttribute("hasBid", true);
                request.getRequestDispatcher("/pages/supplier/tender-detail.jsp").forward(request, response);
                return;
            }

            request.setAttribute("tender", tender);
            request.setAttribute("userName", SessionUtil.getUserName(request));
            request.getRequestDispatcher("/pages/supplier/submit-bid.jsp").forward(request, response);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error displaying bid form", e);
            response.sendRedirect(request.getContextPath() + "/supplier-dashboard");
        }
    }

    /**
     * Processes bid submission with server-side validation.
     * Enforces closing date, one-bid-per-tender, and file upload constraints.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.isLoggedIn(request) || !SessionUtil.hasRole(request, "SUPPLIER")) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            int tenderId = Integer.parseInt(request.getParameter("tenderId"));
            Tender tender = tenderDAO.getById(tenderId);

            if (tender == null) {
                response.sendRedirect(request.getContextPath() + "/supplier-dashboard");
                return;
            }

            // Server-side closing date enforcement
            if (!"Open".equals(tender.getStatus()) || new Date().after(tender.getDeadline())) {
                request.setAttribute("error", "The tender closing date has passed. Bids are no longer accepted.");
                request.setAttribute("tender", tender);
                doGet(request, response);
                return;
            }

            // One-bid-per-tender enforcement
            int supplierId = supplierDAO.getSupplierIdByUserId(SessionUtil.getUserId(request));
            if (hasExistingBid(supplierId, tenderId)) {
                request.setAttribute("error", "You have already submitted a bid for this tender. Only one bid per tender is allowed.");
                request.setAttribute("tender", tender);
                request.setAttribute("hasBid", true);
                request.getRequestDispatcher("/pages/supplier/tender-detail.jsp").forward(request, response);
                return;
            }

            String amountStr = request.getParameter("amount");
            String technicalCompliance = request.getParameter("technicalCompliance");
            String timelineStr = request.getParameter("timeline");

            // Validate fields
            if (amountStr == null || technicalCompliance == null || timelineStr == null ||
                amountStr.trim().isEmpty() || technicalCompliance.trim().isEmpty() || timelineStr.trim().isEmpty()) {
                request.setAttribute("error", "All fields are required.");
                request.setAttribute("tender", tender);
                doGet(request, response);
                return;
            }

            // Technical compliance max 600 chars
            if (technicalCompliance.length() > 600) {
                request.setAttribute("error", "Technical compliance statement must not exceed 600 characters.");
                request.setAttribute("tender", tender);
                doGet(request, response);
                return;
            }

            BigDecimal bidAmount = new BigDecimal(amountStr);
            int proposedTimelineDays = Integer.parseInt(timelineStr);

            // Handle supporting document upload
            String documentPath = null;
            Part filePart = request.getPart("document");
            if (filePart != null && filePart.getSize() > 0) {
                String contentType = filePart.getContentType();
                if (!"application/pdf".equals(contentType) &&
                    !"application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(contentType)) {
                    request.setAttribute("error", "Only PDF or DOCX files are accepted.");
                    request.setAttribute("tender", tender);
                    doGet(request, response);
                    return;
                }
                if (filePart.getSize() > 10 * 1024 * 1024) {
                    request.setAttribute("error", "File size must not exceed 10MB.");
                    request.setAttribute("tender", tender);
                    doGet(request, response);
                    return;
                }

                String uploadDir = getUploadDir();
                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();

                String fileName = "bid_" + supplierId + "_tender_" + tenderId + "_" + System.currentTimeMillis();
                String submittedFileName = filePart.getSubmittedFileName();
                if (submittedFileName != null && submittedFileName.endsWith(".docx")) {
                    fileName += ".docx";
                } else {
                    fileName += ".pdf";
                }
                filePart.write(uploadDir + File.separator + fileName);
                documentPath = fileName;
            }

            // Create Bid JavaBean
            Bid bid = new Bid();
            bid.setTenderId(tenderId);
            bid.setSupplierId(supplierId);
            bid.setAmount(bidAmount);
            bid.setTechnicalCompliance(technicalCompliance.trim());
            bid.setProposedTimelineDays(proposedTimelineDays);
            bid.setDocumentPath(documentPath);

            if (bidDAO.submit(bid)) {
                // Save structured technical criteria
                List<BidTechnicalCriterion> criteria = parseCriteria(request, bid.getId(), supplierId, tenderId);
                if (!criteria.isEmpty()) {
                    criterionDAO.insertBatch(criteria);
                }

                // Send bid received email notification
                if (emailService != null) {
                    try {
                        String supplierEmail = supplierDAO.getEmailByUserId(SessionUtil.getUserId(request));
                        emailService.notifyBidReceived(supplierEmail,
                            tender.getReferenceNumber(), tender.getTitle());
                    } catch (Exception ex) {
                        LOGGER.log(Level.WARNING, "Failed to send bid received email", ex);
                    }
                }
                // POST-Redirect-GET pattern
                response.sendRedirect(request.getContextPath() + "/tender-detail?id=" + tenderId + "&success=true");
            } else {
                request.setAttribute("error", "Failed to submit bid. Please try again.");
                request.setAttribute("tender", tender);
                doGet(request, response);
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error submitting bid", e);
            request.setAttribute("error", "An error occurred. Please try again.");
            doGet(request, response);
        }
    }

    private boolean hasExistingBid(int supplierId, int tenderId) {
        List<Bid> bids = bidDAO.getBySupplierId(supplierId);
        for (Bid b : bids) {
            if (b.getTenderId() == tenderId) return true;
        }
        return false;
    }


    /**
     * Parses structured technical criteria from the bid submission form.
     * Handles dropdown criteria types, custom criterion names, values, and optional evidence documents.
     */
    private List<BidTechnicalCriterion> parseCriteria(HttpServletRequest request, int bidId, int supplierId, int tenderId) {
        List<BidTechnicalCriterion> criteria = new ArrayList<>();
        String uploadDir = getUploadDir();
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        int index = 0;
        while (true) {
            String criterionType = request.getParameter("criterionType_" + index);
            if (criterionType == null || criterionType.trim().isEmpty()) break;

            String criterionName = request.getParameter("criterionName_" + index);
            String criterionValue = request.getParameter("criterionValue_" + index);

            // Skip empty criteria rows
            if (criterionValue == null || criterionValue.trim().isEmpty()) {
                index++;
                continue;
            }

            // For "Other" type, use the custom name; otherwise use the type as display name
            if ("Other".equals(criterionType) && criterionName != null && !criterionName.trim().isEmpty()) {
                criterionName = criterionName.trim();
            } else if (criterionName == null || criterionName.trim().isEmpty()) {
                criterionName = criterionType;
            }

            // Handle evidence document upload for this criterion
            String evidencePath = null;
            try {
                Part evidencePart = request.getPart("criterionEvidence_" + index);
                if (evidencePart != null && evidencePart.getSize() > 0) {
                    String contentType = evidencePart.getContentType();
                    if ("application/pdf".equals(contentType) ||
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(contentType)) {
                        if (evidencePart.getSize() <= 10 * 1024 * 1024) {
                            String fileName = "evidence_bid_" + supplierId + "_tender_" + tenderId + "_crit_" + index + "_" + System.currentTimeMillis();
                            String submittedFileName = evidencePart.getSubmittedFileName();
                            if (submittedFileName != null && submittedFileName.endsWith(".docx")) {
                                fileName += ".docx";
                            } else {
                                fileName += ".pdf";
                            }
                            evidencePart.write(uploadDir + File.separator + fileName);
                            evidencePath = fileName;
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error uploading evidence for criterion " + index, e);
            }

            BidTechnicalCriterion criterion = new BidTechnicalCriterion();
            criterion.setBidId(bidId);
            criterion.setCriterionName(criterionName);
            criterion.setCriterionType(criterionType);
            criterion.setCriterionValue(criterionValue.trim());
            criterion.setEvidenceDocumentPath(evidencePath);
            criteria.add(criterion);

            index++;
        }
        return criteria;
    }

    private String getUploadDir() {
        String uploadDir = getServletContext().getInitParameter("uploadDirectory");
        if (uploadDir == null || uploadDir.isEmpty()) {
            uploadDir = System.getProperty("user.home") + File.separator + "ProcureGov" + File.separator + "uploads";
        }
        return uploadDir;
    }
}
