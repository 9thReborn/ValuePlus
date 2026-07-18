package com.nitax.valueplusbackend.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "bulk_sms_audit_logs")
@Data
public class BulkSmsAuditLog extends BaseEntity{
    private String bulkSmsCampaignId;
    private String advertiserId;
    private BigDecimal totalAmountSpent;
    private long totalSmsTarget;
    private String publisherAPi;
}
