package com.rahul.inventorybilling.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateClientRequest(
        @NotBlank @Size(max = 100) String name,
        @DecimalMin("0.000") @Digits(integer = 9, fraction = 3) BigDecimal openingPureBalance
) {
}
