package com.rahul.inventorybilling.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "inventory_state")
public class InventoryState {

    @Id
    private Long id;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal lockerPureGold;

    public InventoryState() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getLockerPureGold() {
        return lockerPureGold;
    }

    public void setLockerPureGold(BigDecimal lockerPureGold) {
        this.lockerPureGold = lockerPureGold;
    }
}
