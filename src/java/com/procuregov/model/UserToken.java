package com.procuregov.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * UserToken JavaBean for password reset and account confirmation tokens.
 * Maps to the user_tokens table in the database.
 */
public class UserToken implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int userId;
    private String token;
    private String tokenType; // PASSWORD_RESET, ACCOUNT_CONFIRMATION
    private LocalDateTime expiresAt;
    private boolean used;
    private LocalDateTime createdAt;

    public UserToken() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /**
     * Checks if this token has expired.
     * @return true if the token expiry time has passed
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
