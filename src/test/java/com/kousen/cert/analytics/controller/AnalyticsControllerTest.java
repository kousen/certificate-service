package com.kousen.cert.analytics.controller;

import com.kousen.cert.analytics.model.AnalyticsDTO;
import com.kousen.cert.analytics.service.AnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnalyticsControllerTest {

    private AnalyticsService analyticsService;
    private AnalyticsDTO.DashboardData dashboardData;

    @BeforeEach
    void setUp() {
        analyticsService = mock(AnalyticsService.class);
        var summary = new AnalyticsDTO.Summary(
                42, 1, 5, 20, 7, 30, 123.4, "Modern Java Recipes");
        var performance = new AnalyticsDTO.PerformanceMetrics(
                100.0, 50.0, 150.0, 40, 2, 95.2);
        dashboardData = new AnalyticsDTO.DashboardData(
                summary,
                List.of(),
                List.of(new AnalyticsDTO.BookPopularity("Modern Java Recipes", 21, 50.0)),
                List.of(),
                performance,
                Map.of());
        when(analyticsService.getDashboardData()).thenReturn(dashboardData);
    }

    @Test
    void shouldReturnDashboardData() {
        var controller = new AnalyticsController(analyticsService);

        var response = controller.getDashboardData();

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo(dashboardData);
    }

    @Test
    void shouldReturnSummary() {
        var controller = new AnalyticsController(analyticsService);

        var response = controller.getSummary();

        assertThat(response.getBody()).isEqualTo(dashboardData.summary());
        assertThat(response.getBody().totalCertificates()).isEqualTo(42);
    }

    @Test
    void shouldReturnTrends() {
        var controller = new AnalyticsController(analyticsService);

        var response = controller.getTrends();

        assertThat(response.getBody()).isEqualTo(dashboardData.dailyTrend());
    }

    @Test
    void shouldReturnBookPopularity() {
        var controller = new AnalyticsController(analyticsService);

        var response = controller.getBookPopularity();

        assertThat(response.getBody()).isEqualTo(dashboardData.bookPopularity());
    }

    @Test
    void shouldReturnPerformanceMetrics() {
        var controller = new AnalyticsController(analyticsService);

        var response = controller.getPerformance();

        assertThat(response.getBody()).isEqualTo(dashboardData.performance());
        assertThat(response.getBody().successRate()).isEqualTo(95.2);
    }

    @Test
    void shouldPopulateDashboardModel() {
        var controller = new AnalyticsDashboardController(analyticsService);
        var model = new ConcurrentModel();

        String viewName = controller.dashboard(model);

        assertThat(viewName).isEqualTo("analytics/dashboard");
        assertThat(model.getAttribute("dashboardData")).isEqualTo(dashboardData);
    }
}
