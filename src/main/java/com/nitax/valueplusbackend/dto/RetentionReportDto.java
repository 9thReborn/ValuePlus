package com.nitax.valueplusbackend.dto;

import com.univocity.parsers.annotations.Parsed;
import lombok.Data;

@Data
public class RetentionReportDto {
  @Parsed String publisherName;
  @Parsed String sourceId;
  @Parsed String campaignName;
  @Parsed Long totalAcquisition;
  @Parsed Long churnedUsers;
  @Parsed String retentionRate;

  public RetentionReportDto(
      String publisherName,
      String sourceId,
      Long totalAcquisition,
      Long churnedUsers,
      String campaignName) {
    this.publisherName = publisherName;
    this.sourceId = sourceId;
    this.totalAcquisition = totalAcquisition;
    this.churnedUsers = churnedUsers;
    this.campaignName = campaignName;
    this.retentionRate = calculateRetentionRate(totalAcquisition, churnedUsers);
  }

  private String calculateRetentionRate(Long totalAcquisition, Long churnedUsers) {
    if (totalAcquisition == 0) {
      return "0%";
    }
    return String.format(
            "%.2f", ((totalAcquisition - churnedUsers) / (double) totalAcquisition) * 100)
        + "%";
  }
}
