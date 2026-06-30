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

public class ManageTendersServlet extends HttpServlet {

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

        // All tenders for officer management
        List<Tender> tenders = tenderDAO.getAll();
        request.setAttribute("tenders", tenders);

        // Tenders under evaluation (officer acts as evaluator too)
        List<Tender> underEvaluationTenders = tenderDAO.getByStatus("Under Evaluation");
        request.setAttribute("underEvaluationTenders", underEvaluationTenders);

        // Evaluated tenders ready for award
        List<Tender> evaluatedTenders = tenderDAO.getByStatus("Evaluated");
        request.setAttribute("evaluatedTenders", evaluatedTenders);

        request.setAttribute("userName", SessionUtil.getUserName(request));

        request.getRequestDispatcher("/pages/officer/tenders-list.jsp").forward(request, response);
    }
}
