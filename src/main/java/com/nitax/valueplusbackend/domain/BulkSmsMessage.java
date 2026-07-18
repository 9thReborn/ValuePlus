package com.nitax.valueplusbackend.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "bulk_sms_message")
@Data
public class BulkSmsMessage extends  BaseEntity{
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id")
    private BulkSmsCampaign campaign;

    private String phoneNumber;

    private String messageId;

    @Enumerated(EnumType.STRING)
    private SmsDeliveryStatus status; // DELIVERED, FAILED, DND, PENDING

    private String error;

    private Date deliveryTimestamp;
}
