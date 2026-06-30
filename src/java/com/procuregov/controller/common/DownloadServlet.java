package com.procuregov.controller.common;

import com.procuregov.dao.AwardDAO;
import com.procuregov.dao.impl.AwardDAOImpl;
import com.procuregov.model.Award;
import com.procuregov.service.PdfGenerationService;
import com.procuregov.util.SessionUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet serving uploaded files securely through the application.
 * Does not expose the server filesystem path - files are served
 * by filename only, and the upload directory is configured externally.
 * Auto-generates award confirmation PDFs on-demand if missing.
 */
public class DownloadServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(DownloadServlet.class.getName());
    private AwardDAO awardDAO;
    private PdfGenerationService pdfService;

    @Override
    public void init() throws ServletException {
        awardDAO = new AwardDAOImpl();
        try {
            pdfService = new PdfGenerationService();
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING, "PdfGenerationService could not be loaded - auto-generation disabled", t);
            pdfService = null;
        }
    }

    /**
     * Serves an uploaded file by filename parameter.
     * Access is restricted to authenticated users only.
     * If an award confirmation PDF is missing, it is auto-generated on-demand.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String fileName = request.getParameter("file");
        if (fileName == null || fileName.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "File parameter is required.");
            return;
        }

        // Prevent directory traversal attacks - allow subdirectory paths but block parent refs
        fileName = fileName.replace("..", "").replace("\\", "");

        String uploadDir = getUploadDir();
        File file = new File(uploadDir, fileName);

        // If file doesn't exist, try auto-generating award confirmation PDFs
        if (!file.exists() || !file.isFile()) {
            file = tryGenerateAwardPdf(fileName, uploadDir);
            if (file == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found.");
                return;
            }
        }

        // Determine content type
        String contentType = getServletContext().getMimeType(fileName);
        if (contentType == null) {
            if (fileName.endsWith(".pdf")) contentType = "application/pdf";
            else if (fileName.endsWith(".docx")) contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            else contentType = "application/octet-stream";
        }

        response.setContentType(contentType);
        response.setHeader("Content-Disposition", "inline; filename=\"" + file.getName() + "\"");
        response.setContentLengthLong(file.length());

        try (FileInputStream in = new FileInputStream(file);
             OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    /**
     * Attempts to auto-generate an award confirmation PDF if the requested
     * file is a confirmation document that doesn't exist on disk.
     * Tries multiple lookup strategies to find the award.
     * Returns the generated File if successful, null otherwise.
     */
    private File tryGenerateAwardPdf(String fileName, String uploadDir) {
        if (pdfService == null) {
            return null;
        }

        Award award = null;
        String tenderRef = null;

        // Strategy 1: Try extracting tender reference from standard pattern
        if (fileName.startsWith("awards/") && fileName.endsWith("-award-confirmation.pdf")) {
            tenderRef = fileName.substring("awards/".length(), fileName.length() - "-award-confirmation.pdf".length());
            award = awardDAO.getByTenderRef(tenderRef);
        }

        // Strategy 2: Look up award by its confirmation_document_path directly
        if (award == null && fileName.endsWith(".pdf")) {
            award = awardDAO.getByConfirmationPath(fileName);
        }

        if (award == null) {
            return null;
        }

        // Use the tender reference from the award record
        if (tenderRef == null) {
            tenderRef = award.getTenderRefNumber();
        }

        try {
            String generatedPath = pdfService.generateAwardConfirmationPdf(
                tenderRef, award.getTenderTitle(),
                award.getSupplierName(), award.getAwardedValue(),
                award.getJustification(), award.getAwardedAt() != null ? award.getAwardedAt() : new Date(),
                uploadDir);

            if (generatedPath != null) {
                LOGGER.info("Auto-generated award confirmation PDF: " + generatedPath);
                // Update DB path so future requests find the file directly
                if (!generatedPath.equals(award.getConfirmationDocumentPath())) {
                    awardDAO.updateConfirmationPath(award.getId(), generatedPath);
                }
                return new File(uploadDir, generatedPath);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to auto-generate award PDF for: " + fileName, e);
        }
        return null;
    }

    private String getUploadDir() {
        String uploadDir = getServletContext().getInitParameter("uploadDirectory");
        if (uploadDir == null || uploadDir.isEmpty()) {
            uploadDir = System.getProperty("user.home") + File.separator + "ProcureGov" + File.separator + "uploads";
        }
        return uploadDir;
    }
}
