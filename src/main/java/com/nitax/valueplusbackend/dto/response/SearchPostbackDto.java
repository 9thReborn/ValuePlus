package com.nitax.valueplusbackend.dto.response;

import com.nitax.valueplusbackend.domain.Notification;
import java.time.Instant;
import lombok.Data;

@Data
public class SearchPostbackDto {
  String campaign;
  String msisdn;
  String clickId;
  String publisher;
  Notification.NotificationStatus status;
  Instant date;

  public SearchPostbackDto(
      String campaign,
      String msisdn,
      String clickId,
      String publisher,
      Notification.NotificationStatus status,
      Instant date) {
    this.campaign = campaign;
    this.msisdn = msisdn;
    this.clickId = clickId;
    this.publisher = publisher;
    this.status = status;
    this.date = date;
  }
}
