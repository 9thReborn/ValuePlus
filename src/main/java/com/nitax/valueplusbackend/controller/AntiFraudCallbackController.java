package com.nitax.valueplusbackend.controller;

import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nitax.valueplusbackend.dto.request.MFilterCallbackRequest;
import com.nitax.valueplusbackend.dto.request.ScienlabsCallbackRequest;
import com.nitax.valueplusbackend.dto.request.SecureDCallbackRequest;
import com.nitax.valueplusbackend.dto.request.SubscriptionWebhookRequest;
import com.nitax.valueplusbackend.service.SubscriberService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/callbacks/antifraud")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin
public class AntiFraudCallbackController {

  private static final Set<String> ACTIVE_OPERATION_IDS =
      Set.of(
          "SN", "SR", "SAA", "PN", "YR", "GR", "RR", "ACE", "ES", "SP", "PP", "RP", "PCE", "YG",
          "GG", "GCE");

  private final SubscriberService subscriberService;
  private final ObjectMapper objectMapper;

  @GetMapping("/mfilter/success")
  public ResponseEntity<String> mfilterSuccess(
      @RequestParam String serviceId, MFilterCallbackRequest req) {

    String trxId =
        (req.getTrxId() != null && !req.getTrxId().isEmpty()) ? req.getTrxId() : req.getClickId();
    log.info(        "MFilter success callback: serviceId={}, trxId={}, msisdn={}",
        serviceId,
        trxId,
        req.getMsisdn());

    SubscriptionWebhookRequest request = new SubscriptionWebhookRequest();
    request.setServiceId(serviceId);
    request.setTrxId(trxId);
    request.setMsisdn(req.getMsisdn() != null && !req.getMsisdn().isEmpty() ? req.getMsisdn() : "default");
    request.setEventType("SUBSCRIPTION");
    request.setRenewalFlag("true".equalsIgnoreCase(req.getAutoRenew()));

    subscriberService.processSubscriptionWebhook(request, toJson(request));
    return ResponseEntity.ok("OK");
  }

  @GetMapping("/mfilter/failed")
  public ResponseEntity<String> mfilterFailed(
      @RequestParam String serviceId, MFilterCallbackRequest req) {

    String trxId =
        (req.getTrxId() != null && !req.getTrxId().isEmpty()) ? req.getTrxId() : req.getClickId();
    log.info("MFilter failed callback: serviceId={}, trxId={}", serviceId, trxId);

    return ResponseEntity.ok("OK");
  }

  @PostMapping("/secured")
  public ResponseEntity<Void> secureDCallback(
      @RequestParam String serviceId, @RequestBody SecureDCallbackRequest req) {

    log.info(
        "Secure-D callback: serviceId={}, trxId={}, activation={}, msisdn={}",
        serviceId,
        req.getTrxId(),
        req.getActivation(),
        req.getMsisdn());

    if (!"1".equals(req.getActivation())) {
      log.info("Secure-D: ignoring non-activation event, activation={}", req.getActivation());
      return ResponseEntity.ok().build();
    }

    SubscriptionWebhookRequest request = new SubscriptionWebhookRequest();
    request.setServiceId(serviceId);
    request.setTrxId(req.getTrxId());
    request.setMsisdn(
        req.getMsisdn() != null && !req.getMsisdn().isEmpty() ? req.getMsisdn() : "default");
    request.setEventType("SUBSCRIPTION");
    //    request.setRenewalFlag("Y".equalsIgnoreCase(req.getRenFlag()));

    subscriberService.processSubscriptionWebhook(request, toJson(request));
    return ResponseEntity.ok().build();
  }

  @PostMapping("/evina")
  public ResponseEntity<Void> evinaCallback(
      @RequestParam String serviceId, @RequestBody SecureDCallbackRequest req) {

    log.info(
        "Evina callback: serviceId={}, trxId={}, activation={}, msisdn={}",
        serviceId,
        req.getTrxId(),
        req.getActivation(),
        req.getMsisdn());

    if (!"1".equals(req.getActivation())) {
      log.info("Evina: ignoring non-activation event, activation={}", req.getActivation());
      return ResponseEntity.ok().build();
    }

    SubscriptionWebhookRequest request = new SubscriptionWebhookRequest();
    request.setServiceId(serviceId);
    request.setTrxId(req.getTrxId());
    request.setMsisdn(
        req.getMsisdn() != null && !req.getMsisdn().isEmpty() ? req.getMsisdn() : "default");
    request.setEventType("SUBSCRIPTION");
    request.setRenewalFlag("Y".equalsIgnoreCase(req.getRenFlag()));

    subscriberService.processSubscriptionWebhook(request, toJson(request));
    return ResponseEntity.ok().build();
  }

  @PostMapping("/scienlabs")
  public ResponseEntity<Void> scienlabsCallback(
      @RequestParam String serviceId, @RequestBody ScienlabsCallbackRequest req) {

    log.info(
        "Scienlabs callback: serviceId={}, sequenceNo={}, operationId={}, callingParty={}",
        serviceId,
        req.getSequenceNo(),
        req.getOperationId(),
        req.getCallingParty());

    if (!ACTIVE_OPERATION_IDS.contains(req.getOperationId())) {
      log.info("Scienlabs: ignoring operationId={}", req.getOperationId());
      return ResponseEntity.ok().build();
    }

    SubscriptionWebhookRequest request = new SubscriptionWebhookRequest();
    request.setServiceId(serviceId);
    request.setTrxId(req.getSequenceNo());
    request.setMsisdn(
        req.getCallingParty() != null && !req.getCallingParty().isEmpty()
            ? req.getCallingParty()
            : "default");
    request.setEventType("SUBSCRIPTION");
    request.setRenewalFlag("Y".equalsIgnoreCase(req.getRenFlag()));
    request.setAdditionalProperty("operationId", req.getOperationId());
    request.setAdditionalProperty("source", "scienlabs");

    subscriberService.processSubscriptionWebhook(request, toJson(request));
    return ResponseEntity.ok().build();
  }

  private String toJson(Object data) {
    try {
      return objectMapper.writeValueAsString(data);
    } catch (Exception e) {
      log.warn("Failed to serialize payload to JSON", e);
      return data.toString();
    }
  }
}
