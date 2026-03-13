package com.rahul.inventorybilling.dto;

import java.math.BigDecimal;

public record CreateTransactionResponse(
        Long transactionId,
        Long clientId,
        String clientName,
        BigDecimal pureGoldEquivalent,
        BigDecimal updatedClientBalance,
        BigDecimal updatedLockerBalance
) {
}
