package com.nitax.valueplusbackend.service;

import com.nitax.valueplusbackend.domain.Notification;
import com.nitax.valueplusbackend.domain.PayoutClassification;
import com.nitax.valueplusbackend.domain.ReasonCode;
import com.nitax.valueplusbackend.dto.response.PayoutReportDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

public interface PayoutClassificationService {

    PayoutClassification recordClassification(
            Notification notification,
            PayoutClassification.Classification classification,
            ReasonCode reasonCode,
            String message);

    Page<PayoutClassification> findByMsisdn(String msisdn, Pageable pageable);

    Page<PayoutClassification> findByNotificationId(Long notificationId, Pageable pageable);

    Page<PayoutClassification> findByClassification(
            PayoutClassification.Classification classification, Pageable pageable);

    PayoutReportDTO generateReport(Instant startOfDay, Instant endOfDay);

    /** Detail rows behind the report, for the Excel export — reason-code traceability. */
    List<PayoutClassification> findInvalidRecordsForExport(Instant startOfDay, Instant endOfDay);
}