package com.nitax.valueplusbackend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nitax.valueplusbackend.dto.request.PisiCalculateCostRequest;
import com.nitax.valueplusbackend.dto.request.PisiSendBulkSmsRequest;
import com.nitax.valueplusbackend.dto.response.*;
import com.nitax.valueplusbackend.service.PisiBulkSmsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class PisiBulkSmsServiceImpl implements PisiBulkSmsService {
    @Value("${pisi.vaspid}")
    private String vaspid;

    @Value("${pisi.password}")
    private String password;
    @Override
    public PisiAuthenticationResponse authenticate() {
        try {
            // Create RestTemplate instance
            RestTemplate restTemplate = new RestTemplate();

            // Set the headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create the request body
            PisitAuthenticationRequest request = new PisitAuthenticationRequest();
            request.setPassword(password);
            request.setVaspid(vaspid);

            // Create the request entity
            HttpEntity<PisitAuthenticationRequest> requestEntity = new HttpEntity<>(request, headers);

            // Define the URL
            String url = "https://api.pisimobile.com/bulksms/v1/Authentication/login";

            // Send the POST request and get the response as a string
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            // Deserialize the response body
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(responseEntity.getBody(), PisiAuthenticationResponse.class);

        } catch (Exception e) {
            // Handle exceptions (e.g., logging or rethrowing)
            throw new RuntimeException("Failed to authenticate", e);
        }
    }

    @Override
    public PisiSendSmsResponse sendSms(PisiSendBulkSmsRequest request) {
        try {
            // Create RestTemplate instance
            RestTemplate restTemplate = new RestTemplate();
            PisiAuthenticationResponse authResponse = authenticate();
            // Set the headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("vaspid", vaspid);
            headers.set("pisi-authorization-token", "Bearer "+authResponse.getPisiAuthorizationToken());
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // Create the request body
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("Message", request.getMessage());
            body.add("Recipients", request.getRecipients());
            body.add("senderId", request.getSenderId());

            // Create the request entity
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

            // Define the URL
            String url = "https://api.pisimobile.com/bulksms/v1/Sms/sendbulksms";

            // Send the POST request
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            // Return the response body

            return new PisiSendSmsResponse(responseEntity.getBody());
        } catch (Exception e) {
            // Handle exceptions (e.g., logging or rethrowing)
            throw new RuntimeException("Failed to send SMS", e);
        }
    }

    @Override
    public PisiCalculateCostResponse calculateCost(PisiCalculateCostRequest request) {
        try {
            // Create RestTemplate instance
            RestTemplate restTemplate = new RestTemplate();
            PisiAuthenticationResponse authResponse = authenticate();

            // Set the headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("vaspid", vaspid);
            headers.set("pisi-authorization-token", "Bearer " + authResponse.getPisiAuthorizationToken());
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // Create the request body
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("Message", request.getMessage());
            body.add("Recipients", request.getRecipients());
            body.add("file", request.getFile());

            // Create the request entity
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Define the URL
            String url = "https://api.pisimobile.com/bulksms/v1/Sms/calculatecost";

            // Send the POST request
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            // Return the response body
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(responseEntity.getBody(), PisiCalculateCostResponse.class);
//            return responseEntity.getBody();
        } catch (Exception e) {
            // Handle exceptions (e.g., logging or rethrowing)
            throw new RuntimeException("Failed to calculate cost", e);
        }
    }

    @Override
    public PisiGetDeliveryStatusResponse getDIR(String transactionId) {
        try {
            // Create RestTemplate instance
            RestTemplate restTemplate = new RestTemplate();
            PisiAuthenticationResponse authResponse = authenticate();

            // Set the headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("vaspid", vaspid);
            headers.set("pisi-authorization-token", "Bearer " + authResponse.getPisiAuthorizationToken());
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // Create the request body
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("transactionID", transactionId);

            // Create the request entity
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

            // Define the URL
            String url = "https://api.pisimobile.com/bulksms/v1/Sms/getdlr";

            // Send the POST request
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            // Return the response body
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(responseEntity.getBody(), PisiGetDeliveryStatusResponse.class);
//
//            return responseEntity.getBody();
        } catch (Exception e) {
            // Handle exceptions (e.g., logging or rethrowing)
            throw new RuntimeException("Failed to get delivery status", e);
        }
    }
}
