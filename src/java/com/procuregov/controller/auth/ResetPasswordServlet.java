package com.procuregov.controller.auth;

import com.procuregov.dao.UserDAO;
import com.procuregov.dao.UserTokenDAO;
import com.procuregov.dao.impl.UserDAOImpl;
import com.procuregov.dao.impl.UserTokenDAOImpl;
import com.procuregov.model.UserToken;
import com.procuregov.util.PasswordUtil;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ResetPasswordServlet handles the password reset form submission.
 * Verifies the 6-digit reset code, updates the password, and marks the code as used.
 */
public class ResetPasswordServlet extends HttpServlet {

    private UserDAO userDAO;
    private UserTokenDAO tokenDAO;

    @Override
    public void init() throws ServletException {
        userDAO = new UserDAOImpl();
        tokenDAO = new UserTokenDAOImpl();
    }

    /**
     * Displays the reset password form with code pre-filled from URL parameter.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String token = request.getParameter("token");
        if (token != null && !token.isEmpty()) {
            request.setAttribute("token", token);
        }
        request.getRequestDispatcher("/pages/common/reset-password.jsp").forward(request, response);
    }

    /**
     * Processes password reset form submission.
     * Validates the 6-digit reset code, checks expiry, updates password with SHA-256 hashing.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String tokenStr = request.getParameter("token");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        // Validate inputs
        if (tokenStr == null || tokenStr.trim().isEmpty()) {
            request.setAttribute("error", "Reset code is required.");
            request.getRequestDispatcher("/pages/common/reset-password.jsp").forward(request, response);
            return;
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            request.setAttribute("error", "New password is required.");
            request.setAttribute("token", tokenStr);
            request.getRequestDispatcher("/pages/common/reset-password.jsp").forward(request, response);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("error", "Passwords do not match.");
            request.setAttribute("token", tokenStr);
            request.getRequestDispatcher("/pages/common/reset-password.jsp").forward(request, response);
            return;
        }

        // Look up token in database
        UserToken token = tokenDAO.getByToken(tokenStr.trim());
        if (token == null) {
            request.setAttribute("error", "Invalid reset code.");
            request.getRequestDispatcher("/pages/common/reset-password.jsp").forward(request, response);
            return;
        }

        // Check if token is already used
        if (token.isUsed()) {
            request.setAttribute("error", "This reset code has already been used.");
            request.getRequestDispatcher("/pages/common/reset-password.jsp").forward(request, response);
            return;
        }

        // Check if token has expired
        if (token.isExpired()) {
            request.setAttribute("error", "This reset code has expired. Please request a new one.");
            request.getRequestDispatcher("/pages/common/reset-password.jsp").forward(request, response);
            return;
        }

        // Verify token type
        if (!"PASSWORD_RESET".equals(token.getTokenType())) {
            request.setAttribute("error", "Invalid code type.");
            request.getRequestDispatcher("/pages/common/reset-password.jsp").forward(request, response);
            return;
        }

        // Update password with SHA-256 hashing and unlock account
        String newHash = PasswordUtil.hashPassword(newPassword);
        boolean passwordUpdated = userDAO.updatePassword(token.getUserId(), newHash);

        if (passwordUpdated) {
            // Mark token as used
            tokenDAO.markAsUsed(token.getId());
            request.setAttribute("success", "Password reset successful! You can now login with your new password.");
            request.getRequestDispatcher("/pages/common/login.jsp").forward(request, response);
        } else {
            request.setAttribute("error", "Failed to reset password. Please try again.");
            request.setAttribute("token", tokenStr);
            request.getRequestDispatcher("/pages/common/reset-password.jsp").forward(request, response);
        }
    }
}
