package com.kousen.cert.analytics.controller;

import com.kousen.cert.analytics.model.AnalyticsDTO;
import com.kousen.cert.analytics.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    
    private final AnalyticsService analyticsService;
    
    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }
    
    @GetMapping("/dashboard")
    public ResponseEntity<AnalyticsDTO.DashboardData> getDashboardData() {
        return ResponseEntity.ok(analyticsService.getDashboardData());
    }
    
    @GetMapping("/summary")
    public ResponseEntity<AnalyticsDTO.Summary> getSummary() {
        AnalyticsDTO.DashboardData dashboard = analyticsService.getDashboardData();
        return ResponseEntity.ok(dashboard.summary());
    }
    
    @GetMapping("/trends")
    public ResponseEntity<?> getTrends() {
        AnalyticsDTO.DashboardData dashboard = analyticsService.getDashboardData();
        return ResponseEntity.ok(dashboard.dailyTrend());
    }
    
    @GetMapping("/books")
    public ResponseEntity<?> getBookPopularity() {
        AnalyticsDTO.DashboardData dashboard = analyticsService.getDashboardData();
        return ResponseEntity.ok(dashboard.bookPopularity());
    }
    
    @GetMapping("/performance")
    public ResponseEntity<AnalyticsDTO.PerformanceMetrics> getPerformance() {
        AnalyticsDTO.DashboardData dashboard = analyticsService.getDashboardData();
        return ResponseEntity.ok(dashboard.performance());
    }
}