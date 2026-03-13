package com.rahul.inventorybilling.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal currentPureBalance;

    public Client() {
    }

    public Client(String name, BigDecimal currentPureBalance) {
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

    public void setCurrentPureBalance(BigDecimal currentPureBalance) {
        this.currentPureBalance = currentPureBalance;
    }
}
