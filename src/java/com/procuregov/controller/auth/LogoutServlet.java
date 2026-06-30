package com.procuregov.controller.auth;

import com.procuregov.util.SessionUtil;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * LogoutServlet explicitly invalidates the HttpSession and redirects to login.
 * Exam requirement: logout must invalidate the HttpSession.
 */
public class LogoutServlet extends HttpServlet {

    /**
     * Handles logout by invalidating session and redirecting to login page.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        SessionUtil.destroySession(request);
        response.sendRedirect(request.getContextPath() + "/login");
    }
}
