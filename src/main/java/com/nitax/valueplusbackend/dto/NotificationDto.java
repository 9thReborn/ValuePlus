package com.nitax.valueplusbackend.dto;

import com.nitax.valueplusbackend.domain.Notification;
import com.univocity.parsers.annotations.Parsed;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class NotificationDto {

  @Parsed private Instant user_joined_date;
  @Parsed private Notification.NotificationStatus status;
  @Parsed private String click_id;
  @Parsed private String source_id;
  @Parsed private Long duration;
  @Parsed private String campaign_name;
  @Parsed private Double cpa;
  @Parsed private Double revenue;
  @Parsed private String churnGrade;
  @Parsed private long churnPercent;

  public NotificationDto(
      Instant user_joined_date,
      Notification.NotificationStatus status,
      String click_id,
      String source_id,
      Long duration,
      String campaign_name,
      Double cpa,
      Double revenue) {
    this.user_joined_date = user_joined_date;
    this.status = status;
    this.click_id = click_id;
    this.source_id = source_id;
    this.duration = duration;
    this.campaign_name = campaign_name;
    this.cpa = cpa;
    this.revenue = revenue;
  }
}
