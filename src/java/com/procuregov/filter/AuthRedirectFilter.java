package com.procuregov.filter;

import com.procuregov.util.SessionUtil;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * AuthRedirectFilter protects /pages/ from unauthorized access.
 * Exam requirement: all protected pages must verify session validity and role.
 * Unauthorized access must redirect to login with 'Access Denied' message.
 */
public class AuthRedirectFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No initialization needed
    }

    /**
     * Checks if user is logged in and has the correct role for the requested page.
     * Redirects to login page with 'Access Denied' message if not authenticated or wrong role.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (!SessionUtil.isLoggedIn(httpRequest)) {
            httpRequest.setAttribute("error", "Access Denied. Please login to access this page.");
            httpRequest.getRequestDispatcher("/pages/common/login.jsp").forward(httpRequest, httpResponse);
            return;
        }

        // Role-based access control: verify user role matches the page being accessed
        String role = SessionUtil.getUserRole(httpRequest);
        String uri = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();

        if (uri.startsWith(contextPath + "/pages/officer/") && !"OFFICER".equals(role)) {
            httpResponse.sendRedirect(contextPath + SessionUtil.getDashboardUrl(role));
            return;
        }
        if (uri.startsWith(contextPath + "/pages/supplier/") && !"SUPPLIER".equals(role)) {
            httpResponse.sendRedirect(contextPath + SessionUtil.getDashboardUrl(role));
            return;
        }
        if (uri.startsWith(contextPath + "/pages/evaluator/") && !"EVALUATOR".equals(role)) {
            httpResponse.sendRedirect(contextPath + SessionUtil.getDashboardUrl(role));
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // No cleanup needed
    }
}