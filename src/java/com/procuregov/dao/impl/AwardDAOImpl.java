package com.procuregov.dao.impl;

import com.procuregov.dao.AwardDAO;
import com.procuregov.model.Award;
import com.procuregov.util.DBConnectionUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JDBC implementation of AwardDAO.
 * Handles all award-related database operations including lookup by tender reference and confirmation path.
 */
public class AwardDAOImpl implements AwardDAO {

    private static final Logger LOGGER = Logger.getLogger(AwardDAOImpl.class.getName());

    @Override
    public boolean create(Award award) {
        String sql = "INSERT INTO awards (tender_id, winning_bid_id, awarded_value, justification, awarded_by, confirmation_document_path) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, award.getTenderId());
            stmt.setInt(2, award.getWinningBidId());
            stmt.setBigDecimal(3, award.getAwardedValue());
            stmt.setString(4, award.getJustification());
            stmt.setInt(5, award.getAwardedBy());
            stmt.setString(6, award.getConfirmationDocumentPath());
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating award", e);
            return false;
        }
    }

    @Override
    public Award getByTenderId(int tenderId) {
        String sql = "SELECT a.*, t.title as tender_title, t.reference_number as tender_ref_number, " +
                "s.company_name as supplier_name, s.id as winning_supplier_id " +
                "FROM awards a JOIN tenders t ON a.tender_id = t.id JOIN bids b ON a.winning_bid_id = b.id JOIN suppliers s ON b.supplier_id = s.id WHERE a.tender_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tenderId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting award by tender id: " + tenderId, e);
        }
        return null;
    }

    @Override
    public List<Award> getAll() {
        String sql = "SELECT a.*, t.title as tender_title, t.reference_number as tender_ref_number, s.company_name as supplier_name " +
                "FROM awards a JOIN tenders t ON a.tender_id = t.id JOIN bids b ON a.winning_bid_id = b.id JOIN suppliers s ON b.supplier_id = s.id ORDER BY a.awarded_at DESC";
        List<Award> list = new ArrayList<>();
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting all awards", e);
        }
        return list;
    }

    @Override
    public List<Award> getBySupplierId(int supplierId) {
        String sql = "SELECT a.*, t.title as tender_title, t.reference_number as tender_ref_number, " +
                "s.company_name as supplier_name, s.id as winning_supplier_id " +
                "FROM awards a JOIN tenders t ON a.tender_id = t.id " +
                "JOIN bids b ON a.winning_bid_id = b.id JOIN suppliers s ON b.supplier_id = s.id " +
                "WHERE b.supplier_id = ? ORDER BY a.awarded_at DESC";
        List<Award> list = new ArrayList<>();
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, supplierId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting awards by supplier id: " + supplierId, e);
        }
        return list;
    }

    @Override
    public Award getByTenderRef(String tenderRef) {
        String sql = "SELECT a.*, t.title as tender_title, t.reference_number as tender_ref_number, " +
                "s.company_name as supplier_name, s.id as winning_supplier_id " +
                "FROM awards a JOIN tenders t ON a.tender_id = t.id " +
                "JOIN bids b ON a.winning_bid_id = b.id JOIN suppliers s ON b.supplier_id = s.id " +
                "WHERE t.reference_number = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenderRef);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error looking up award by tender ref: " + tenderRef, e);
        }
        return null;
    }

    @Override
    public Award getByConfirmationPath(String path) {
        String sql = "SELECT a.*, t.title as tender_title, t.reference_number as tender_ref_number, " +
                "s.company_name as supplier_name, s.id as winning_supplier_id " +
                "FROM awards a JOIN tenders t ON a.tender_id = t.id " +
                "JOIN bids b ON a.winning_bid_id = b.id JOIN suppliers s ON b.supplier_id = s.id " +
                "WHERE a.confirmation_document_path = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, path);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error looking up award by confirmation path: " + path, e);
        }
        return null;
    }

    @Override
    public boolean updateConfirmationPath(int awardId, String newPath) {
        String sql = "UPDATE awards SET confirmation_document_path = ? WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPath);
            stmt.setInt(2, awardId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error updating confirmation path for award: " + awardId, e);
        }
        return false;
    }

    private Award mapRow(ResultSet rs) throws SQLException {
        Award a = new Award();
        a.setId(rs.getInt("id"));
        a.setTenderId(rs.getInt("tender_id"));
        a.setWinningBidId(rs.getInt("winning_bid_id"));
        a.setAwardedValue(rs.getBigDecimal("awarded_value"));
        a.setJustification(rs.getString("justification"));
        a.setAwardedBy(rs.getInt("awarded_by"));
        a.setAwardedAt(rs.getTimestamp("awarded_at"));
        a.setTenderTitle(rs.getString("tender_title"));
        a.setTenderRefNumber(rs.getString("tender_ref_number"));
        a.setSupplierName(rs.getString("supplier_name"));
        a.setConfirmationDocumentPath(rs.getString("confirmation_document_path"));
        try {
            a.setWinningSupplierId(rs.getInt("winning_supplier_id"));
        } catch (SQLException e) {
            // winning_supplier_id may not be in all result sets
        }
        return a;
    }
}
