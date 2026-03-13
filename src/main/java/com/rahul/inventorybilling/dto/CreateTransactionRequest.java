package com.rahul.inventorybilling.dto;

import com.rahul.inventorybilling.model.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTransactionRequest(
        @NotNull Long clientId,
        @NotNull TransactionType transactionType,
        @NotNull @DecimalMin("0.001") @Digits(integer = 9, fraction = 3) BigDecimal grossWeight,
        @NotNull @DecimalMin("0.01") @Digits(integer = 3, fraction = 2) BigDecimal purityPercent,
        @DecimalMin("0.00") @Digits(integer = 3, fraction = 2) BigDecimal makingChargePercent,
        @NotNull LocalDate transactionDate,
        @Size(max = 300) String notes
) {
}
