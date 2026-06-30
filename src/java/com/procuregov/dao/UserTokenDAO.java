package com.procuregov.dao;

import com.procuregov.model.UserToken;

/**
 * Data Access Object interface for UserToken entity.
 * Handles password reset and account confirmation tokens.
 */
public interface UserTokenDAO {

    /**
     * Saves a new token to the database.
     * @param token the UserToken to save
     * @return true if the save succeeded
     */
    boolean save(UserToken token);

    /**
     * Finds a token by its string value.
     * @param token the token string to look up
     * @return the UserToken, or null if not found
     */
    UserToken getByToken(String token);

    /**
     * Marks a token as used after it has been consumed.
     * @param tokenId the token's database ID
     * @return true if the update succeeded
     */
    boolean markAsUsed(int tokenId);

    /**
     * Deletes all tokens of a given type for a user.
     * Used to invalidate old tokens before creating new ones.
     * @param userId the user's ID
     * @param tokenType the token type (PASSWORD_RESET or ACCOUNT_CONFIRMATION)
     * @return true if deletion succeeded
     */
    boolean deleteByUserAndType(int userId, String tokenType);
}
