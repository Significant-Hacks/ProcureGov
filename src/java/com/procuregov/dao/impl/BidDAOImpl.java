package com.procuregov.dao.impl;

import com.procuregov.dao.BidDAO;
import com.procuregov.model.Bid;
import com.procuregov.util.DBConnectionUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JDBC implementation of BidDAO.
 * Handles all bid-related database operations including submission and retrieval.
 */
public class BidDAOImpl implements BidDAO {

    private static final Logger LOGGER = Logger.getLogger(BidDAOImpl.class.getName());

    @Override
    public boolean submit(Bid bid) {
        String sql = "INSERT INTO bids (tender_id, supplier_id, bid_amount, technical_compliance, proposed_timeline_days, document_path) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, bid.getTenderId());
            stmt.setInt(2, bid.getSupplierId());
            stmt.setBigDecimal(3, bid.getAmount());
            stmt.setString(4, bid.getTechnicalCompliance());
            stmt.setInt(5, bid.getProposedTimelineDays());
            stmt.setString(6, bid.getDocumentPath());
            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) bid.setId(generatedKeys.getInt(1));
                }
            }
            return result;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error submitting bid", e);
            return false;
        }
    }

    @Override
    public Bid getById(int id) {
        String sql = "SELECT b.*, t.title as tender_title, t.reference_number as tender_ref_number, s.company_name " +
                "FROM bids b JOIN tenders t ON b.tender_id = t.id JOIN suppliers s ON b.supplier_id = s.id WHERE b.id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting bid by id: " + id, e);
        }
        return null;
    }

    @Override
    public List<Bid> getByTenderId(int tenderId) {
        String sql = "SELECT b.*, t.title as tender_title, t.reference_number as tender_ref_number, s.company_name " +
                "FROM bids b JOIN tenders t ON b.tender_id = t.id JOIN suppliers s ON b.supplier_id = s.id WHERE b.tender_id = ? ORDER BY b.bid_amount ASC";
        List<Bid> list = new ArrayList<>();
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tenderId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting bids by tender id: " + tenderId, e);
        }
        return list;
    }

    @Override
    public List<Bid> getBySupplierId(int supplierId) {
        String sql = "SELECT b.*, t.title as tender_title, t.reference_number as tender_ref_number, s.company_name " +
                "FROM bids b JOIN tenders t ON b.tender_id = t.id JOIN suppliers s ON b.supplier_id = s.id WHERE b.supplier_id = ? ORDER BY b.submitted_at DESC";
        List<Bid> list = new ArrayList<>();
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, supplierId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting bids by supplier id: " + supplierId, e);
        }
        return list;
    }

    @Override
    public boolean update(Bid bid) {
        String sql = "UPDATE bids SET bid_amount = ?, technical_compliance = ?, proposed_timeline_days = ?, document_path = ? WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, bid.getAmount());
            stmt.setString(2, bid.getTechnicalCompliance());
            stmt.setInt(3, bid.getProposedTimelineDays());
            stmt.setString(4, bid.getDocumentPath());
            stmt.setInt(5, bid.getId());
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating bid: " + bid.getId(), e);
            return false;
        }
    }

    @Override
    public int countByTenderId(int tenderId) {
        String sql = "SELECT COUNT(*) FROM bids WHERE tender_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tenderId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting bids for tender: " + tenderId, e);
        }
        return 0;
    }

    private Bid mapRow(ResultSet rs) throws SQLException {
        Bid b = new Bid();
        b.setId(rs.getInt("id"));
        b.setTenderId(rs.getInt("tender_id"));
        b.setSupplierId(rs.getInt("supplier_id"));
        b.setAmount(rs.getBigDecimal("bid_amount"));
        b.setTechnicalCompliance(rs.getString("technical_compliance"));
        b.setProposedTimelineDays(rs.getInt("proposed_timeline_days"));
        b.setSubmittedAt(rs.getTimestamp("submitted_at"));
        b.setTenderTitle(rs.getString("tender_title"));
        b.setTenderRefNumber(rs.getString("tender_ref_number"));
        b.setCompanyName(rs.getString("company_name"));
        b.setDocumentPath(rs.getString("document_path"));
        return b;
    }
}
