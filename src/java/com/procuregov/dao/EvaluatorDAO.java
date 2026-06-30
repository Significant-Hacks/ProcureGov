package com.procuregov.dao;

import java.util.List;

/**
 * Data Access Object interface for Evaluator entity and related lookups.
 * Enforces separation of concerns between data access and business logic.
 */
public interface EvaluatorDAO {

    /**
     * Gets the evaluator database ID for a given user ID.
     * @param userId the users table primary key
     * @return the evaluators table ID, or -1 if not found
     */
    int getEvaluatorIdByUserId(int userId);

    /**
     * Gets all evaluator IDs assigned to a specific tender.
     * @param tenderId the tender ID
     * @return list of evaluator IDs
     */
    List<Integer> getAssignedEvaluatorIds(int tenderId);

    /**
     * Checks if a specific evaluator has scored a specific bid.
     * @param evaluatorId the evaluator ID
     * @param bidId the bid ID
     * @return true if the evaluator has already scored this bid
     */
    boolean hasEvaluatorScoredBid(int evaluatorId, int bidId);

    /**
     * Gets email addresses of all evaluators assigned to a specific tender.
     * @param tenderId the tender ID
     * @return list of Object arrays: [evaluatorId, userId, email]
     */
    List<Object[]> getAssignedEvaluatorInfo(int tenderId);

    /**
     * Checks if any evaluation score exists for a tender (i.e. evaluation has started).
     * @param tenderId the tender ID
     * @return true if at least one score has been submitted
     */
    boolean hasAnyEvaluationForTender(int tenderId);

    /**
     * Counts how many bids an evaluator has scored for a specific tender.
     * @param evaluatorId the evaluator ID
     * @param tenderId the tender ID
     * @return the number of bids scored by this evaluator for this tender
     */
    int countScoredBidsByEvaluatorAndTender(int evaluatorId, int tenderId);

    /**
     * Assigns all active evaluators to a tender.
     * @param tenderId the tender ID
     * @return number of evaluators assigned
     */
    int assignAllEvaluatorsToTender(int tenderId);

    /**
     * Gets all evaluator IDs in the system.
     * @return list of all evaluator IDs
     */
    List<Integer> getAllEvaluatorIds();
}
