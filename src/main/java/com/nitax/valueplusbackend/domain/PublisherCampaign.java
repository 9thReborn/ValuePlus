package com.nitax.valueplusbackend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "publisher_campaign")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PublisherCampaign extends BaseEntity {
    @Column(name = "pub_camp_id", nullable = false)
    private String pubCampId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "publisher_id", nullable = false)
    private Publisher publisher;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @Column(name = "publisher_cpa", nullable = false)
    private Double publisherCpa;

    @Column(name = "publisher_campaign_link", nullable = false)
    private String publisherCampaignLink;

    @Column(name = "is_campaign_active", nullable = false, columnDefinition = "boolean default true")
    private boolean active;

    @Column(name = "is_campaign_paused", nullable = false, columnDefinition = "boolean default false")
    private boolean paused;

    @Column(name = "is_campaign_deleted", nullable = false, columnDefinition = "boolean default false")
    private boolean deleted;

    private String pauseReason;
    private String deleteReason;
    private String activationReason;

}
