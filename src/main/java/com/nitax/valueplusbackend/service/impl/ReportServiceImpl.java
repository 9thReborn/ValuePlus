package com.nitax.valueplusbackend.service.impl;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

import com.nitax.valueplusbackend.domain.*;
import com.nitax.valueplusbackend.dto.*;
import com.nitax.valueplusbackend.service.*;
import com.univocity.parsers.common.processor.BeanWriterProcessor;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
  private static final Logger log = LoggerFactory.getLogger(ReportServiceImpl.class);
  private final NotificationService notificationService;
  private final EmailService emailService;
  private final PublisherService publisherService;
  private final AdvertiserService advertiserService;
  private final CampaignService campaignService;
  private final PublisherCampaignService publisherCampaignService;

  public void genrateDailyUnsubscribersReportForPubs() throws IOException {
    List<Publisher> publishers = publisherService.findAll();
    List<File> reportFiles = new ArrayList<>();

    for (Publisher publisher : publishers) {
      List<NotificationDto> churnNotifications =
          notificationService.getDailyUnsubscribersForPublisher(publisher.getPubId());
      if (churnNotifications.isEmpty()) {
        log.info("No churn for publisher {}", publisher.getName());
        continue;
      }
      List<NotificationDto> postbackNotifications =
          notificationService.getDailyNotificationsForPublisher(publisher.getPubId());

      long totlsubscribers = postbackNotifications.size();
      long totalUnsubscribers = churnNotifications.size();
      double totalChurnCost =
          churnNotifications.stream().mapToDouble(NotificationDto::getCpa).sum();
      long churnPercent = (totalUnsubscribers / totlsubscribers) * 100;
      String churnGrade = getChurnGrade(churnPercent);

      // build line for supplementary stats
      churnNotifications.add(
          NotificationDto.builder()
              .campaign_name(null)
              .user_joined_date(null)
              .status(null)
              .click_id(null)
              .source_id(null)
              .churnGrade(null)
              .churnGrade(null)
              .duration(null)
              .campaign_name(null)
              .cpa(null)
              .build());
      churnNotifications.add(
          NotificationDto.builder()
              .campaign_name(null)
              .user_joined_date(null)
              .status(null)
              .click_id(null)
              .source_id(null)
              .churnGrade(null)
              .churnGrade(null)
              .duration(null)
              .campaign_name(null)
              .cpa(null)
              .build());

      churnNotifications.add(
          NotificationDto.builder()
              .click_id("total churn cost")
              .churnGrade("Churn Grade")
              .source_id("Churn Percent")
              .campaign_name("total unsubscribers")
              .build());

      churnNotifications.add(
          NotificationDto.builder()
              .click_id(String.valueOf(totalChurnCost))
              .churnGrade(churnGrade)
              .source_id(String.valueOf(churnPercent))
              .campaign_name(String.valueOf(totalUnsubscribers))
              .build());

      Path report =
          Paths.get(
              "./reports/daily/daily unsubscribers report for "
                  + publisher.getIdentifier()
                  + ".csv");

      generateChurnReport(report, churnNotifications);
      if (Files.size(report) > 0) {
        reportFiles.add(report.toFile());
        // send email to publisher
        emailService.sendDailyChurnReportToPublisher(report.toFile(), publisher.getEmail());
      }
    }

    if (!reportFiles.isEmpty()) {
      //      emailService.sendDailyChurnReport(reportFiles);
    }

    // Delete all reports after sending them
    for (File reportFile : reportFiles) {
      Files.deleteIfExists(reportFile.toPath());
    }
  }

  private String getChurnGrade(long churnPercent) {
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

  @Override
  public void genrateWeeklyUnsubscribersReport() throws IOException {
    List<Publisher> publishers = publisherService.findAll();
    List<File> reportFiles = new ArrayList<>();

    for (Publisher publisher : publishers) {
      List<NotificationDto> churnNotifications =
          notificationService.getWeeklyUnsubscribersForPublisher(publisher.getPubId());
      if (churnNotifications.isEmpty()) {
        log.info("No churn for publisher {}", publisher.getName());
        continue;
      }
      log.info("Churn found for publisher {}", publisher.getName());
      // set revenue to zero
      for (NotificationDto notificationDto : churnNotifications) {
        notificationDto.setRevenue(0.0);
      }

      List<NotificationDto> postbackNotifications =
          notificationService.getWeeklyNotificationsForPublisher(publisher.getPubId());

      long totlsubscribers = postbackNotifications.size();
      long totalUnsubscribers = churnNotifications.size();
      double totalChurnCost =
          churnNotifications.stream().mapToDouble(NotificationDto::getCpa).sum();
      long churnPercent = (totalUnsubscribers / totlsubscribers) * 100;
      String churnGrade = getChurnGrade(churnPercent);

      // build line for supplementary stats
      churnNotifications.add(
          NotificationDto.builder()
              .campaign_name(null)
              .user_joined_date(null)
              .status(null)
              .click_id(null)
              .source_id(null)
              .churnGrade(null)
              .churnGrade(null)
              .duration(null)
              .campaign_name(null)
              .cpa(null)
              .build());
      churnNotifications.add(
          NotificationDto.builder()
              .campaign_name(null)
              .user_joined_date(null)
              .status(null)
              .click_id(null)
              .source_id(null)
              .churnGrade(null)
              .churnGrade(null)
              .duration(null)
              .campaign_name(null)
              .cpa(null)
              .build());

      churnNotifications.add(
          NotificationDto.builder()
              .click_id("total churn cost")
              .churnGrade("Churn Grade")
              .source_id("Churn Percent")
              .campaign_name("total unsubscribers")
              .build());

      churnNotifications.add(
          NotificationDto.builder()
              .click_id(String.valueOf(totalChurnCost))
              .churnGrade(churnGrade)
              .source_id(String.valueOf(churnPercent))
              .campaign_name(String.valueOf(totalUnsubscribers))
              .build());

      Path report =
          Paths.get(
              "./reports/weekly/weekly unsubscribers report for " + publisher.getName() + ".csv");

      generateChurnReport(report, churnNotifications);
      if (!churnNotifications.isEmpty() && Files.size(report) > 0) {
        emailService.sendWeeklyChurnReportToPublisher(
            report.toFile(),
            publisher.getName(),
            LocalDate.now().minusDays(6).toString(),
            publisher.getEmail());
        reportFiles.add(report.toFile());
      }
    }

    //    if (!reportFiles.isEmpty()) {
    //      emailService.sendWeeklyChurnReport(reportFiles);
    //    }

    // Delete all reports after sending them
    for (File reportFile : reportFiles) {
      Files.deleteIfExists(reportFile.toPath());
    }
  }

  @Override
  public void sendMonthlyCostReportToAdvertisers() throws IOException {
    List<Advertiser> advertisers = advertiserService.findAll();

    for (Advertiser advertiser : advertisers) {
      double totalCost = 0.0;
      long totalAcquision = 0;
      List<Campaign> advertiserCampaign = advertiserService.getCampaigns(advertiser.getId());
      List<File> reportFiles = new ArrayList<>();
      List<CampaignCostReportDto> reportList = new ArrayList<>();
      for (Campaign campaign : advertiserCampaign) {
        double campaignCost;
        long campaignAcquisition;
        List<NotificationDto> notifications =
            notificationService.getMonthlyNotificationsForCampaign(campaign.getCampaignId());
        campaignAcquisition = notifications.size();
        campaignCost = notifications.stream().mapToDouble(NotificationDto::getRevenue).sum();
        BigDecimal roundedCampaignCost =
            BigDecimal.valueOf(campaignCost)
                .setScale(2, RoundingMode.HALF_EVEN)
                .stripTrailingZeros();

        totalCost += campaignCost;
        totalAcquision += campaignAcquisition;

        CampaignCostReportDto report =
            CampaignCostReportDto.builder()
                .campaignName(campaign.getName())
                .campaignCost("USD" + roundedCampaignCost.doubleValue())
                .campaignStatus(campaign.getStatus())
                .totalAcquisition(campaignAcquisition)
                .build();

        reportList.add(report);
      }
      Path reportFile =
          Paths.get("./reports/monthly/monthly_cost_for_" + advertiser.getBusinessName() + ".csv");

      BigDecimal roundedCampaignCost =
          BigDecimal.valueOf(totalCost).setScale(2, RoundingMode.HALF_EVEN).stripTrailingZeros();
      CampaignCostReportDto totalCostLine =
          CampaignCostReportDto.builder()
              .campaignName("Total Cost")
              .totalAcquisition(totalAcquision)
              .campaignCost("USD " + roundedCampaignCost.doubleValue())
              .build();
      reportList.add(totalCostLine);

      generateCampaignReport(reportFile, reportList);
      if (!reportList.isEmpty() && Files.size(reportFile) > 0) {
        reportFiles.add(reportFile.toFile());
      }

      if (!reportFiles.isEmpty()) {
        emailService.sendMonthlyCostToAdvertiser(reportFiles, advertiser.getEmail());
      }

      // Delete all reports after sending them
      for (File existingReportFile : reportFiles) {
        Files.deleteIfExists(existingReportFile.toPath());
      }
    }
  }

  @Override
  public void sendAmountOwedReportToPublisherMonthly() throws IOException {
    List<Publisher> publishers = publisherService.findAll();
    List<File> reportFiles = new ArrayList<>();

    for (Publisher publisher : publishers) {
      double totalCost = 0.0;
      long totalAcquision = 0;
      long totalBadAcquisition = 0;
      double totalBadAcquisitionCost = 0.0;
      List<PublisherConversionReportDto> reports =
          notificationService.getMonthlyNotificationsForPublishers(publisher.getPubId());

      Path reportFile =
          Paths.get("./reports/monthly/conversions for " + publisher.getName() + ".csv");

      for (PublisherConversionReportDto report : reports) {
        totalCost += report.getTotalCost();
        totalAcquision += report.getTotalGoodAcquisition();
        totalBadAcquisition += report.getTotalBadAcquisition();
        totalBadAcquisitionCost += report.getTotalBadAcquisitionCost();
      }
      reports.add(
          PublisherConversionReportDto.builder()
              .campaignName("total")
              .totalGoodAcquisition(totalAcquision)
              .totalCost(totalCost)
              .totalBadAcquisition(totalBadAcquisition)
              .totalBadAcquisitionCost(totalBadAcquisitionCost)
              .build());
      generateConversionReportsForCps(reportFile, reports);

      if (!reports.isEmpty() && Files.size(reportFile) > 0) {
        reportFiles.add(reportFile.toFile());
        emailService.sendAmountOwedReportToPublisherMonthly(reportFile, publisher.getEmail());
      }
    }

    // Delete all reports after sending them
    for (File reportFile : reportFiles) {
      Files.deleteIfExists(reportFile.toPath());
    }
  }

  @Override
  public void sendDailySummaryReportsToAdvertisers() throws IOException {

    List<Advertiser> advertisers = advertiserService.findAll();

    List<File> reportFiles = new ArrayList<>();
    for (Advertiser advertiser : advertisers) {
      Path reportFile =
          Paths.get(
              "./reports/daily/daily campaign performance summary report for "
                  + advertiser.getBusinessName()
                  + " "
                  + LocalDate.now().minusDays(1)
                  + ".csv");
      long numOfCampaigns = 0L;
      long numOfActiveCampaigns = 0;
      long numOfConversions = 0;
      //      double totalCost = 0.00;
      List<CampaignCostReportDto> campaignReportList = new ArrayList<>();

      Long conversionsCount =
          notificationService.getCountOfDailyNotificationsForAdvertisers(advertiser);
      numOfConversions = conversionsCount != null ? conversionsCount : 0;

      Long activeCampaignsCount = advertiserService.getNumberOfActiveCampaigns(advertiser);
      numOfActiveCampaigns = activeCampaignsCount != null ? activeCampaignsCount : 0;

      Long campaignsCount = advertiserService.getNumberOfCampaigns(advertiser);
      numOfCampaigns = campaignsCount != null ? campaignsCount : 0;

      List<Campaign> advertiserCampaign = advertiserService.getCampaigns(advertiser.getId());
      for (Campaign campaign : advertiserCampaign) {
        double campaignCost;
        long campaignAcquisition;
        List<NotificationDto> notifications =
            notificationService.getDailyNotificationsForCampaign(campaign.getCampaignId());
        campaignAcquisition = notifications.size();
        //        campaignCost = campaignAcquisition * campaign.getCostPerUser();
        //        BigDecimal roundedCampaignCost =
        //            BigDecimal.valueOf(campaignCost)
        //                .setScale(2, RoundingMode.HALF_EVEN)
        //                .stripTrailingZeros();

        //        totalCost += roundedCampaignCost.doubleValue();

        CampaignCostReportDto report =
            CampaignCostReportDto.builder()
                .campaignName(campaign.getName())
                //                .campaignCost("USD" + roundedCampaignCost.doubleValue())
                .campaignStatus(campaign.getStatus())
                .totalAcquisition(campaignAcquisition)
                .build();

        campaignReportList.add(report);
      }

      AdvertiserSummaryReportDto summaryReport =
          AdvertiserSummaryReportDto.builder()
              .numberOfCampaigns(numOfCampaigns)
              .numberOfActiveCampaigns(numOfActiveCampaigns)
              .numberOfConversions(numOfConversions)
              //              .totalCost(
              //                  "USD"
              //                      + BigDecimal.valueOf(totalCost)
              //                          .setScale(2, RoundingMode.HALF_EVEN)
              //                          .stripTrailingZeros()
              //                          .doubleValue())
              .campaignCostReports(campaignReportList)
              .build();

      generateAdvertiserSummaryReport(reportFile, summaryReport);

      if (Files.size(reportFile) > 0) {
        reportFiles.add(reportFile.toFile());
      }
      if (!reportFiles.isEmpty()) {
        emailService.sendDailySummaryReportToAdvertiser(reportFile, advertiser.getEmail());
      }
    }

    // Delete all reports after sending them
    for (File reportFile : reportFiles) {
      Files.deleteIfExists(reportFile.toPath());
    }
  }

  @Override
  public void sendMonthlySummaryReportsToAdvertisers() throws IOException {

    List<Advertiser> advertisers = advertiserService.findAll();

    List<File> reportFiles = new ArrayList<>();

    for (Advertiser advertiser : advertisers) {
      String monthName =
          LocalDateTime.now()
              .minusMonths(1)
              .getMonth()
              .getDisplayName(TextStyle.FULL, Locale.ENGLISH);
      Path reportFile =
          Paths.get(
              "./reports/monthly/monthly campaign performance summary report for "
                  + advertiser.getBusinessName()
                  + " "
                  + monthName
                  + " .csv");
      long numOfCampaigns = 0L;
      long numOfActiveCampaigns = 0;
      long numOfConversions = 0;
      //      double totalCost = 0.00;
      List<CampaignCostReportDto> campaignReportList = new ArrayList<>();

      Long conversionsCount =
          notificationService.getCountOfMonthlyNotificationsForAdvertisers(advertiser);
      numOfConversions = conversionsCount != null ? conversionsCount : 0;

      Long activeCampaignsCount = advertiserService.getNumberOfActiveCampaigns(advertiser);
      numOfActiveCampaigns = activeCampaignsCount != null ? activeCampaignsCount : 0;

      Long campaignsCount = advertiserService.getNumberOfCampaigns(advertiser);
      numOfCampaigns = campaignsCount != null ? campaignsCount : 0;

      List<Campaign> advertiserCampaign = advertiserService.getActiveCampaigns(advertiser);
      for (Campaign campaign : advertiserCampaign) {
        double campaignCost;
        long campaignAcquisition;
        List<NotificationDto> notifications =
            notificationService.getMonthlyNotificationsForCampaign(campaign.getCampaignId());
        campaignAcquisition = notifications.size();
        campaignCost = campaignAcquisition * campaign.getCostPerUser();
        BigDecimal roundedCampaignCost =
            BigDecimal.valueOf(campaignCost)
                .setScale(2, RoundingMode.HALF_EVEN)
                .stripTrailingZeros();

        //        totalCost += roundedCampaignCost.doubleValue();

        CampaignCostReportDto report =
            CampaignCostReportDto.builder()
                .campaignName(campaign.getName())
                .campaignCost("USD" + roundedCampaignCost.doubleValue())
                .totalAcquisition(campaignAcquisition)
                .campaignStatus(campaign.getStatus())
                .build();

        campaignReportList.add(report);
      }

      AdvertiserSummaryReportDto summaryReport =
          AdvertiserSummaryReportDto.builder()
              .numberOfCampaigns(numOfCampaigns)
              .numberOfActiveCampaigns(numOfActiveCampaigns)
              .numberOfConversions(numOfConversions)
              //              .totalCost(
              //                  "USD"
              //                      + BigDecimal.valueOf(totalCost)
              //                          .setScale(2, RoundingMode.HALF_EVEN)
              //                          .stripTrailingZeros()
              //                          .doubleValue())
              .campaignCostReports(campaignReportList)
              .build();

      generateAdvertiserSummaryReport(reportFile, summaryReport);

      if (Files.size(reportFile) > 0) {
        reportFiles.add(reportFile.toFile());
      }
      if (!reportFiles.isEmpty()) {
        emailService.sendMonthlySummaryReportToAdvertiser(reportFile, advertiser.getEmail());
      }
    }

    // Delete all reports after sending them
    for (File reportFile : reportFiles) {
      Files.deleteIfExists(reportFile.toPath());
    }
  }

  @Override
  public void genrateWeeklyPerformanceSummaryReport() throws IOException {
    List<Advertiser> advertisers = advertiserService.findAll();

    long totalNumberOfAdvertisers = advertisers.size();
    long totalNumOfCampaigns = 0;
    long totalNumOfActiveCampaigns = 0;
    long totalAcquisition = 0;
    long totalGoodAcquisition = 0;
    long totalChurnCount = 0;
    //    long churnPercent = 0;
    //    String churnGrade = "";
    double totalChurnCost = 0.00;
    double totalRevenue = 0.00;
    List<Campaign> campaigns = campaignService.getAllCampaigns();

    totalNumOfCampaigns = campaigns.size();

    for (Campaign campaign : campaigns) {
      long campaignAcquisition = 0;
      long campaignGoodAcquisition = 0;
      long campaignChurnCount = 0;
      double vpRevenue = 0.00;
      double campaignChurnCost = 0.00;

      if (campaign.getStatus().equals("ACTIVE")) {
        totalNumOfActiveCampaigns++;
      }
      List<NotificationDto> notifications =
          notificationService.getWeeklyNotificationsForCampaign(campaign.getCampaignId());
      campaignAcquisition = notifications.size();
      vpRevenue = notifications.stream().mapToDouble(NotificationDto::getRevenue).sum();

      BigDecimal roundedCampaignCost =
          BigDecimal.valueOf(vpRevenue).setScale(2, RoundingMode.HALF_EVEN).stripTrailingZeros();
      totalRevenue += roundedCampaignCost.doubleValue();
      totalAcquisition += campaignAcquisition;

      for (NotificationDto notification : notifications) {
        if (notification.getStatus().equals(Notification.NotificationStatus.UNSUBSCRIBED)
            && notification.getDuration() <= 7) {
          campaignChurnCount++;
        } else {
          campaignGoodAcquisition++;
        }
      }
      campaignChurnCost =
          notifications.stream()
              .filter(
                  n ->
                      n.getDuration() <= 7
                          && n.getStatus().equals(Notification.NotificationStatus.UNSUBSCRIBED))
              .mapToDouble(NotificationDto::getCpa)
              .sum();
      BigDecimal roundedCampaignChurnCost =
          BigDecimal.valueOf(campaignChurnCost)
              .setScale(2, RoundingMode.HALF_EVEN)
              .stripTrailingZeros();

      totalChurnCost += roundedCampaignChurnCost.doubleValue();
      totalChurnCount += campaignChurnCount;
      totalGoodAcquisition += campaignGoodAcquisition;
    }

    //    churnPercent = (totalChurnCount / totalAcquisition) * 100;
    //    churnGrade = getChurnGrade(churnPercent);

    ExecSummaryReport execSummaryReport =
        getExecSummaryReport(
            totalNumberOfAdvertisers,
            totalNumOfCampaigns,
            totalNumOfActiveCampaigns,
            totalAcquisition,
            totalGoodAcquisition,
            totalChurnCount,
            totalChurnCost,
            totalRevenue);

    ZoneId zoneId = ZoneId.of("UTC");

    LocalDate now = LocalDate.now(zoneId);
    LocalDate startOfWeek =
        now.minusDays(now.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue());
    LocalDate endOfWeek = startOfWeek.plusDays(6);

    Path reportFile =
        Paths.get(
            "./reports/weekly/valueplus weekly performance summary report for "
                + startOfWeek
                + " - "
                + endOfWeek
                + " .csv");

    generateExecSummaryReport(reportFile, execSummaryReport);

    emailService.sendWeeklyExecSummaryReport(reportFile);

    Files.deleteIfExists(reportFile);
  }

  @Override
  public void genrateMonthlyPerformanceSummaryReport() throws IOException {
    List<Advertiser> advertisers = advertiserService.findAll();

    long totalNumberOfAdvertisers = advertisers.size();
    long totalNumOfCampaigns = 0;
    long totalNumOfActiveCampaigns = 0;
    long totalAcquisition = 0;
    long totalGoodAcquisition = 0;
    long totalChurnCount = 0;
    double totalChurnCost = 0.00;
    double totalRevenue = 0.00;
    //    long churnPercent = 0;
    //    String churnGrade = "";
    List<Campaign> campaigns = campaignService.getAllCampaigns();

    totalNumOfCampaigns = campaigns.size();

    for (Campaign campaign : campaigns) {
      long campaignAcquisition = 0;
      long campaignGoodAcquisition = 0;
      long campaignChurnCount = 0;
      double vpRevenue = 0.00;
      double campaignChurnCost = 0.00;

      if (campaign.getStatus().equals("ACTIVE")) {
        totalNumOfActiveCampaigns++;
      }
      List<NotificationDto> notifications =
          notificationService.getMonthlyNotificationsForCampaign(campaign.getCampaignId());
      campaignAcquisition = notifications.size();
      vpRevenue = notifications.stream().mapToDouble(NotificationDto::getRevenue).sum();

      BigDecimal roundedCampaignCost =
          BigDecimal.valueOf(vpRevenue).setScale(2, RoundingMode.HALF_EVEN).stripTrailingZeros();
      totalRevenue += roundedCampaignCost.doubleValue();
      totalAcquisition += campaignAcquisition;

      for (NotificationDto notification : notifications) {
        if (notification.getStatus().equals(Notification.NotificationStatus.UNSUBSCRIBED)
            && notification.getDuration() <= 7) {
          campaignChurnCount++;
        } else {
          campaignGoodAcquisition++;
        }
      }
      campaignChurnCost =
          notifications.stream()
              .filter(
                  n ->
                      n.getDuration() <= 7
                          && n.getStatus().equals(Notification.NotificationStatus.UNSUBSCRIBED))
              .mapToDouble(NotificationDto::getCpa)
              .sum();
      BigDecimal roundedCampaignChurnCost =
          BigDecimal.valueOf(campaignChurnCost)
              .setScale(2, RoundingMode.HALF_EVEN)
              .stripTrailingZeros();

      totalChurnCost += roundedCampaignChurnCost.doubleValue();
      totalChurnCount += campaignChurnCount;
      totalGoodAcquisition += campaignGoodAcquisition;
    }

    //    if (totalChurnCount == 0 && totalAcquisition == 0) {
    //    } else {
    //      churnPercent = (totalChurnCount / totalAcquisition) * 100;
    //    }
    //    churnGrade = getChurnGrade(churnPercent);

    ExecSummaryReport execSummaryReport =
        getExecSummaryReport(
            totalNumberOfAdvertisers,
            totalNumOfCampaigns,
            totalNumOfActiveCampaigns,
            totalAcquisition,
            totalGoodAcquisition,
            totalChurnCount,
            totalChurnCost,
            totalRevenue);

    String monthName =
        LocalDateTime.now()
            .minusMonths(1)
            .getMonth()
            .getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    Path reportFile =
        Paths.get(
            "./reports/monthly/valueplus monthly performance summary report for "
                + monthName
                + " .csv");

    generateExecSummaryReport(reportFile, execSummaryReport);

    emailService.sendMonthlyExecSummaryReport(reportFile);

    Files.deleteIfExists(reportFile);
  }

  @Override
  public void notifyAdminWhenPubsArentPushingTraffic() {
    // get all publishers
    List<Publisher> publishers = publisherService.findAll();

    // for each publisher, get the last conversion record
    for (Publisher publisher : publishers) {
      Notification lastConversion =
          notificationService.getLastConversionForPublisher(publisher.getPubId());

      // if the last conversion is older than 15 mins, send an email to the admin
      if (lastConversion != null) {
        ZoneId zoneId = ZoneId.of("UTC");
        Instant lastConversionTime = lastConversion.getCreatedDate();
        Instant currentTime = LocalDateTime.now().atZone(zoneId).toInstant();
        Duration duration = Duration.between(lastConversionTime, currentTime);
        if (duration.toMinutes() > 15) {
          log.info(
              "Publisher {} has not pushed traffic for the last 15 minutes", publisher.getName());
          emailService.senddNoConversionNotificationToAdmin(publisher);
        }
      }
    }
  }

  @Override
  public void generateRetentionReport() throws IOException {
    // get all publishers
    List<Publisher> publishers = publisherService.findAll();

    for (Publisher publisher : publishers) {
      List<RetentionReportDto> retentionNotifications =
          notificationService.getWeeklyRetentionForPublisher(publisher.getPubId());

      if (retentionNotifications.isEmpty()) {
        log.info("No retention for publisher {}", publisher.getName());
        continue;
      }

      Path report =
          Paths.get("./reports/retention/retention report for " + publisher.getName() + ".csv");

      generateRetentionReportFile(report, retentionNotifications);

      if (!retentionNotifications.isEmpty() && Files.size(report) > 0) {
        emailService.sendRetentionReportToPublisher(
            report.toFile(), publisher.getName(), publisher.getEmail());
        report.toFile().delete();
      }
    }
  }

  @Override
  public void generateWeeklyPublisherChurnReport() throws IOException {

    List<PubChurnReportDto> pubChurnReportDtoList =
        notificationService.getWeeklyChurnForValueplus();

    if (pubChurnReportDtoList.isEmpty()) {
      log.info("No churn report for VP");
    }

    Path report = Paths.get("./reports/churn/churn report.csv");

    generatePublisherChurnReportFile(report, pubChurnReportDtoList);

    if (!pubChurnReportDtoList.isEmpty() && Files.size(report) > 0) {
      emailService.sendWeeklyChurnReportToValueplus(report.toFile());
      report.toFile().delete();
    }
  }

  @Override
  public void generateDailyPublisherChurnReport() throws IOException {
    List<PubChurnReportDto> pubChurnReportDtoList = notificationService.getDailyChurnForPublisher();

    if (pubChurnReportDtoList.isEmpty()) {
      log.info("No churn report for VP");
    }

    Path report = Paths.get("./reports/churn/daily churn report.csv");

    generatePublisherChurnReportFile(report, pubChurnReportDtoList);

    if (!pubChurnReportDtoList.isEmpty() && Files.size(report) > 0) {
      emailService.sendDailyChurnReportToValueplus(report.toFile());
      report.toFile().delete();
    }
  }

  private void generatePublisherChurnReportFile(
      Path report, List<PubChurnReportDto> pubChurnReportDtoList) throws IOException {
    if (Objects.isNull(pubChurnReportDtoList)) {
      return;
    }

    Files.createDirectories(report.getParent());
    if (!Files.exists(report)) {
      try {
        Files.createFile(report);
      } catch (IOException e) {
        log.error("Error creating churn report file", e);
      }
    }
    try (Writer outputWriter =
        new OutputStreamWriter(Files.newOutputStream(report, CREATE, APPEND))) {

      BeanWriterProcessor<PubChurnReportDto> outProcessor =
          new BeanWriterProcessor<PubChurnReportDto>(PubChurnReportDto.class);
      CsvWriterSettings outSettings = new CsvWriterSettings();

      outSettings.setRowWriterProcessor(outProcessor);
      CsvWriter outWriter = new CsvWriter(outputWriter, outSettings);

      outWriter.writeHeaders();

      for (PubChurnReportDto notification : pubChurnReportDtoList) {
        long churnPercent =
            (notification.getChurnedUsers() / notification.getTotalAcquisition()) * 100;
        String churnGrade = getChurnGrade(churnPercent);
        notification.setChurnGrade(churnGrade);
        outWriter.processRecord(notification);
        outWriter.flush();
      }
      outWriter.close();

    } catch (IOException e) {
      log.error("Error creating churn report file", e);
    }
  }

  private void generateRetentionReportFile(
      Path report, List<RetentionReportDto> retentionNotifications) throws IOException {
    if (Objects.isNull(retentionNotifications)) {
      return;
    }

    Files.createDirectories(report.getParent());
    if (!Files.exists(report)) {
      try {
        Files.createFile(report);
      } catch (IOException e) {
        log.error("Error creating retention report file", e);
      }
    }
    try (Writer outputWriter =
        new OutputStreamWriter(Files.newOutputStream(report, CREATE, APPEND))) {

      BeanWriterProcessor<RetentionReportDto> outProcessor =
          new BeanWriterProcessor<RetentionReportDto>(RetentionReportDto.class);
      CsvWriterSettings outSettings = new CsvWriterSettings();

      outSettings.setRowWriterProcessor(outProcessor);
      CsvWriter outWriter = new CsvWriter(outputWriter, outSettings);

      outWriter.writeHeaders();

      for (RetentionReportDto notification : retentionNotifications) {

        outWriter.processRecord(notification);
        outWriter.flush();
      }
      outWriter.close();

    } catch (IOException e) {
      log.error("Error creating retention report file", e);
    }
  }

  private ExecSummaryReport getExecSummaryReport(
      long totalNumberOfAdvertisers,
      long totalNumOfCampaigns,
      long totalNumOfActiveCampaigns,
      long totalAcquisition,
      long totalGoodAcquisition,
      long totalChurnCount,
      double totalChurnCost,
      double totalRevenue) {

    double income = totalRevenue - totalChurnCount;

    return ExecSummaryReport.builder()
        .totalNumberOfAdvertisers(totalNumberOfAdvertisers)
        .totalNumOfCampaigns(totalNumOfCampaigns)
        .totalNumOfActiveCampaigns(totalNumOfActiveCampaigns)
        .totalAcquisition(totalAcquisition)
        .totalGoodAcquisition(totalGoodAcquisition)
        .totalChurnCount(totalChurnCount)
        .totalRevenue(String.format("USD %.2f", totalRevenue))
        .totalChurnCost(String.format("USD %.2f", totalChurnCost))
        .income(String.format("USD %.2f", income))
        .build();
  }

  private void generateExecSummaryReport(Path reportFile, ExecSummaryReport execSummaryReport)
      throws IOException {
    if (Objects.isNull(execSummaryReport)) {
      return;
    }

    Files.createDirectories(reportFile.getParent());
    if (!Files.exists(reportFile)) {
      try {
        Files.createFile(reportFile);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    try (Writer outputWriter =
        new OutputStreamWriter(Files.newOutputStream(reportFile, CREATE, APPEND))) {
      CsvWriterSettings settings = new CsvWriterSettings();
      settings.setHeaderWritingEnabled(false); // Disable writing header row

      CsvWriter csvWriter = new CsvWriter(outputWriter, settings);

      // Write first section header row
      csvWriter.writeHeaders("Summary Report");

      // Write summary report data
      csvWriter.writeRow(
          "Total number of advertisers", execSummaryReport.getTotalNumberOfAdvertisers());
      csvWriter.writeRow("Total number of campaigns", execSummaryReport.getTotalNumOfCampaigns());
      csvWriter.writeRow(
          "Total number of active campaigns", execSummaryReport.getTotalNumOfActiveCampaigns());
      csvWriter.writeRow("Total number of acquisitions", execSummaryReport.getTotalAcquisition());
      csvWriter.writeRow("Total good acquisitions", execSummaryReport.getTotalGoodAcquisition());
      csvWriter.writeRow(
          "Total revenue (Incl. Advertisers' churn)", execSummaryReport.getTotalRevenue());
      csvWriter.writeRow("Total churn count", execSummaryReport.getTotalChurnCount());
      csvWriter.writeRow("Total churn cost", execSummaryReport.getTotalChurnCost());
      csvWriter.writeRow("Churn grade", execSummaryReport.getChurnGrade());
      csvWriter.writeRow("Churn percent", execSummaryReport.getChurnPercent());
      csvWriter.writeRow("Income", execSummaryReport.getIncome());
    }
  }

  private void generateAdvertiserSummaryReport(
      Path reportFile, AdvertiserSummaryReportDto summaryReport) throws IOException {
    if (Objects.isNull(summaryReport)) {
      return;
    }

    Files.createDirectories(reportFile.getParent());
    if (!Files.exists(reportFile)) {
      try {
        Files.createFile(reportFile);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    try (Writer outputWriter =
        new OutputStreamWriter(Files.newOutputStream(reportFile, CREATE, APPEND))) {
      CsvWriterSettings settings = new CsvWriterSettings();
      settings.setHeaderWritingEnabled(false); // Disable writing header row

      CsvWriter csvWriter = new CsvWriter(outputWriter, settings);

      // Write first section header row
      csvWriter.writeHeaders("Data", "Value");
      // Write first section data
      csvWriter.writeRow("Number of Campaigns", summaryReport.getNumberOfCampaigns());
      csvWriter.writeRow("Number of Active Campaigns", summaryReport.getNumberOfActiveCampaigns());
      csvWriter.writeRow("Number of Conversions", summaryReport.getNumberOfConversions());
      csvWriter.writeRow("Total Cost", summaryReport.getTotalCost());

      // Write a blank line to separate sections
      csvWriter.writeEmptyRow();
      csvWriter.writeEmptyRow();
      csvWriter.writeEmptyRow();

      csvWriter.writeRow("Campaign Name", "Total Conversion", "Campaign Cost", "Campaign Status");

      List<CampaignCostReportDto> campaignCostReports = summaryReport.getCampaignCostReports();
      for (CampaignCostReportDto report : campaignCostReports) {
        csvWriter.writeRow(
            report.getCampaignName(),
            report.getTotalAcquisition(),
            report.getCampaignCost(),
            report.getCampaignStatus());
      }
    }
  }

  private void generateConversionReportsForCps(
      Path reportFile, List<PublisherConversionReportDto> reports) throws IOException {
    if (reports.isEmpty()) {
      return;
    }
    Files.createDirectories(reportFile.getParent());
    if (!Files.exists(reportFile)) {
      try {
        Files.createFile(reportFile);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    try (Writer outputWriter =
        new OutputStreamWriter(Files.newOutputStream(reportFile, CREATE, APPEND))) {

      BeanWriterProcessor<PublisherConversionReportDto> outProcessor =
          new BeanWriterProcessor<>(PublisherConversionReportDto.class);
      CsvWriterSettings outSettings = new CsvWriterSettings();

      outSettings.setRowWriterProcessor(outProcessor);
      CsvWriter outWriter = new CsvWriter(outputWriter, outSettings);

      outWriter.writeHeaders();

      for (PublisherConversionReportDto report : reports) {

        outWriter.processRecord(report);
        outWriter.flush();
      }
      outWriter.close();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void generateChurnReport(Path report, List<NotificationDto> notifications)
      throws IOException {
    if (notifications.isEmpty()) {
      return;
    }

    Files.createDirectories(report.getParent());
    if (!Files.exists(report)) {
      try {
        Files.createFile(report);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    try (Writer outputWriter =
        new OutputStreamWriter(Files.newOutputStream(report, CREATE, APPEND))) {

      BeanWriterProcessor<NotificationDto> outProcessor =
          new BeanWriterProcessor<NotificationDto>(NotificationDto.class);
      CsvWriterSettings outSettings = new CsvWriterSettings();

      outSettings.setRowWriterProcessor(outProcessor);
      CsvWriter outWriter = new CsvWriter(outputWriter, outSettings);

      outWriter.writeHeaders();
      // TODO: stream notifications from DB

      for (NotificationDto notification : notifications) {

        outWriter.processRecord(notification);
        outWriter.flush();
      }
      outWriter.close();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void generateCampaignReport(Path reportFile, List<CampaignCostReportDto> reports)
      throws IOException {
    if (reports.isEmpty()) {
      return;
    }
    Files.createDirectories(reportFile.getParent());
    if (!Files.exists(reportFile)) {
      try {
        Files.createFile(reportFile);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    try (Writer outputWriter =
        new OutputStreamWriter(Files.newOutputStream(reportFile, CREATE, APPEND))) {

      BeanWriterProcessor<CampaignCostReportDto> outProcessor =
          new BeanWriterProcessor<>(CampaignCostReportDto.class);
      CsvWriterSettings outSettings = new CsvWriterSettings();

      outSettings.setRowWriterProcessor(outProcessor);
      CsvWriter outWriter = new CsvWriter(outputWriter, outSettings);

      outWriter.writeHeaders();

      for (CampaignCostReportDto report : reports) {

        outWriter.processRecord(report);
        outWriter.flush();
      }
      outWriter.close();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void generateAndSendAdminChurnReport() throws IOException {
    LocalDate endDay = LocalDate.now(ZoneOffset.UTC).minusDays(1);
    Instant endDate = endDay.atTime(23, 59, 59).toInstant(ZoneOffset.UTC);
    Instant startDate = endDay.minusDays(6).atStartOfDay(ZoneOffset.UTC).toInstant();

    String period = endDay.minusDays(6) + " 00:00 to " + endDay + " 23:59";

    List<AdminChurnReportDto> rows = notificationService.fetchAdminChurnReport(startDate, endDate);

    if (rows.isEmpty()) {
      log.info("No admin churn report data for week ending {}", endDay);
      return;
    }

    Files.createDirectories(Paths.get("./reports/churn/"));

    // Group rows by publisherId
    Map<String, List<AdminChurnReportDto>> byPublisher =
        rows.stream().collect(Collectors.groupingBy(AdminChurnReportDto::getPublisherId));

    CSVFormat format =
        CSVFormat.DEFAULT
            .builder()
            .setHeader(
                "Acquisition Day",
                "Marketer ID",
                "Marketer Name",
                "Source ID",
                "Campaign",
                "Total Acquired",
                "Total Churned 72h",
                "Total Survived 72h",
                "Churn 72h %")
            .build();

    // Build a single Excel workbook — one sheet per publisher
    Path masterPath = Paths.get("./reports/churn/admin-weekly-churn-report.xlsx");
    Files.deleteIfExists(masterPath);
    List<File> pubCsvFiles = new ArrayList<>();

    try (Workbook workbook = new XSSFWorkbook()) {
      String[] headers = {
        "Acquisition Day",
        "Marketer ID",
        "Marketer Name",
        "Source ID",
        "Campaign",
        "Total Acquired (Publisher)",
        "Advertiser Hook Received",
        "Total Churned 72h",
        "Total Survived 72h",
        "Churn 72h %"
      };

      CellStyle headerStyle = workbook.createCellStyle();
      Font headerFont = workbook.createFont();
      headerFont.setBold(true);
      headerStyle.setFont(headerFont);
      headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
      headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
      for (BorderStyle bs : new BorderStyle[] {BorderStyle.THIN}) {
        headerStyle.setBorderTop(bs);
        headerStyle.setBorderBottom(bs);
        headerStyle.setBorderLeft(bs);
        headerStyle.setBorderRight(bs);
      }

      CellStyle totalStyle = workbook.createCellStyle();
      Font totalFont = workbook.createFont();
      totalFont.setBold(true);
      totalStyle.setFont(totalFont);

      for (Map.Entry<String, List<AdminChurnReportDto>> entry : byPublisher.entrySet()) {
        String publisherId = entry.getKey();
        List<AdminChurnReportDto> pubRows = entry.getValue();
        String publisherName = pubRows.get(0).getPublisherName();

        // Sheet name max 31 chars, strip invalid chars
        String sheetName = publisherName.replaceAll("[\\\\/*?\\[\\]:]", "");
        if (sheetName.length() > 31) sheetName = sheetName.substring(0, 31);

        Sheet sheet = workbook.createSheet(sheetName);

        // Header row
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
          Cell cell = headerRow.createCell(i);
          cell.setCellValue(headers[i]);
          cell.setCellStyle(headerStyle);
        }

        long pubAcquired = 0, pubChurned = 0;
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
          pubAcquired += r.getTotalAcquired();
          pubChurned += r.getTotalChurned();
        }

        // Total row
        long pubSurvived = pubAcquired - pubChurned;
        String pubChurnPct =
            pubAcquired == 0
                ? "0.00%"
                : String.format("%.2f%%", (pubChurned * 100.0) / pubAcquired);
        Row totalRow = sheet.createRow(rowNum);
        long pubAdvHook =
            pubRows.stream().mapToLong(AdminChurnReportDto::getTotalAdvertiserHookReceived).sum();
        totalRow.createCell(0).setCellValue("TOTAL");
        totalRow.createCell(5).setCellValue(pubAcquired);
        totalRow.createCell(6).setCellValue(pubAdvHook);
        totalRow.createCell(7).setCellValue(pubChurned);
        totalRow.createCell(8).setCellValue(pubSurvived);
        totalRow.createCell(9).setCellValue(pubChurnPct);
        for (int i = 0; i <= 9; i++) {
          Cell c = totalRow.getCell(i);
          if (c == null) c = totalRow.createCell(i);
          c.setCellStyle(totalStyle);
        }

        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

        // Generate individual CSV for this publisher
        String safeName = publisherName.replaceAll("[^a-zA-Z0-9 _-]", "").trim();
        Path pubCsv = Paths.get("./reports/churn/" + safeName + "-weekly-churn-report.csv");
        Files.deleteIfExists(pubCsv);
        long csvAcquired = 0, csvChurned = 0;
        try (Writer writer = new OutputStreamWriter(Files.newOutputStream(pubCsv, CREATE, APPEND));
            CSVPrinter printer = new CSVPrinter(writer, format)) {
          for (AdminChurnReportDto r : pubRows) {
            printer.printRecord(
                r.getAcquisitionDay(),
                r.getPublisherId(),
                r.getPublisherName(),
                r.getSourceId(),
                r.getCampaignName() != null ? r.getCampaignName() : "",
                r.getTotalAcquired(),
                r.getTotalChurned(),
                r.getTotalSurvived(),
                r.getChurnPercent());
            csvAcquired += r.getTotalAcquired();
            csvChurned += r.getTotalChurned();
          }
          long csvSurvived = csvAcquired - csvChurned;
          String csvChurnPct =
              csvAcquired == 0
                  ? "0.00%"
                  : String.format("%.2f%%", (csvChurned * 100.0) / csvAcquired);
          printer.printRecord(
              "TOTAL", "", "", "", "", csvAcquired, csvChurned, csvSurvived, csvChurnPct);
        }
        pubCsvFiles.add(pubCsv.toFile());

        // Send CSV to publisher
        try {
          Publisher publisher = publisherService.findByPubId(publisherId);
          if (publisher.getEmail() != null && !publisher.getEmail().isBlank()) {
            emailService.sendWeeklyPublisherChurnReportToPublisher(
                pubCsv.toFile(), publisherName, publisher.getEmail(), period);
          }
        } catch (Exception e) {
          log.warn("Could not send report to publisher {}: {}", publisherId, e.getMessage());
        }
      }

      try (var out = Files.newOutputStream(masterPath)) {
        workbook.write(out);
      }
    }

    // Send master Excel + all publisher CSVs to admin
    List<File> adminFiles = new ArrayList<>();
    adminFiles.add(masterPath.toFile());
    adminFiles.addAll(pubCsvFiles);
    emailService.sendAdminWeeklyChurnReportMaster(adminFiles, period);
    adminFiles.forEach(File::delete);
  }

  @Override
  public void sendChurnReportToPublishers(
      List<AdminChurnReportDto> rows, String period, int churnDurationHours) throws IOException {
    if (rows.isEmpty()) return;

    Files.createDirectories(Paths.get("./reports/churn/"));

    String churnLabel = churnDurationHours + "h";
    CSVFormat format =
        CSVFormat.DEFAULT
            .builder()
            .setHeader(
                "Acquisition Day",
                "Marketer ID",
                "Marketer Name",
                "Source ID",
                "Campaign",
                "Total Acquired",
                "Total Churned " + churnLabel,
                "Total Survived " + churnLabel,
                "Churn " + churnLabel + " %")
            .build();

    Map<String, List<AdminChurnReportDto>> byPublisher =
        rows.stream().collect(Collectors.groupingBy(AdminChurnReportDto::getPublisherId));

    List<File> tempFiles = new ArrayList<>();
    for (Map.Entry<String, List<AdminChurnReportDto>> entry : byPublisher.entrySet()) {
      String publisherId = entry.getKey();
      List<AdminChurnReportDto> pubRows = entry.getValue();
      String publisherName = pubRows.get(0).getPublisherName();

      String safeName = publisherName.replaceAll("[^a-zA-Z0-9 _-]", "").trim();
      Path pubCsv = Paths.get("./reports/churn/" + safeName + "-churn-report.csv");
      Files.deleteIfExists(pubCsv);

      long csvAcquired = 0, csvChurned = 0;
      try (java.io.Writer writer =
              new java.io.OutputStreamWriter(
                  Files.newOutputStream(
                      pubCsv,
                      java.nio.file.StandardOpenOption.CREATE,
                      java.nio.file.StandardOpenOption.APPEND));
          CSVPrinter printer = new CSVPrinter(writer, format)) {
        for (AdminChurnReportDto r : pubRows) {
          printer.printRecord(
              r.getAcquisitionDay(),
              r.getPublisherId(),
              r.getPublisherName(),
              r.getSourceId(),
              r.getCampaignName() != null ? r.getCampaignName() : "",
              r.getTotalAcquired(),
              r.getTotalChurned(),
              r.getTotalSurvived(),
              r.getChurnPercent());
          csvAcquired += r.getTotalAcquired();
          csvChurned += r.getTotalChurned();
        }
        long csvSurvived = csvAcquired - csvChurned;
        String csvChurnPct =
            csvAcquired == 0
                ? "0.00%"
                : String.format("%.2f%%", (csvChurned * 100.0) / csvAcquired);
        printer.printRecord(
            "TOTAL", "", "", "", "", csvAcquired, csvChurned, csvSurvived, csvChurnPct);
      }
      tempFiles.add(pubCsv.toFile());

      try {
        Publisher publisher = publisherService.findByPubId(publisherId);
        if (publisher.getEmail() != null && !publisher.getEmail().isBlank()) {
          emailService.sendWeeklyPublisherChurnReportToPublisher(
              pubCsv.toFile(), publisherName, publisher.getEmail(), period);
        }
      } catch (Exception e) {
        log.warn("Could not send churn report to publisher {}: {}", publisherId, e.getMessage());
      }
    }

    tempFiles.forEach(File::delete);
  }
}
