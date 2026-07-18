package com.nitax.valueplusbackend.dto.request;

import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
@Data
public class PubConversionRequestDto {
    private String publisherName;
    private String publisherId;
    private String churnPeriod;
    private boolean sourceId;
    private Date startDate;
   private Date endDate;
}
