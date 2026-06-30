package com.procuregov.controller.common;

import com.procuregov.dao.AwardDAO;
import com.procuregov.dao.TenderDAO;
import com.procuregov.dao.impl.AwardDAOImpl;
import com.procuregov.dao.impl.TenderDAOImpl;
import com.procuregov.model.Award;
import com.procuregov.model.Tender;
import com.procuregov.util.SessionUtil;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet handling the Award Notice page.
 * Visible to all suppliers who bid on the tender and to officers.
 * Shows tender reference, title, winning supplier, awarded value, date, and justification.
 */
public class AwardNoticeServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AwardNoticeServlet.class.getName());
    private AwardDAO awardDAO;
    private TenderDAO tenderDAO;

    @Override
    public void init() throws ServletException {
        awardDAO = new AwardDAOImpl();
        tenderDAO = new TenderDAOImpl();
    }

    /**
     * Displays the award notice for a tender.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            int tenderId = Integer.parseInt(request.getParameter("tender"));
            Tender tender = tenderDAO.getById(tenderId);
            if (tender == null) {
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }

            Award award = awardDAO.getByTenderId(tenderId);
            if (award == null) {
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }

            request.setAttribute("tender", tender);
            request.setAttribute("award", award);
            request.setAttribute("userName", SessionUtil.getUserName(request));
            request.getRequestDispatcher("/pages/common/award-notice.jsp").forward(request, response);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error displaying award notice", e);
            response.sendRedirect(request.getContextPath() + "/login");
        }
    }
}
