package com.kousen.cert.analytics.model;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Entity for storing pre-calculated aggregations of analytics data.
 * This table is used to improve dashboard performance by avoiding
 * expensive calculations on each request.
 */
@Entity
@Table(name = "aggregated_metrics")
public class AggregatedMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String metricName;

    @Column(nullable = false)
    private String metricKey;

    @Column(nullable = false)
    private String timeFrame;

    @Column(nullable = false)
    private Double metricValue;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(nullable = false)
    private Instant calculatedAt;

    @PrePersist
    protected void onCreate() {
        if (calculatedAt == null) {
            calculatedAt = Instant.now();
        }
    }

    public AggregatedMetrics() {}

    public AggregatedMetrics(String metricName, String metricKey, String timeFrame, Double metricValue, Instant timestamp) {
        this.metricName = metricName;
        this.metricKey = metricKey;
        this.timeFrame = timeFrame;
        this.metricValue = metricValue;
        this.timestamp = timestamp;
        this.calculatedAt = Instant.now();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public String getMetricKey() {
        return metricKey;
    }

    public void setMetricKey(String metricKey) {
        this.metricKey = metricKey;
    }

    public String getTimeFrame() {
        return timeFrame;
    }

    public void setTimeFrame(String timeFrame) {
        this.timeFrame = timeFrame;
    }

    public Double getMetricValue() {
        return metricValue;
    }

    public void setMetricValue(Double metricValue) {
        this.metricValue = metricValue;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Instant getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(Instant calculatedAt) {
        this.calculatedAt = calculatedAt;
    }

    @Override
    public String toString() {
        return "AggregatedMetrics{" +
                "id=" + id +
                ", metricName='" + metricName + '\'' +
                ", metricKey='" + metricKey + '\'' +
                ", timeFrame='" + timeFrame + '\'' +
                ", metricValue=" + metricValue +
                ", timestamp=" + timestamp +
                ", calculatedAt=" + calculatedAt +
                '}';
    }
}