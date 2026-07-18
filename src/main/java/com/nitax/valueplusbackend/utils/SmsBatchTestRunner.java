package com.nitax.valueplusbackend.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.nitax.valueplusbackend.dto.response.GeminiSendBulkSmsResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

// --- Placeholder POJOs for demonstration ---
// --- Main Test Runner Class ---
public class SmsBatchTestRunner {

    private static final Logger LOGGER = Logger.getLogger(SmsBatchTestRunner.class.getName());
    private static final int BATCH_SIZE = 1000;
    // The number of batches to run.
    private static final int NUMBER_OF_BATCHES = 20;
    // Placeholder URL for the receipt endpoint.
    private static final String receiptUrl = "http://example.com/receipt";


    private GeminiSendBulkSmsResponse sendBulkSms(GeminiSendBulkSmsRequest sendBulkSmsRequest) {
        try {
            // Ensure all destination values are Strings
            List<String> destinations = sendBulkSmsRequest.getDestinations().stream()
                    .map(String::valueOf)
                    .collect(Collectors.toList());
            sendBulkSmsRequest.setSource("TestSender");
            sendBulkSmsRequest.setContent("Test message for batch delivery.");
            sendBulkSmsRequest.setDestinations(destinations);
            sendBulkSmsRequest.setReceiptURL(receiptUrl);

            // Convert the request object to a JSON string.
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(sendBulkSmsRequest);
            LOGGER.info("Sending JSON: " + json);

            // Set up the HTTP headers for the request.
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set("Authorization", "Basic RWljeExhYVREVkpvWENwOmozOFF4SUlO");

            // Wrap the JSON and headers into an HttpEntity.
            HttpEntity<String> requestEntity = new HttpEntity<>(json, headers);

            // Send the POST request using RestTemplate.
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                    "https://smsportal.geminigroupng.com/v1/sms/multi/",
                    requestEntity,
                    String.class
            );

            // Parse the JSON response back into a response object.
            return objectMapper.readValue(responseEntity.getBody(), GeminiSendBulkSmsResponse.class);
        } catch (Exception e) {
            // Handle any exceptions during the process.
            throw new RuntimeException("Failed to send bulk SMS", e);
        }
    }

    public void runSuccessiveBatchTest() {
        LOGGER.info("--------------------------------------------------");
        LOGGER.info("Starting successive batch test for " + NUMBER_OF_BATCHES + " batches...");
        LOGGER.info("--------------------------------------------------");

        // Loop through and send each batch.
        for (int i = 1; i <= NUMBER_OF_BATCHES; i++) {
            LOGGER.info("Sending batch " + i + " of " + NUMBER_OF_BATCHES + "...");
            List<String> phoneNumbers = new ArrayList<>();
            for (int j = 0; j < BATCH_SIZE; j++) {
                // Generate a new set of dummy phone numbers for each batch.
                phoneNumbers.add("23480" + String.format("%08d", (i * BATCH_SIZE) + j));
            }

            GeminiSendBulkSmsRequest request = new GeminiSendBulkSmsRequest();
            request.setDestinations(phoneNumbers);
            request.setContent("Test message for batch delivery.");
            request.setSource("TestSender");

            // The code proceeds to the next batch as soon as this method returns.
            GeminiSendBulkSmsResponse response = sendBulkSms(request);

            LOGGER.info("Batch " + i + " sent successfully.");
            LOGGER.info("Response for batch " + i + ": Transaction ID - " + response.getTransactionID() + ", Message IDs - " + response.getMessageID());
        }

        LOGGER.info("--------------------------------------------------");
        LOGGER.info("Successive batch test completed!");
        LOGGER.info("--------------------------------------------------");
    }

    // Main method to run the test from a command line.
    public static void main(String[] args) {
        SmsBatchTestRunner runner = new SmsBatchTestRunner();
        runner.runSuccessiveBatchTest();
    }

    // Placeholder POJOs moved to the top of the file for clarity.
}
