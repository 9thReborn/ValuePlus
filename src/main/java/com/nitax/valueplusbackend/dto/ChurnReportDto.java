package com.nitax.valueplusbackend.dto;

import com.univocity.parsers.annotations.Parsed;
import com.univocity.parsers.annotations.Trim;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChurnReportDto {
  @Trim
  @Parsed(field = "msisdn")
  private String msisdn;

  @Trim
  @Parsed(field = "transaction_id")
  private String trxId;

  @Trim
  @Parsed(field = "campaign_name")
  private String campaignName;

  @Trim
  @Parsed(field = "publisher_id")
  private String publisherId;

  @Trim
  @Parsed(field = "source_id")
  private String sourceId;

  @Trim
  @Parsed(field = "duration")
  private Long duration;

  @Trim
  @Parsed(field = "joined_date")
  private String joinedDate;

  @Parsed(field = "unsubscribe_timestamp")
  private String unsubscribedDate;
}
