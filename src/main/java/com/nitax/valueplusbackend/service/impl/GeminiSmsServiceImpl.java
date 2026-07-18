package com.nitax.valueplusbackend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nitax.valueplusbackend.dto.request.GeminiSendBulkSmsRequest;
import com.nitax.valueplusbackend.dto.response.GeminiQuerySmsStatusResponse;
import com.nitax.valueplusbackend.dto.response.GeminiRetrieveAccountBalanceResponse;
import com.nitax.valueplusbackend.dto.response.GeminiSendBulkSmsResponse;
import com.nitax.valueplusbackend.dto.response.PisiGetDeliveryStatusResponse;
import com.nitax.valueplusbackend.service.GeminiSmsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GeminiSmsServiceImpl implements GeminiSmsService {
    @Value("${app.gemini-receipt-url}")
    private String receiptUrl;
    @Override
    public GeminiSendBulkSmsResponse sendBulkSms(GeminiSendBulkSmsRequest sendBulkSmsRequest) {
        try {
            // Ensure all destination values are Strings
            List<String> destinations = sendBulkSmsRequest.getDestinations().stream()
                    .map(String::valueOf)
                    .collect(Collectors.toList());
            sendBulkSmsRequest.setDestinations(destinations);
            sendBulkSmsRequest.setReceiptURL(receiptUrl);

            // Convert to JSON string
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(sendBulkSmsRequest);
            System.out.println("Sending JSON: " + json);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set("Authorization", "Basic RWljeExhYVREVkpvWENwOmozOFF4SUlO");

            // Wrap JSON and headers into HttpEntity
            HttpEntity<String> requestEntity = new HttpEntity<>(json, headers);

            // Send the POST request
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                    "https://smsportal.geminigroupng.com/v1/sms/multi/",
                    requestEntity,
                    String.class
            );

            // Parse and return the response
            return objectMapper.readValue(responseEntity.getBody(), GeminiSendBulkSmsResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send bulk SMS", e);
        }
    }


    @Override
    public GeminiQuerySmsStatusResponse querySmsStatus(String messageId) {
        try {
            // Create RestTemplate instance
            RestTemplate restTemplate = new RestTemplate();

            // Set the headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic RWljeExhYVREVkpvWENwOmozOFF4SUlO");

            // Create the request entity with headers
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            // Build the URL with the query parameter
            String url = "https://smsportal.geminigroupng.com/v1/sms/query/?messageID=" + messageId;

            // Send the GET request
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            // Return the response body
//            return responseEntity.getBody();
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(responseEntity.getBody(), GeminiQuerySmsStatusResponse.class);
//
        } catch (Exception e) {
            // Handle exceptions (e.g., logging or rethrowing)
            throw new RuntimeException("Failed to query SMS status", e);
        }
    }

    @Override
    public GeminiRetrieveAccountBalanceResponse retrieveAccountBalance() {
        try {
            // Create RestTemplate instance
            RestTemplate restTemplate = new RestTemplate();

            // Set the headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic RWljeExhYVREVkpvWENwOmozOFF4SUlO");

            // Create the request entity with headers
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            // Define the URL
            String url = "https://smsportal.geminigroupng.com/v1/account/balance/";

            // Send the GET request
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            // Return the response body
//            return responseEntity.getBody();
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(responseEntity.getBody(), GeminiRetrieveAccountBalanceResponse.class);
//
        } catch (Exception e) {
            // Handle exceptions (e.g., logging or rethrowing)
            throw new RuntimeException("Failed to retrieve account balance", e);
        }
    }
}
