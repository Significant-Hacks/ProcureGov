package com.procuregov.dao.impl;

import com.procuregov.dao.SupplierDAO;
import com.procuregov.util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JDBC implementation of SupplierDAO.
 * All supplier-related database queries are centralized here.
 */
public class SupplierDAOImpl implements SupplierDAO {

    private static final Logger LOGGER = Logger.getLogger(SupplierDAOImpl.class.getName());

    @Override
    public int getSupplierIdByUserId(int userId) {
        String sql = "SELECT id FROM suppliers WHERE user_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting supplier id for user: " + userId, e);
        }
        return -1;
    }

    @Override
    public String getEmailByUserId(int userId) {
        String sql = "SELECT email FROM users WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString("email");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting email for user: " + userId, e);
        }
        return null;
    }

    @Override
    public List<String> getAllActiveEmails() {
        List<String> emails = new ArrayList<>();
        String sql = "SELECT u.email FROM users u JOIN suppliers s ON u.id = s.user_id WHERE u.is_active = 1";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                emails.add(rs.getString("email"));
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error fetching active supplier emails", e);
        }
        return emails;
    }

    @Override
    public List<String> getBidderEmails(int tenderId) {
        List<String> emails = new ArrayList<>();
        String sql = "SELECT DISTINCT u.email FROM bids b JOIN suppliers s ON b.supplier_id = s.id JOIN users u ON s.user_id = u.id WHERE b.tender_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tenderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    emails.add(rs.getString("email"));
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error getting bidder emails for tender: " + tenderId, e);
        }
        return emails;
    }

    @Override
    public String getSupplierNameByBidId(int bidId) {
        String sql = "SELECT s.company_name FROM bids b JOIN suppliers s ON b.supplier_id = s.id WHERE b.id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bidId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString("company_name");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error getting supplier name for bid: " + bidId, e);
        }
        return "Unknown Supplier";
    }

    @Override
    public int getSupplierIdByBidId(int bidId) {
        String sql = "SELECT supplier_id FROM bids WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bidId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("supplier_id");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error getting supplier id for bid: " + bidId, e);
        }
        return -1;
    }

    @Override
    public List<Object[]> getBidderInfo(int tenderId) {
        List<Object[]> bidders = new ArrayList<>();
        String sql = "SELECT DISTINCT u.email, s.id as supplier_id FROM bids b " +
                "JOIN suppliers s ON b.supplier_id = s.id JOIN users u ON s.user_id = u.id WHERE b.tender_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tenderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    bidders.add(new Object[]{rs.getString("email"), rs.getInt("supplier_id")});
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error getting bidder info for tender: " + tenderId, e);
        }
        return bidders;
    }
}
