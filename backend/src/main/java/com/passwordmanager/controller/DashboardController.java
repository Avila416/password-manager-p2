package com.passwordmanager.controller;


import lombok.extern.slf4j.Slf4j;
import com.passwordmanager.dto.DashboardResponse;
import com.passwordmanager.service.DashboardService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@Slf4j
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping
    public DashboardResponse dashboard() {
        log.info("DashboardController.dashboard called");
        return service.getDashboard();
    }
}


