package com.nitax.valueplusbackend.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bulk_sms_campaign_target")
@Data
@NoArgsConstructor
public class BulkSMSCampaignTarget extends  BaseEntity{
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name="bulkSmsCampaignId")
    private BulkSmsCampaign campaign;
    private String phoneNumber;
    private BulkSmsDeliveryStatus status;
    private String advertiserId;
    private String publisherApi;
}
