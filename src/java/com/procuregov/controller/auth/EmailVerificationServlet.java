package com.procuregov.controller.auth;

import com.procuregov.dao.UserDAO;
import com.procuregov.dao.UserTokenDAO;
import com.procuregov.dao.impl.UserDAOImpl;
import com.procuregov.dao.impl.UserTokenDAOImpl;
import com.procuregov.model.UserToken;
import com.procuregov.service.EmailService;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * EmailVerificationServlet handles AJAX requests for email verification
 * during supplier registration. Two actions:
 *   action=send   → generates 6-digit code, emails it, stores in user_tokens
 *   action=verify → checks code against DB, marks verified in session
 */
public class EmailVerificationServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(EmailVerificationServlet.class.getName());
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
            LOGGER.log(Level.WARNING, "EmailService not available - email verification disabled", e);
            emailService = null;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String action = request.getParameter("action");
        String email = request.getParameter("email");

        if (email == null || email.trim().isEmpty()) {
            out.print("{\"success\":false,\"message\":\"Email is required.\"}");
            out.flush();
            return;
        }

        email = email.trim();

        if ("send".equals(action)) {
            handleSendCode(request, response, email, out);
        } else if ("verify".equals(action)) {
            handleVerifyCode(request, response, email, out);
        } else {
            out.print("{\"success\":false,\"message\":\"Invalid action.\"}");
            out.flush();
        }
    }

    /**
     * Generates a 6-digit verification code, emails it, stores in user_tokens.
     */
    private void handleSendCode(HttpServletRequest request, HttpServletResponse response, String email, PrintWriter out) {
        // Check if email already registered
        if (userDAO.emailExists(email)) {
            out.print("{\"success\":false,\"message\":\"This email is already registered.\"}");
            out.flush();
            return;
        }

        // Delete any existing ACCOUNT_CONFIRMATION codes for this email's user
        // Since user doesn't exist yet, we use email as lookup - store with a temp user_id of 0
        // Actually, we need a different approach: store the code keyed by email in session
        // Let's use session to track pending verification codes

        String code = generateCode();

        // Send code via email
        boolean emailSent = false;
        if (emailService != null && emailService.isConfigured()) {
            emailSent = emailService.sendEmailVerificationCode(email, code);
        }

        if (emailSent) {
            // Store code in session for later verification
            request.getSession().setAttribute("emailVerifyCode", code);
            request.getSession().setAttribute("emailVerifyTarget", email);
            request.getSession().setAttribute("emailVerified", false);
            request.getSession().setAttribute("emailVerifyCodeTime", System.currentTimeMillis());

            LOGGER.info("Verification code sent to: " + email);
            out.print("{\"success\":true,\"message\":\"Verification code sent to your email.\"}");
        } else {
            // Fallback: store code in session AND return it for on-screen display
            request.getSession().setAttribute("emailVerifyCode", code);
            request.getSession().setAttribute("emailVerifyTarget", email);
            request.getSession().setAttribute("emailVerified", false);
            request.getSession().setAttribute("emailVerifyCodeTime", System.currentTimeMillis());

            LOGGER.warning("Email not sent - returning code in response as fallback");
            out.print("{\"success\":true,\"message\":\"Email not configured. Code shown below.\",\"code\":\"" + code + "\"}");
        }
        out.flush();
    }

    /**
     * Verifies the code entered by the user against the session-stored code.
     */
    private void handleVerifyCode(HttpServletRequest request, HttpServletResponse response, String email, PrintWriter out) {
        String code = request.getParameter("code");

        if (code == null || code.trim().isEmpty()) {
            out.print("{\"success\":false,\"message\":\"Verification code is required.\"}");
            out.flush();
            return;
        }

        // Get stored code from session
        String storedCode = (String) request.getSession().getAttribute("emailVerifyCode");
        String storedEmail = (String) request.getSession().getAttribute("emailVerifyTarget");
        Long codeTime = (Long) request.getSession().getAttribute("emailVerifyCodeTime");

        if (storedCode == null || storedEmail == null) {
            out.print("{\"success\":false,\"message\":\"No verification code found. Please request a new one.\"}");
            out.flush();
            return;
        }

        // Check code expiry (10 minutes)
        if (codeTime != null && (System.currentTimeMillis() - codeTime) > 10 * 60 * 1000) {
            request.getSession().removeAttribute("emailVerifyCode");
            request.getSession().removeAttribute("emailVerifyTarget");
            request.getSession().removeAttribute("emailVerifyCodeTime");
            out.print("{\"success\":false,\"message\":\"Verification code has expired. Please request a new one.\"}");
            out.flush();
            return;
        }

        // Check email matches
        if (!email.equalsIgnoreCase(storedEmail)) {
            out.print("{\"success\":false,\"message\":\"Email does not match the verified email.\"}");
            out.flush();
            return;
        }

        // Check code matches
        if (code.trim().equals(storedCode)) {
            request.getSession().setAttribute("emailVerified", true);
            request.getSession().setAttribute("emailVerifiedAddress", email);
            // Remove code from session (one-time use)
            request.getSession().removeAttribute("emailVerifyCode");
            request.getSession().removeAttribute("emailVerifyCodeTime");

            out.print("{\"success\":true,\"message\":\"Email verified successfully!\"}");
        } else {
            out.print("{\"success\":false,\"message\":\"Invalid verification code.\"}");
        }
        out.flush();
    }

    /**
     * Generates a random 6-digit numeric code.
     */
    private String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
