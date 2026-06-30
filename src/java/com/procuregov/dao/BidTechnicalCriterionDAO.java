package com.procuregov.dao;

import com.procuregov.model.BidTechnicalCriterion;
import java.util.List;

/**
 * Data Access Object for bid_technical_criteria table.
 * Handles CRUD operations for structured technical compliance criteria.
 */
public interface BidTechnicalCriterionDAO {
    boolean insert(BidTechnicalCriterion criterion);
    boolean insertBatch(List<BidTechnicalCriterion> criteria);
    List<BidTechnicalCriterion> getByBidId(int bidId);
    boolean deleteByBidId(int bidId);
}
