package com.procuregov.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class Bid implements Serializable {
    private int id;
    private int tenderId;
    private int supplierId;
    private BigDecimal amount;
    private String technicalCompliance;
    private int proposedTimelineDays;
    private Date submittedAt;
    private String tenderTitle;
    private String tenderRefNumber;
    private String companyName;
    private String documentPath;
    private List<BidTechnicalCriterion> technicalCriteria;

    public Bid() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getTenderId() { return tenderId; }
    public void setTenderId(int tenderId) { this.tenderId = tenderId; }
    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getTechnicalCompliance() { return technicalCompliance; }
    public void setTechnicalCompliance(String technicalCompliance) { this.technicalCompliance = technicalCompliance; }
    public int getProposedTimelineDays() { return proposedTimelineDays; }
    public void setProposedTimelineDays(int proposedTimelineDays) { this.proposedTimelineDays = proposedTimelineDays; }
    public Date getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Date submittedAt) { this.submittedAt = submittedAt; }
    public String getTenderTitle() { return tenderTitle; }
    public void setTenderTitle(String tenderTitle) { this.tenderTitle = tenderTitle; }
    public String getTenderRefNumber() { return tenderRefNumber; }
    public void setTenderRefNumber(String tenderRefNumber) { this.tenderRefNumber = tenderRefNumber; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getDocumentPath() { return documentPath; }
    public void setDocumentPath(String documentPath) { this.documentPath = documentPath; }
    public List<BidTechnicalCriterion> getTechnicalCriteria() { return technicalCriteria; }
    public void setTechnicalCriteria(List<BidTechnicalCriterion> technicalCriteria) { this.technicalCriteria = technicalCriteria; }
}
