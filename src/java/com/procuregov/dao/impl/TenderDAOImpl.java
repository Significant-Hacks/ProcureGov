package com.procuregov.dao.impl;

import com.procuregov.dao.TenderDAO;
import com.procuregov.model.Tender;
import com.procuregov.util.DBConnectionUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JDBC implementation of TenderDAO.
 * Handles all tender-related database operations including CRUD and status filtering.
 */
public class TenderDAOImpl implements TenderDAO {

    private static final Logger LOGGER = Logger.getLogger(TenderDAOImpl.class.getName());

    @Override
    public boolean create(Tender tender) {
        String sql = "INSERT INTO tenders (reference_number, title, category, description, estimated_value, submission_deadline, notice_document_path, show_estimated_value, status, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, tender.getReferenceNumber());
            stmt.setString(2, tender.getTitle());
            stmt.setString(3, tender.getCategory());
            stmt.setString(4, tender.getDescription());
            stmt.setBigDecimal(5, tender.getEstimatedValue());
            stmt.setTimestamp(6, new Timestamp(tender.getDeadline().getTime()));
            stmt.setString(7, tender.getNoticeDocumentPath());
            stmt.setBoolean(8, tender.isShowEstimatedValue());
            stmt.setString(9, tender.getStatus());
            stmt.setInt(10, tender.getCreatedBy());
            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        tender.setId(generatedKeys.getInt(1));
                    }
                }
            }
            return result;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating tender", e);
            return false;
        }
    }

    @Override
    public boolean update(Tender tender) {
        String sql = "UPDATE tenders SET title=?, category=?, description=?, estimated_value=?, submission_deadline=?, show_estimated_value=?, status=? WHERE id=?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tender.getTitle());
            stmt.setString(2, tender.getCategory());
            stmt.setString(3, tender.getDescription());
            stmt.setBigDecimal(4, tender.getEstimatedValue());
            stmt.setTimestamp(5, new Timestamp(tender.getDeadline().getTime()));
            stmt.setBoolean(6, tender.isShowEstimatedValue());
            stmt.setString(7, tender.getStatus());
            stmt.setInt(8, tender.getId());
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating tender", e);
            return false;
        }
    }

    @Override
    public Tender getById(int id) {
        String sql = "SELECT * FROM tenders WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting tender by id: " + id, e);
        }
        return null;
    }

    @Override
    public List<Tender> getAll() {
        String sql = "SELECT * FROM tenders ORDER BY created_at DESC";
        List<Tender> list = new ArrayList<>();
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting all tenders", e);
        }
        return list;
    }

    @Override
    public List<Tender> getByStatus(String status) {
        String sql = "SELECT * FROM tenders WHERE status = ? ORDER BY created_at DESC";
        List<Tender> list = new ArrayList<>();
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting tenders by status: " + status, e);
        }
        return list;
    }

    @Override
    public List<Tender> getOpenTenders() {
        String sql = "SELECT * FROM tenders WHERE status = 'Open' ORDER BY submission_deadline ASC";
        List<Tender> list = new ArrayList<>();
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting open tenders", e);
        }
        return list;
    }

    @Override
    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM tenders WHERE status = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting tenders by status: " + status, e);
        }
        return 0;
    }

    /**
     * Automatically transitions Open tenders whose deadline has passed to Closed status.
     * Called by the officer dashboard servlet on each page load.
     * @return number of tenders transitioned
     */
    public int autoCloseExpiredTenders() {
        String sql = "UPDATE tenders SET status = 'Closed' WHERE status = 'Open' AND submission_deadline < NOW()";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            return stmt.executeUpdate();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error auto-closing expired tenders", e);
        }
        return 0;
    }

    @Override
    public List<Tender> getByAssignedEvaluator(int evaluatorId) {
        String sql = "SELECT t.* FROM tenders t JOIN tender_evaluators te ON t.id = te.tender_id " +
                     "WHERE te.evaluator_id = ? ORDER BY t.created_at DESC";
        List<Tender> list = new ArrayList<>();
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, evaluatorId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting tenders for evaluator: " + evaluatorId, e);
        }
        return list;
    }

    private Tender mapRow(ResultSet rs) throws SQLException {
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
        return t;
    }
}