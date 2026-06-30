package com.procuregov.dao.impl;

import com.procuregov.dao.UserTokenDAO;
import com.procuregov.model.UserToken;
import com.procuregov.util.DBConnectionUtil;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JDBC implementation of UserTokenDAO.
 * Handles password reset and account confirmation tokens.
 */
public class UserTokenDAOImpl implements UserTokenDAO {

    private static final Logger LOGGER = Logger.getLogger(UserTokenDAOImpl.class.getName());

    @Override
    public boolean save(UserToken token) {
        String sql = "INSERT INTO user_tokens (user_id, token, token_type, expires_at, used) VALUES (?, ?, ?, ?, FALSE)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, token.getUserId());
            stmt.setString(2, token.getToken());
            stmt.setString(3, token.getTokenType());
            stmt.setTimestamp(4, Timestamp.valueOf(token.getExpiresAt()));
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving token", e);
        }
        return false;
    }

    @Override
    public UserToken getByToken(String tokenStr) {
        String sql = "SELECT id, user_id, token, token_type, expires_at, used, created_at FROM user_tokens WHERE token = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tokenStr);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                UserToken token = new UserToken();
                token.setId(rs.getInt("id"));
                token.setUserId(rs.getInt("user_id"));
                token.setToken(rs.getString("token"));
                token.setTokenType(rs.getString("token_type"));
                token.setExpiresAt(rs.getTimestamp("expires_at").toLocalDateTime());
                token.setUsed(rs.getBoolean("used"));
                token.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                return token;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting token", e);
        }
        return null;
    }

    @Override
    public boolean markAsUsed(int tokenId) {
        String sql = "UPDATE user_tokens SET used = TRUE WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tokenId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error marking token as used: " + tokenId, e);
        }
        return false;
    }

    @Override
    public boolean deleteByUserAndType(int userId, String tokenType) {
        String sql = "DELETE FROM user_tokens WHERE user_id = ? AND token_type = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, tokenType);
            return stmt.executeUpdate() >= 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting tokens for user: " + userId, e);
        }
        return false;
    }
}
