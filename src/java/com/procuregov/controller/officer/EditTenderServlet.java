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
 * Servlet handling tender editing by Procurement Officers.
 * Officers can only edit tenders in Draft status.
 * Once published (Open), the tender is locked for editing.
 */
@MultipartConfig(maxFileSize = 5242880) // 5MB max
public class EditTenderServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(EditTenderServlet.class.getName());
    private TenderDAO tenderDAO;

    @Override
    public void init() throws ServletException {
        tenderDAO = new TenderDAOImpl();
    }

    /**
     * Displays the edit tender form for Draft status tenders.
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

        try {
            int tenderId = Integer.parseInt(idStr);
            Tender tender = tenderDAO.getById(tenderId);
            
            if (tender == null) {
                response.sendRedirect(request.getContextPath() + "/officer-dashboard");
                return;
            }

            // Only allow editing of Draft tenders
            if (!"Draft".equals(tender.getStatus())) {
                request.setAttribute("error", "Only tenders in Draft status can be edited.");
                request.setAttribute("tender", tender);
                request.getRequestDispatcher("/pages/officer/manage-tenders.jsp").forward(request, response);
                return;
            }

            request.setAttribute("tender", tender);
            request.setAttribute("userName", SessionUtil.getUserName(request));
            request.getRequestDispatcher("/pages/officer/edit-tender.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/officer-dashboard");
        }
    }

    /**
     * Processes tender edit form submission.
     * Updates tender fields while keeping reference number and status unchanged.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.isLoggedIn(request) || !SessionUtil.hasRole(request, "OFFICER")) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            int tenderId = Integer.parseInt(request.getParameter("id"));
            Tender tender = tenderDAO.getById(tenderId);

            if (tender == null) {
                response.sendRedirect(request.getContextPath() + "/officer-dashboard");
                return;
            }

            // Only allow editing of Draft tenders
            if (!"Draft".equals(tender.getStatus())) {
                request.setAttribute("error", "Only tenders in Draft status can be edited.");
                request.setAttribute("tender", tender);
                request.getRequestDispatcher("/pages/officer/manage-tenders.jsp").forward(request, response);
                return;
            }

            // Update tender fields
            tender.setTitle(request.getParameter("title"));
            tender.setCategory(request.getParameter("category"));
            tender.setDescription(request.getParameter("description"));
            
            String valueStr = request.getParameter("value");
            if (valueStr != null && !valueStr.trim().isEmpty()) {
                tender.setEstimatedValue(new BigDecimal(valueStr));
            }

            String deadlineStr = request.getParameter("deadline");
            if (deadlineStr != null && !deadlineStr.trim().isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
                tender.setDeadline(sdf.parse(deadlineStr));
            }

            // Handle show_estimated_value toggle
            String showEstValue = request.getParameter("showEstimatedValue");
            tender.setShowEstimatedValue(showEstValue != null && showEstValue.equalsIgnoreCase("on"));

            // Handle optional document upload
            Part filePart = request.getPart("notice");
            if (filePart != null && filePart.getSize() > 0) {
                String fileName = getSubmittedFileName(filePart);
                if (fileName != null && fileName.toLowerCase().endsWith(".pdf")) {
                    String uploadDir = getServletContext().getInitParameter("uploadDirectory");
                    if (uploadDir == null) {
                        uploadDir = System.getProperty("java.io.tmpdir");
                    }
                    
                    String newFilePath = uploadDir + File.separator + "tender_" + tenderId + "_" + System.currentTimeMillis() + ".pdf";
                    filePart.write(newFilePath);
                    tender.setNoticeDocumentPath(newFilePath);
                }
            }

            if (tenderDAO.update(tender)) {
                request.setAttribute("success", "Tender updated successfully.");
            } else {
                request.setAttribute("error", "Failed to update tender.");
            }

            request.setAttribute("tender", tender);
            request.setAttribute("userName", SessionUtil.getUserName(request));
            request.getRequestDispatcher("/pages/officer/manage-tenders.jsp").forward(request, response);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error editing tender", e);
            request.setAttribute("error", "An error occurred while editing the tender.");
            response.sendRedirect(request.getContextPath() + "/officer-dashboard");
        }
    }

    private String getSubmittedFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] items = contentDisp.split(";");
        for (String item : items) {
            if (item.trim().startsWith("filename")) {
                return item.substring(item.indexOf("=") + 2, item.length() - 1);
            }
        }
        return null;
    }
}
