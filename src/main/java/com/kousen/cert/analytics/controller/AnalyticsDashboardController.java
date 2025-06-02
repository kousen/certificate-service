package com.kousen.cert.analytics.controller;

import com.kousen.cert.analytics.service.AnalyticsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AnalyticsDashboardController {
    
    private final AnalyticsService analyticsService;
    
    public AnalyticsDashboardController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }
    
    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("dashboardData", analyticsService.getDashboardData());
        return "analytics/dashboard";
    }
}