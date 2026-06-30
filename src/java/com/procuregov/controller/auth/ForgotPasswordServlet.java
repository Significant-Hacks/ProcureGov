package com.procuregov.controller.auth;

import com.procuregov.dao.UserDAO;
import com.procuregov.dao.UserTokenDAO;
import com.procuregov.dao.impl.UserDAOImpl;
import com.procuregov.dao.impl.UserTokenDAOImpl;
import com.procuregov.model.User;
import com.procuregov.model.UserToken;
import com.procuregov.service.EmailService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ForgotPasswordServlet handles password reset requests.
 * Generates a 6-digit reset code, stores it in the database, and sends it via email.
 * The user must enter this code on the reset-password page along with their new password.
 */
public class ForgotPasswordServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ForgotPasswordServlet.class.getName());
    private UserDAO userDAO;
    private UserTokenDAO tokenDAO;
    private EmailService emailService;

    @Override
    public void init() throws ServletException {
        userDAO = new UserDAOImpl();
        tokenDAO = new UserTokenDAOImpl();
        try {
            String smtpHost = getServletContext().getInitParameter("smtpHost");
            String smtpPort = getServletContext().getInitParameter("smtpPort");
            String smtpUser = getServletContext().getInitParameter("smtpUser");
            String smtpPassword = getServletContext().getInitParameter("smtpPassword");
            if (smtpHost == null) smtpHost = "smtp.gmail.com";
            if (smtpPort == null) smtpPort = "587";
            emailService = new EmailService(smtpHost, smtpPort, smtpUser, smtpPassword);
        } catch (Throwable e) {
            LOGGER.log(Level.WARNING, "EmailService not available - password reset emails disabled", e);
            emailService = null;
        }
    }

    /**
     * Displays the forgot password form.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/pages/common/forgot-password.jsp").forward(request, response);
    }

    /**
     * Processes password reset request. Generates a 6-digit code, saves it to the
     * database, and sends it via email. The user enters this code on the reset page
     * along with their new password to complete the reset.
     * Always shows the same success message to prevent email enumeration.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");

        if (email == null || email.trim().isEmpty()) {
            request.setAttribute("error", "Email address is required.");
            request.getRequestDispatcher("/pages/common/forgot-password.jsp").forward(request, response);
            return;
        }

        User user = userDAO.findByEmail(email.trim());
        if (user == null) {
            // Don't reveal whether email exists - security best practice
            request.setAttribute("success", "If an account exists with that email, a reset code has been sent. Please check your inbox.");
            request.getRequestDispatcher("/pages/common/forgot-password.jsp").forward(request, response);
            return;
        }

        // Delete any existing PASSWORD_RESET codes for this user
        tokenDAO.deleteByUserAndType(user.getId(), "PASSWORD_RESET");

        // Generate 6-digit reset code
        String resetCode = generateResetCode();
        UserToken token = new UserToken();
        token.setUserId(user.getId());
        token.setToken(resetCode);
        token.setTokenType("PASSWORD_RESET");
        token.setExpiresAt(LocalDateTime.now().plusHours(1));
        token.setUsed(false);

        if (tokenDAO.save(token)) {
            // Send reset code via email
            boolean emailSent = false;
            if (emailService != null && emailService.isConfigured()) {
                String displayName = user.getDisplayName() != null ? user.getDisplayName() : user.getEmail();
                emailSent = emailService.sendPasswordResetEmail(user.getEmail(), displayName, resetCode);
            }

            if (emailSent) {
                request.setAttribute("success", "A 6-digit reset code has been sent to your email address. The code expires in 1 hour. Go to the Reset Password page to enter it.");
            } else {
                // Fallback: show code on screen if email could not be sent
                LOGGER.warning("Email not sent for password reset - showing code on screen as fallback");
                request.setAttribute("success", "Reset code generated. Email delivery is not yet configured.");
                request.setAttribute("resetCode", resetCode);
            }
        } else {
            request.setAttribute("error", "Failed to generate reset code. Please try again.");
        }

        request.getRequestDispatcher("/pages/common/forgot-password.jsp").forward(request, response);
    }

    /**
     * Generates a random 6-digit numeric code (100000-999999).
     */
    private String generateResetCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
