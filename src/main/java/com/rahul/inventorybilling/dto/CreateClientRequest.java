package com.rahul.inventorybilling.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * The JSON body of POST /api/clients.
 *
 * Jackson needs the empty constructor and the setters so it can build this
 * object from the request body. The annotations are checked before the
 * controller method runs, because the controller marks it @Valid.
 */
public class CreateClientRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @DecimalMin("0.000")
    @Digits(integer = 9, fraction = 3)
    private BigDecimal openingPureBalance;

    public CreateClientRequest() {
    }

    public CreateClientRequest(String name, BigDecimal openingPureBalance) {
        this.name = name;
        this.openingPureBalance = openingPureBalance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getOpeningPureBalance() {
        return openingPureBalance;
    }

    public void setOpeningPureBalance(BigDecimal openingPureBalance) {
        this.openingPureBalance = openingPureBalance;
    }
}
