package com.rahul.inventorybilling.dto;

import java.math.BigDecimal;

/** The four numbers shown on the dashboard. */
public class DashboardResponse {

    private BigDecimal lockerPureGold;
    private BigDecimal totalClientPureGold;
    private BigDecimal totalBusinessPureGold;
    private long clientCount;

    public DashboardResponse(BigDecimal lockerPureGold,
                             BigDecimal totalClientPureGold,
                             BigDecimal totalBusinessPureGold,
                             long clientCount) {
        this.lockerPureGold = lockerPureGold;
        this.totalClientPureGold = totalClientPureGold;
        this.totalBusinessPureGold = totalBusinessPureGold;
        this.clientCount = clientCount;
    }

    public BigDecimal getLockerPureGold() {
        return lockerPureGold;
    }

    public BigDecimal getTotalClientPureGold() {
        return totalClientPureGold;
    }

    public BigDecimal getTotalBusinessPureGold() {
        return totalBusinessPureGold;
    }

    public long getClientCount() {
        return clientCount;
    }
}
