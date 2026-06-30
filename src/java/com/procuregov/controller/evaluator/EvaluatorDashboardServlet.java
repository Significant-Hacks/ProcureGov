package com.procuregov.controller.evaluator;

import com.procuregov.dao.EvaluationDAO;
import com.procuregov.dao.EvaluatorDAO;
import com.procuregov.dao.TenderDAO;
import com.procuregov.dao.impl.EvaluationDAOImpl;
import com.procuregov.dao.impl.EvaluatorDAOImpl;
import com.procuregov.dao.impl.TenderDAOImpl;
import com.procuregov.model.Evaluation;
import com.procuregov.model.Tender;
import com.procuregov.util.SessionUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EvaluatorDashboardServlet extends HttpServlet {

    private EvaluationDAO evaluationDAO;
    private TenderDAO tenderDAO;
    private EvaluatorDAO evaluatorDAO;

    @Override
    public void init() throws ServletException {
        evaluationDAO = new EvaluationDAOImpl();
        tenderDAO = new TenderDAOImpl();
        evaluatorDAO = new EvaluatorDAOImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String role = SessionUtil.getUserRole(request);
        if (!"EVALUATOR".equals(role) && !"OFFICER".equals(role)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int userId = SessionUtil.getUserId(request);
        int evaluatorId = evaluatorDAO.getEvaluatorIdByUserId(userId);

        // Get all tenders assigned to this evaluator
        List<Tender> allAssigned = tenderDAO.getByAssignedEvaluator(evaluatorId);

        // Split by status for stats and lists
        List<Tender> underEvaluationTenders = new ArrayList<>();
        List<Tender> evaluatedTenders = new ArrayList<>();
        List<Tender> awardedTenders = new ArrayList<>();
        List<Tender> closedTenders = new ArrayList<>();

        for (Tender t : allAssigned) {
            switch (t.getStatus()) {
                case "Under Evaluation": underEvaluationTenders.add(t); break;
                case "Evaluated": evaluatedTenders.add(t); break;
                case "Awarded": awardedTenders.add(t); break;
                case "Closed": closedTenders.add(t); break;
            }
        }

        // Stats
        request.setAttribute("totalAssigned", allAssigned.size());
        request.setAttribute("underEvalCount", underEvaluationTenders.size());
        request.setAttribute("evaluatedCount", evaluatedTenders.size());
        request.setAttribute("awardedCount", awardedTenders.size());

        // Pending evaluations (tenders under evaluation where evaluator hasn't scored all bids)
        List<Evaluation> pendingEvaluations = evaluationDAO.getPendingByEvaluatorId(evaluatorId);
        request.setAttribute("pendingCount", pendingEvaluations.size());

        // Completed scores count (individual bid scores submitted)
        List<Evaluation> myEvaluations = evaluationDAO.getByEvaluatorId(evaluatorId);
        request.setAttribute("completedCount", myEvaluations.size());

        // Full lists for detailed sections
        request.setAttribute("myEvaluations", myEvaluations);
        request.setAttribute("pendingEvaluations", pendingEvaluations);
        request.setAttribute("underEvaluationTenders", underEvaluationTenders);
        request.setAttribute("evaluatedTenders", evaluatedTenders);
        request.setAttribute("awardedTenders", awardedTenders);
        request.setAttribute("closedTenders", closedTenders);

        request.setAttribute("userName", SessionUtil.getUserName(request));
        request.getRequestDispatcher("/pages/evaluator/dashboard.jsp").forward(request, response);
    }
}
