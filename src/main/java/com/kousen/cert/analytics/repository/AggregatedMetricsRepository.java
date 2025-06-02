package com.kousen.cert.analytics.repository;

import com.kousen.cert.analytics.model.AggregatedMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for accessing and manipulating AggregatedMetrics entities.
 */
@Repository
public interface AggregatedMetricsRepository extends JpaRepository<AggregatedMetrics, Long> {
    
    /**
     * Find metrics by name.
     */
    List<AggregatedMetrics> findByMetricName(String metricName);
    
    /**
     * Find metrics by name and key.
     */
    List<AggregatedMetrics> findByMetricNameAndMetricKey(String metricName, String metricKey);
    
    /**
     * Find metrics by name and time frame.
     */
    List<AggregatedMetrics> findByMetricNameAndTimeFrame(String metricName, String timeFrame);
    
    /**
     * Find metrics by name, key, and time frame.
     */
    List<AggregatedMetrics> findByMetricNameAndMetricKeyAndTimeFrame(String metricName, String metricKey, String timeFrame);
    
    /**
     * Find metrics by name, key, time frame, and timestamp between.
     */
    List<AggregatedMetrics> findByMetricNameAndMetricKeyAndTimeFrameAndTimestampBetween(
            String metricName, String metricKey, String timeFrame, Instant start, Instant end);
    
    /**
     * Find the most recent metric by name, key, and time frame.
     */
    @Query("SELECT m FROM AggregatedMetrics m " +
           "WHERE m.metricName = :metricName " +
           "AND m.metricKey = :metricKey " +
           "AND m.timeFrame = :timeFrame " +
           "ORDER BY m.timestamp DESC, m.calculatedAt DESC")
    List<AggregatedMetrics> findMostRecentByMetricNameAndMetricKeyAndTimeFrame(
            @Param("metricName") String metricName,
            @Param("metricKey") String metricKey,
            @Param("timeFrame") String timeFrame);
    
    /**
     * Find the most recent metric by name and time frame.
     */
    @Query("SELECT m FROM AggregatedMetrics m " +
           "WHERE m.metricName = :metricName " +
           "AND m.timeFrame = :timeFrame " +
           "ORDER BY m.timestamp DESC, m.calculatedAt DESC")
    List<AggregatedMetrics> findMostRecentByMetricNameAndTimeFrame(
            @Param("metricName") String metricName,
            @Param("timeFrame") String timeFrame);
    
    /**
     * Delete metrics older than the specified date.
     */
    void deleteByCalculatedAtBefore(Instant date);
}