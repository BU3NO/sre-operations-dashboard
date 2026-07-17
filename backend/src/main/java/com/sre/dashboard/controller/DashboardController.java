package com.sre.dashboard.controller;

import com.sre.dashboard.dto.DashboardSummaryResponse;
import com.sre.dashboard.service.MonitoredServiceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final MonitoredServiceService service;

    public DashboardController(MonitoredServiceService service) {
        this.service = service;
    }

    @GetMapping("/summary")
    public DashboardSummaryResponse summary() {
        return service.summary();
    }
}
