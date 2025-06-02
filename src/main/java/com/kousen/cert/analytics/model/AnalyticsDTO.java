package com.kousen.cert.analytics.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class AnalyticsDTO {
    
    public record Summary(
        long totalCertificates,
        long certificatesToday,
        long certificatesThisWeek,
        long certificatesThisMonth,
        long totalVerifications,
        long uniquePurchasers,
        double averageGenerationTime,
        String mostPopularBook
    ) {}
    
    public record TimeSeriesData(
        Instant timestamp,
        long count
    ) {}
    
    public record BookPopularity(
        String bookTitle,
        long count,
        double percentage
    ) {}
    
    public record RecentActivity(
        String certificateId,
        String purchaserName,
        String bookTitle,
        Instant timestamp,
        String eventType
    ) {}
    
    public record PerformanceMetrics(
        double avgGenerationTimeMs,
        double avgSigningTimeMs,
        double avgTotalTimeMs,
        long successCount,
        long failureCount,
        double successRate
    ) {}
    
    public record DashboardData(
        Summary summary,
        List<TimeSeriesData> dailyTrend,
        List<BookPopularity> bookPopularity,
        List<RecentActivity> recentActivities,
        PerformanceMetrics performance,
        Map<String, Object> systemMetrics
    ) {}
}