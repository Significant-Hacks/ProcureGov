package com.procuregov.dao;

import java.util.List;

/**
 * Data Access Object interface for Supplier entity and related lookups.
 * Enforces separation of concerns between data access and business logic.
 */
public interface SupplierDAO {

    /**
     * Gets the supplier database ID for a given user ID.
     * @param userId the users table primary key
     * @return the suppliers table ID, or -1 if not found
     */
    int getSupplierIdByUserId(int userId);

    /**
     * Gets the email address for a user by their user ID.
     * @param userId the users table primary key
     * @return the email address, or null if not found
     */
    String getEmailByUserId(int userId);

    /**
     * Gets all active supplier email addresses for tender notification.
     * @return list of email addresses
     */
    List<String> getAllActiveEmails();

    /**
     * Gets all bidder email addresses for a specific tender.
     * @param tenderId the tender ID
     * @return list of email addresses
     */
    List<String> getBidderEmails(int tenderId);

    /**
     * Gets the supplier company name for a specific bid.
     * @param bidId the bid ID
     * @return the company name, or null if not found
     */
    String getSupplierNameByBidId(int bidId);

    /**
     * Gets the supplier ID for a specific bid.
     * @param bidId the bid ID
     * @return the supplier ID, or -1 if not found
     */
    int getSupplierIdByBidId(int bidId);

    /**
     * Gets bidder info (email + supplier ID) for a specific tender.
     * Used for Won/Not Won email differentiation.
     * @param tenderId the tender ID
     * @return list of Object arrays: [email, supplierId]
     */
    List<Object[]> getBidderInfo(int tenderId);
}
