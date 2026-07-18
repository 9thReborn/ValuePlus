package com.nitax.valueplusbackend.dto;

import com.univocity.parsers.annotations.Parsed;
import lombok.Data;

@Data
public class PubChurnReportDto {
  @Parsed String publisherName;
  @Parsed String campaignName;
  @Parsed Long totalAcquisition;
  @Parsed Long churnedUsers;
  @Parsed Long churnRate;
  @Parsed String churnGrade;

  public PubChurnReportDto(
      String publisherName, String campaignName, Long totalAcquisition, Long churnedUsers) {
    this.publisherName = publisherName;
    this.campaignName = campaignName;
    this.totalAcquisition = totalAcquisition;
    this.churnedUsers = churnedUsers;
    this.churnRate = calculateChurnRate(totalAcquisition, churnedUsers);
    this.churnGrade = calculateChurnGrade(churnRate);
  }

  private String calculateChurnGrade(Long churnPercent) {
    if (churnPercent < 5) {
      return "Very Good";
    } else if (churnPercent < 10) {
      return "Good";
    } else if (churnPercent < 15) {
      return "Bad";
    } else {
      return "Very Bad";
    }
  }

  private Long calculateChurnRate(Long totalAcquisition, Long churnedUsers) {
    if (totalAcquisition == 0) {
      return 0L;
    }
    return (churnedUsers / totalAcquisition) * 100;
  }
}
