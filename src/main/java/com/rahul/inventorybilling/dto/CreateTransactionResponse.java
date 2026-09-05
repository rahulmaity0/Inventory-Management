package com.rahul.inventorybilling.dto;

import java.math.BigDecimal;

/**
 * What POST /api/transactions sends back: what was recorded, and the two
 * balances after the change, so the caller does not have to fetch them again.
 */
public class CreateTransactionResponse {

    private Long transactionId;
    private Long clientId;
    private String clientName;
    private BigDecimal pureGoldEquivalent;
    private BigDecimal updatedClientBalance;
    private BigDecimal updatedLockerBalance;

    public CreateTransactionResponse(Long transactionId,
                                     Long clientId,
                                     String clientName,
                                     BigDecimal pureGoldEquivalent,
                                     BigDecimal updatedClientBalance,
                                     BigDecimal updatedLockerBalance) {
        this.transactionId = transactionId;
        this.clientId = clientId;
        this.clientName = clientName;
        this.pureGoldEquivalent = pureGoldEquivalent;
        this.updatedClientBalance = updatedClientBalance;
        this.updatedLockerBalance = updatedLockerBalance;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public Long getClientId() {
        return clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public BigDecimal getPureGoldEquivalent() {
        return pureGoldEquivalent;
    }

    public BigDecimal getUpdatedClientBalance() {
        return updatedClientBalance;
    }

    public BigDecimal getUpdatedLockerBalance() {
        return updatedLockerBalance;
    }
}
