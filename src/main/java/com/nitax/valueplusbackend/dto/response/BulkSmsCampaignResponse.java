package com.nitax.valueplusbackend.dto.response;

import com.nitax.valueplusbackend.domain.BulkSmsCampaignStatus;
import lombok.Data;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Data
public class BulkSmsCampaignResponse {
    private String transactionId;
    private BulkSmsCampaignStatus status;
    private String csv;
    private long targetNumbers;
    private Date scheduledDate;
    private long totalNumbers;
    private long totalDelivered;
    private long totalFailed;
    private long totalDND;
    private String name;
    private String senderId;
    private String content;
    private String phoneNumbers;
    private String country;
    private String state;
    private String lga;
    private String bulkSmsCampaignId;
    private Instant createdDate;
    private double pointUsed;
    private List<String> messageID;
    private double totalPoints;
    private double pointRemaining;
//    private String advertiserName;
    private double totalAmountSpent;
}
