package com.rahul.inventorybilling.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "gold_transactions")
public class GoldTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal grossWeight;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal purityPercent;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal makingChargePercent;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal effectivePurityPercent;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal pureGoldEquivalent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Column(nullable = false)
    private LocalDate transactionDate;

    @Column(length = 300)
    private String notes;

    public GoldTransaction() {
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public void setGrossWeight(BigDecimal grossWeight) {
        this.grossWeight = grossWeight;
    }

    public void setPurityPercent(BigDecimal purityPercent) {
        this.purityPercent = purityPercent;
    }

    public void setMakingChargePercent(BigDecimal makingChargePercent) {
        this.makingChargePercent = makingChargePercent;
    }

    public void setEffectivePurityPercent(BigDecimal effectivePurityPercent) {
        this.effectivePurityPercent = effectivePurityPercent;
    }

    public void setPureGoldEquivalent(BigDecimal pureGoldEquivalent) {
        this.pureGoldEquivalent = pureGoldEquivalent;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Long getId() {
        return id;
    }

    public Client getClient() {
        return client;
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

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public String getNotes() {
        return notes;
    }
}
