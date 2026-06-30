package com.procuregov.controller.common;

import com.procuregov.dao.UserDAO;
import com.procuregov.dao.impl.UserDAOImpl;
import com.procuregov.model.User;
import com.procuregov.util.PasswordUtil;
import com.procuregov.util.SessionUtil;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ProfileServlet handles user profile viewing and password updates.
 * All authenticated users can access their profile.
 */
public class ProfileServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ProfileServlet.class.getName());
    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        userDAO = new UserDAOImpl();
    }

    /**
     * Displays the user's profile page.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int userId = SessionUtil.getUserId(request);
        User user = userDAO.getById(userId);
        
        if (user == null) {
            SessionUtil.destroySession(request);
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        request.setAttribute("user", user);
        request.setAttribute("userName", SessionUtil.getUserName(request));
        request.getRequestDispatcher("/pages/profile/view.jsp").forward(request, response);
    }

    /**
     * Handles password update form submission.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        int userId = SessionUtil.getUserId(request);
        User user = userDAO.getById(userId);

        // Validate current password
        if (!PasswordUtil.verifyPassword(currentPassword, user.getPasswordHash())) {
            request.setAttribute("error", "Current password is incorrect.");
            request.setAttribute("user", user);
            request.getRequestDispatcher("/pages/profile/view.jsp").forward(request, response);
            return;
        }

        // Validate new password
        if (newPassword == null || newPassword.trim().isEmpty()) {
            request.setAttribute("error", "New password cannot be empty.");
            request.setAttribute("user", user);
            request.getRequestDispatcher("/pages/profile/view.jsp").forward(request, response);
            return;
        }

        // Validate password confirmation
        if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("error", "New passwords do not match.");
            request.setAttribute("user", user);
            request.getRequestDispatcher("/pages/profile/view.jsp").forward(request, response);
            return;
        }

        // Update password
        String newHash = PasswordUtil.hashPassword(newPassword);
        if (userDAO.updatePassword(userId, newHash)) {
            request.setAttribute("success", "Password updated successfully.");
            LOGGER.info("Password updated for user: " + user.getEmail());
        } else {
            request.setAttribute("error", "Failed to update password. Please try again.");
        }

        request.setAttribute("user", userDAO.getById(userId));
        request.getRequestDispatcher("/pages/profile/view.jsp").forward(request, response);
    }
}
