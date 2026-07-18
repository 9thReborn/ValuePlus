package com.nitax.valueplusbackend.dto;

import com.univocity.parsers.annotations.Parsed;
import com.univocity.parsers.annotations.Trim;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublisherConversionReportDto {
  @Trim
  @Parsed(field = "campaign_name")
  private String campaignName;

  @Trim
  @Parsed(field = "total_good_acquisition")
  private long totalGoodAcquisition;

  @Trim
  @Parsed(field = "total_bad_acquisition")
  private long totalBadAcquisition;

  @Trim
  @Parsed(field = "total_bad_acquisition_cost")
  private double totalBadAcquisitionCost;

  @Trim
  @Parsed(field = "total_good_acquisition_cost")
  private double totalCost;

  public PublisherConversionReportDto(
      String campaignName,
      Long totalGoodAcquisition,
      Long totalBadAcquisition,
      Double totalCost,
      Double totalBadAcquisitionCost) {
    this.campaignName = campaignName;
    this.totalGoodAcquisition = totalGoodAcquisition;
    this.totalBadAcquisition = totalBadAcquisition;
    this.totalCost = totalCost;
    this.totalBadAcquisitionCost = totalBadAcquisitionCost;
  }
}
