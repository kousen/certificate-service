package com.kousen.cert.analytics.repository;

import com.kousen.cert.analytics.model.CertificateEvent;
import com.kousen.cert.analytics.model.CertificateEvent.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface CertificateEventRepository extends JpaRepository<CertificateEvent, Long> {
    
    List<CertificateEvent> findByEventType(EventType eventType);
    
    List<CertificateEvent> findByCertificateId(String certificateId);
    
    List<CertificateEvent> findByTimestampBetween(Instant start, Instant end);
    
    List<CertificateEvent> findByEventTypeAndTimestampBetween(EventType eventType, Instant start, Instant end);
    
    @Query("SELECT COUNT(DISTINCT e.purchaserEmail) FROM CertificateEvent e WHERE e.purchaserEmail IS NOT NULL")
    long countUniquePurchasers();
    
    @Query("SELECT e.bookTitle, COUNT(e) as count FROM CertificateEvent e " +
           "WHERE e.eventType = :eventType AND e.bookTitle IS NOT NULL " +
           "GROUP BY e.bookTitle ORDER BY count DESC")
    List<Object[]> findBookPopularity(@Param("eventType") EventType eventType);
    
    @Query("SELECT DATE(e.timestamp) as date, COUNT(e) as count FROM CertificateEvent e " +
           "WHERE e.eventType = :eventType AND e.timestamp BETWEEN :start AND :end " +
           "GROUP BY DATE(e.timestamp) ORDER BY date")
    List<Object[]> findDailyEventCounts(@Param("eventType") EventType eventType, 
                                        @Param("start") Instant start, 
                                        @Param("end") Instant end);
    
    @Query("SELECT AVG(e.durationMs) FROM CertificateEvent e " +
           "WHERE e.eventType = :eventType AND e.durationMs IS NOT NULL")
    Double findAverageDuration(@Param("eventType") EventType eventType);
    
    List<CertificateEvent> findTop10ByOrderByTimestampDesc();
    
    @Query("SELECT COUNT(e) FROM CertificateEvent e " +
           "WHERE e.eventType = :eventType AND e.timestamp > :since")
    long countEventsSince(@Param("eventType") EventType eventType, @Param("since") Instant since);
}