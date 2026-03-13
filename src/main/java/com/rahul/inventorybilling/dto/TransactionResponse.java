package com.rahul.inventorybilling.dto;

import com.rahul.inventorybilling.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponse(
        Long id,
        LocalDate transactionDate,
        TransactionType transactionType,
        BigDecimal grossWeight,
        BigDecimal purityPercent,
        BigDecimal makingChargePercent,
        BigDecimal effectivePurityPercent,
        BigDecimal pureGoldEquivalent,
        String notes
) {
}
