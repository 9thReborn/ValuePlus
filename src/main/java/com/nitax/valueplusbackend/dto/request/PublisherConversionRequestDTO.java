package com.nitax.valueplusbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PublisherConversionRequestDTO {
  private String publisherName;
  private String publisherId;
  private String churnPeriod;
  private boolean sourceId;
  @PastOrPresent private LocalDateTime startDate;
  @PastOrPresent private LocalDateTime endDate;
}
