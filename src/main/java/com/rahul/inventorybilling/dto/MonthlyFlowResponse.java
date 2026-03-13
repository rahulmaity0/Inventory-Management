package com.rahul.inventorybilling.dto;

import java.math.BigDecimal;

public record MonthlyFlowResponse(
        String month,
        BigDecimal issuePureGold,
        BigDecimal receiptPureGold
) {
}
