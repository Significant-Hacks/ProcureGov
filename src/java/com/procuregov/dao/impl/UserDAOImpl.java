package com.procuregov.dao.impl;

import com.procuregov.dao.UserDAO;
import com.procuregov.model.User;
import com.procuregov.util.DBConnectionUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JDBC implementation of UserDAO.
 * All database operations use JNDI connection pooling via DBConnectionUtil.
 * SQLExceptions are caught and logged; stack traces are never shown to users.
 */
public class UserDAOImpl implements UserDAO {

    private static final Logger LOGGER = Logger.getLogger(UserDAOImpl.class.getName());

    /**
     * Registers a new supplier user with profile data.
     * Inserts into users table and suppliers table within a transaction.
     */
    @Override
    public boolean registerSupplier(User user) {
        Connection conn = null;
        try {
            conn = DBConnectionUtil.getConnection();
            conn.setAutoCommit(false);

            // Insert into users table
            String userSql = "INSERT INTO users (email, password_hash, role, is_active) VALUES (?, ?, 'SUPPLIER', TRUE)";
            PreparedStatement userStmt = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS);
            userStmt.setString(1, user.getEmail());
            userStmt.setString(2, user.getPasswordHash());
            userStmt.executeUpdate();

            ResultSet generatedKeys = userStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int userId = generatedKeys.getInt(1);

                // Insert into suppliers table
                String supplierSql = "INSERT INTO suppliers (user_id, company_name, registration_number, physical_address, contact_number, is_verified) VALUES (?, ?, ?, ?, ?, FALSE)";
                PreparedStatement supplierStmt = conn.prepareStatement(supplierSql);
                supplierStmt.setInt(1, userId);
                supplierStmt.setString(2, user.getCompanyName());
                supplierStmt.setString(3, user.getRegistrationNumber());
                supplierStmt.setString(4, user.getPhysicalAddress());
                supplierStmt.setString(5, user.getContactNumber());
                supplierStmt.executeUpdate();

                conn.commit();
                return true;
            } else {
                conn.rollback();
                return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error registering supplier", e);
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { LOGGER.log(Level.SEVERE, "Rollback failed", ex); }
            return false;
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException e) { LOGGER.log(Level.WARNING, "Connection close failed", e); }
        }
    }

    /**
     * Finds a user by email, joining with role-specific profile tables.
     */
    @Override
    public User findByEmail(String email) {
        String sql = "SELECT u.id, u.email, u.password_hash, u.role, u.is_active, u.failed_login_attempts, u.account_locked " +
                "FROM users u WHERE u.email = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = mapBasicUser(rs);
                loadProfileData(conn, user);
                return user;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding user by email: " + email, e);
        }
        return null;
    }

    /**
     * Gets a user by ID, including profile data.
     */
    @Override
    public User getById(int id) {
        String sql = "SELECT u.id, u.email, u.password_hash, u.role, u.is_active, u.failed_login_attempts, u.account_locked " +
                "FROM users u WHERE u.id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = mapBasicUser(rs);
                loadProfileData(conn, user);
                return user;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting user by id: " + id, e);
        }
        return null;
    }

    /**
     * Checks if an email is already registered.
     */
    @Override
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error checking email existence: " + email, e);
        }
        return false;
    }

    /**
     * Updates the user's password hash.
     */
    @Override
    public boolean updatePassword(int userId, String newPasswordHash) {
        String sql = "UPDATE users SET password_hash = ?, failed_login_attempts = 0, account_locked = FALSE WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPasswordHash);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating password for user: " + userId, e);
        }
        return false;
    }

    /**
     * Increments the failed login attempt counter.
     */
    @Override
    public void incrementFailedAttempts(int userId) {
        String sql = "UPDATE users SET failed_login_attempts = failed_login_attempts + 1 WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error incrementing failed attempts for user: " + userId, e);
        }
    }

    /**
     * Resets the failed login attempt counter after successful login.
     */
    @Override
    public void resetFailedAttempts(int userId) {
        String sql = "UPDATE users SET failed_login_attempts = 0, account_locked = FALSE WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error resetting failed attempts for user: " + userId, e);
        }
    }

    /**
     * Sets the account locked status.
     */
    @Override
    public void setAccountLocked(int userId, boolean locked) {
        String sql = "UPDATE users SET account_locked = ? WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, locked);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting account locked for user: " + userId, e);
        }
    }

    /**
     * Sets the account active status.
     */
    @Override
    public void setActive(int userId, boolean active) {
        String sql = "UPDATE users SET is_active = ? WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, active);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting active status for user: " + userId, e);
        }
    }

    /**
     * Counts suppliers with a registration number matching the given prefix.
     */
    @Override
    public int countSuppliersByRegNumberPrefix(String prefix) {
        String sql = "SELECT COUNT(*) FROM suppliers WHERE registration_number LIKE ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, prefix + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting suppliers by prefix: " + prefix, e);
        }
        return 0;
    }

    /**
     * Gets email addresses and user IDs of all active procurement officers.
     */
    @Override
    public List<Object[]> getOfficerEmails() {
        List<Object[]> result = new ArrayList<>();
        String sql = "SELECT id, email FROM users WHERE role = 'OFFICER' AND is_active = TRUE";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                result.add(new Object[]{rs.getInt("id"), rs.getString("email")});
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting officer emails", e);
        }
        return result;
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Maps a ResultSet row to a basic User object (users table only).
     */
    private User mapBasicUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(rs.getString("role"));
        user.setActive(rs.getBoolean("is_active"));
        user.setFailedLoginAttempts(rs.getInt("failed_login_attempts"));
        user.setAccountLocked(rs.getBoolean("account_locked"));
        return user;
    }

    /**
     * Loads role-specific profile data from the appropriate table.
     */
    private void loadProfileData(Connection conn, User user) throws SQLException {
        switch (user.getRole()) {
            case "SUPPLIER":
                loadSupplierProfile(conn, user);
                break;
            case "OFFICER":
                loadOfficerProfile(conn, user);
                break;
            case "EVALUATOR":
                loadEvaluatorProfile(conn, user);
                break;
        }
    }

    private void loadSupplierProfile(Connection conn, User user) throws SQLException {
        String sql = "SELECT company_name, registration_number, physical_address, contact_number, is_verified FROM suppliers WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, user.getId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                user.setCompanyName(rs.getString("company_name"));
                user.setRegistrationNumber(rs.getString("registration_number"));
                user.setPhysicalAddress(rs.getString("physical_address"));
                user.setContactNumber(rs.getString("contact_number"));
                user.setVerified(rs.getBoolean("is_verified"));
                user.setDisplayName(rs.getString("company_name"));
            }
        }
    }

    private void loadOfficerProfile(Connection conn, User user) throws SQLException {
        String sql = "SELECT full_name, department, staff_id FROM officers WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, user.getId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                user.setDisplayName(rs.getString("full_name"));
                user.setDepartment(rs.getString("department"));
                user.setStaffId(rs.getString("staff_id"));
            }
        }
    }

    private void loadEvaluatorProfile(Connection conn, User user) throws SQLException {
        String sql = "SELECT full_name, department, staff_id FROM evaluators WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, user.getId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                user.setDisplayName(rs.getString("full_name"));
                user.setDepartment(rs.getString("department"));
                user.setStaffId(rs.getString("staff_id"));
            }
        }
    }
}
