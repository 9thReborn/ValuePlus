package com.nitax.valueplusbackend.service.impl;

import com.nitax.valueplusbackend.domain.SMSLog;
import com.nitax.valueplusbackend.dto.PISISendSMSResponse;
import com.nitax.valueplusbackend.dto.request.SendSMSByList;
import com.nitax.valueplusbackend.repository.SMSLogRepository;
import com.nitax.valueplusbackend.service.SMSService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class PISISMSServiceImpl implements SMSService {

  private final RestService restService;
  private final SMSLogRepository smsLogRepository;

  @Value("${pisi.auth-endpoint}")
  private String AUTH_ENDPOINT;

  @Value("${pisi.send-sms-by-list-endpoint}")
  private String SEND_SMS_BY_LIST_ENDPOINT;

  @Value("${pisi.send-single-sms-endpoint}")
  private String sendSingleSMSEndpoint;

  @Value("${pisi.auth-email}")
  private String AUTH_EMAIL;

  @Value("${pisi.auth-password}")
  private String AUTH_PASSWORD;

  public SMSLog sendBulkSmsFromList(SendSMSByList sendSMSByList) {

    // Authenticate user (assuming this remains the same)
    String token = authenticateUser();

    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);
    headers.setContentType(MediaType.APPLICATION_JSON);

    String urlWithParam =
        UriComponentsBuilder.fromUriString(SEND_SMS_BY_LIST_ENDPOINT)
            .queryParam("message", sendSMSByList.getMessage())
            .toUriString();

    HttpEntity<SendSMSByList> requestEntity = new HttpEntity<>(sendSMSByList, headers);

    PISISendSMSResponse sendSMSRequest =
        restService.sendPostRequest(
            urlWithParam, requestEntity, PISISendSMSResponse.class, headers);

    SMSLog smsLog = new SMSLog();
    smsLog.setSmsText(sendSMSByList.getMessage());
    //    smsLog.setTransactionId(sendSMSRequest.getTrxid());
    smsLog.setStatus(SMSLog.Status.PENDING);

    return smsLogRepository.save(smsLog);
  }

  @Override
  public SMSLog logSMS(SMSLog log) {
    return null;
  }

  @Override
  public PISISendSMSResponse sendSMS(String number, String text) {
    String token = authenticateUser();

    HttpHeaders headers = new HttpHeaders();
    headers.set("PISI-AUTHORIZATION-TOKEN", "Bearer " + token);
    headers.set("VASPID", "9");

    MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
    requestBody.add("message", text);
    requestBody.add("msisdn", number);
    requestBody.add("pisisid", "20");
    requestBody.add("trxid", LocalDateTime.now().toString());

    HttpEntity<MultiValueMap<String, String>> requestEntity =
        new HttpEntity<>(requestBody, headers);

    PISISendSMSResponse sendSMSResponse =
        restService.sendPostRequest(
            sendSingleSMSEndpoint, requestEntity, PISISendSMSResponse.class, headers);

    return sendSMSResponse;
  }

  public String authenticateUser() {
    return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJwYXlsb2FkIjp7InZhc3AiOiI5In0sImlhdCI6MTcyODY3NTA0MiwiZXhwIjoxNzQ0MjI3MDQyfQ.3uj3EIKWSBOyLdL-WDHTAI5l3bWAebqUY9KrbUBdH8k";

    // uncomment this block if you want to authenticate the user, auth token expires in 180 days and
    // this was generated 14th November 2024
    //    String urlWithParams =
    //        UriComponentsBuilder.fromUriString(AUTH_ENDPOINT)
    //            .queryParam("emailAddress", AUTH_EMAIL)
    //            .queryParam("password", AUTH_PASSWORD)
    //            .toUriString();
    //
    //    HttpHeaders headers = new HttpHeaders();
    //    headers.setContentType(MediaType.APPLICATION_JSON);
    //
    //    AuthenticationRequest authenticationRequest = new AuthenticationRequest();
    //    authenticationRequest.setEmailAddress(AUTH_EMAIL);
    //    authenticationRequest.setPassword(AUTH_PASSWORD);
    //    HttpEntity<AuthenticationRequest> requestEntity =
    //        new HttpEntity<>(authenticationRequest, headers);
    //
    //    AuthenticationResponse response =
    //        restService.sendPostRequest(
    //            urlWithParams, requestEntity, AuthenticationResponse.class, headers);
    //    return response.getToken();
  }
}
