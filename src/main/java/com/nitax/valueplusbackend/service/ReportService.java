package com.nitax.valueplusbackend.service;

import java.io.IOException;
import java.util.List;

import com.nitax.valueplusbackend.dto.AdminChurnReportDto;

public interface ReportService {
  void genrateDailyUnsubscribersReportForPubs() throws IOException;

  void genrateWeeklyUnsubscribersReport() throws IOException;

  void sendMonthlyCostReportToAdvertisers() throws IOException;

  void sendAmountOwedReportToPublisherMonthly() throws IOException;

  void sendDailySummaryReportsToAdvertisers() throws IOException;

  void sendMonthlySummaryReportsToAdvertisers() throws IOException;

  void genrateWeeklyPerformanceSummaryReport() throws IOException;

  void genrateMonthlyPerformanceSummaryReport() throws IOException;

  void notifyAdminWhenPubsArentPushingTraffic();

  void generateRetentionReport() throws IOException;

  void generateWeeklyPublisherChurnReport() throws IOException;

  void generateDailyPublisherChurnReport() throws IOException;

  void generateAndSendAdminChurnReport() throws IOException;

  void sendChurnReportToPublishers(List<AdminChurnReportDto> rows, String period, int churnDurationHours) throws IOException;
}
