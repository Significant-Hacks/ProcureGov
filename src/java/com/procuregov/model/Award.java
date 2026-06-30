package com.procuregov.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class Award implements Serializable {
    private int id;
    private int tenderId;
    private int winningBidId;
    private BigDecimal awardedValue;
    private String justification;
    private int awardedBy;
    private Date awardedAt;
    private String tenderTitle;
    private String tenderRefNumber;
    private String supplierName;
    private String confirmationDocumentPath;
    private int winningSupplierId;

    public Award() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getTenderId() { return tenderId; }
    public void setTenderId(int tenderId) { this.tenderId = tenderId; }
    public int getWinningBidId() { return winningBidId; }
    public void setWinningBidId(int winningBidId) { this.winningBidId = winningBidId; }
    public BigDecimal getAwardedValue() { return awardedValue; }
    public void setAwardedValue(BigDecimal awardedValue) { this.awardedValue = awardedValue; }
    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }
    public int getAwardedBy() { return awardedBy; }
    public void setAwardedBy(int awardedBy) { this.awardedBy = awardedBy; }
    public Date getAwardedAt() { return awardedAt; }
    public void setAwardedAt(Date awardedAt) { this.awardedAt = awardedAt; }
    public String getTenderTitle() { return tenderTitle; }
    public void setTenderTitle(String tenderTitle) { this.tenderTitle = tenderTitle; }
    public String getTenderRefNumber() { return tenderRefNumber; }
    public void setTenderRefNumber(String tenderRefNumber) { this.tenderRefNumber = tenderRefNumber; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getConfirmationDocumentPath() { return confirmationDocumentPath; }
    public void setConfirmationDocumentPath(String confirmationDocumentPath) { this.confirmationDocumentPath = confirmationDocumentPath; }
    public int getWinningSupplierId() { return winningSupplierId; }
    public void setWinningSupplierId(int winningSupplierId) { this.winningSupplierId = winningSupplierId; }
}
