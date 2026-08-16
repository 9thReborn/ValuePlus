package com.nitax.valueplusbackend.service.impl;

import com.nitax.valueplusbackend.domain.Notification;
import com.nitax.valueplusbackend.domain.PayoutClassification;
import com.nitax.valueplusbackend.domain.ReasonCode;
import com.nitax.valueplusbackend.dto.response.PayoutReportDTO;
import com.nitax.valueplusbackend.dto.response.PayoutReportReasonBreakdownDTO;
import com.nitax.valueplusbackend.repository.NotificationRepository;
import com.nitax.valueplusbackend.repository.PayoutClassificationRepository;
import com.nitax.valueplusbackend.service.PayoutClassificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayoutClassificationServiceImpl implements PayoutClassificationService {

    private final PayoutClassificationRepository payoutClassificationRepository;
    private final NotificationRepository notificationRepository;

    @Override
    public PayoutClassification recordClassification(
            Notification notification,
            PayoutClassification.Classification classification,
            ReasonCode reasonCode,
            String message) {
        PayoutClassification record = new PayoutClassification();
        record.setNotification(notification);
        record.setMsisdn(notification.getMsisdn());
        record.setCampaignId(notification.getCampaignId());
        record.setPublisherId(notification.getPublisherId());
        record.setClassification(classification);
        record.setReasonCode(reasonCode == null ? ReasonCode.NONE : reasonCode);
        record.setMessage(message);
        record.setClassifiedAt(Instant.now());

        PayoutClassification saved = payoutClassificationRepository.save(record);
        log.info(
                "Payout classification recorded: notificationId={}, msisdn={}, classification={}, reasonCode={}",
                notification.getId(),
                record.getMsisdn(),
                classification,
                record.getReasonCode());
        return saved;
    }

    @Override
    public Page<PayoutClassification> findByMsisdn(String msisdn, Pageable pageable) {
        return payoutClassificationRepository.findByMsisdnOrderByClassifiedAtDesc(msisdn, pageable);
    }

    @Override
    public Page<PayoutClassification> findByNotificationId(Long notificationId, Pageable pageable) {
        return payoutClassificationRepository.findByNotificationIdOrderByClassifiedAtDesc(
                notificationId, pageable);
    }

    @Override
    public Page<PayoutClassification> findByClassification(
            PayoutClassification.Classification classification, Pageable pageable) {
        return payoutClassificationRepository.findByClassificationOrderByClassifiedAtDesc(
                classification, pageable);
    }

    @Override
    public PayoutReportDTO generateReport(Instant startOfDay, Instant endOfDay) {
        long totalConversions =
                notificationRepository.countConversionsWithDateRange(startOfDay, endOfDay);

        List<PayoutReportReasonBreakdownDTO> invalidBreakdown =
                payoutClassificationRepository.findReasonBreakdown(
                        PayoutClassification.Classification.INVALID_FOR_PAYOUT, startOfDay, endOfDay);

        long invalidCount = invalidBreakdown.stream().mapToLong(PayoutReportReasonBreakdownDTO::getCount).sum();
        double invalidValueRemoved =
                invalidBreakdown.stream().mapToDouble(PayoutReportReasonBreakdownDTO::getValueRemoved).sum();

        // payableCount is inferred (totalConversions - invalidCount), not counted directly — see
        // PayoutReportDTO's javadoc for why: no writer proactively records PAYABLE rows yet.
        long payableCount = Math.max(0, totalConversions - invalidCount);

        return new PayoutReportDTO(
                startOfDay, endOfDay, totalConversions, invalidCount, payableCount, invalidValueRemoved,
                invalidBreakdown);
    }

    @Override
    public List<PayoutClassification> findInvalidRecordsForExport(
            Instant startOfDay, Instant endOfDay) {
        return payoutClassificationRepository.findByClassificationAndClassifiedAtBetweenOrderByClassifiedAtDesc(
                PayoutClassification.Classification.INVALID_FOR_PAYOUT, startOfDay, endOfDay);
    }
}
