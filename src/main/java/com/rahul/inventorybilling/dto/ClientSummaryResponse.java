package com.rahul.inventorybilling.dto;

import java.math.BigDecimal;

public record ClientSummaryResponse(
        Long id,
        String name,
        BigDecimal currentPureBalance
) {
}
