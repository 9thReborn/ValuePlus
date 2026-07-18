package com.nitax.valueplusbackend.service.impl;

import com.nitax.valueplusbackend.dto.AdminChurnReportDto;
import com.nitax.valueplusbackend.dto.response.AdvertiserConversionCpaBreakdownDTO;
import com.nitax.valueplusbackend.dto.response.AdvertiserConversionDTO;
import com.nitax.valueplusbackend.dto.response.PublisherCampaignConversionsDTO;

import com.nitax.valueplusbackend.dto.response.PublisherConversionsDTO;
import com.nitax.valueplusbackend.service.ExcelExportService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ExcelExportServiceImpl implements ExcelExportService {

  private static final String[] HEADERS = {
    "Campaign Name",
    "Conversions",
    "Churn",
    "Budget",
    "CPA",
    "Country"
  };

  @Override
  public ByteArrayOutputStream exportAdvertiserConversionsToExcel(
      List<AdvertiserConversionDTO> conversions) {
    try (Workbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

      // Group conversions by advertiser name
      Map<String, List<AdvertiserConversionDTO>> conversionsByAdvertiser =
          conversions.stream()
              .collect(Collectors.groupingBy(AdvertiserConversionDTO::getAdvertiserName));

      // Create header style
      CellStyle headerStyle = createHeaderStyle(workbook);
      CellStyle dataStyle = createDataStyle(workbook);
      CellStyle currencyStyle = createCurrencyStyle(workbook);

      // Create a sheet for each advertiser
      for (Map.Entry<String, List<AdvertiserConversionDTO>> entry :
          conversionsByAdvertiser.entrySet()) {
        String advertiserName = entry.getKey();
        List<AdvertiserConversionDTO> advertiserConversions = entry.getValue();

        // Sanitize sheet name (Excel has a 31-character limit and doesn't allow certain characters)
        String sheetName = sanitizeSheetName(advertiserName);
        Sheet sheet = workbook.createSheet(sheetName);

        // Create header row
        createHeaderRow(sheet, headerStyle);

        // Populate data rows
        int rowNum = 1;
        for (AdvertiserConversionDTO conversion : advertiserConversions) {
          Row row = sheet.createRow(rowNum++);
          populateDataRow(row, conversion, dataStyle, currencyStyle);
        }

        // Add summary row
        createSummaryRow(sheet, advertiserConversions, rowNum, headerStyle, currencyStyle);

        // Auto-size columns for better readability
        for (int i = 0; i < HEADERS.length; i++) {
          sheet.autoSizeColumn(i);
        }
      }

      // If no data, create an empty sheet with headers
      if (conversionsByAdvertiser.isEmpty()) {
        Sheet sheet = workbook.createSheet("No Data");
        createHeaderRow(sheet, headerStyle);
      }

      workbook.write(outputStream);
      return outputStream;

    } catch (IOException e) {
      log.error("Error generating Excel report: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to generate Excel report", e);
    }
  }

  private CellStyle createHeaderStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    Font font = workbook.createFont();
    font.setBold(true);
    font.setColor(IndexedColors.WHITE.getIndex());
    style.setFont(font);
    style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    style.setAlignment(HorizontalAlignment.CENTER);
    style.setBorderBottom(BorderStyle.THIN);
    style.setBorderTop(BorderStyle.THIN);
    style.setBorderLeft(BorderStyle.THIN);
    style.setBorderRight(BorderStyle.THIN);
    return style;
  }

  private CellStyle createDataStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    style.setBorderBottom(BorderStyle.THIN);
    style.setBorderTop(BorderStyle.THIN);
    style.setBorderLeft(BorderStyle.THIN);
    style.setBorderRight(BorderStyle.THIN);
    return style;
  }

  private CellStyle createCurrencyStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    DataFormat format = workbook.createDataFormat();
    style.setDataFormat(format.getFormat("#,##0.00"));
    style.setBorderBottom(BorderStyle.THIN);
    style.setBorderTop(BorderStyle.THIN);
    style.setBorderLeft(BorderStyle.THIN);
    style.setBorderRight(BorderStyle.THIN);
    return style;
  }

  private void createHeaderRow(Sheet sheet, CellStyle headerStyle) {
    Row headerRow = sheet.createRow(0);
    for (int i = 0; i < HEADERS.length; i++) {
      Cell cell = headerRow.createCell(i);
      cell.setCellValue(HEADERS[i]);
      cell.setCellStyle(headerStyle);
    }
  }

  private void populateDataRow(
      Row row, AdvertiserConversionDTO conversion, CellStyle dataStyle, CellStyle currencyStyle) {
    int cellNum = 0;

    // Campaign Name
    Cell cell = row.createCell(cellNum++);
    cell.setCellValue(conversion.getCampaignName() != null ? conversion.getCampaignName() : "");
    cell.setCellStyle(dataStyle);

    // Conversions
    cell = row.createCell(cellNum++);
    cell.setCellValue(conversion.getConversions() != null ? conversion.getConversions() : 0);
    cell.setCellStyle(dataStyle);

    // Churn
    cell = row.createCell(cellNum++);
    cell.setCellValue(conversion.getChurn() != null ? conversion.getChurn() : 0);
    cell.setCellStyle(dataStyle);

    // Budget
    cell = row.createCell(cellNum++);
    cell.setCellValue(conversion.getBudget() != null ? conversion.getBudget() : 0);
    cell.setCellStyle(currencyStyle);

    // CPA
    cell = row.createCell(cellNum++);
    cell.setCellValue(conversion.getCpa() != null ? conversion.getCpa() : 0);
    cell.setCellStyle(currencyStyle);

    // Country
    cell = row.createCell(cellNum);
    cell.setCellValue(conversion.getCountry() != null ? conversion.getCountry() : "");
    cell.setCellStyle(dataStyle);
  }

  private void createSummaryRow(
      Sheet sheet,
      List<AdvertiserConversionDTO> conversions,
      int rowNum,
      CellStyle headerStyle,
      CellStyle currencyStyle) {
    // Add an empty row before summary
    rowNum++;
    Row summaryRow = sheet.createRow(rowNum);

    // Calculate totals
    long totalConversions =
        conversions.stream()
            .mapToLong(c -> c.getConversions() != null ? c.getConversions() : 0)
            .sum();
    long totalChurn =
        conversions.stream().mapToLong(c -> c.getChurn() != null ? c.getChurn() : 0).sum();
    long totalBudget =
        conversions.stream().mapToLong(c -> c.getBudget() != null ? c.getBudget() : 0).sum();

    // Summary label
    Cell cell = summaryRow.createCell(0);
    cell.setCellValue("TOTAL");
    cell.setCellStyle(headerStyle);

    // Total Conversions
    cell = summaryRow.createCell(1);
    cell.setCellValue(totalConversions);
    cell.setCellStyle(headerStyle);

    // Total Churn
    cell = summaryRow.createCell(2);
    cell.setCellValue(totalChurn);
    cell.setCellStyle(headerStyle);

    // Total Budget
    cell = summaryRow.createCell(3);
    cell.setCellValue(totalBudget);
    cell.setCellStyle(headerStyle);
  }

  private String sanitizeSheetName(String name) {
    if (name == null || name.isEmpty()) {
      return "Unknown";
    }
    // Remove invalid characters for Excel sheet names
    String sanitized = name.replaceAll("[\\[\\]\\*\\?/\\\\:]", "_");
    // Truncate to 31 characters (Excel limit)
    if (sanitized.length() > 31) {
      sanitized = sanitized.substring(0, 31);
    }
    return sanitized;
  }

  // ==================== Publisher Campaign Conversions Export ====================

  private static final String[] PUBLISHER_HEADERS = {
    "Campaign Name",
    "Conversions",
    "Churn",
    "Amount Spent",
    "CPA"
  };

  @Override
  public ByteArrayOutputStream exportPublisherCampaignConversionsToExcel(
      List<PublisherCampaignConversionsDTO> conversions) {
    try (Workbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

      // Group conversions by publisher name
      Map<String, List<PublisherCampaignConversionsDTO>> conversionsByPublisher =
          conversions.stream()
              .collect(Collectors.groupingBy(PublisherCampaignConversionsDTO::getPublisherName));

      // Create styles
      CellStyle headerStyle = createHeaderStyle(workbook);
      CellStyle dataStyle = createDataStyle(workbook);
      CellStyle currencyStyle = createCurrencyStyle(workbook);

      // Create a sheet for each publisher
      for (Map.Entry<String, List<PublisherCampaignConversionsDTO>> entry :
          conversionsByPublisher.entrySet()) {
        String publisherName = entry.getKey();
        List<PublisherCampaignConversionsDTO> publisherConversions = entry.getValue();

        // Sanitize sheet name
        String sheetName = sanitizeSheetName(publisherName);
        Sheet sheet = workbook.createSheet(sheetName);

        // Create header row
        createPublisherHeaderRow(sheet, headerStyle);

        // Populate data rows
        int rowNum = 1;
        for (PublisherCampaignConversionsDTO conversion : publisherConversions) {
          Row row = sheet.createRow(rowNum++);
          populatePublisherDataRow(row, conversion, dataStyle, currencyStyle);
        }

        // Add summary row
        createPublisherSummaryRow(sheet, publisherConversions, rowNum, headerStyle);

        // Auto-size columns
        for (int i = 0; i < PUBLISHER_HEADERS.length; i++) {
          sheet.autoSizeColumn(i);
        }
      }

      // If no data, create an empty sheet with headers
      if (conversionsByPublisher.isEmpty()) {
        Sheet sheet = workbook.createSheet("No Data");
        createPublisherHeaderRow(sheet, headerStyle);
      }

      workbook.write(outputStream);
      return outputStream;

    } catch (IOException e) {
      log.error("Error generating Publisher Excel report: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to generate Publisher Excel report", e);
    }
  }

  private void createPublisherHeaderRow(Sheet sheet, CellStyle headerStyle) {
    Row headerRow = sheet.createRow(0);
    for (int i = 0; i < PUBLISHER_HEADERS.length; i++) {
      Cell cell = headerRow.createCell(i);
      cell.setCellValue(PUBLISHER_HEADERS[i]);
      cell.setCellStyle(headerStyle);
    }
  }

  private void populatePublisherDataRow(
      Row row,
      PublisherCampaignConversionsDTO conversion,
      CellStyle dataStyle,
      CellStyle currencyStyle) {
    int cellNum = 0;

    // Campaign Name
    Cell cell = row.createCell(cellNum++);
    cell.setCellValue(conversion.getCampaignName() != null ? conversion.getCampaignName() : "");
    cell.setCellStyle(dataStyle);

    // Conversions
    cell = row.createCell(cellNum++);
    cell.setCellValue(conversion.getConversions() != null ? conversion.getConversions() : 0);
    cell.setCellStyle(dataStyle);

    // Churn
    cell = row.createCell(cellNum++);
    cell.setCellValue(conversion.getChurn() != null ? conversion.getChurn() : 0);
    cell.setCellStyle(dataStyle);

    // Amount Spent
    cell = row.createCell(cellNum++);
    double amountSpent = 0;
    if (conversion.getAmountSpent() != null && !conversion.getAmountSpent().isEmpty()) {
      try {
        amountSpent = Double.parseDouble(conversion.getAmountSpent());
      } catch (NumberFormatException e) {
        amountSpent = 0;
      }
    }
    cell.setCellValue(amountSpent);
    cell.setCellStyle(currencyStyle);

    // CPA
    cell = row.createCell(cellNum);
    cell.setCellValue(conversion.getCPA() != null ? conversion.getCPA() : 0);
    cell.setCellStyle(currencyStyle);
  }

  private void createPublisherSummaryRow(
      Sheet sheet,
      List<PublisherCampaignConversionsDTO> conversions,
      int rowNum,
      CellStyle headerStyle) {
    // Add an empty row before summary
    rowNum++;
    Row summaryRow = sheet.createRow(rowNum);

    // Calculate totals
    long totalConversions =
        conversions.stream()
            .mapToLong(c -> c.getConversions() != null ? c.getConversions() : 0)
            .sum();
    long totalChurn =
        conversions.stream().mapToLong(c -> c.getChurn() != null ? c.getChurn() : 0).sum();
    double totalAmountSpent =
        conversions.stream()
            .mapToDouble(
                c -> {
                  if (c.getAmountSpent() != null && !c.getAmountSpent().isEmpty()) {
                    try {
                      return Double.parseDouble(c.getAmountSpent());
                    } catch (NumberFormatException e) {
                      return 0;
                    }
                  }
                  return 0;
                })
            .sum();

    // Summary label
    Cell cell = summaryRow.createCell(0);
    cell.setCellValue("TOTAL");
    cell.setCellStyle(headerStyle);

    // Total Conversions
    cell = summaryRow.createCell(1);
    cell.setCellValue(totalConversions);
    cell.setCellStyle(headerStyle);

    // Total Churn
    cell = summaryRow.createCell(2);
    cell.setCellValue(totalChurn);
    cell.setCellStyle(headerStyle);

    // Total Amount Spent
    cell = summaryRow.createCell(3);
    cell.setCellValue(totalAmountSpent);
    cell.setCellStyle(headerStyle);
  }

  // ==================== Publisher Conversions Export ====================

  private static final String[] PUBLISHER_CONV_HEADERS = {
    "Publisher Name", "Conversions", "Churn", "Amount Spent", "CPA"
  };

  @Override
  public ByteArrayOutputStream exportPublisherConversionsToExcel(
      List<PublisherConversionsDTO> conversions) {
    try (Workbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

      // Create styles
      CellStyle headerStyle = createHeaderStyle(workbook);
      CellStyle dataStyle = createDataStyle(workbook);
      CellStyle currencyStyle = createCurrencyStyle(workbook);

      // Create single sheet for all publishers
      Sheet sheet = workbook.createSheet("Publisher Conversions");

      // Create header row
      Row headerRow = sheet.createRow(0);
      for (int i = 0; i < PUBLISHER_CONV_HEADERS.length; i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(PUBLISHER_CONV_HEADERS[i]);
        cell.setCellStyle(headerStyle);
      }

      // Populate data rows
      int rowNum = 1;
      for (PublisherConversionsDTO conversion : conversions) {
        Row row = sheet.createRow(rowNum++);
        populatePublisherConvDataRow(row, conversion, dataStyle, currencyStyle);
      }

      // Add summary row
      createPublisherConvSummaryRow(sheet, conversions, rowNum, headerStyle, currencyStyle);

      // Auto-size columns
      for (int i = 0; i < PUBLISHER_CONV_HEADERS.length; i++) {
        sheet.autoSizeColumn(i);
      }

      workbook.write(outputStream);
      return outputStream;

    } catch (IOException e) {
      log.error("Error generating Publisher Conversions Excel report: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to generate Publisher Conversions Excel report", e);
    }
  }

  private void populatePublisherConvDataRow(
      Row row, PublisherConversionsDTO conversion, CellStyle dataStyle, CellStyle currencyStyle) {
    int cellNum = 0;

    // Publisher Name
    Cell cell = row.createCell(cellNum++);
    cell.setCellValue(conversion.getPublisherName() != null ? conversion.getPublisherName() : "");
    cell.setCellStyle(dataStyle);

    // Conversions
    cell = row.createCell(cellNum++);
    cell.setCellValue(conversion.getConversions() != null ? conversion.getConversions() : 0);
    cell.setCellStyle(dataStyle);

    // Churn
    cell = row.createCell(cellNum++);
    cell.setCellValue(conversion.getChurn() != null ? conversion.getChurn() : 0);
    cell.setCellStyle(dataStyle);

    // Amount Spent (convert from minor to major - divide by 100)
    cell = row.createCell(cellNum++);
    double amountSpent = 0;
    if (conversion.getAmountSpent() != null) {
      try {
        amountSpent = Double.parseDouble(conversion.getAmountSpent()) / 100.0;
      } catch (NumberFormatException e) {
        amountSpent = 0;
      }
    }
    cell.setCellValue(amountSpent);
    cell.setCellStyle(currencyStyle);

    // CPA
    cell = row.createCell(cellNum);
    cell.setCellValue(conversion.getCPA() != null ? conversion.getCPA() : 0);
    cell.setCellStyle(currencyStyle);
  }

  private void createPublisherConvSummaryRow(
      Sheet sheet,
      List<PublisherConversionsDTO> conversions,
      int rowNum,
      CellStyle headerStyle,
      CellStyle currencyStyle) {
    // Add an empty row before summary
    rowNum++;
    Row summaryRow = sheet.createRow(rowNum);

    // Calculate totals
    long totalConversions =
        conversions.stream()
            .mapToLong(c -> c.getConversions() != null ? c.getConversions() : 0)
            .sum();
    long totalChurn =
        conversions.stream().mapToLong(c -> c.getChurn() != null ? c.getChurn() : 0).sum();
    double totalAmountSpent =
        conversions.stream()
            .mapToDouble(
                c -> {
                  if (c.getAmountSpent() != null) {
                    try {
                      return Double.parseDouble(c.getAmountSpent()) / 100.0;
                    } catch (NumberFormatException e) {
                      return 0;
                    }
                  }
                  return 0;
                })
            .sum();

    // Summary label
    Cell cell = summaryRow.createCell(0);
    cell.setCellValue("TOTAL");
    cell.setCellStyle(headerStyle);

    // Total Conversions
    cell = summaryRow.createCell(1);
    cell.setCellValue(totalConversions);
    cell.setCellStyle(headerStyle);

    // Total Churn
    cell = summaryRow.createCell(2);
    cell.setCellValue(totalChurn);
    cell.setCellStyle(headerStyle);

    // Total Amount Spent
    cell = summaryRow.createCell(3);
    cell.setCellValue(totalAmountSpent);
    cell.setCellStyle(headerStyle);
  }

  // ==================== Advertiser Conversions CPA Breakdown Export ====================

  private static final String[] CPA_BREAKDOWN_HEADERS = {
    "Campaign Name",
    "Country",
    "Conversions",
    "Amount Spent",
    "Churn"
  };

  @Override
  public ByteArrayOutputStream exportAdvertiserConversionsCpaBreakdownToExcel(
      List<AdvertiserConversionCpaBreakdownDTO> conversions) {
    try (Workbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

      // Group conversions by advertiser name
      Map<String, List<AdvertiserConversionCpaBreakdownDTO>> conversionsByAdvertiser =
          conversions.stream()
              .collect(
                  Collectors.groupingBy(AdvertiserConversionCpaBreakdownDTO::getAdvertiserName));

      // Create styles
      CellStyle headerStyle = createHeaderStyle(workbook);
      CellStyle dataStyle = createDataStyle(workbook);
      CellStyle currencyStyle = createCurrencyStyle(workbook);

      // Create a sheet for each advertiser
      for (Map.Entry<String, List<AdvertiserConversionCpaBreakdownDTO>> entry :
          conversionsByAdvertiser.entrySet()) {
        String advertiserName = entry.getKey();
        List<AdvertiserConversionCpaBreakdownDTO> advertiserConversions = entry.getValue();

        // Sanitize sheet name
        String sheetName = sanitizeSheetName(advertiserName);
        Sheet sheet = workbook.createSheet(sheetName);

        // Create header row
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < CPA_BREAKDOWN_HEADERS.length; i++) {
          Cell cell = headerRow.createCell(i);
          cell.setCellValue(CPA_BREAKDOWN_HEADERS[i]);
          cell.setCellStyle(headerStyle);
        }

        // Populate data rows
        int rowNum = 1;
        for (AdvertiserConversionCpaBreakdownDTO conversion : advertiserConversions) {
          Row row = sheet.createRow(rowNum++);
          populateCpaBreakdownDataRow(row, conversion, dataStyle, currencyStyle);
        }

        // Add summary row
        createCpaBreakdownSummaryRow(
            sheet, advertiserConversions, rowNum, headerStyle, currencyStyle);

        // Auto-size columns
        for (int i = 0; i < CPA_BREAKDOWN_HEADERS.length; i++) {
          sheet.autoSizeColumn(i);
        }
      }

      // If no data, create an empty sheet with headers
      if (conversionsByAdvertiser.isEmpty()) {
        Sheet sheet = workbook.createSheet("No Data");
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < CPA_BREAKDOWN_HEADERS.length; i++) {
          Cell cell = headerRow.createCell(i);
          cell.setCellValue(CPA_BREAKDOWN_HEADERS[i]);
          cell.setCellStyle(headerStyle);
        }
      }

      workbook.write(outputStream);
      return outputStream;

    } catch (IOException e) {
      log.error("Error generating CPA Breakdown Excel report: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to generate CPA Breakdown Excel report", e);
    }
  }

  private void populateCpaBreakdownDataRow(
      Row row,
      AdvertiserConversionCpaBreakdownDTO conversion,
      CellStyle dataStyle,
      CellStyle currencyStyle) {
    int cellNum = 0;

    // Campaign Name
    Cell cell = row.createCell(cellNum++);
    cell.setCellValue(conversion.getCampaignName() != null ? conversion.getCampaignName() : "");
    cell.setCellStyle(dataStyle);

    // Country
    cell = row.createCell(cellNum++);
    cell.setCellValue(conversion.getCountry() != null ? conversion.getCountry() : "");
    cell.setCellStyle(dataStyle);

    // Conversions
    cell = row.createCell(cellNum++);
    cell.setCellValue(conversion.getConversions() != null ? conversion.getConversions() : 0);
    cell.setCellStyle(dataStyle);

    // Amount Spent
    cell = row.createCell(cellNum++);
    cell.setCellValue(conversion.getAmountSpent() != null ? conversion.getAmountSpent() : 0);
    cell.setCellStyle(currencyStyle);

    // Churn
    cell = row.createCell(cellNum);
    cell.setCellValue(conversion.getChurn() != null ? conversion.getChurn() : 0);
    cell.setCellStyle(dataStyle);
  }

  private void createCpaBreakdownSummaryRow(
      Sheet sheet,
      List<AdvertiserConversionCpaBreakdownDTO> conversions,
      int rowNum,
      CellStyle headerStyle,
      CellStyle currencyStyle) {
    // Add an empty row before summary
    rowNum++;
    Row summaryRow = sheet.createRow(rowNum);

    // Calculate totals
    long totalConversions =
        conversions.stream()
            .mapToLong(c -> c.getConversions() != null ? c.getConversions() : 0)
            .sum();
    long totalChurn =
        conversions.stream().mapToLong(c -> c.getChurn() != null ? c.getChurn() : 0).sum();
    double totalAmountSpent =
        conversions.stream()
            .mapToDouble(c -> c.getAmountSpent() != null ? c.getAmountSpent() : 0)
            .sum();

    // Summary label
    Cell cell = summaryRow.createCell(0);
    cell.setCellValue("TOTAL");
    cell.setCellStyle(headerStyle);

    // Country - empty
    cell = summaryRow.createCell(1);
    cell.setCellStyle(headerStyle);

    // Total Conversions
    cell = summaryRow.createCell(2);
    cell.setCellValue(totalConversions);
    cell.setCellStyle(headerStyle);

    // Total Amount Spent
    cell = summaryRow.createCell(3);
    cell.setCellValue(totalAmountSpent);
    cell.setCellStyle(headerStyle);

    // Total Churn
    cell = summaryRow.createCell(4);
    cell.setCellValue(totalChurn);
    cell.setCellStyle(headerStyle);
  }

  @Override
  public ByteArrayOutputStream exportAdminChurnReportToExcel(List<AdminChurnReportDto> rows) {
    try (Workbook workbook = new XSSFWorkbook()) {
      CellStyle headerStyle = workbook.createCellStyle();
      Font headerFont = workbook.createFont();
      headerFont.setBold(true);
      headerStyle.setFont(headerFont);
      headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
      headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
      headerStyle.setBorderTop(BorderStyle.THIN);
      headerStyle.setBorderBottom(BorderStyle.THIN);
      headerStyle.setBorderLeft(BorderStyle.THIN);
      headerStyle.setBorderRight(BorderStyle.THIN);

      CellStyle totalStyle = workbook.createCellStyle();
      Font totalFont = workbook.createFont();
      totalFont.setBold(true);
      totalStyle.setFont(totalFont);

      String[] headers = {
          "Acquisition Day", "Marketer ID", "Marketer Name", "Source ID", "Campaign",
          "Total Acquired (Publisher)", "Advertiser Hook Received",
          "Total Churned", "Total Survived", "Churn %", "Amount Spent"
      };

      Map<String, List<AdminChurnReportDto>> byPublisher =
          rows.stream().collect(Collectors.groupingBy(AdminChurnReportDto::getPublisherId));

      for (Map.Entry<String, List<AdminChurnReportDto>> entry : byPublisher.entrySet()) {
        List<AdminChurnReportDto> pubRows = entry.getValue();
        String publisherName = pubRows.get(0).getPublisherName();
        String sheetName = publisherName.replaceAll("[\\\\/*?\\[\\]:]", "");
        if (sheetName.length() > 31) sheetName = sheetName.substring(0, 31);

        Sheet sheet = workbook.createSheet(sheetName);
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
          Cell cell = headerRow.createCell(i);
          cell.setCellValue(headers[i]);
          cell.setCellStyle(headerStyle);
        }

        long pubAcquired = 0, pubChurned = 0;
        double pubAmountSpent = 0;
        int rowNum = 1;
        for (AdminChurnReportDto r : pubRows) {
          Row row = sheet.createRow(rowNum++);
          row.createCell(0).setCellValue(r.getAcquisitionDay().toString());
          row.createCell(1).setCellValue(r.getPublisherId());
          row.createCell(2).setCellValue(r.getPublisherName());
          row.createCell(3).setCellValue(r.getSourceId() != null ? r.getSourceId() : "");
          row.createCell(4).setCellValue(r.getCampaignName() != null ? r.getCampaignName() : "");
          row.createCell(5).setCellValue(r.getTotalAcquired());
          row.createCell(6).setCellValue(r.getTotalAdvertiserHookReceived());
          row.createCell(7).setCellValue(r.getTotalChurned());
          row.createCell(8).setCellValue(r.getTotalSurvived());
          row.createCell(9).setCellValue(r.getChurnPercent());
          row.createCell(10).setCellValue(r.getAmountSpent());
          pubAcquired += r.getTotalAcquired();
          pubChurned += r.getTotalChurned();
          pubAmountSpent += r.getAmountSpent();
        }

        long pubSurvived = pubAcquired - pubChurned;
        long pubAdvHook = pubRows.stream().mapToLong(AdminChurnReportDto::getTotalAdvertiserHookReceived).sum();
        String pubChurnPct = pubAcquired == 0 ? "0.00%"
            : String.format("%.2f%%", (pubChurned * 100.0) / pubAcquired);
        Row totalRow = sheet.createRow(rowNum);
        totalRow.createCell(0).setCellValue("TOTAL");
        totalRow.createCell(5).setCellValue(pubAcquired);
        totalRow.createCell(6).setCellValue(pubAdvHook);
        totalRow.createCell(7).setCellValue(pubChurned);
        totalRow.createCell(8).setCellValue(pubSurvived);
        totalRow.createCell(9).setCellValue(pubChurnPct);
        totalRow.createCell(10).setCellValue(pubAmountSpent);
        for (int i = 0; i <= 10; i++) {
          Cell c = totalRow.getCell(i);
          if (c == null) c = totalRow.createCell(i);
          c.setCellStyle(totalStyle);
        }
        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
      }

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      workbook.write(out);
      return out;
    } catch (IOException e) {
      throw new RuntimeException("Failed to generate churn report Excel", e);
    }
  }

}
