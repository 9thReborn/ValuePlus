package com.nitax.valueplusbackend.domain;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Entity
@Table(name = "publishers")
@Data
public class Publisher extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "pub_url", nullable = false)
    private String pubUrl;

    @Column(name = "pub_id", nullable = false)
    private String pubId;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @Column(name = "website")
    private String website;

    private String identifier;

    @Column(name = "postback_url")
    private String postbackUrl;

    @Column(name = "total_due_amount")
    private Double totalDueAmount;

    @Column(name = "click_id_parameter")
    private String clickIdParameter;

    @Column(name = "source_id_parameter")
    private String sourceIdParameter;

    private String businessName;

    private String skypeId;

    @Column(name = "isEmailVerified")
    private Boolean isEmailVerified = false;

    @Column(name = "isAccountActive")
    private Boolean isAccountActive = true;

    @Column(name = "unverified_date", nullable = true)
    @Setter(AccessLevel.NONE)
    private Instant unverifiedDate = Instant.now();


    @Column(name = "rejected_date", nullable = true)
    @Setter(AccessLevel.NONE)
    private Instant rejectedDate;

    @Column(name = "deleted", columnDefinition = "boolean default false")
    private boolean deleted = Boolean.FALSE;


    @Column(name = "api_key", unique = true)
    private String apiKey;

    @Enumerated(EnumType.STRING)
    private PublisherStatus status;

    @OneToOne
    @JoinColumn(name = "role_id", referencedColumnName = "id", nullable = false)
    private AppRoles role;

    public Publisher() {
    }

    @PrePersist
    public void prepersist() {
        if (this.status == null) {

            this.setStatus(PublisherStatus.APPROVED.AWAIT_APPROVAL.UNVERIFIED.REJECTED.UNVERIFIED.ACTIVE.INACTIVE.SUSPENDED);
        }
    }
}
