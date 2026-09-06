package com.rahul.inventorybilling.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** What GET /api/gold-price sends back to the dashboard. */
public class GoldPriceResponse {

    private BigDecimal pricePerGram;
    private BigDecimal pricePerTenGram;
    private String currency;
    private String source;
    private LocalDateTime asOf;

    public GoldPriceResponse(BigDecimal pricePerGram,
                             BigDecimal pricePerTenGram,
                             String currency,
                             String source,
                             LocalDateTime asOf) {
        this.pricePerGram = pricePerGram;
        this.pricePerTenGram = pricePerTenGram;
        this.currency = currency;
        this.source = source;
        this.asOf = asOf;
    }

    public BigDecimal getPricePerGram() { return pricePerGram; }
    public BigDecimal getPricePerTenGram() { return pricePerTenGram; }
    public String getCurrency() { return currency; }
    public String getSource() { return source; }
    public LocalDateTime getAsOf() { return asOf; }
}
