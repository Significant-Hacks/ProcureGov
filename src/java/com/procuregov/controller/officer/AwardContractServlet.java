package com.procuregov.controller.officer;

import com.procuregov.dao.AwardDAO;
import com.procuregov.dao.BidDAO;
import com.procuregov.dao.EvaluationDAO;
import com.procuregov.dao.SupplierDAO;
import com.procuregov.dao.TenderDAO;
import com.procuregov.dao.impl.AwardDAOImpl;
import com.procuregov.dao.impl.BidDAOImpl;
import com.procuregov.dao.impl.EvaluationDAOImpl;
import com.procuregov.dao.impl.SupplierDAOImpl;
import com.procuregov.dao.impl.TenderDAOImpl;
import com.procuregov.model.Award;
import com.procuregov.model.Bid;
import com.procuregov.model.Evaluation;
import com.procuregov.model.Tender;
import com.procuregov.service.EmailService;
import com.procuregov.service.EvaluationService;
import com.procuregov.service.PdfGenerationService;
import com.procuregov.util.SessionUtil;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
 * Servlet handling contract award by Procurement Officers.
 * Displays ranked bids by final score with individual evaluator breakdowns,
 * allows officer to select winner and enter justification, then transitions
 * tender to Awarded status.
 */
@MultipartConfig(maxFileSize = 10485760) // 10MB max
public class AwardContractServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AwardContractServlet.class.getName());
    private TenderDAO tenderDAO;
    private BidDAO bidDAO;
    private AwardDAO awardDAO;
    private EvaluationDAO evaluationDAO;
    private SupplierDAO supplierDAO;
    private EvaluationService evaluationService;
    private EmailService emailService;
    private PdfGenerationService pdfService;

    @Override
    public void init() throws ServletException {
        tenderDAO = new TenderDAOImpl();
        bidDAO = new BidDAOImpl();
        awardDAO = new AwardDAOImpl();
        evaluationDAO = new EvaluationDAOImpl();
        supplierDAO = new SupplierDAOImpl();
        evaluationService = new EvaluationService();
        try {
            pdfService = new PdfGenerationService();
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING, "PdfGenerationService not available - PDF auto-generation disabled", t);
            pdfService = null;
        }
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
     * Displays the award contract page with ranked bids and individual evaluator scores.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String role = SessionUtil.getUserRole(request);
        if (!"OFFICER".equals(role) && !"EVALUATOR".equals(role)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            int tenderId = Integer.parseInt(request.getParameter("id"));
            Tender tender = tenderDAO.getById(tenderId);
            if (tender == null) {
                String dashboard = "OFFICER".equals(role) ? "/officer-dashboard" : "/evaluator-dashboard";
                response.sendRedirect(request.getContextPath() + dashboard);
                return;
            }

            if (!"Evaluated".equals(tender.getStatus()) && !"Awarded".equals(tender.getStatus())) {
                String dashboard = "OFFICER".equals(role) ? "/officer-dashboard" : "/evaluator-dashboard";
                response.sendRedirect(request.getContextPath() + dashboard);
                return;
            }

            request.setAttribute("isOfficer", "OFFICER".equals(role));

            // Get bids and rank by final score, including individual evaluator scores
            List<Bid> bids = bidDAO.getByTenderId(tenderId);
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

            request.setAttribute("tender", tender);
            request.setAttribute("rankedBids", rankedBids);
            request.setAttribute("userName", SessionUtil.getUserName(request));
            request.getRequestDispatcher("/pages/officer/award-contract.jsp").forward(request, response);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error displaying award page", e);
            response.sendRedirect(request.getContextPath() + "/officer-dashboard");
        }
    }

    /**
     * Processes the award: validates awarded value, saves award record, transitions tender to Awarded.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.isLoggedIn(request) || !SessionUtil.hasRole(request, "OFFICER")) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            int tenderId = Integer.parseInt(request.getParameter("tenderId"));
            int winningBidId = Integer.parseInt(request.getParameter("bidId"));
            String justification = request.getParameter("justification");
            String awardedValueStr = request.getParameter("awardedValue");

            if (justification == null || justification.trim().isEmpty() ||
                awardedValueStr == null || awardedValueStr.trim().isEmpty()) {
                request.setAttribute("error", "Justification and awarded value are required.");
                doGet(request, response);
                return;
            }

            Tender tender = tenderDAO.getById(tenderId);
            if (tender == null || !"Evaluated".equals(tender.getStatus())) {
                response.sendRedirect(request.getContextPath() + "/officer-dashboard");
                return;
            }

            BigDecimal awardedValue = new BigDecimal(awardedValueStr);

            // Validate awarded value is positive
            if (awardedValue.compareTo(BigDecimal.ZERO) <= 0) {
                request.setAttribute("error", "Awarded value must be a positive amount.");
                doGet(request, response);
                return;
            }

            // Validate awarded value >= winning bid amount
            Bid winningBid = bidDAO.getById(winningBidId);
            if (winningBid != null && awardedValue.compareTo(winningBid.getAmount()) < 0) {
                request.setAttribute("error", "Awarded value (M " + awardedValue.toPlainString() +
                    ") must be at least equal to the winning bid amount (M " + winningBid.getAmount().toPlainString() + ").");
                doGet(request, response);
                return;
            }

            // Handle confirmation document upload (optional override)
            String confirmationDocPath = null;
            try {
                Part confirmPart = request.getPart("confirmationDocument");
                if (confirmPart != null && confirmPart.getSize() > 0) {
                    String contentType = confirmPart.getContentType();
                    if ("application/pdf".equals(contentType) ||
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(contentType)) {
                        if (confirmPart.getSize() <= 10 * 1024 * 1024) {
                            String uploadDir = getUploadDir();
                            File dir = new File(uploadDir);
                            if (!dir.exists()) dir.mkdirs();
                            String fileName = "award_tender_" + tenderId + "_confirmation_" + System.currentTimeMillis();
                            String submittedFileName = confirmPart.getSubmittedFileName();
                            if (submittedFileName != null && submittedFileName.endsWith(".docx")) {
                                fileName += ".docx";
                            } else {
                                fileName += ".pdf";
                            }
                            confirmPart.write(uploadDir + File.separator + fileName);
                            confirmationDocPath = fileName;
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error uploading confirmation document", e);
            }

            // Auto-generate confirmation PDF if no document was uploaded
            if (confirmationDocPath == null && pdfService != null) {
                try {
                    String supplierName = supplierDAO.getSupplierNameByBidId(winningBidId);
                    String uploadDir = getUploadDir();
                    confirmationDocPath = pdfService.generateAwardConfirmationPdf(
                        tender.getReferenceNumber(), tender.getTitle(),
                        supplierName, awardedValue,
                        justification.trim(), new Date(),
                        uploadDir);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error auto-generating confirmation PDF", e);
                }
            }

            // Always set a default confirmation path so the download link appears
            // and the DownloadServlet can auto-generate the PDF on demand
            if (confirmationDocPath == null) {
                confirmationDocPath = "awards/" + tender.getReferenceNumber() + "-award-confirmation.pdf";
            }

            Award award = new Award();
            award.setTenderId(tenderId);
            award.setWinningBidId(winningBidId);
            award.setAwardedValue(awardedValue);
            award.setJustification(justification.trim());
            award.setAwardedBy(SessionUtil.getUserId(request));
            award.setConfirmationDocumentPath(confirmationDocPath);

            if (awardDAO.create(award)) {
                // Transition tender to Awarded
                tender.setStatus("Awarded");
                tenderDAO.update(tender);

                // Send award notification emails to ALL bidding suppliers with Won/Not Won differentiation
                if (emailService != null) {
                    try {
                        int winningSupplierId = supplierDAO.getSupplierIdByBidId(winningBidId);
                        String awardNoticeLink = request.getRequestURL().toString()
                                .replace(request.getRequestURI(), request.getContextPath() + "/award-notice?tender=" + tenderId);
                        List<Object[]> allBidderInfo = supplierDAO.getBidderInfo(tenderId);
                        for (Object[] bidder : allBidderInfo) {
                            try {
                                String bidderEmail = (String) bidder[0];
                                int bidderSupplierId = (Integer) bidder[1];
                                boolean isWinner = (bidderSupplierId == winningSupplierId);
                                emailService.notifyAwardDecision(bidderEmail,
                                    tender.getReferenceNumber(), tender.getTitle(),
                                    awardedValue.toPlainString(), isWinner, awardNoticeLink);
                            } catch (Exception ex) {
                                LOGGER.log(Level.WARNING, "Failed to send award email to: " + bidder[0], ex);
                            }
                        }
                    } catch (Exception ex) {
                        LOGGER.log(Level.WARNING, "Failed to send award notification emails", ex);
                    }
                }

                response.sendRedirect(request.getContextPath() + "/award-notice?tender=" + tenderId);
            } else {
                request.setAttribute("error", "Failed to create award record.");
                doGet(request, response);
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing award", e);
            response.sendRedirect(request.getContextPath() + "/officer-dashboard");
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

    private String getUploadDir() {
        String uploadDir = getServletContext().getInitParameter("uploadDirectory");
        if (uploadDir == null || uploadDir.isEmpty()) {
            uploadDir = System.getProperty("user.home") + File.separator + "ProcureGov" + File.separator + "uploads";
        }
        return uploadDir;
    }

}
