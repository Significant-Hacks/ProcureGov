package com.procuregov.dao.impl;

import com.procuregov.dao.BidTechnicalCriterionDAO;
import com.procuregov.model.BidTechnicalCriterion;
import com.procuregov.util.DBConnectionUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of BidTechnicalCriterionDAO.
 * Handles database operations for structured technical compliance criteria.
 */
public class BidTechnicalCriterionDAOImpl implements BidTechnicalCriterionDAO {

    private static final Logger LOGGER = Logger.getLogger(BidTechnicalCriterionDAOImpl.class.getName());

    @Override
    public boolean insert(BidTechnicalCriterion criterion) {
        String sql = "INSERT INTO bid_technical_criteria (bid_id, criterion_name, criterion_type, criterion_value, evidence_document_path) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, criterion.getBidId());
            stmt.setString(2, criterion.getCriterionName());
            stmt.setString(3, criterion.getCriterionType());
            stmt.setString(4, criterion.getCriterionValue());
            stmt.setString(5, criterion.getEvidenceDocumentPath());
            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) criterion.setId(generatedKeys.getInt(1));
                }
            }
            return result;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error inserting technical criterion", e);
            return false;
        }
    }

    @Override
    public boolean insertBatch(List<BidTechnicalCriterion> criteria) {
        String sql = "INSERT INTO bid_technical_criteria (bid_id, criterion_name, criterion_type, criterion_value, evidence_document_path) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (BidTechnicalCriterion criterion : criteria) {
                stmt.setInt(1, criterion.getBidId());
                stmt.setString(2, criterion.getCriterionName());
                stmt.setString(3, criterion.getCriterionType());
                stmt.setString(4, criterion.getCriterionValue());
                stmt.setString(5, criterion.getEvidenceDocumentPath());
                stmt.addBatch();
            }
            int[] results = stmt.executeBatch();
            for (int r : results) {
                if (r <= 0) return false;
            }
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error batch inserting technical criteria", e);
            return false;
        }
    }

    @Override
    public List<BidTechnicalCriterion> getByBidId(int bidId) {
        String sql = "SELECT * FROM bid_technical_criteria WHERE bid_id = ? ORDER BY id ASC";
        List<BidTechnicalCriterion> list = new ArrayList<>();
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bidId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting technical criteria for bid: " + bidId, e);
        }
        return list;
    }

    @Override
    public boolean deleteByBidId(int bidId) {
        String sql = "DELETE FROM bid_technical_criteria WHERE bid_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bidId);
            return stmt.executeUpdate() >= 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting technical criteria for bid: " + bidId, e);
            return false;
        }
    }

    private BidTechnicalCriterion mapRow(ResultSet rs) throws SQLException {
        BidTechnicalCriterion c = new BidTechnicalCriterion();
        c.setId(rs.getInt("id"));
        c.setBidId(rs.getInt("bid_id"));
        c.setCriterionName(rs.getString("criterion_name"));
        c.setCriterionType(rs.getString("criterion_type"));
        c.setCriterionValue(rs.getString("criterion_value"));
        c.setEvidenceDocumentPath(rs.getString("evidence_document_path"));
        return c;
    }
}
