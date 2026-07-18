package com.nitax.valueplusbackend.domain;

import com.nitax.valueplusbackend.utils.enums.CampaignTypes;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "campaigns")
@Data
@NoArgsConstructor
public class Campaign extends BaseEntity {

  private String campaignId;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "advertiser_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Advertiser advertiser;

  @Column(name = "name")
  private String name;

  @Column(name = "url")
  private String url;

  @Column(name = "acquisition", columnDefinition = "bigint")
  private Long acquisition;

  @Column(name = "reach", columnDefinition = "bigint")
  private Long reach;

  @Column(name = "budget", columnDefinition = "bigint")
  private Long budget;

  @Column(name = "daily_cap", columnDefinition = "bigint")
  private Long dailyCap;

  @Column(name = "objective")
  private String objective;

  @Column(name = "age_range")
  private String ageRange;

  @Column(name = "gender")
  private String gender;

  @Column(name = "interest")
  private String interest;

  @Column(name = "country")
  private String country;

  @Column(name = "image")
  private String image;

  @Column(name = "start_date")
  private Instant startDate;

  @Column(name = "end_date")
  private Instant endDate;

  @Column(name = "cost_per_user")
  private Double costPerUser;

  @Column(name = "cpa_cost_per_user")
  private Double cpaCostPerUser;

  @Column(name = "campaign_cost")
  private Double campaignCost;

  @Column(name = "cpa_campaign_cost")
  private Double cpaCampaignCost;

  @Column(name = "last_50_percent_reminder_at")
  private Instant last50PercentReminderAt;

  @Column(name = "last_75_percent_reminder_at")
  private Instant last75PercentReminderAt;

  @Column(name = "last_90_percent_reminder_at")
  private Instant last90PercentReminderAt;

  @Column(name = "last_100_percent_reminder_at")
  private Instant last100PercentReminderAt;

  @Column(name = "status", columnDefinition = "varchar(20) default 'INACTIVE'")
  private String status;

  @Column(name = "pause_reason")
  private String pauseReason;

  @Column(name = "is_disabled", columnDefinition = "boolean default false")
  private Boolean isDisabled;

  @Column(name = "disable_reason")
  private String disableReason;

  @Column(name = "campaign_type", columnDefinition = "varchar(10) default 'CPA'")
  @Enumerated(EnumType.STRING)
  private CampaignTypes type;

  @Column(name = "is_deleted", columnDefinition = "boolean default false")
  private boolean isDeleted;

  private String deleteReason;

  @Column(name = "is_approved", columnDefinition = "boolean default false")
  private boolean isApproved;

  @Column(name = "rejection_reason")
  private String rejectionReason;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  private Instant disabledAt;

  @Column(name = "carrier_connection")
  private String carrierConnection;

  @Column(name = "traffic_quality")
  private String trafficQuality;

  @Column(name = "click_flow")
  private String clickFlow;

  @Column(name = "restriction_Type")
  private String restrictionType;
  @Column(name = "payoutModel")
  private String payoutModel;
  @Column(name = "connectionType")
  private String connectionType;

  public Campaign(Campaign campaign) {
    this.setId(campaign.getId());
    this.setCreatedDate(campaign.getCreatedDate());
    this.setLastModifiedDate(campaign.getLastModifiedDate());
    this.campaignId = campaign.getCampaignId();
    this.advertiser = campaign.getAdvertiser();
    this.name = campaign.getName();
    this.url = campaign.getUrl();
    this.acquisition = campaign.getAcquisition();
    this.reach = campaign.getReach();
    this.budget = campaign.getBudget();
    this.dailyCap = campaign.getDailyCap();
    this.objective = campaign.getObjective();
    this.ageRange = campaign.getAgeRange();
    this.gender = campaign.getGender();
    this.interest = campaign.getInterest();
    this.country = campaign.getCountry();
    this.image = campaign.getImage();
    this.startDate = campaign.getStartDate();
    this.endDate = campaign.getEndDate();
    this.costPerUser = campaign.getCostPerUser();
    this.cpaCostPerUser = campaign.getCpaCostPerUser();
    this.campaignCost = campaign.getCampaignCost();
    this.cpaCampaignCost = campaign.getCpaCampaignCost();
    this.last50PercentReminderAt = campaign.getLast50PercentReminderAt();
    this.last75PercentReminderAt = campaign.getLast75PercentReminderAt();
    this.last90PercentReminderAt = campaign.getLast90PercentReminderAt();
    this.last100PercentReminderAt = campaign.getLast100PercentReminderAt();
    this.status = campaign.getStatus();
    this.isDisabled = campaign.getIsDisabled();
    this.type = campaign.getType();
    this.isDeleted = campaign.isDeleted();
    this.isApproved = campaign.isApproved();
    this.deletedAt = campaign.getDeletedAt();
    this.carrierConnection = campaign.getCarrierConnection();
    this.trafficQuality = campaign.getTrafficQuality();
    this.pauseReason = campaign.getPauseReason();
    this.disableReason = campaign.getDisableReason();
    this.rejectionReason = campaign.getRejectionReason();
    this.deleteReason = campaign.getDeleteReason();
    this.disabledAt = campaign.getDisabledAt();
    this.clickFlow = campaign.getClickFlow();

    this.restrictionType = campaign.getRestrictionType();
    this.connectionType = campaign.getConnectionType();
    this.payoutModel = campaign.getPayoutModel();
  }
}
