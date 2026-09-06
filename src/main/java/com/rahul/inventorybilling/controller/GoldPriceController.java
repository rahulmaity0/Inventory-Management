package com.rahul.inventorybilling.controller;

import com.rahul.inventorybilling.dto.GoldPriceResponse;
import com.rahul.inventorybilling.service.GoldPriceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** The dashboard ticker reads this. */
@RestController
@RequestMapping("/api/gold-price")
public class GoldPriceController {

    private final GoldPriceService goldPriceService;

    public GoldPriceController(GoldPriceService goldPriceService) {
        this.goldPriceService = goldPriceService;
    }

    @GetMapping
    public GoldPriceResponse getGoldPrice() {
        // No work happens here - the price was already fetched on a schedule.
        return goldPriceService.getPrice();
    }
}
