package com.procuregov.dao;

import com.procuregov.model.Evaluation;
import java.util.List;

public interface EvaluationDAO {
    boolean score(Evaluation evaluation);
    List<Evaluation> getByBidId(int bidId);
    List<Evaluation> getByEvaluatorId(int evaluatorId);
    List<Evaluation> getPendingByEvaluatorId(int evaluatorId);
}
