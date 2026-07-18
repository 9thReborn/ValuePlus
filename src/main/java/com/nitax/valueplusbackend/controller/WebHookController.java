package com.nitax.valueplusbackend.controller;

import com.nitax.valueplusbackend.domain.BulkSmsCampaign;
import com.nitax.valueplusbackend.domain.BulkSmsCampaignStatus;
import com.nitax.valueplusbackend.domain.BulkSmsMessage;
import com.nitax.valueplusbackend.domain.SmsDeliveryStatus;
import com.nitax.valueplusbackend.dto.request.SmsDeliveryReportDto;
import com.nitax.valueplusbackend.repository.BulkSmsMessageRepository;
import com.nitax.valueplusbackend.service.BulkSmsCampaignService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/webhooks")
@AllArgsConstructor
public class WebHookController {
    private final BulkSmsMessageRepository messageRepo;
    private final BulkSmsCampaignService bulkSmsCampaignService;

    @PostMapping("/status")
    public ResponseEntity<String> receiveDeliveryStatus(@RequestBody SmsDeliveryReportDto report) {
        if (report.getMessageIDs() == null || report.getMessageIDs().isEmpty()) {
            return ResponseEntity.badRequest().body("No message IDs provided");
        }

        SmsDeliveryStatus status = mapStatus(report.getStatusText());
        Instant ts = Instant.parse(report.getTs());
        String error = report.getError();

        List<BulkSmsMessage> messages = messageRepo.findByMessageIdIn(report.getMessageIDs());

        for (BulkSmsMessage message : messages) {
            message.setStatus(status);
            message.setDeliveryTimestamp(Date.from(ts));
            message.setError(error);

            switch (message.getStatus()){
                case DELIVERED ->
                {
                    BulkSmsCampaign campaign = bulkSmsCampaignService.getBulkSmsCampaignById(message.getCampaign().getId());
                    campaign.setTotalDelivered(campaign.getTotalDelivered() + 1);
                    bulkSmsCampaignService.save(campaign);
                }
                case DND -> {
                    BulkSmsCampaign campaign = bulkSmsCampaignService.getBulkSmsCampaignById(message.getCampaign().getId());
                    campaign.setTotalDND(campaign.getTotalDND() + 1);
                    bulkSmsCampaignService.save(campaign);
                }
                case FAILED -> {
                    BulkSmsCampaign campaign = bulkSmsCampaignService.getBulkSmsCampaignById(message.getCampaign().getId());
                    campaign.setTotalFailed(campaign.getTotalFailed() + 1);
                    bulkSmsCampaignService.save(campaign);
                }
                default -> message.setError("Unknown status");
            }

        }

        messageRepo.saveAll(messages);

        return ResponseEntity.ok("Delivery status updated for " + messages.size() + " message(s).");
    }

    private SmsDeliveryStatus mapStatus(String statusText) {
        return switch (statusText.toLowerCase()) {
            case "delivered" -> SmsDeliveryStatus.DELIVERED;
            case "undelivered" -> SmsDeliveryStatus.FAILED;
            case "dnd" -> SmsDeliveryStatus.DND;
            case "pending" -> SmsDeliveryStatus.PENDING;
            default -> SmsDeliveryStatus.FAILED;
        };
    }
}
