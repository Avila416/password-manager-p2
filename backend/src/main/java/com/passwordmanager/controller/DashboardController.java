package com.passwordmanager.controller;

import com.passwordmanager.dto.DashboardResponse;
import com.passwordmanager.service.DashboardService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping
    public DashboardResponse dashboard() {
        return service.getDashboard();
    }
}
