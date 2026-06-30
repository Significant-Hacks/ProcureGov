package com.procuregov.controller.officer;

import com.procuregov.dao.TenderDAO;
import com.procuregov.dao.impl.TenderDAOImpl;
import com.procuregov.model.Tender;
import com.procuregov.util.SessionUtil;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class OfficerDashboardServlet extends HttpServlet {

    private TenderDAO tenderDAO;

    @Override
    public void init() throws ServletException {
        tenderDAO = new TenderDAOImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.isLoggedIn(request) || !SessionUtil.hasRole(request, "OFFICER")) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Auto-close tenders whose deadline has passed
        tenderDAO.autoCloseExpiredTenders();

        // Dashboard stats
        List<Tender> allTenders = tenderDAO.getAll();
        request.setAttribute("totalTenders", allTenders.size());
        request.setAttribute("draftCount", tenderDAO.countByStatus("Draft"));
        request.setAttribute("openCount", tenderDAO.countByStatus("Open"));
        request.setAttribute("closedCount", tenderDAO.countByStatus("Closed"));
        request.setAttribute("evaluationCount", tenderDAO.countByStatus("Under Evaluation"));
        request.setAttribute("evaluatedCount", tenderDAO.countByStatus("Evaluated"));
        request.setAttribute("awardedCount", tenderDAO.countByStatus("Awarded"));

        // Recent tenders (last 5)
        int recentLimit = Math.min(5, allTenders.size());
        List<Tender> recentTenders = allTenders.subList(0, recentLimit);
        request.setAttribute("recentTenders", recentTenders);

        // Tenders needing attention: drafts + closed (need publish or start evaluation)
        List<Tender> draftTenders = tenderDAO.getByStatus("Draft");
        List<Tender> closedTenders = tenderDAO.getByStatus("Closed");
        request.setAttribute("draftTenders", draftTenders);
        request.setAttribute("closedTenders", closedTenders);

        // Evaluated tenders ready for award
        List<Tender> evaluatedTenders = tenderDAO.getByStatus("Evaluated");
        request.setAttribute("evaluatedTenders", evaluatedTenders);

        // Tenders under evaluation
        List<Tender> underEvaluationTenders = tenderDAO.getByStatus("Under Evaluation");
        request.setAttribute("underEvaluationTenders", underEvaluationTenders);

        request.setAttribute("userName", SessionUtil.getUserName(request));

        request.getRequestDispatcher("/pages/officer/dashboard.jsp").forward(request, response);
    }
}