package com.procuregov.dao;

import com.procuregov.model.Award;
import java.util.List;

public interface AwardDAO {
    boolean create(Award award);
    Award getByTenderId(int tenderId);
    List<Award> getAll();
    List<Award> getBySupplierId(int supplierId);

    /**
     * Looks up an award by the tender's reference number.
     * @param tenderRef the tender reference number (e.g. MPW-2026-0001)
     * @return the Award with joined data, or null if not found
     */
    Award getByTenderRef(String tenderRef);

    /**
     * Looks up an award by its confirmation document path.
     * @param path the confirmation_document_path value
     * @return the Award with joined data, or null if not found
     */
    Award getByConfirmationPath(String path);

    /**
     * Updates the confirmation document path for an award.
     * @param awardId the award ID
     * @param newPath the new path value
     * @return true if the update succeeded
     */
    boolean updateConfirmationPath(int awardId, String newPath);
}
