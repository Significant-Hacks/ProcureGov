package com.procuregov.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class Tender implements Serializable {
    private int id;
    private String referenceNumber;
    private String title;
    private String category;
    private String description;
    private BigDecimal estimatedValue;
    private Date deadline;
    private String status;
    private int createdBy;
    private Date createdAt;
    private String noticeDocumentPath;
    private boolean showEstimatedValue;

    public Tender() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getEstimatedValue() { return estimatedValue; }
    public void setEstimatedValue(BigDecimal estimatedValue) { this.estimatedValue = estimatedValue; }
    public Date getDeadline() { return deadline; }
    public void setDeadline(Date deadline) { this.deadline = deadline; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public String getNoticeDocumentPath() { return noticeDocumentPath; }
    public void setNoticeDocumentPath(String noticeDocumentPath) { this.noticeDocumentPath = noticeDocumentPath; }
    public boolean isShowEstimatedValue() { return showEstimatedValue; }
    public void setShowEstimatedValue(boolean showEstimatedValue) { this.showEstimatedValue = showEstimatedValue; }
}
