package com.rahul.inventorybilling.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/api")
    public Map<String, Object> home() {
        return Map.of(
                "message", "Jewellery inventory backend API is running",
                "endpoints", new String[]{"/api/dashboard", "/api/clients", "/api/clients/{clientId}/ledger", "/api/transactions", "/api/reports/monthly-flow"},
                "nextStep", "Open / to use the simple frontend"
        );
    }
}
