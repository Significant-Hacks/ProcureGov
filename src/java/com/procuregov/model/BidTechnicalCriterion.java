package com.procuregov.model;

import java.io.Serializable;

/**
 * Represents a structured technical compliance criterion submitted with a bid.
 * Suppliers select from predefined criterion types (dropdown) or add custom entries,
 * with optional evidence document attachments.
 */
public class BidTechnicalCriterion implements Serializable {

    private int id;
    private int bidId;
    private String criterionName;
    private String criterionType;
    private String criterionValue;
    private String evidenceDocumentPath;

    public BidTechnicalCriterion() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getBidId() { return bidId; }
    public void setBidId(int bidId) { this.bidId = bidId; }
    public String getCriterionName() { return criterionName; }
    public void setCriterionName(String criterionName) { this.criterionName = criterionName; }
    public String getCriterionType() { return criterionType; }
    public void setCriterionType(String criterionType) { this.criterionType = criterionType; }
    public String getCriterionValue() { return criterionValue; }
    public void setCriterionValue(String criterionValue) { this.criterionValue = criterionValue; }
    public String getEvidenceDocumentPath() { return evidenceDocumentPath; }
    public void setEvidenceDocumentPath(String evidenceDocumentPath) { this.evidenceDocumentPath = evidenceDocumentPath; }
}
