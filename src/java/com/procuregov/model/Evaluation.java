package com.procuregov.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class Evaluation implements Serializable {
    private int id;
    private int bidId;
    private int tenderId;
    private int evaluatorId;
    private BigDecimal technicalScore;
    private BigDecimal priceScore;
    private BigDecimal timelineScore;
    private BigDecimal weightedTotal;
    private Date submittedAt;
    private String tenderTitle;
    private String tenderRefNumber;
    private String supplierName;
    private BigDecimal bidAmount;
    private String evaluatorName;

    public Evaluation() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getBidId() { return bidId; }
    public void setBidId(int bidId) { this.bidId = bidId; }
    public int getTenderId() { return tenderId; }
    public void setTenderId(int tenderId) { this.tenderId = tenderId; }
    public int getEvaluatorId() { return evaluatorId; }
    public void setEvaluatorId(int evaluatorId) { this.evaluatorId = evaluatorId; }
    public BigDecimal getTechnicalScore() { return technicalScore; }
    public void setTechnicalScore(BigDecimal technicalScore) { this.technicalScore = technicalScore; }
    public BigDecimal getPriceScore() { return priceScore; }
    public void setPriceScore(BigDecimal priceScore) { this.priceScore = priceScore; }
    public BigDecimal getTimelineScore() { return timelineScore; }
    public void setTimelineScore(BigDecimal timelineScore) { this.timelineScore = timelineScore; }
    public BigDecimal getWeightedTotal() { return weightedTotal; }
    public void setWeightedTotal(BigDecimal weightedTotal) { this.weightedTotal = weightedTotal; }
    public Date getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Date submittedAt) { this.submittedAt = submittedAt; }
    public String getTenderTitle() { return tenderTitle; }
    public void setTenderTitle(String tenderTitle) { this.tenderTitle = tenderTitle; }
    public String getTenderRefNumber() { return tenderRefNumber; }
    public void setTenderRefNumber(String tenderRefNumber) { this.tenderRefNumber = tenderRefNumber; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public BigDecimal getBidAmount() { return bidAmount; }
    public void setBidAmount(BigDecimal bidAmount) { this.bidAmount = bidAmount; }
    public String getEvaluatorName() { return evaluatorName; }
    public void setEvaluatorName(String evaluatorName) { this.evaluatorName = evaluatorName; }
}
