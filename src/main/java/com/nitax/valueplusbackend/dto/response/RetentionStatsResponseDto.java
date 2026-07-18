package com.nitax.valueplusbackend.dto.response;

import lombok.Data;

@Data
public class RetentionStatsResponseDto {
  private long numberOfUnsubscribers;
  private double percentageOfUnsubscribers;
  private long numberOfAllSubscribers;
}
