package com.rahul.inventorybilling.dto;

import com.rahul.inventorybilling.model.ItemType;
import com.rahul.inventorybilling.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

/** One line of a client's ledger. */
public class TransactionResponse {

    private Long id;
    private LocalDate transactionDate;
    private TransactionType transactionType;
    private BigDecimal grossWeight;
    private BigDecimal purityPercent;
    private BigDecimal makingChargePercent;
    private BigDecimal effectivePurityPercent;
    private BigDecimal pureGoldEquivalent;
    private ItemType itemType;
    private String notes;

    public TransactionResponse(Long id,
                               LocalDate transactionDate,
                               TransactionType transactionType,
                               BigDecimal grossWeight,
                               BigDecimal purityPercent,
                               BigDecimal makingChargePercent,
                               BigDecimal effectivePurityPercent,
                               BigDecimal pureGoldEquivalent,
                               ItemType itemType,
                               String notes) {
        this.id = id;
        this.transactionDate = transactionDate;
        this.transactionType = transactionType;
        this.grossWeight = grossWeight;
        this.purityPercent = purityPercent;
        this.makingChargePercent = makingChargePercent;
        this.effectivePurityPercent = effectivePurityPercent;
        this.pureGoldEquivalent = pureGoldEquivalent;
        this.itemType = itemType;
        this.notes = notes;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public BigDecimal getGrossWeight() {
        return grossWeight;
    }

    public BigDecimal getPurityPercent() {
        return purityPercent;
    }

    public BigDecimal getMakingChargePercent() {
        return makingChargePercent;
    }

    public BigDecimal getEffectivePurityPercent() {
        return effectivePurityPercent;
    }

    public BigDecimal getPureGoldEquivalent() {
        return pureGoldEquivalent;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public String getNotes() {
        return notes;
    }
}
