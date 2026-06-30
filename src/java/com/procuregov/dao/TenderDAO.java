package com.procuregov.dao;

import com.procuregov.model.Tender;
import java.util.List;

public interface TenderDAO {
    boolean create(Tender tender);
    boolean update(Tender tender);
    Tender getById(int id);
    List<Tender> getAll();
    List<Tender> getByStatus(String status);
    List<Tender> getOpenTenders();
    int countByStatus(String status);
    int autoCloseExpiredTenders();

    /**
     * Gets all tenders assigned to a specific evaluator (via tender_evaluators).
     * @param evaluatorId the evaluator ID
     * @return list of tenders assigned to this evaluator
     */
    List<Tender> getByAssignedEvaluator(int evaluatorId);
}
