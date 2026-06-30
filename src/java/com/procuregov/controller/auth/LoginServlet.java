package com.procuregov.controller.auth;

import com.procuregov.dao.UserDAO;
import com.procuregov.dao.impl.UserDAOImpl;
import com.procuregov.model.User;
import com.procuregov.util.PasswordUtil;
import com.procuregov.util.SessionUtil;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * LoginServlet handles user authentication for all three roles.
 * Enforces: POST login, failed attempt counting, session lockout after 3 failures,
 * role-based redirect, SHA-256 password verification.
 */
public class LoginServlet extends HttpServlet {

    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        userDAO = new UserDAOImpl();
    }

    /**
     * Displays the login page.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // If already logged in, redirect to dashboard
        if (SessionUtil.isLoggedIn(request)) {
            String role = SessionUtil.getUserRole(request);
            response.sendRedirect(request.getContextPath() + SessionUtil.getDashboardUrl(role));
            return;
        }
        request.getRequestDispatcher("/pages/common/login.jsp").forward(request, response);
    }

    /**
     * Processes login form submission via POST.
     * Enforces: account lockout after 3 failed attempts, role-based redirect.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        // Check if account is locked in this session
        if (SessionUtil.isAccountLocked(request)) {
            request.setAttribute("error", "Account is temporarily locked due to 3 failed attempts. Please close your browser and try again.");
            request.getRequestDispatcher("/pages/common/login.jsp").forward(request, response);
            return;
        }

        // Validate input
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            request.setAttribute("error", "Email and password are required.");
            request.getRequestDispatcher("/pages/common/login.jsp").forward(request, response);
            return;
        }

        // Find user by email
        User user = userDAO.findByEmail(email.trim());

        if (user == null) {
            handleFailedLogin(request, response, "Invalid email or password.");
            return;
        }

        // Check if account is locked in database
        if (user.isAccountLocked()) {
            request.setAttribute("error", "Your account has been locked. Please contact the administrator.");
            request.getRequestDispatcher("/pages/common/login.jsp").forward(request, response);
            return;
        }

        // Check if account is active
        if (!user.isActive()) {
            request.setAttribute("error", "Your account is not yet activated. Please check your email for confirmation.");
            request.getRequestDispatcher("/pages/common/login.jsp").forward(request, response);
            return;
        }

        // Verify password using SHA-256
        if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            // Increment failed attempts in database
            userDAO.incrementFailedAttempts(user.getId());
            // If 3 or more failed attempts, lock the account
            if (user.getFailedLoginAttempts() + 1 >= 3) {
                userDAO.setAccountLocked(user.getId(), true);
            }
            handleFailedLogin(request, response, "Invalid email or password.");
            return;
        }

        // Successful login - reset failed attempts and create session
        userDAO.resetFailedAttempts(user.getId());
        SessionUtil.createSession(request, user.getId(), user.getEmail(), user.getRole(), user.getDisplayName());

        // Redirect to role-specific dashboard
        String dashboardUrl = SessionUtil.getDashboardUrl(user.getRole());
        response.sendRedirect(request.getContextPath() + dashboardUrl);
    }

    /**
     * Handles a failed login attempt by recording it in the session.
     */
    private void handleFailedLogin(HttpServletRequest request, HttpServletResponse response, String errorMessage) throws ServletException, IOException {
        int attempts = SessionUtil.recordFailedLogin(request);
        int remaining = 3 - attempts;
        if (remaining > 0) {
            request.setAttribute("error", errorMessage + " " + remaining + " attempt(s) remaining before lockout.");
        } else {
            request.setAttribute("error", "Account locked due to 3 failed attempts. Please close your browser and try again.");
        }
        request.setAttribute("email", request.getParameter("email"));
        request.getRequestDispatcher("/pages/common/login.jsp").forward(request, response);
    }
}
