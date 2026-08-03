package com.nitax.valueplusbackend.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nitax.valueplusbackend.domain.Subscriber;
import com.nitax.valueplusbackend.domain.SubscriberEvent;

@Repository
public interface SubscriberEventRepository extends JpaRepository<SubscriberEvent, Long> {

  List<SubscriberEvent> findBySubscriber(Subscriber subscriber);

  List<SubscriberEvent> findBySubscriberId(Long subscriberId);

  List<SubscriberEvent> findByEventType(SubscriberEvent.EventType eventType);

  List<SubscriberEvent> findByEventTimestampBetween(Instant start, Instant end);

  List<SubscriberEvent> findBySubscriberAndEventType(Subscriber subscriber, SubscriberEvent.EventType eventType);

  @Query("""
      SELECT e FROM SubscriberEvent e
      JOIN e.subscriber s
      WHERE (:advertiserId IS NULL OR s.advertiserId = :advertiserId)
        AND (:msisdn IS NULL OR s.msisdn = :msisdn)
        AND (:serviceId IS NULL OR s.serviceId = :serviceId)
        AND (:eventType IS NULL OR e.eventType = :eventType)
        AND (:startDate IS NULL OR e.eventTimestamp >= :startDate)
        AND (:endDate IS NULL OR e.eventTimestamp <= :endDate)
      ORDER BY e.eventTimestamp DESC
      """)
  Page<SubscriberEvent> findByFilters(
      @Param("advertiserId") String advertiserId,
      @Param("msisdn") String msisdn,
      @Param("serviceId") String serviceId,
      @Param("eventType") SubscriberEvent.EventType eventType,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate,
      Pageable pageable);

  List<SubscriberEvent> findBySubscriberIdOrderByEventTimestampAsc(Long subscriberId);

  boolean existsByIdempotencyKey(String idempotencyKey);

  Optional<SubscriberEvent> findByIdempotencyKey(String idempotencyKey);

  @Query("""
      SELECT e FROM SubscriberEvent e
      JOIN FETCH e.subscriber s
      WHERE e.eventType = 'DEACTIVATION'
        AND NOT EXISTS (
          SELECT 1 FROM Notification n
          WHERE n.msisdn = s.msisdn
            AND n.status = 'UNSUBSCRIBED'
        )
      """)
  Page<SubscriberEvent> findDeactivationsWithoutUnsubscribedNotification(Pageable pageable);

    @Query("""
      SELECT e FROM SubscriberEvent e
      JOIN e.subscriber s
      WHERE s.msisdn = :msisdn
        AND s.serviceId = :serviceId
        AND e.eventType = 'ACTIVATION'
        AND e.eventTimestamp >= :since
      ORDER BY e.eventTimestamp DESC
      """)
    List<SubscriberEvent> findRecentActivationsForMsisdnAndService(
            @Param("msisdn") String msisdn,
            @Param("serviceId") String serviceId,
            @Param("since") Instant since);

    @Query("""
      SELECT e FROM SubscriberEvent e
      JOIN e.subscriber s
      WHERE s.msisdn = :msisdn
        AND e.eventType = 'DEACTIVATION'
        AND e.eventTimestamp >= :since
      ORDER BY e.eventTimestamp DESC
      """)
    List<SubscriberEvent> findRecentDeactivationsForMsisdn(
            @Param("msisdn") String msisdn, @Param("since") Instant since);

    @Query("""
      SELECT e FROM SubscriberEvent e
      JOIN e.subscriber s
      WHERE s.msisdn = :msisdn
      ORDER BY e.eventTimestamp DESC
      """)
    List<SubscriberEvent> findAllByMsisdnOrderByEventTimestampDesc(@Param("msisdn") String msisdn);
}
