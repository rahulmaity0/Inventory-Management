package com.rahul.inventorybilling.dto;

import com.rahul.inventorybilling.model.ItemType;
import com.rahul.inventorybilling.model.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * The JSON body of POST /api/transactions.
 *
 * makingChargePercent is the only optional number - the service treats a
 * missing value as zero.
 */
public class CreateTransactionRequest {

    @NotNull
    private Long clientId;

    @NotNull
    private TransactionType transactionType;

    @NotNull
    @DecimalMin("0.001")
    @Digits(integer = 9, fraction = 3)
    private BigDecimal grossWeight;

    @NotNull
    @DecimalMin("0.01")
    @Digits(integer = 3, fraction = 2)
    private BigDecimal purityPercent;

    @DecimalMin("0.00")
    @Digits(integer = 3, fraction = 2)
    private BigDecimal makingChargePercent;

    @NotNull
    private LocalDate transactionDate;

    // Optional - a receipt of loose metal is not an ornament at all.
    private ItemType itemType;

    @Size(max = 300)
    private String notes;

    public CreateTransactionRequest() {
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getGrossWeight() {
        return grossWeight;
    }

    public void setGrossWeight(BigDecimal grossWeight) {
        this.grossWeight = grossWeight;
    }

    public BigDecimal getPurityPercent() {
        return purityPercent;
    }

    public void setPurityPercent(BigDecimal purityPercent) {
        this.purityPercent = purityPercent;
    }

    public BigDecimal getMakingChargePercent() {
        return makingChargePercent;
    }

    public void setMakingChargePercent(BigDecimal makingChargePercent) {
        this.makingChargePercent = makingChargePercent;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
