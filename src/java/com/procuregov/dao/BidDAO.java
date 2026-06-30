package com.procuregov.dao;

import com.procuregov.model.Bid;
import java.util.List;

public interface BidDAO {
    boolean submit(Bid bid);
    boolean update(Bid bid);
    Bid getById(int id);
    List<Bid> getByTenderId(int tenderId);
    List<Bid> getBySupplierId(int supplierId);
    int countByTenderId(int tenderId);
}
