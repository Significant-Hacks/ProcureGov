package com.procuregov.controller.officer;

import com.procuregov.dao.TenderDAO;
import com.procuregov.dao.impl.TenderDAOImpl;
import com.procuregov.model.Tender;
import com.procuregov.util.SessionUtil;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

/**
 * Servlet handling tender creation by Procurement Officers.
 * Generates reference numbers automatically (MPW-YYYY-NNNN),
 * handles PDF upload via Part API, and saves tenders in Draft status.
 */
@MultipartConfig(maxFileSize = 5242880) // 5MB max
public class CreateTenderServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(CreateTenderServlet.class.getName());
    private TenderDAO tenderDAO;

    @Override
    public void init() throws ServletException {
        tenderDAO = new TenderDAOImpl();
    }

    /**
     * Displays the create tender form.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.isLoggedIn(request) || !SessionUtil.hasRole(request, "OFFICER")) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        request.setAttribute("userName", SessionUtil.getUserName(request));
        request.getRequestDispatcher("/pages/officer/create-tender.jsp").forward(request, response);
    }

    /**
     * Processes tender creation form submission.
     * Generates reference number, handles file upload, saves as Draft.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.isLoggedIn(request) || !SessionUtil.hasRole(request, "OFFICER")) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            String title = request.getParameter("title");
            String category = request.getParameter("category");
            String description = request.getParameter("description");
            String valueStr = request.getParameter("value");
            String deadlineStr = request.getParameter("deadline");

            // Validate required fields
            if (title == null || title.trim().isEmpty() ||
                category == null || description == null ||
                valueStr == null || deadlineStr == null) {
                request.setAttribute("error", "All fields are required.");
                doGet(request, response);
                return;
            }

            BigDecimal estimatedValue = new BigDecimal(valueStr);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            Date deadline = sdf.parse(deadlineStr);

            // Generate unique reference number: MPW-YYYY-NNNN
            String referenceNumber = generateReferenceNumber();

            // Handle file upload
            String documentPath = null;
            Part filePart = request.getPart("notice");
            if (filePart != null && filePart.getSize() > 0) {
                String contentType = filePart.getContentType();
                if (!"application/pdf".equals(contentType)) {
                    request.setAttribute("error", "Only PDF files are accepted for the tender notice.");
                    doGet(request, response);
                    return;
                }
                if (filePart.getSize() > 5 * 1024 * 1024) {
                    request.setAttribute("error", "File size must not exceed 5MB.");
                    doGet(request, response);
                    return;
                }

                String uploadDir = getUploadDir();
                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();

                String fileName = referenceNumber + ".pdf";
                String filePath = uploadDir + File.separator + fileName;
                filePart.write(filePath);
                documentPath = fileName;
            }

            // Create Tender JavaBean
            Tender tender = new Tender();
            tender.setReferenceNumber(referenceNumber);
            tender.setTitle(title.trim());
            tender.setCategory(category);
            tender.setDescription(description.trim());
            tender.setEstimatedValue(estimatedValue);
            tender.setDeadline(deadline);
            tender.setStatus("Draft");
            tender.setCreatedBy(SessionUtil.getUserId(request));
            tender.setNoticeDocumentPath(documentPath);

            // Handle show_estimated_value toggle (checkbox: present = true, absent = false)
            String showEstValue = request.getParameter("showEstimatedValue");
            tender.setShowEstimatedValue(showEstValue != null && showEstValue.equalsIgnoreCase("on"));

            if (tenderDAO.create(tender)) {
                response.sendRedirect(request.getContextPath() + "/officer-dashboard");
            } else {
                request.setAttribute("error", "Failed to create tender. Please try again.");
                doGet(request, response);
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating tender", e);
            request.setAttribute("error", "An error occurred. Please try again.");
            doGet(request, response);
        }
    }

    /**
     * Generates a unique reference number in format MPW-YYYY-NNNN.
     * NNNN is a sequential number padded to 4 digits.
     */
    private String generateReferenceNumber() {
        String year = new SimpleDateFormat("yyyy").format(new Date());
        int seq = getNextSequence();
        return String.format("MPW-%s-%04d", year, seq);
    }

    /**
     * Gets the next sequential number for reference generation.
     * Uses try-with-resources for proper connection handling.
     */
    private int getNextSequence() {
        String year = new SimpleDateFormat("yyyy").format(new Date());
        String sql = "SELECT COUNT(*) as cnt FROM tenders WHERE reference_number LIKE ?";
        try (java.sql.Connection conn = com.procuregov.util.DBConnectionUtil.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "MPW-" + year + "-%");
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cnt") + 1;
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating reference number", e);
        }
        return 1;
    }

    /**
     * Gets the configurable upload directory from context init parameter.
     * Falls back to user home + /ProcureGov/uploads/.
     */
    private String getUploadDir() {
        String uploadDir = getServletContext().getInitParameter("uploadDirectory");
        if (uploadDir == null || uploadDir.isEmpty()) {
            uploadDir = System.getProperty("user.home") + File.separator + "ProcureGov" + File.separator + "uploads";
        }
        return uploadDir;
    }
}
