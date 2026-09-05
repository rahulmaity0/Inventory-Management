package com.rahul.inventorybilling.dto;

import java.math.BigDecimal;

/** One row of the monthly report: a month, and the two totals for it. */
public class MonthlyFlowResponse {

    private String month;
    private BigDecimal issuePureGold;
    private BigDecimal receiptPureGold;

    public MonthlyFlowResponse(String month, BigDecimal issuePureGold, BigDecimal receiptPureGold) {
        this.month = month;
        this.issuePureGold = issuePureGold;
        this.receiptPureGold = receiptPureGold;
    }

    public String getMonth() {
        return month;
    }

    public BigDecimal getIssuePureGold() {
        return issuePureGold;
    }

    public BigDecimal getReceiptPureGold() {
        return receiptPureGold;
    }
}
