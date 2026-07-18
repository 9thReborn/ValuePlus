package com.nitax.valueplusbackend.service;

import com.nitax.valueplusbackend.domain.Advertiser;
import com.nitax.valueplusbackend.domain.AdvertiserStatus;
import com.nitax.valueplusbackend.domain.Campaign;
import com.nitax.valueplusbackend.domain.Publisher;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("prod")
public class ScheduledTasks {
  private final CampaignService campaignService;
  private final AdvertiserService advertiserService;
  private final PublisherCampaignService publisherCampaignService;
  private final PublisherService publisherService;
  private final ReportService reportService;
  private final ClicksConversionsService clicksConversionsService;
  private final NotificationService notificationService;
  private final EmailService emailService;
  private final PhoneNumberService phoneNumberService;

  //  @Scheduled(cron = "0 0 */6 * * ?")
  public void checkForAllUnVerifiedUsersAndRejectedUser() {
    // Your task logic here
    List<Advertiser> rejectUsers = advertiserService.getAllRejectUsers();
    List<Advertiser> unverifiedUsers = advertiserService.getAllUnverifiedUsers();
    rejectUsers.forEach(c -> log.info(c.getEmail()));
    unverifiedUsers.forEach(c -> log.info(c.getEmail()));

    // combine both and delete ones that are 30 days or more
    log.info("deleting Advertisers that are unverified or rejected after 30 days");
    unverifiedUsers.stream()
        .filter(advertiser -> advertiser.getUnverifiedDate() != null)
        .filter(
            advertiser ->
                ChronoUnit.DAYS.between(advertiser.getUnverifiedDate(), Instant.now()) == 25)
        .forEach(emailService::sendUnVerifiedWithin25DaysMail);

    unverifiedUsers.stream()
        .filter(advertiser -> advertiser.getUnverifiedDate() != null)
        .filter(
            advertiser ->
                ChronoUnit.DAYS.between(advertiser.getUnverifiedDate(), Instant.now()) >= 30)
        .forEach(advertiserService::deleteAdvertiser);

    rejectUsers.stream()
        .filter(advertiser -> advertiser.getRejectedDate() != null)
        .filter(
            advertiser ->
                ChronoUnit.DAYS.between(advertiser.getRejectedDate(), Instant.now()) >= 30)
        .forEach(advertiserService::deleteAdvertiser);
    log.info("deleting Advertisers that are unverified or rejected after 30 days done...");
  }

  @Scheduled(cron = "0 */15 * * * ?")
  //  @Scheduled(cron = "0 */1 * * * ?")
  public void calculateCampaignCostJob() {
    log.info("Calculating campaign cost and click...");
    campaignService.calculateCampaignCostForCurrentMonth();
    log.info("Campaign cost calculated clicks and prices updated.");
  }

  //  @Scheduled(cron = "0 0 * * * *")
  //  @Scheduled(cron = "0 */1 * * * ?")
  public void createPublisherCampaignMappings() {
    log.info("Remapping all empty Campaigns to publishers");
    List<Campaign> allCampaigns = campaignService.getAllCampaigns();
    List<Publisher> allPublishers = publisherService.getAllPublishers();

    allCampaigns.forEach(
        campaign -> {
          allPublishers.forEach(
              publisher -> {
                // if the mapping exists skip. if not create
                if (!publisherCampaignService.existsByPublisherAndCampaign(publisher, campaign)) {
                  this.publisherCampaignService.createPublisherCampaign(
                      publisher, campaign, campaign.getCpaCostPerUser());
                }
              });
        });
    log.info("Remapping all empty Campaigns to publishers");
  }

  //  @Scheduled(cron = "0 0 */6 * * *")
  //  @Scheduled(cron = "0 */1 * * * ?")
  public void checkforInactiveAdvertisers() {
    log.info("Checking for Inactive advertisers by thier campaigns");
    List<Advertiser> allAdvertisers = advertiserService.getAllAdvertiser();

    allAdvertisers.forEach(
        advertiser -> {
          boolean hasActiveCampaign = campaignService.getNumberOfActiveCampaigns(advertiser) > 0;
          if (!hasActiveCampaign) {
            // if the advertiser doesn't have active campaigns.
            advertiser.setStatus(AdvertiserStatus.INACTIVE);
            advertiserService.updateAdvertiser(advertiser);
            emailService.sendNotificationToAdvertiser(advertiser);
          } else if (hasActiveCampaign
              && advertiser.getStatus().equals(AdvertiserStatus.INACTIVE)) {
            // if i have active campaigns and i'm inactive. set me to active
            advertiser.setStatus(AdvertiserStatus.ACTIVE);
            advertiserService.updateAdvertiser(advertiser);
          }
        });
    log.info("Checking for Inactive advertisers by thier campaigns . Ended");
  }

  //  //  @Scheduled(cron = "0 */10 * * * ?")
  //  public void refreshNotificationView() {
  //    log.info("Refreshing notification view...");
  //    notificationService.refreshNotificationView();
  //    log.info("Notification view refreshed.");
  //  }
  //
  //  //  @Scheduled(cron = "0 */15 * * * ?")
  //  public void resendPendingConversions() {
  //    log.info("Resending pending conversions...");
  //    clicksConversionsService.resendPendingConversions();
  //    log.info("Pending conversions resent.");
  //  }

  //  @Scheduled(cron = "0 */30 * * * ?")
  public void sendBudgetUsageReminders() {
    log.info("Sending usage notification to advertisers...");
    campaignService.sendBudgetUsageReminders();
    log.info("usage notification sent to advertisers.");
  }

  //  @Scheduled(cron = "0 0 6 * * *")
  //  @Scheduled(cron = "* */1 * * * ?")
  public void genrateDailyUnsubscribersReportForPubs() throws IOException {
    log.info("Generating pub's daily unsubscribers report...");
    reportService.genrateDailyUnsubscribersReportForPubs();
    log.info("Daily pub's unsubscribers report generated.");
  }

  // @Scheduled(cron = "0 0 6 * * SUN")
  //  @Scheduled(cron = "0 */1 * * * ?")
  public void genrateWeeklyUnsubscribersReport() throws IOException {
    log.info("Generating weekly unsubscribers report...");
    reportService.genrateWeeklyUnsubscribersReport();
    log.info("weekly unsubscribers report generated.");
  }

  // @Scheduled(cron = "0 0 6 * * SUN")
  public void generateRetentionReport() throws IOException {
    log.info("Generating retention report...");
    reportService.generateRetentionReport();
    log.info("Retention report generated.");
  }

  // @Scheduled(cron = "0 0 6 * * SUN")
  //  @Scheduled(cron = "* */1 * * * ?")
  public void generateWeeklyPublisherChurnReport() throws IOException {
    log.info("Generating publisher churn report...");
    reportService.generateWeeklyPublisherChurnReport();
    log.info("Publisher churn report generated.");
  }

  //  @Scheduled(cron = "* */1 * * * ?")
  // run 9am every day
  // @Scheduled(cron = "0 0 9 * * *")
  public void generateDailyPublisherChurnReport() throws IOException {
    log.info("Generating publisher churn report...");
    reportService.generateDailyPublisherChurnReport();
    log.info("Publisher churn report generated.");
  }

  @Scheduled(cron = "0 0 8 * * SAT")
  public void generateAndSendAdminChurnReport() throws IOException {
    log.info("Generating admin weekly churn report...");
    reportService.generateAndSendAdminChurnReport();
    log.info("Admin weekly churn report sent.");
  }

  @Scheduled(cron = "0 0 23 * * ?")
  public void loadPhoneNumber() throws IOException {
    log.info("Loading phone number...");
    phoneNumberService.loadPhoneNumberFileToDB();
    log.info("Phone number loaded.");
  }

  //  @Scheduled(cron = "* */1 * * * ?")
  public void sendPendingSMS() {
    log.info("Sending pending SMS...");
    phoneNumberService.sendPendingSMS();
    log.info("Pending SMS sent.");
  }

  //  @Scheduled(cron = "0 0 8 * * SUN")
  public void genrateWeeklyPerformanceSummaryReport() throws IOException {
    log.info("Generating weekly performance summary report...");
    reportService.genrateWeeklyPerformanceSummaryReport();
    log.info("weekly performance summary report generated.");
  }

  //  @Scheduled(cron = "0 0 9 1 * ?")
  //  @Scheduled(cron = "* */1 * * * ?")
  public void genrateMonthlyPerformanceSummaryReport() throws IOException {
    log.info("Generating monthly performance summary report...");
    reportService.genrateMonthlyPerformanceSummaryReport();
    log.info("monthly performance summary report generated.");
  }

  //  @Scheduled(cron = "0 15 9 2 * ?")
  //  @Scheduled(cron = "* */1 * * * ?")
  public void sendMonthlyCostReportToAdvertisers() throws IOException {
    log.info("Sending monthly cost report to advertisers...");
    reportService.sendMonthlyCostReportToAdvertisers();
    log.info("Monthly cost report sent to advertisers.");
  }

  //  @Scheduled(cron = "0 20 9 2 * ?")
  //  @Scheduled(cron = "* */1 * * * ?")
  public void sendAmountOwedReportToPublisherMonthly() throws IOException {
    log.info("Sending monthly cost report to publishers...");
    reportService.sendAmountOwedReportToPublisherMonthly();
    log.info("Monthly cost report sent to publishers.");
  }

  //  @Scheduled(cron = "* */1 * * * ?")
  //  @Scheduled(cron = "0 0 8 * * *")
  public void sendDailySummaryReportsToAdvertisers() throws IOException {
    log.info("Sending daily summary reports to advertisers...");
    reportService.sendDailySummaryReportsToAdvertisers();
    log.info("Summary reports sent to advertisers.");
  }

  //  @Scheduled(cron = "0 30 9 2 * ?")
  public void sendMonthlySummaryReportsToAdvertisers() throws IOException {
    log.info("Sending monthly summary reports to advertisers...");
    reportService.sendMonthlySummaryReportsToAdvertisers();
    log.info("Summary reports sent to advertisers.");
  }

  // create a cron job to notify admin when pubs aren't pushing traffic
  //  @Scheduled(cron = "0 */10 * * * *")
  public void notifyAdminWhenPubsArentPushingTraffic() {
    log.info("Notifying admin when publishers aren't pushing traffic...");
    reportService.notifyAdminWhenPubsArentPushingTraffic();
    log.info("Admin notified when publishers aren't pushing traffic.");
  }

  //  @Scheduled(cron = "0 0 1 * * ?") // Runs at 1 AM on the first day of each month
  //  @Scheduled(cron = "0 */1 * * * *")
  public void deleteOldRecords() {
    log.info("Deleting old records...");
    notificationService.deleteOldRecords();
    log.info("Old records deleted.");
  }
}
