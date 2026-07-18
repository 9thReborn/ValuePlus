package com.nitax.valueplusbackend.service;

import com.nitax.valueplusbackend.dto.SecureDNotificationDto;
import com.nitax.valueplusbackend.dto.request.ClickTrackingDto;
import org.springframework.scheduling.annotation.Async;

public interface ClicksConversionsService {
  String getRedirectUrl(String campaignId, ClickTrackingDto clickTrackingDto);

  void handleAdvertiserPostbackByGET(
      String advertiserId,
      String campaignId,
      String trxId,
      String sourceId,
      String msisdn,
      String success,
      String activation);

  void handleSecureDWebhook(SecureDNotificationDto secureDNotificationDto);

  void resendPendingConversions();

  @Async
  void handleAdvertiserPostbackByGET2(
      String trxId, String sourceId, String msisdn, String success, String number);
}
