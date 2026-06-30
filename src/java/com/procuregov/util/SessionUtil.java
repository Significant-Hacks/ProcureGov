package com.procuregov.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Utility class for session state management.
 * Handles session validation, role checking, and login attempt tracking.
 */
public class SessionUtil {

    /** Maximum failed login attempts before account lockout */
    private static final int MAX_FAILED_ATTEMPTS = 3;

    /**
     * Checks if a user is currently logged in with a valid session.
     * @param request the HTTP request
     * @return true if the user has a valid session with user ID
     */
    public static boolean isLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return false;
        return session.getAttribute("userId") != null;
    }

    /**
     * Gets the logged-in user's ID from the session.
     * @param request the HTTP request
     * @return the user ID, or -1 if not logged in
     */
    public static int getUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            return (Integer) session.getAttribute("userId");
        }
        return -1;
    }

    /**
     * Gets the logged-in user's role from the session.
     * @param request the HTTP request
     * @return the role string, or null if not logged in
     */
    public static String getUserRole(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (String) session.getAttribute("userRole");
        }
        return null;
    }

    /**
     * Gets the logged-in user's display name from the session.
     * @param request the HTTP request
     * @return the display name, or null if not logged in
     */
    public static String getUserName(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (String) session.getAttribute("userName");
        }
        return null;
    }

    /**
     * Gets the logged-in user's email from the session.
     * @param request the HTTP request
     * @return the email, or null if not logged in
     */
    public static String getUserEmail(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (String) session.getAttribute("userEmail");
        }
        return null;
    }

    /**
     * Creates a new session for a successfully authenticated user.
     * @param request the HTTP request
     * @param userId the user's database ID
     * @param email the user's email
     * @param role the user's role
     * @param displayName the user's display name
     */
    public static void createSession(HttpServletRequest request, int userId, String email, String role, String displayName) {
        HttpSession session = request.getSession(true);
        session.setAttribute("userId", userId);
        session.setAttribute("userEmail", email);
        session.setAttribute("userRole", role);
        session.setAttribute("userName", displayName);
        session.setAttribute("failedLoginAttempts", 0);
        session.setAttribute("accountLocked", false);
        OnlineUserTracker.userLoggedIn(userId);
    }

    /**
     * Invalidates the current session (logout).
     * @param request the HTTP request
     */
    public static void destroySession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId != null) {
                OnlineUserTracker.userLoggedOut(userId);
            }
            session.invalidate();
        }
    }

    /**
     * Records a failed login attempt in the session.
     * After MAX_FAILED_ATTEMPTS, the account is locked for the session duration.
     * @param request the HTTP request
     * @return the current number of failed attempts
     */
    public static int recordFailedLogin(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        Integer attempts = (Integer) session.getAttribute("failedLoginAttempts");
        if (attempts == null) attempts = 0;
        attempts++;
        session.setAttribute("failedLoginAttempts", attempts);
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            session.setAttribute("accountLocked", true);
        }
        return attempts;
    }

    /**
     * Checks if the account is temporarily locked due to failed attempts.
     * The lock persists for the session duration as per exam requirements.
     * @param request the HTTP request
     * @return true if the account is locked in this session
     */
    public static boolean isAccountLocked(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Boolean locked = (Boolean) session.getAttribute("accountLocked");
            return locked != null && locked;
        }
        return false;
    }

    /**
     * Gets the dashboard URL for a given role.
     * @param role the user's role
     * @return the URL path to the role's dashboard
     */
    public static String getDashboardUrl(String role) {
        if (role == null) return "/login";
        switch (role) {
            case "OFFICER": return "/officer-dashboard";
            case "SUPPLIER": return "/supplier-dashboard";
            case "EVALUATOR": return "/evaluator-dashboard";
            default: return "/login";
        }
    }

    /**
     * Checks if the current user has the required role.
     * @param request the HTTP request
     * @param requiredRole the role to check against
     * @return true if the user has the required role
     */
    public static boolean hasRole(HttpServletRequest request, String requiredRole) {
        String role = getUserRole(request);
        return requiredRole.equals(role);
    }
}