package com.nitax.valueplusbackend.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@Entity
@Table(
        name = "payout_classifications",
        indexes = {
                @Index(name = "idx_payout_classification_msisdn_time", columnList = "msisdn, classified_at"),
                @Index(name = "idx_payout_classification_notification", columnList = "notification_id"),
                @Index(name = "idx_payout_classification_type", columnList = "classification")
        })
@Data
@EqualsAndHashCode(callSuper = true)
public class PayoutClassification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @Column(name = "msisdn", nullable = false)
    private String msisdn;

    @Column(name = "campaign_id")
    private String campaignId;

    @Column(name = "publisher_id")
    private String publisherId;

    @Enumerated(EnumType.STRING)
    @Column(name = "classification", nullable = false)
    private Classification classification;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_code", nullable = false)
    private ReasonCode reasonCode = ReasonCode.NONE;

    @Column(name = "message")
    private String message;

    @Column(name = "classified_at", nullable = false)
    private Instant classifiedAt;

    public enum Classification {
        PAYABLE,
        REVIEW_REQUIRED,
        INVALID_FOR_PAYOUT
    }
}

