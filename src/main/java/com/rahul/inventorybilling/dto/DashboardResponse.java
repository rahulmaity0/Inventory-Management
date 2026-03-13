package com.rahul.inventorybilling.dto;

import java.math.BigDecimal;

public record DashboardResponse(
        BigDecimal lockerPureGold,
        BigDecimal totalClientPureGold,
        BigDecimal totalBusinessPureGold,
        long clientCount
) {
}
