package com.procuregov.dao.impl;

import com.procuregov.dao.EvaluationDAO;
import com.procuregov.model.Evaluation;
import com.procuregov.util.DBConnectionUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JDBC implementation of EvaluationDAO.
 * Handles all evaluation score database operations.
 */
public class EvaluationDAOImpl implements EvaluationDAO {

    private static final Logger LOGGER = Logger.getLogger(EvaluationDAOImpl.class.getName());

    @Override
    public boolean score(Evaluation evaluation) {
        String sql = "INSERT INTO evaluations (bid_id, tender_id, evaluator_id, technical_score, price_score, timeline_score, weighted_total) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, evaluation.getBidId());
            stmt.setInt(2, evaluation.getTenderId());
            stmt.setInt(3, evaluation.getEvaluatorId());
            stmt.setBigDecimal(4, evaluation.getTechnicalScore());
            stmt.setBigDecimal(5, evaluation.getPriceScore());
            stmt.setBigDecimal(6, evaluation.getTimelineScore());
            stmt.setBigDecimal(7, evaluation.getWeightedTotal());
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving evaluation", e);
            return false;
        }
    }

    @Override
    public List<Evaluation> getByBidId(int bidId) {
        String sql = "SELECT e.*, t.title as tender_title, t.reference_number as tender_ref_number, s.company_name as supplier_name, b.bid_amount, ev.full_name as evaluator_name " +
                "FROM evaluations e JOIN bids b ON e.bid_id = b.id JOIN tenders t ON b.tender_id = t.id JOIN suppliers s ON b.supplier_id = s.id JOIN evaluators ev ON e.evaluator_id = ev.id WHERE e.bid_id = ?";
        List<Evaluation> list = new ArrayList<>();
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bidId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting evaluations by bid id: " + bidId, e);
        }
        return list;
    }

    @Override
    public List<Evaluation> getByEvaluatorId(int evaluatorId) {
        String sql = "SELECT e.*, t.title as tender_title, t.reference_number as tender_ref_number, s.company_name as supplier_name, b.bid_amount, ev.full_name as evaluator_name " +
                "FROM evaluations e JOIN bids b ON e.bid_id = b.id JOIN tenders t ON b.tender_id = t.id JOIN suppliers s ON b.supplier_id = s.id JOIN evaluators ev ON e.evaluator_id = ev.id " +
                "WHERE e.evaluator_id = ? ORDER BY e.submitted_at DESC";
        List<Evaluation> list = new ArrayList<>();
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, evaluatorId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting evaluations by evaluator id: " + evaluatorId, e);
        }
        return list;
    }

    @Override
    public List<Evaluation> getPendingByEvaluatorId(int evaluatorId) {
        // Show tenders assigned to this evaluator that are Under Evaluation
        // AND still have at least one bid the evaluator hasn't scored yet
        // (includes both not-started and partially-scored tenders)
        String sql = "SELECT t.title as tender_title, t.reference_number as tender_ref_number, t.id as tender_id, " +
                "(SELECT COUNT(*) FROM bids b WHERE b.tender_id = t.id) as total_bids, " +
                "(SELECT COUNT(*) FROM evaluations e JOIN bids b ON e.bid_id = b.id WHERE b.tender_id = t.id AND e.evaluator_id = ?) as scored_bids " +
                "FROM tender_evaluators te JOIN tenders t ON te.tender_id = t.id " +
                "WHERE te.evaluator_id = ? AND t.status = 'Under Evaluation' " +
                "AND EXISTS (SELECT 1 FROM bids b WHERE b.tender_id = t.id AND b.id NOT IN (SELECT e.bid_id FROM evaluations e WHERE e.evaluator_id = ?))";
        List<Evaluation> list = new ArrayList<>();
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, evaluatorId);
            stmt.setInt(2, evaluatorId);
            stmt.setInt(3, evaluatorId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Evaluation ev = new Evaluation();
                ev.setTenderId(rs.getInt("tender_id"));
                ev.setTenderTitle(rs.getString("tender_title"));
                ev.setTenderRefNumber(rs.getString("tender_ref_number"));
                // Store progress info in bidAmount (total) and technicalScore (scored) as workaround
                ev.setBidAmount(rs.getBigDecimal("total_bids"));
                ev.setTechnicalScore(rs.getBigDecimal("scored_bids"));
                list.add(ev);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting pending evaluations for evaluator: " + evaluatorId, e);
        }
        return list;
    }

    private Evaluation mapRow(ResultSet rs) throws SQLException {
        Evaluation e = new Evaluation();
        e.setId(rs.getInt("id"));
        e.setBidId(rs.getInt("bid_id"));
        e.setTenderId(rs.getInt("tender_id"));
        e.setEvaluatorId(rs.getInt("evaluator_id"));
        e.setTechnicalScore(rs.getBigDecimal("technical_score"));
        e.setPriceScore(rs.getBigDecimal("price_score"));
        e.setTimelineScore(rs.getBigDecimal("timeline_score"));
        e.setWeightedTotal(rs.getBigDecimal("weighted_total"));
        e.setSubmittedAt(rs.getTimestamp("submitted_at"));
        e.setTenderTitle(rs.getString("tender_title"));
        e.setTenderRefNumber(rs.getString("tender_ref_number"));
        e.setSupplierName(rs.getString("supplier_name"));
        e.setBidAmount(rs.getBigDecimal("bid_amount"));
        e.setEvaluatorName(rs.getString("evaluator_name"));
        return e;
    }
}
