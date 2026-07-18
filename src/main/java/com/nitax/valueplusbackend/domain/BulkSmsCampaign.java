package com.nitax.valueplusbackend.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "bulk_sms_campaign")
@Data
@NoArgsConstructor
public class BulkSmsCampaign extends BaseEntity {
    private String name;
    private String senderId;
    private String content;
    private String phoneNumbers;
    private String country;
    private String state;
    private String lga;
    private String geography;
    private String bulkSmsCampaignId;
    private String transactionId;
    private String processor; //GEMINI PISI
    private String processChannel;
    @Enumerated(value = EnumType.STRING)
    private BulkSmsCampaignStatus status;
    private String csv;
    private boolean isActive;
    private long targetNumbers;
    private Date scheduledDate;
    private long totalNumbers;
    private long totalDelivered;
    private long totalFailed;
    private long totalDND;
    @ElementCollection
    @CollectionTable(name = "bulk_sms_campaign_message_ids", joinColumns = @JoinColumn(name = "bulk_sms_campaign_id"))
    @Column(name = "message_id")
    private List<String> messageID;

    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BulkSmsMessage> messages;


    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name="advertiser_id")
    private Advertiser advertiser;

    @ElementCollection
    @CollectionTable(name = "bulk_sms_campaign_excluded_numbers", joinColumns = @JoinColumn(name = "bulk_sms_campaign_id"))
    @Column(name = "excluded_number")
    private List<String> excludedNumbers;
}
