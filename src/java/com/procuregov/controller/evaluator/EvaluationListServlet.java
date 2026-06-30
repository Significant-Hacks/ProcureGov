package com.procuregov.controller.evaluator;

import com.procuregov.dao.EvaluationDAO;
import com.procuregov.dao.TenderDAO;
import com.procuregov.dao.impl.EvaluationDAOImpl;
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

public class EvaluationListServlet extends HttpServlet {

    private EvaluationDAO evaluationDAO;
    private TenderDAO tenderDAO;

    @Override
    public void init() throws ServletException {
        evaluationDAO = new EvaluationDAOImpl();
        tenderDAO = new TenderDAOImpl();
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
        int evaluatorId = getEvaluatorId(userId);

        // Tenders under evaluation assigned to this evaluator
        List<Tender> underEvaluationTenders = getAssignedTendersByStatus(evaluatorId, "Under Evaluation");
        request.setAttribute("underEvaluationTenders", underEvaluationTenders);

        // Pending evaluations
        List<Evaluation> pendingEvaluations = evaluationDAO.getPendingByEvaluatorId(evaluatorId);
        request.setAttribute("pendingEvaluations", pendingEvaluations);

        // Completed evaluations
        List<Evaluation> myEvaluations = evaluationDAO.getByEvaluatorId(evaluatorId);
        request.setAttribute("myEvaluations", myEvaluations);

        // Evaluated tenders assigned to this evaluator (was using global getByStatus - fixed)
        List<Tender> evaluatedTenders = getAssignedTendersByStatus(evaluatorId, "Evaluated");
        request.setAttribute("evaluatedTenders", evaluatedTenders);

        // Awarded tenders assigned to this evaluator (was using global getByStatus - fixed)
        List<Tender> awardedTenders = getAssignedTendersByStatus(evaluatorId, "Awarded");
        request.setAttribute("awardedTenders", awardedTenders);

        request.setAttribute("userName", SessionUtil.getUserName(request));

        request.getRequestDispatcher("/pages/evaluator/evaluation-list.jsp").forward(request, response);
    }

    private List<Tender> getAssignedTendersByStatus(int evaluatorId, String status) {
        String sql = "SELECT t.* FROM tenders t JOIN tender_evaluators te ON t.id = te.tender_id " +
                     "WHERE te.evaluator_id = ? AND t.status = ? ORDER BY t.submission_deadline ASC";
        List<Tender> list = new ArrayList<>();
        try (java.sql.Connection conn = com.procuregov.util.DBConnectionUtil.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, evaluatorId);
            stmt.setString(2, status);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Tender t = new Tender();
                    t.setId(rs.getInt("id"));
                    t.setReferenceNumber(rs.getString("reference_number"));
                    t.setTitle(rs.getString("title"));
                    t.setCategory(rs.getString("category"));
                    t.setDescription(rs.getString("description"));
                    t.setEstimatedValue(rs.getBigDecimal("estimated_value"));
                    t.setDeadline(rs.getTimestamp("submission_deadline"));
                    t.setStatus(rs.getString("status"));
                    t.setCreatedBy(rs.getInt("created_by"));
                    t.setCreatedAt(rs.getTimestamp("created_at"));
                    t.setNoticeDocumentPath(rs.getString("notice_document_path"));
                    t.setShowEstimatedValue(rs.getBoolean("show_estimated_value"));
                    list.add(t);
                }
            }
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(EvaluationListServlet.class.getName())
                .log(java.util.logging.Level.SEVERE, "Error getting assigned tenders by status", e);
        }
        return list;
    }

    private int getEvaluatorId(int userId) {
        String sql = "SELECT id FROM evaluators WHERE user_id = ?";
        try (java.sql.Connection conn = com.procuregov.util.DBConnectionUtil.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(EvaluationListServlet.class.getName())
                .log(java.util.logging.Level.SEVERE, "Error getting evaluator id", e);
        }
        return -1;
    }
}
