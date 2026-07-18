package com.nitax.valueplusbackend.controller;

import com.nitax.valueplusbackend.dto.SecureDNotificationDto;
import com.nitax.valueplusbackend.dto.request.ClickTrackingDto;
import com.nitax.valueplusbackend.dto.response.ApiResponse;
import com.nitax.valueplusbackend.service.ClicksConversionsService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cc")
@Slf4j
public class ClicksConversionsController {
  private final ClicksConversionsService clicksConversionsService;

  @GetMapping("/redirect/{campaignId}")
  @ResponseStatus(HttpStatus.FOUND)
  public void redirect(
      @PathVariable String campaignId,
      ClickTrackingDto clickTrackingDto,
      HttpServletResponse response)
      throws IOException {
    String url = clicksConversionsService.getRedirectUrl(campaignId, clickTrackingDto);

    response.sendRedirect(url);
  }

  @GetMapping("/{advertiserId}/{campaignId}")
  public ResponseEntity<ApiResponse<String>> handleAdvertiserPostbackByGET(
      @PathVariable String advertiserId,
      @PathVariable String campaignId,
      @RequestParam String trxId,
      @RequestParam(required = false) String sourceId,
      @RequestParam(required = false) String msisdn) {

    clicksConversionsService.handleAdvertiserPostbackByGET(
        advertiserId, campaignId, trxId, sourceId, msisdn, "Success", "1");

    ApiResponse<String> apiResponse =
        ApiResponse.<String>builder().success(true).data("Postback handled successfully").build();

    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @PostMapping("/conv/{advertiserId}")
  public ResponseEntity<ApiResponse<String>> handleAdvertiserPostback(
      @RequestBody SecureDNotificationDto secureDNotificationDto,
      @PathVariable String advertiserId) {

    log.info("secureDNotificationDto: {}", secureDNotificationDto);

    secureDNotificationDto.setAdvertiserId(advertiserId);

    String rawTrxId = secureDNotificationDto.getTrxId();
    if (rawTrxId != null && rawTrxId.contains("_")) {
      secureDNotificationDto.setCampaignId(rawTrxId.split("_")[0]);
    }
    clicksConversionsService.handleSecureDWebhook(secureDNotificationDto);

    ApiResponse<String> apiResponse =
        ApiResponse.<String>builder().success(true).data("Postback handled successfully").build();

    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }
}
