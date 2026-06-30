package com.procuregov.dao;

import com.procuregov.model.User;
import java.util.List;

/**
 * Data Access Object interface for User entity.
 * Enforces separation of concerns between data access and business logic.
 */
public interface UserDAO {

    /**
     * Registers a new supplier user with profile data.
     * @param user the User JavaBean with all required fields
     * @return true if registration succeeded
     */
    boolean registerSupplier(User user);

    /**
     * Finds a user by email address for login authentication.
     * @param email the user's email
     * @return the User with profile data, or null if not found
     */
    User findByEmail(String email);

    /**
     * Gets a user by their database ID, including profile data.
     * @param id the user's primary key
     * @return the User with profile data, or null if not found
     */
    User getById(int id);

    /**
     * Checks if an email address is already registered.
     * @param email the email to check
     * @return true if the email exists in the database
     */
    boolean emailExists(String email);

    /**
     * Updates the user's password hash (for password reset).
     * @param userId the user's ID
     * @param newPasswordHash the new SHA-256 hash
     * @return true if the update succeeded
     */
    boolean updatePassword(int userId, String newPasswordHash);

    /**
     * Increments the failed login attempt counter in the database.
     * @param userId the user's ID
     */
    void incrementFailedAttempts(int userId);

    /**
     * Resets the failed login attempt counter to zero after successful login.
     * @param userId the user's ID
     */
    void resetFailedAttempts(int userId);

    /**
     * Sets the account locked status in the database.
     * @param userId the user's ID
     * @param locked true to lock, false to unlock
     */
    void setAccountLocked(int userId, boolean locked);

    /**
     * Sets the account active status (for account confirmation).
     * @param userId the user's ID
     * @param active true to activate
     */
    void setActive(int userId, boolean active);

    /**
     * Counts suppliers with a registration number matching the given prefix.
     * Used to auto-generate unique registration numbers.
     * @param prefix the registration number prefix (e.g. SUP-2026-)
     * @return the count of matching suppliers
     */
    int countSuppliersByRegNumberPrefix(String prefix);

    /**
     * Gets email addresses and user IDs of all active procurement officers.
     * Used for notifying officers when evaluations are complete.
     * @return list of Object arrays: [userId, email]
     */
    List<Object[]> getOfficerEmails();
}
