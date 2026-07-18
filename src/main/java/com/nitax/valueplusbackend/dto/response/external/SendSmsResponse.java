package com.nitax.valueplusbackend.dto.response.external;

import com.nitax.valueplusbackend.domain.BulkSmsCampaignStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SendSmsResponse {
    private String smsId;
    private String message;
    private String status;
    private BulkSmsCampaignStatus smsStatus;
    private List<String> messageID;
}
