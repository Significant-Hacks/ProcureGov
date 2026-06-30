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
 * Servlet handling tender detail view for suppliers.
 * Shows full tender information and a bid submission button if the tender is Open
 * and the supplier has not yet bid on it.
 */
@MultipartConfig(maxFileSize = 10485760)
public class TenderDetailServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(TenderDetailServlet.class.getName());
    private TenderDAO tenderDAO;
    private BidDAO bidDAO;
    private BidTechnicalCriterionDAO criterionDAO;
    private SupplierDAO supplierDAO;

    @Override
    public void init() throws ServletException {
        tenderDAO = new TenderDAOImpl();
        bidDAO = new BidDAOImpl();
        criterionDAO = new BidTechnicalCriterionDAOImpl();
        supplierDAO = new SupplierDAOImpl();
    }

    /**
     * Displays tender detail page for a supplier.
     * Checks if supplier has already submitted a bid for this tender.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            int tenderId = Integer.parseInt(request.getParameter("id"));
            Tender tender = tenderDAO.getById(tenderId);
            if (tender == null) {
                response.sendRedirect(request.getContextPath() + "/supplier-dashboard");
                return;
            }

            request.setAttribute("tender", tender);

            // Check if supplier already bid on this tender
            if ("SUPPLIER".equals(SessionUtil.getUserRole(request))) {
                int supplierId = supplierDAO.getSupplierIdByUserId(SessionUtil.getUserId(request));
                boolean hasBid = false;
                if (supplierId > 0) {
                    List<Bid> myBids = bidDAO.getBySupplierId(supplierId);
                    for (Bid b : myBids) {
                        if (b.getTenderId() == tenderId) {
                            hasBid = true;
                            request.setAttribute("existingBid", b);
                            // Load technical criteria for the bid
                            List<BidTechnicalCriterion> criteria = criterionDAO.getByBidId(b.getId());
                            request.setAttribute("existingCriteria", criteria);
                            break;
                        }
                    }
                }
                request.setAttribute("hasBid", hasBid);
                request.setAttribute("supplierId", supplierId);
            }

            request.setAttribute("userName", SessionUtil.getUserName(request));
            request.getRequestDispatcher("/pages/supplier/tender-detail.jsp").forward(request, response);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error displaying tender detail", e);
            response.sendRedirect(request.getContextPath() + "/supplier-dashboard");
        }
    }

    /**
     * Handles bid editing when supplier submits changes on an open tender they already bid on.
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

            int supplierId = supplierDAO.getSupplierIdByUserId(SessionUtil.getUserId(request));

            // Find existing bid
            Bid existingBid = null;
            List<Bid> myBids = bidDAO.getBySupplierId(supplierId);
            for (Bid b : myBids) {
                if (b.getTenderId() == tenderId) {
                    existingBid = b;
                    break;
                }
            }

            if (existingBid == null) {
                response.sendRedirect(request.getContextPath() + "/supplier-dashboard");
                return;
            }

            // Verify tender is still Open and deadline not passed
            if (!"Open".equals(tender.getStatus()) || new Date().after(tender.getDeadline())) {
                request.setAttribute("error", "This bid can no longer be edited. The tender is no longer accepting bids.");
                request.setAttribute("tender", tender);
                request.setAttribute("existingBid", existingBid);
                request.setAttribute("hasBid", true);
                request.setAttribute("existingCriteria", criterionDAO.getByBidId(existingBid.getId()));
                request.setAttribute("supplierId", supplierId);
                request.setAttribute("userName", SessionUtil.getUserName(request));
                request.getRequestDispatcher("/pages/supplier/tender-detail.jsp").forward(request, response);
                return;
            }

            String amountStr = request.getParameter("amount");
            String technicalCompliance = request.getParameter("technicalCompliance");
            String timelineStr = request.getParameter("timeline");

            if (amountStr == null || technicalCompliance == null || timelineStr == null ||
                amountStr.trim().isEmpty() || technicalCompliance.trim().isEmpty() || timelineStr.trim().isEmpty()) {
                request.setAttribute("error", "All fields are required.");
                request.setAttribute("tender", tender);
                request.setAttribute("existingBid", existingBid);
                request.setAttribute("hasBid", true);
                request.setAttribute("existingCriteria", criterionDAO.getByBidId(existingBid.getId()));
                request.setAttribute("supplierId", supplierId);
                request.setAttribute("userName", SessionUtil.getUserName(request));
                request.getRequestDispatcher("/pages/supplier/tender-detail.jsp").forward(request, response);
                return;
            }

            if (technicalCompliance.length() > 600) {
                request.setAttribute("error", "Technical compliance statement must not exceed 600 characters.");
                request.setAttribute("tender", tender);
                request.setAttribute("existingBid", existingBid);
                request.setAttribute("hasBid", true);
                request.setAttribute("existingCriteria", criterionDAO.getByBidId(existingBid.getId()));
                request.setAttribute("supplierId", supplierId);
                request.setAttribute("userName", SessionUtil.getUserName(request));
                request.getRequestDispatcher("/pages/supplier/tender-detail.jsp").forward(request, response);
                return;
            }

            BigDecimal bidAmount = new BigDecimal(amountStr);
            int proposedTimelineDays = Integer.parseInt(timelineStr);

            // Handle supporting document upload
            String documentPath = existingBid.getDocumentPath();
            Part filePart = request.getPart("document");
            if (filePart != null && filePart.getSize() > 0) {
                String contentType = filePart.getContentType();
                if (!"application/pdf".equals(contentType) &&
                    !"application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(contentType)) {
                    request.setAttribute("error", "Only PDF or DOCX files are accepted.");
                    request.setAttribute("tender", tender);
                    request.setAttribute("existingBid", existingBid);
                    request.setAttribute("hasBid", true);
                    request.setAttribute("existingCriteria", criterionDAO.getByBidId(existingBid.getId()));
                    request.setAttribute("supplierId", supplierId);
                    request.setAttribute("userName", SessionUtil.getUserName(request));
                    request.getRequestDispatcher("/pages/supplier/tender-detail.jsp").forward(request, response);
                    return;
                }
                if (filePart.getSize() > 10 * 1024 * 1024) {
                    request.setAttribute("error", "File size must not exceed 10MB.");
                    request.setAttribute("tender", tender);
                    request.setAttribute("existingBid", existingBid);
                    request.setAttribute("hasBid", true);
                    request.setAttribute("existingCriteria", criterionDAO.getByBidId(existingBid.getId()));
                    request.setAttribute("supplierId", supplierId);
                    request.setAttribute("userName", SessionUtil.getUserName(request));
                    request.getRequestDispatcher("/pages/supplier/tender-detail.jsp").forward(request, response);
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

            // Update bid
            existingBid.setAmount(bidAmount);
            existingBid.setTechnicalCompliance(technicalCompliance.trim());
            existingBid.setProposedTimelineDays(proposedTimelineDays);
            existingBid.setDocumentPath(documentPath);

            if (bidDAO.update(existingBid)) {
                // Delete old criteria and re-insert
                criterionDAO.deleteByBidId(existingBid.getId());
                List<BidTechnicalCriterion> criteria = parseCriteria(request, existingBid.getId(), supplierId, tenderId);
                if (!criteria.isEmpty()) {
                    criterionDAO.insertBatch(criteria);
                }
                response.sendRedirect(request.getContextPath() + "/tender-detail?id=" + tenderId + "&success=edit");
            } else {
                request.setAttribute("error", "Failed to update bid. Please try again.");
                request.setAttribute("tender", tender);
                request.setAttribute("existingBid", existingBid);
                request.setAttribute("hasBid", true);
                request.setAttribute("existingCriteria", criterionDAO.getByBidId(existingBid.getId()));
                request.setAttribute("supplierId", supplierId);
                request.setAttribute("userName", SessionUtil.getUserName(request));
                request.getRequestDispatcher("/pages/supplier/tender-detail.jsp").forward(request, response);
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating bid", e);
            response.sendRedirect(request.getContextPath() + "/supplier-dashboard");
        }
    }

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

            if (criterionValue == null || criterionValue.trim().isEmpty()) {
                index++;
                continue;
            }

            if ("Other".equals(criterionType) && criterionName != null && !criterionName.trim().isEmpty()) {
                criterionName = criterionName.trim();
            } else if (criterionName == null || criterionName.trim().isEmpty()) {
                criterionName = criterionType;
            }

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
