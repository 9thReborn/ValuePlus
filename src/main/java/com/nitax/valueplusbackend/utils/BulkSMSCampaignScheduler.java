package com.nitax.valueplusbackend.utils;

import com.nitax.valueplusbackend.domain.BulkSmsCampaign;
import com.nitax.valueplusbackend.domain.BulkSmsCampaignStatus;
import com.nitax.valueplusbackend.domain.BulkSmsMessage;
import com.nitax.valueplusbackend.domain.SmsDeliveryStatus;
import com.nitax.valueplusbackend.dto.request.GeminiSendBulkSmsRequest;
import com.nitax.valueplusbackend.dto.request.PisiSendBulkSmsRequest;
import com.nitax.valueplusbackend.dto.response.GeminiQuerySmsStatusResponse;
import com.nitax.valueplusbackend.dto.response.GeminiSendBulkSmsResponse;
import com.nitax.valueplusbackend.dto.response.PisiGetDeliveryStatusResponse;
import com.nitax.valueplusbackend.dto.response.PisiSendSmsResponse;
import com.nitax.valueplusbackend.exception.BulkSmsCampaignException;
import com.nitax.valueplusbackend.repository.BulkSmsMessageRepository;
import com.nitax.valueplusbackend.service.BulkSmsCampaignService;
import com.nitax.valueplusbackend.service.GeminiSmsService;
import com.nitax.valueplusbackend.service.PisiBulkSmsService;
import com.nitax.valueplusbackend.service.WalletService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkSMSCampaignScheduler {
    private final BulkSmsCampaignService bulkSmsCampaignService;
    private final PisiBulkSmsService pisiBulkSmsService;
    private final GeminiSmsService geminiSmsService;
    private final WalletService walletService;
    private final BulkSmsMessageRepository messageRepo;

//    @Scheduled(fixedRate = 60000) // Runs every 60 seconds
//    public void processPisiScheduledCampaigns() {
//        // Fetch campaigns with status SCHEDULED
//        List<BulkSmsCampaign> scheduledCampaigns = bulkSmsCampaignService.findByStatus(BulkSmsCampaignStatus.SCHEDULED);
//
//        for (BulkSmsCampaign campaign : scheduledCampaigns) {
//            // Check if the scheduled date is the current time or earlier
//            if (campaign.getScheduledDate() != null && campaign.getScheduledDate().toInstant().isBefore(Instant.now())) {
//
//                double currentWalletPoint = walletService.fetchWalletPointBalanceByAdvertiserId(campaign.getAdvertiser().getId());
//
//                double requiredPoints =  campaign.getTotalNumbers() * SMS_CPA;
//
//                if (currentWalletPoint < requiredPoints){
//                    //Todo: Send sms to advertiser that they do not have enough to send out this campaign
//                    throw new BulkSmsCampaignException("Insufficient wallet points to send sms, please reach out to admin or fund your wallet");
//                }
//
//                // Prepare the SMS request
//                PisiSendBulkSmsRequest smsRequest = new PisiSendBulkSmsRequest();
//                smsRequest.setMessage(campaign.getContent());
//                smsRequest.setSenderId(campaign.getSenderId());
//                smsRequest.setRecipients(campaign.getPhoneNumbers());
//
//                // Send the SMS
//                PisiSendSmsResponse response = pisiBulkSmsService.sendSms(smsRequest);
//
//                // Update the campaign status and transaction ID
//                campaign.setStatus(BulkSmsCampaignStatus.IN_PROGRESS);
//                campaign.setTransactionId(response.getTransactionId());
//
//                // Deduct cost for this sms sent
//                walletService.deductWalletPointAndBalanceByAdvertiserId(campaign.getAdvertiser().getId(),requiredPoints);
//                bulkSmsCampaignService.save(campaign);
//
//            }
//        }
//    }

//    @Scheduled(fixedRate = 60000) // Runs every 60 seconds
//    public void processGeminiScheduledCampaigns() {
//        // Fetch campaigns with status SCHEDULED
//        List<BulkSmsCampaign> scheduledCampaigns = bulkSmsCampaignService.findByStatus(BulkSmsCampaignStatus.SCHEDULED);
//
//        for (BulkSmsCampaign campaign : scheduledCampaigns) {
//            // Check if the scheduled date is the current time or earlier
//            if (campaign.getScheduledDate() != null && campaign.getScheduledDate().toInstant().isBefore(Instant.now())) {
//
//                double currentWalletPoint = walletService.fetchWalletPointBalanceByAdvertiserId(campaign.getAdvertiser().getId());
//
//                double requiredPoints =  campaign.getTotalNumbers() * SMS_CPA;
//
//                if (currentWalletPoint < requiredPoints){
//                    //Todo: Send sms to advertiser that they do not have enough to send out this campaign
//                    throw new BulkSmsCampaignException("Insufficient wallet points to send sms, please reach out to admin or fund your wallet");
//                }
//
//                // Prepare the SMS request
//                GeminiSendBulkSmsRequest smsRequest = new GeminiSendBulkSmsRequest();
//                smsRequest.setText(campaign.getContent());
//                smsRequest.setSource(campaign.getSenderId());
////                smsRequest.setDestinations(campaign.getPhoneNumbers());
//
//                String phoneNumbers = campaign.getPhoneNumbers(); // Assuming this is the comma-separated string
//                List<String> phoneNumberList = Arrays.stream(phoneNumbers.split(","))
//                        .map(String::trim) // Optional: Trim whitespace around numbers
//                        .toList(); // Convert to List
//                smsRequest.setDestinations(phoneNumberList);
//
//                // Send the SMS
//                GeminiSendBulkSmsResponse response =  geminiSmsService.sendBulkSms(smsRequest);
//                campaign.setTransactionId(response.getTransactionID());
//                campaign.setMessageID(response.getMessageID());
//                campaign.setStatus(BulkSmsCampaignStatus.IN_PROGRESS);
//                walletService.deductWalletPointAndBalanceByAdvertiserId(campaign.getAdvertiser().getId(),requiredPoints);
//                bulkSmsCampaignService.save(campaign);
//
//            }
//        }
//    }


    @Transactional
    @Scheduled(fixedRate = 60000) // Runs every 60 seconds
    public void processGeminiScheduledCampaigns() {
        System.out.println("Running processGeminiScheduledCampaigns job at " + new Date());
        log.info("Running processGeminiScheduledCampaigns job at " + new Date());

        List<BulkSmsCampaign> scheduledCampaigns = bulkSmsCampaignService.findByStatus(BulkSmsCampaignStatus.SCHEDULED);

        if (scheduledCampaigns.isEmpty()) {
            System.out.println("No SCHEDULED campaigns to process. Job finished.");
            log.info("No SCHEDULED campaigns to process. Job finished.");
            return;
        }

        System.out.println("Found " + scheduledCampaigns.size() + " scheduled campaigns to process.");
        log.info("Found " + scheduledCampaigns.size() + " scheduled campaigns to process.");

        List<BulkSmsCampaign> campaignsToUpdate = new ArrayList<>();
        // List<BulkSmsMessage> newBulkSmsMessages = new ArrayList<>(); // This list seems unused here, keep it if it's used elsewhere after this method

        for (BulkSmsCampaign campaign : scheduledCampaigns) {
            try {
                System.out.println("Processing scheduled campaign ID: " + campaign.getId() + ", scheduled for: " + campaign.getScheduledDate());

                if (campaign.getScheduledDate() != null && !campaign.getScheduledDate().toInstant().isAfter(Instant.now())) {

                    double currentWalletPoint = walletService.fetchWalletPointBalanceByAdvertiserId(campaign.getAdvertiser().getId());

                    // **** CRITICAL CHANGE HERE ****
                    // Calculate required points: 1 SMS = 1 Point
                    // So, requiredPoints is simply the total number of SMS messages.
                    // The conversion from points to monetary value is handled within walletService.deductWalletPointAndBalanceByAdvertiserId
                    double requiredPoints = campaign.getTotalNumbers(); // Each SMS costs 1 point

                    if (currentWalletPoint < requiredPoints) {
                        System.out.println("Insufficient wallet points for campaign ID " + campaign.getId() + ". Required: " + requiredPoints + ", Available: " + currentWalletPoint);
                        campaign.setStatus(BulkSmsCampaignStatus.FAILED);
                        campaignsToUpdate.add(campaign);
                        continue;
                    }

                    // Prepare the SMS request
                    GeminiSendBulkSmsRequest smsRequest = new GeminiSendBulkSmsRequest();
                    smsRequest.setText(campaign.getContent());
                    smsRequest.setSource(campaign.getSenderId());

                    String phoneNumbers = campaign.getPhoneNumbers();
                    List<String> phoneNumberList = Arrays.stream(phoneNumbers.split(","))
                            .map(String::trim)
                            .toList();
                    smsRequest.setDestinations(phoneNumberList); // Pass the list of numbers

                    // Send the SMS
                    GeminiSendBulkSmsResponse response = geminiSmsService.sendBulkSms(smsRequest);

                    // Update campaign details
                    campaign.setTransactionId(response.getTransactionID());
//                    campaign.setMessageID(response.getMessageID()); // This is likely the campaign-level ID
                    campaign.setStatus(BulkSmsCampaignStatus.IN_PROGRESS);

                    campaign.setProcessor("GEMINI");
                    campaign.setPhoneNumbers("");

                    // Deduct wallet points
                    // The 'requiredPoints' passed here is the number of points (1 point per SMS)
                    // The wallet service will internally convert this to monetary value for balance deduction.
                    walletService.deductWalletPointAndBalanceByAdvertiserId(campaign.getAdvertiser().getId(), requiredPoints);

                    campaignsToUpdate.add(campaign);

                    storeMessageDetails(response.getMessageID(), campaign, phoneNumberList);

                    System.out.println("Successfully started campaign ID " + campaign.getId() + ". TransactionID: " + response.getTransactionID() + ". Created " + phoneNumberList.size() + " individual messages.");

                } else {
                    System.out.println("Campaign ID " + campaign.getId() + " is scheduled for a future date. Skipping.");
                }

            } catch (BulkSmsCampaignException e) {
                System.err.println("Business error processing campaign ID " + campaign.getId() + ": " + e.getMessage());
                campaign.setStatus(BulkSmsCampaignStatus.FAILED);
                campaignsToUpdate.add(campaign);
            } catch (Exception e) {
                System.err.println("Unexpected error processing campaign ID " + campaign.getId() + ": " + e.getMessage());
                log.info(e.getLocalizedMessage());
                campaign.setStatus(BulkSmsCampaignStatus.FAILED);
                campaignsToUpdate.add(campaign);
            }
        }

        if (!campaignsToUpdate.isEmpty()) {
            bulkSmsCampaignService.saveAll(campaignsToUpdate);
            System.out.println("Saved " + campaignsToUpdate.size() + " scheduled campaign updates.");
        }

        System.out.println("processGeminiScheduledCampaigns job completed.");
    }

    private void storeMessageDetails(List<String > messageIds, BulkSmsCampaign bulkSmsCampaign,List<String > phoneNumbers){
        for (int i = 0; i < messageIds.size(); i++) {
            BulkSmsMessage message = new BulkSmsMessage();
            message.setCampaign(bulkSmsCampaign);
            message.setPhoneNumber(phoneNumbers.get(i));
            message.setMessageId(messageIds.get(i));
            message.setStatus(SmsDeliveryStatus.PENDING); // initial state
            messageRepo.save(message);
        }

    }

//    @Scheduled(fixedRate = 60000) // Runs every 60 seconds
//    public void processInProgressCampaigns() {
//        List<BulkSmsCampaign> inProgressCampaigns = bulkSmsCampaignService.findByStatus(BulkSmsCampaignStatus.IN_PROGRESS);
//
//        for (BulkSmsCampaign campaign : inProgressCampaigns) {
//            try {
//                // Check if the scheduled date is older than 3 weeks
//                if (campaign.getScheduledDate() != null &&
//                        campaign.getScheduledDate().toInstant().isBefore(Instant.now().minus(21, ChronoUnit.DAYS))) {
//
//                    // Refund points used for the campaign
//                    double pointsToRefund = campaign.getTotalNumbers() * SMS_CPA;
//                    walletService.refundWalletPointAndBalanceByAdvertiserId(campaign.getAdvertiser().getId(), pointsToRefund);
//
//                    // Mark campaign as FAILED
//                    campaign.setStatus(BulkSmsCampaignStatus.FAILED);
//                    bulkSmsCampaignService.save(campaign);
//                    continue; // Skip further processing for this campaign
//                }
//
//                if (campaign.getProcessor().equals("PISI")) {
//                    PisiGetDeliveryStatusResponse deliveryStatusResponse = pisiBulkSmsService.getDIR(campaign.getTransactionId());
//                    if (deliveryStatusResponse.isDelivered()) {
//                        campaign.setTotalDND(Long.parseLong(deliveryStatusResponse.getUndeliveredDND()));
//                        campaign.setTotalFailed(Long.parseLong(deliveryStatusResponse.getUndelivered()));
//                        campaign.setTotalDelivered(campaign.getTotalNumbers() - (campaign.getTotalDND() + campaign.getTotalFailed()));
//                        bulkSmsCampaignService.save(campaign);
//                    }
//                } else {
//                    for (String messageId : campaign.getMessageID()) {
//                        GeminiQuerySmsStatusResponse deliveryStatusResponse = geminiSmsService.querySmsStatus(messageId);
//                        if (deliveryStatusResponse.getStatus().equalsIgnoreCase("1") &&
//                                deliveryStatusResponse.getStatusText().equalsIgnoreCase("delivered")) {
//                            campaign.setStatus(BulkSmsCampaignStatus.COMPLETED);
//                            campaign.setTotalDelivered(campaign.getTotalNumbers() - (campaign.getTotalDND() + campaign.getTotalFailed()));
//                            bulkSmsCampaignService.save(campaign);
//                        }
//                    }
//                }
//            } catch (Exception e) {
//                log.info("Something went wrong -->{}", e.getLocalizedMessage());
//            }
//        }
//    }



    @Scheduled(cron = "0 */5 * * * ?") // Every 5 minutes
    @Transactional // <--- Crucial for session management and atomicity
//    @Scheduled(cron = "*/30 * * * * ?") // Runs every 30 seconds
    public void checkPendingMessages() {
        System.out.println("Running checkPendingMessages job at " + new Date());

        List<BulkSmsMessage> pendingMessages = messageRepo.findByStatusIn(
                List.of(SmsDeliveryStatus.PENDING, SmsDeliveryStatus.UNKNOWN)
        );

        if (pendingMessages.isEmpty()) {
            System.out.println("No pending or unknown messages to check. Job finished.");
            return;
        }

        List<BulkSmsMessage> messagesToUpdate = new ArrayList<>();
        // Use a Map to hold campaigns that need updates, keyed by their ID
        Map<Long, BulkSmsCampaign> campaignsToUpdateMap = new HashMap<>();

        System.out.println("Found " + pendingMessages.size() + " pending or unknown messages.");

        for (BulkSmsMessage message : pendingMessages) {
            try {
                System.out.println("Querying status for message ID: " + message.getMessageId() + ", current status: " + message.getStatus());

                GeminiQuerySmsStatusResponse response = geminiSmsService.querySmsStatus(message.getMessageId());
                String apiStatusText = response.getStatusText();

                System.out.println("Received API statusText for message ID " + message.getMessageId() + ": '" + apiStatusText + "'");

                SmsDeliveryStatus status = mapStatus(apiStatusText);

                System.out.println("Mapped API statusText '" + apiStatusText + "' to SmsDeliveryStatus: " + status);

                // Only update if the status has changed (optional, but good practice)
                if (message.getStatus() != status) {
                    message.setStatus(status);
                    messagesToUpdate.add(message); // Add to list for batch update

                    BulkSmsCampaign campaign = message.getCampaign(); // Get the lazy-loaded proxy (within transaction)

                    // Get the actual campaign object from the map if already present,
                    // otherwise put the current one into the map
                    campaignsToUpdateMap.putIfAbsent(campaign.getId(), campaign);
                    BulkSmsCampaign campaignForUpdate = campaignsToUpdateMap.get(campaign.getId());


                    switch (status) {
                        case DELIVERED -> campaignForUpdate.setTotalDelivered(campaignForUpdate.getTotalDelivered() + 1);
                        case FAILED -> campaignForUpdate.setTotalFailed(campaignForUpdate.getTotalFailed() + 1);
                        case DND -> campaignForUpdate.setTotalDND(campaignForUpdate.getTotalDND() + 1);
                        case UNDELIVERABLE -> campaignForUpdate.setTotalFailed(campaignForUpdate.getTotalFailed() + 1); // Often undeliverable is treated as a failed count
                        // No action needed for PENDING or UNKNOWN if they remain in that state
                    }

                    // Check for campaign completion *after* incrementing counts
                    if (
                            campaignForUpdate.getTotalNumbers() > 0 && // Avoid division by zero or erroneous completion for 0 messages
                            (campaignForUpdate.getTotalDelivered() + campaignForUpdate.getTotalFailed() + campaignForUpdate.getTotalDND()) >= campaignForUpdate.getTotalNumbers() ) {
                        campaignForUpdate.setStatus(BulkSmsCampaignStatus.COMPLETED);
                        System.out.println("Campaign " + campaignForUpdate.getId() + " marked as COMPLETED.");
                    }

                } else {
                    System.out.println("Message ID " + message.getMessageId() + ": Status has not changed (" + status + "). No update needed.");
                }

            } catch (Exception e) {
                System.err.println("Error checking status for message ID " + message.getMessageId() + ": " + e.getMessage());
                log.info(e.getLocalizedMessage());

            }
        }

        if (!messagesToUpdate.isEmpty()) {
            messageRepo.saveAll(messagesToUpdate);
            System.out.println("Saved " + messagesToUpdate.size() + " message status updates.");
        } else {
            System.out.println("No message statuses required saving.");
        }


        if (!campaignsToUpdateMap.isEmpty()) {
            bulkSmsCampaignService.saveAll(campaignsToUpdateMap.values());
            System.out.println("Saved " + campaignsToUpdateMap.size() + " campaign updates.");
        } else {
            System.out.println("No campaign updates required saving.");
        }


        System.out.println("SMS delivery status job completed. Total messages processed: " + pendingMessages.size() + ". Job finished."); // Report on initial messages
    }


//    private void updateMessages(List<SmsStatusResponseDto> reports) {
//        List<BulkSmsMessage> messagesToUpdate = new ArrayList<>();
//
//        for (SmsStatusResponseDto report : reports) {
//            Optional<BulkSmsMessage> optionalMessage = messageRepo.findByMessageId(report.getMessageId());
//            if (optionalMessage.isPresent()) {
//                BulkSmsMessage message = optionalMessage.get();
//                message.setStatus(mapStatus(report.getStatusText()));
//                message.setDeliveryTimestamp(parseIsoDate(report.getTs()));
//                message.setErrorCode(report.getError());
//                messagesToUpdate.add(message);
//            }
//        }
//
//        messageRepo.saveAll(messagesToUpdate);
//    }

    private SmsDeliveryStatus mapStatus(String statusText) {
        if (statusText == null) {
            System.out.println("mapStatus received null statusText. Defaulting to UNKNOWN.");
            return SmsDeliveryStatus.UNKNOWN;
        }
        String lowerCaseStatusText = statusText.trim().toLowerCase();
        System.out.println("mapStatus processing trimmed and lowercased statusText: '" + lowerCaseStatusText + "'");

        return switch (lowerCaseStatusText) {
            case "delivered" -> SmsDeliveryStatus.DELIVERED;
            case "undeliverable" -> SmsDeliveryStatus.UNDELIVERABLE;
            case "pending" -> SmsDeliveryStatus.PENDING;
            case "unknown" -> SmsDeliveryStatus.UNKNOWN;
            default -> {
                System.out.println("Unhandled statusText encountered: '" + lowerCaseStatusText + "'. Mapping to FAILED.");
                yield SmsDeliveryStatus.FAILED;
            }
        };
    }

//    private Instant parseIsoDate(String iso) {
//        try {
//            return Instant.parse(iso);
//        } catch (Exception e) {
//            return null;
//        }
//    }
}
