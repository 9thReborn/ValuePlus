package com.nitax.valueplusbackend.dto.request;

import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AdvertiserConversionRequestDTO {
  private String advertiserName;
  @PastOrPresent private LocalDateTime startDate;
  @PastOrPresent private LocalDateTime endDate;
  private RecordStatus conversionType;
}
