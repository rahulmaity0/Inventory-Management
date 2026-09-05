package com.rahul.inventorybilling.dto;

import java.math.BigDecimal;

/**
 * What we send back to describe one client.
 * A plain class: the constructor fills the fields, the getters read them.
 * Jackson turns the getters into JSON keys (id, name, currentPureBalance).
 */
public class ClientSummaryResponse {

    private Long id;
    private String name;
    private BigDecimal currentPureBalance;

    public ClientSummaryResponse(Long id, String name, BigDecimal currentPureBalance) {
        this.id = id;
        this.name = name;
        this.currentPureBalance = currentPureBalance;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getCurrentPureBalance() {
        return currentPureBalance;
    }
}
