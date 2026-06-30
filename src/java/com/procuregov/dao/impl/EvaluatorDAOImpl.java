package com.procuregov.dao.impl;

import com.procuregov.dao.EvaluatorDAO;
import com.procuregov.util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JDBC implementation of EvaluatorDAO.
 * All evaluator-related database queries are centralized here.
 */
public class EvaluatorDAOImpl implements EvaluatorDAO {

    private static final Logger LOGGER = Logger.getLogger(EvaluatorDAOImpl.class.getName());

    @Override
    public int getEvaluatorIdByUserId(int userId) {
        String sql = "SELECT id FROM evaluators WHERE user_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting evaluator id for user: " + userId, e);
        }
        return -1;
    }

    @Override
    public List<Integer> getAssignedEvaluatorIds(int tenderId) {
        List<Integer> evaluatorIds = new ArrayList<>();
        String sql = "SELECT evaluator_id FROM tender_evaluators WHERE tender_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tenderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    evaluatorIds.add(rs.getInt("evaluator_id"));
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting assigned evaluators for tender: " + tenderId, e);
        }
        return evaluatorIds;
    }

    @Override
    public boolean hasEvaluatorScoredBid(int evaluatorId, int bidId) {
        String sql = "SELECT COUNT(*) FROM evaluations WHERE evaluator_id = ? AND bid_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, evaluatorId);
            stmt.setInt(2, bidId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error checking if evaluator " + evaluatorId + " scored bid " + bidId, e);
        }
        return false;
    }

    @Override
    public List<Object[]> getAssignedEvaluatorInfo(int tenderId) {
        List<Object[]> result = new ArrayList<>();
        String sql = "SELECT ev.id AS evaluator_id, ev.user_id, u.email " +
                "FROM tender_evaluators te " +
                "JOIN evaluators ev ON te.evaluator_id = ev.id " +
                "JOIN users u ON ev.user_id = u.id " +
                "WHERE te.tender_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tenderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(new Object[]{rs.getInt("evaluator_id"), rs.getInt("user_id"), rs.getString("email")});
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting assigned evaluator info for tender: " + tenderId, e);
        }
        return result;
    }

    @Override
    public boolean hasAnyEvaluationForTender(int tenderId) {
        String sql = "SELECT COUNT(*) FROM evaluations WHERE tender_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tenderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error checking if any evaluation exists for tender: " + tenderId, e);
        }
        return false;
    }

    @Override
    public int countScoredBidsByEvaluatorAndTender(int evaluatorId, int tenderId) {
        String sql = "SELECT COUNT(DISTINCT bid_id) FROM evaluations WHERE evaluator_id = ? AND tender_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, evaluatorId);
            stmt.setInt(2, tenderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting scored bids for evaluator " + evaluatorId + " on tender " + tenderId, e);
        }
        return 0;
    }

    @Override
    public int assignAllEvaluatorsToTender(int tenderId) {
        // First get all evaluator IDs
        List<Integer> allIds = getAllEvaluatorIds();
        if (allIds.isEmpty()) return 0;

        // Get already-assigned evaluator IDs for this tender
        List<Integer> existingIds = getAssignedEvaluatorIds(tenderId);

        String sql = "INSERT IGNORE INTO tender_evaluators (tender_id, evaluator_id) VALUES (?, ?)";
        int assigned = 0;
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int evaluatorId : allIds) {
                if (!existingIds.contains(evaluatorId)) {
                    stmt.setInt(1, tenderId);
                    stmt.setInt(2, evaluatorId);
                    stmt.addBatch();
                    assigned++;
                }
            }
            if (assigned > 0) {
                stmt.executeBatch();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error assigning evaluators to tender: " + tenderId, e);
        }
        return assigned;
    }

    @Override
    public List<Integer> getAllEvaluatorIds() {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT id FROM evaluators";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                ids.add(rs.getInt("id"));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting all evaluator IDs", e);
        }
        return ids;
    }
}
