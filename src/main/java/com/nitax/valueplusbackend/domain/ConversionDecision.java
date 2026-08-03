package com.nitax.valueplusbackend.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@Entity
@Table(
        name = "conversion_decisions",
        indexes = {
                @Index(name = "idx_decision_msisdn_service_time", columnList = "msisdn, service_id, decision_time"),
                @Index(name = "idx_decision_publisher_time", columnList = "publisher_id, decision_time"),
                @Index(name = "idx_decision_event", columnList = "subscriber_event_id"),
                @Index(name = "idx_decision_type", columnList = "decision")
        })
@Data
@EqualsAndHashCode(callSuper = true)
public class ConversionDecision extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_event_id", nullable = false)
    private SubscriberEvent subscriberEvent;

    @Column(name = "msisdn", nullable = false)
    private String msisdn;

    @Column(name = "service_id")
    private String serviceId;

    @Column(name = "publisher_id")
    private String publisherId;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false)
    private ValidationDecision decision;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_code", nullable = false)
    private ReasonCode reasonCode = ReasonCode.NONE;

    @Column(name = "message")
    private String message;

    @Column(name = "decision_time", nullable = false)
    private Instant decisionTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "reviewer_status", nullable = false)
    private ReviewerStatus reviewerStatus = ReviewerStatus.NOT_REQUIRED;

    @Column(name = "is_replay", nullable = false)
    private boolean replay = false;

    public enum ReviewerStatus {
        NOT_REQUIRED,
        PENDING_REVIEW,
        REVIEWED
    }
}
