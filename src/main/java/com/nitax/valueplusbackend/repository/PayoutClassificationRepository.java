package com.nitax.valueplusbackend.repository;

import com.nitax.valueplusbackend.domain.PayoutClassification;
import com.nitax.valueplusbackend.dto.response.PayoutReportReasonBreakdownDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface PayoutClassificationRepository extends JpaRepository<PayoutClassification, Long> {

    Page<PayoutClassification> findByMsisdnOrderByClassifiedAtDesc(String msisdn, Pageable pageable);

    Page<PayoutClassification> findByNotificationIdOrderByClassifiedAtDesc(
            Long notificationId, Pageable pageable);

    Page<PayoutClassification> findByClassificationOrderByClassifiedAtDesc(
            PayoutClassification.Classification classification, Pageable pageable);

    @Query(
            "SELECT new com.nitax.valueplusbackend.dto.response.PayoutReportReasonBreakdownDTO("
                    + "pc.reasonCode, COUNT(pc), SUM(pc.notification.cpaRevenue)) "
                    + "FROM PayoutClassification pc "
                    + "WHERE pc.classification = :classification "
                    + "  AND pc.classifiedAt BETWEEN :start AND :end "
                    + "GROUP BY pc.reasonCode")
    List<PayoutReportReasonBreakdownDTO> findReasonBreakdown(
            @Param("classification") PayoutClassification.Classification classification,
            @Param("start") Instant start,
            @Param("end") Instant end);

    List<PayoutClassification> findByClassificationAndClassifiedAtBetweenOrderByClassifiedAtDesc(
            PayoutClassification.Classification classification, Instant start, Instant end);
}
