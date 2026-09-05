package com.rahul.inventorybilling.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** A health check that also lists what else is available. */
@RestController
public class HomeController {

    @GetMapping("/api")
    public Map<String, Object> home() {
        List<String> endpoints = new ArrayList<>();
        endpoints.add("/api/dashboard");
        endpoints.add("/api/clients");
        endpoints.add("/api/clients/{clientId}/ledger");
        endpoints.add("/api/transactions");
        endpoints.add("/api/reports/monthly-flow");

        // LinkedHashMap keeps the keys in the order they were added, so the
        // JSON always comes out the same way round.
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Jewellery inventory backend API is running");
        response.put("endpoints", endpoints);
        response.put("nextStep", "Open / to use the simple frontend");
        return response;
    }
}
