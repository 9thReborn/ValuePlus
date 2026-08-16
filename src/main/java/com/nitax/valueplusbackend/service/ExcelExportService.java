package com.nitax.valueplusbackend.service;

import java.io.ByteArrayOutputStream;
import java.util.List;

import com.nitax.valueplusbackend.domain.PayoutClassification;
import com.nitax.valueplusbackend.dto.AdminChurnReportDto;
import com.nitax.valueplusbackend.dto.response.AdvertiserConversionCpaBreakdownDTO;
import com.nitax.valueplusbackend.dto.response.AdvertiserConversionDTO;
import com.nitax.valueplusbackend.dto.response.PublisherCampaignConversionsDTO;

import com.nitax.valueplusbackend.dto.response.PublisherConversionsDTO;

public interface ExcelExportService {

  /**
   * Exports advertiser conversions to an Excel file with separate sheets per advertiser.
   *
   * @param conversions List of advertiser conversion data
   * @return ByteArrayOutputStream containing the Excel file data
   */
  ByteArrayOutputStream exportAdvertiserConversionsToExcel(List<AdvertiserConversionDTO> conversions);

  /**
   * Exports publisher campaign conversions to an Excel file with separate sheets per publisher.
   *
   * @param conversions List of publisher campaign conversion data
   * @return ByteArrayOutputStream containing the Excel file data
   */
  ByteArrayOutputStream exportPublisherCampaignConversionsToExcel(List<PublisherCampaignConversionsDTO> conversions);

  /**
   * Exports publisher conversions to an Excel file.
   *
   * @param conversions List of publisher conversion data
   * @return ByteArrayOutputStream containing the Excel file data
   */
  ByteArrayOutputStream exportPublisherConversionsToExcel(List<PublisherConversionsDTO> conversions);

  /**
   * Exports advertiser conversions with CPA breakdown to an Excel file.
   * Shows conversions at each unique CPA rate for accurate amount tracking.
   *
   * @param conversions List of advertiser conversion data with CPA breakdown
   * @return ByteArrayOutputStream containing the Excel file data
   */
  ByteArrayOutputStream exportAdvertiserConversionsCpaBreakdownToExcel(
      List<AdvertiserConversionCpaBreakdownDTO> conversions);

  ByteArrayOutputStream exportAdminChurnReportToExcel(List<AdminChurnReportDto> rows);

    /**
     * Exports invalid-for-payout {@link PayoutClassification}
     * rows with their structured {@code reasonCode} as its own column, rather than the free-text
     * {@code Notification.message} field this replaces (see {@code ReasonCode}'s javadoc).
     *
     * @param records List of INVALID_FOR_PAYOUT classification rows for the requested date range
     * @return ByteArrayOutputStream containing the Excel file data
     */
    ByteArrayOutputStream exportPayoutReportToExcel(List<PayoutClassification> records);
}
