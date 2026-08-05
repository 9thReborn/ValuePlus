package com.nitax.valueplusbackend.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@Entity
@Table(
        name = "blocklist",
        indexes = {
                @Index(name = "idx_blocklist_msisdn_scope", columnList = "msisdn, scope"),
                @Index(name = "idx_blocklist_expires_at", columnList = "expires_at")
        })
@Data
@EqualsAndHashCode(callSuper = true)
public class Blocklist extends BaseEntity {

    @Column(name = "msisdn", nullable = false)
    private String msisdn;

    /** GLOBAL = blocks the MSISDN across every service. SERVICE = a single service_id. */
    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false)
    private Scope scope;

    /** Null for GLOBAL-scope blocks. */
    @Column(name = "service_id")
    private String serviceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_code", nullable = false)
    private ReasonCode reasonCode;

    /** Null means permanent (escalation to longer or permanent restriction.) */
    @Column(name = "expires_at")
    private Instant expiresAt;

    /** True once an admin manually lifts the block before its natural expiry. */
    @Column(name = "released", nullable = false)
    private boolean released = false;

    @Column(name = "released_at")
    private Instant releasedAt;

    @Column(name = "released_by")
    private String releasedBy;

    /** "SYSTEM" for automated blocks, or the admin's email for manual blocks. */
    @Column(name = "created_by", nullable = false)
    private String createdBy;

    public enum Scope {
        GLOBAL,
        SERVICE
    }
}
