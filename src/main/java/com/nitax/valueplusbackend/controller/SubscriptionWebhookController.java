package com.nitax.valueplusbackend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nitax.valueplusbackend.domain.Subscriber;
import com.nitax.valueplusbackend.dto.request.SubscriptionWebhookRequest;
import com.nitax.valueplusbackend.dto.response.ApiResponse;
import com.nitax.valueplusbackend.service.SubscriberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhooks/subscription")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin
public class SubscriptionWebhookController {

  private final SubscriberService subscriberService;
  private final ObjectMapper objectMapper;

  /**
   * Webhook endpoint to receive subscription events from advertisers. No authentication required.
   * Accepts msisdn, serviceId, eventType, renewalFlag, trxId (contains campaignId_publisherId_...),
   * timestamp. The raw JSON payload is stored in the event record. After recording subscription,
   * also registers conversion and sends to publisher.
   */
  @PostMapping
  public ResponseEntity<ApiResponse<String>> handleSubscriptionWebhook(
      @RequestBody String rawBody) {
    log.info("Received subscription webhook: {}", rawBody);

    try {
      SubscriptionWebhookRequest webhookRequest =
          objectMapper.readValue(rawBody, SubscriptionWebhookRequest.class);

      // Validate required fields
      if (webhookRequest.getMsisdn() == null || webhookRequest.getMsisdn().isEmpty()) {
        return new ResponseEntity<>(
            ApiResponse.<String>builder().success(false).data("msisdn is required").build(),
            HttpStatus.BAD_REQUEST);
      }

      if (webhookRequest.getServiceId() == null || webhookRequest.getServiceId().isEmpty()) {
        return new ResponseEntity<>(
            ApiResponse.<String>builder().success(false).data("serviceId is required").build(),
            HttpStatus.BAD_REQUEST);
      }

      if (webhookRequest.getEventType() == null || webhookRequest.getEventType().isEmpty()) {
        return new ResponseEntity<>(
            ApiResponse.<String>builder().success(false).data("eventType is required").build(),
            HttpStatus.BAD_REQUEST);
      }

      if (webhookRequest.getTrxId() == null || webhookRequest.getTrxId().isEmpty()) {
        return new ResponseEntity<>(
            ApiResponse.<String>builder().success(false).data("trxId is required").build(),
            HttpStatus.BAD_REQUEST);
      }

      Subscriber subscriber = subscriberService.processSubscriptionWebhook(webhookRequest, rawBody);

      ApiResponse<String> apiResponse =
          ApiResponse.<String>builder()
              .success(true)
              .data("Subscription event processed. Subscriber ID: " + subscriber.getId())
              .build();

      return new ResponseEntity<>(apiResponse, HttpStatus.OK);

    } catch (JsonProcessingException e) {
      log.error("Error parsing webhook request: {}", e.getMessage());
      return new ResponseEntity<>(
          ApiResponse.<String>builder().success(false).data("Invalid JSON payload").build(),
          HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      log.error("Error processing subscription webhook: ", e);
      return new ResponseEntity<>(
          ApiResponse.<String>builder()
              .success(false)
              .data("Error processing webhook: " + e.getMessage())
              .build(),
          HttpStatus.BAD_REQUEST);
    }
  }

  @PostMapping("/backfill-unsubscribed")
  public ResponseEntity<ApiResponse<String>> backfillMissingUnsubscribedNotifications() {
    log.info("Backfill endpoint triggered for missing UNSUBSCRIBED notifications");
    subscriberService.backfillMissingUnsubscribedNotifications();
    return new ResponseEntity<>(
        ApiResponse.<String>builder().success(true).data("Backfill started").build(), HttpStatus.ACCEPTED);
  }
}
