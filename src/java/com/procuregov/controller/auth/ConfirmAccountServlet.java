package com.procuregov.controller.auth;

import com.procuregov.dao.UserDAO;
import com.procuregov.dao.UserTokenDAO;
import com.procuregov.dao.impl.UserDAOImpl;
import com.procuregov.dao.impl.UserTokenDAOImpl;
import com.procuregov.model.UserToken;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ConfirmAccountServlet handles account confirmation via token.
 * Activates the user account after token verification.
 */
public class ConfirmAccountServlet extends HttpServlet {

    private UserDAO userDAO;
    private UserTokenDAO tokenDAO;

    @Override
    public void init() throws ServletException {
        userDAO = new UserDAOImpl();
        tokenDAO = new UserTokenDAOImpl();
    }

    /**
     * Handles account confirmation via token in URL parameter.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String tokenStr = request.getParameter("token");

        if (tokenStr == null || tokenStr.trim().isEmpty()) {
            request.setAttribute("error", "Confirmation token is required.");
            request.getRequestDispatcher("/pages/common/login.jsp").forward(request, response);
            return;
        }

        UserToken token = tokenDAO.getByToken(tokenStr.trim());
        if (token == null) {
            request.setAttribute("error", "Invalid confirmation token.");
            request.getRequestDispatcher("/pages/common/login.jsp").forward(request, response);
            return;
        }

        if (token.isUsed()) {
            request.setAttribute("error", "This confirmation token has already been used.");
            request.getRequestDispatcher("/pages/common/login.jsp").forward(request, response);
            return;
        }

        if (token.isExpired()) {
            request.setAttribute("error", "This confirmation token has expired. Please request a new one.");
            request.getRequestDispatcher("/pages/common/login.jsp").forward(request, response);
            return;
        }

        if (!"ACCOUNT_CONFIRMATION".equals(token.getTokenType())) {
            request.setAttribute("error", "Invalid token type.");
            request.getRequestDispatcher("/pages/common/login.jsp").forward(request, response);
            return;
        }

        // Activate the user account
        userDAO.setActive(token.getUserId(), true);
        tokenDAO.markAsUsed(token.getId());

        request.setAttribute("success", "Account confirmed successfully! You can now login.");
        request.getRequestDispatcher("/pages/common/login.jsp").forward(request, response);
    }
}
