package com.rahul.inventorybilling.controller;

import com.rahul.inventorybilling.dto.MonthlyFlowResponse;
import com.rahul.inventorybilling.service.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/monthly-flow")
    public List<MonthlyFlowResponse> getMonthlyFlow() {
        return reportService.getMonthlyFlow();
    }
}
