package com.nitax.valueplusbackend.service;

import com.nitax.valueplusbackend.domain.Advertiser;
import com.nitax.valueplusbackend.domain.Campaign;
import com.nitax.valueplusbackend.domain.Publisher;
import com.nitax.valueplusbackend.domain.PublisherCampaign;
import com.nitax.valueplusbackend.dto.request.ContactRequestDto;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public interface EmailService {
  void sendNotificationToAdmin(Advertiser adv);
  void sendNotificationToAdmin(Publisher pub);

  void sendUnVerifiedWithin25DaysMail(Advertiser adv);

  void sendNotificationToAdvertiserReject(Advertiser adv, String text);

  void sendNotificationToAdvertiser(Advertiser adv);
  void sendNotificationToPublisher(Publisher pub);

  void sendSimpleMessage(String to, String subject, String text);

  void sendBulkMessages(List<String> recipients, String subject, String text);

  void sendDailyChurnReport(List<File> files);

  void sendWeeklyChurnReport(List<File> files);

  void sendMonthlyCostToAdvertiser(List<File> reportFiles, String email);

  void sendAmountOwedReportToPublisherMonthly(Path reportFile, String email);

  void sendDailySummaryReportToAdvertiser(Path reportFile, String email);

  void sendMonthlySummaryReportToAdvertiser(Path reportFile, String email);

  void sendWeeklyExecSummaryReport(Path reportFile);

  void sendMonthlyExecSummaryReport(Path reportFile);

  void sendAdvertiserVerificationMail(Advertiser advertiser, String authToken);
  void sendPublisherVerificationMail(Publisher publisher, String authToken);
  void sendAdminPublisherVerificationMail(Publisher publisher, MultipartFile multipartFile);

  void sendCampaignCreatedMail(Campaign campaign, Advertiser advertiser);

  void send50PercentUsageNotificationToAdvertiser(Advertiser advertiser, Campaign campaign);

  void send75PercentUsageNotificationToAdvertiser(Advertiser advertiser, Campaign campaign);

  void send90PercentUsageNotificationToAdvertiser(Advertiser advertiser, Campaign campaign);

  void send100PercentUsageNotificationToAdvertiser(Advertiser advertiser, Campaign campaign);

  void sendCampaignUpdatedMail(
      Campaign updatedCampaign, Advertiser advertiser, String changesSummary);

  void sendCampaignPausedMail(Campaign pausedCampaign, String pauseReason);

  @Async
  void sendCampaignPausedMailToPublishers(List<PublisherCampaign> pausedCampaign, String pauseReason);
  void sendCampaignActivatedMailToPublishers(List<PublisherCampaign> pausedCampaign, String pauseReason);

  void sendCampaignPausedMaiToPublishers(List<PublisherCampaign>  pausedCampaign, String pauseReason);

  @Async
  void sendPublisherCampaignPausedMailTOAdmin(PublisherCampaign pausedCampaign, String pauseReason);

  void sendCampaignEnabledMail(Campaign enabledCampaign);

  @Async
  void sendCampaignEnabledMailToPublishersPushingCampaign(List<PublisherCampaign> publisherCampaignList, Campaign enabledCampaign);

  void sendCampaignDeletedMail(Campaign deletedCampaign, String deleteReason);

  void sendCampaignDisabledMail(Campaign disabledCampaign, String disableReason);

  void sendContactRequest(ContactRequestDto disabledCampaign);

  void sendDailyChurnReportToPublisher(File file, String email);

  void sendPublisherCampaignStartToPublisher(PublisherCampaign publisherCampaign);

  void senddNoConversionNotificationToAdmin(Publisher publisher);

  void sendRetentionReportToPublisher(File file, String publisherName, String email);

  void sendWeeklyChurnReportToPublisher(File file, String publisherName, String date, String email);

  void sendWeeklyChurnReportToValueplus(File file);

  void sendDailyChurnReportToValueplus(File file);

  void sendCampaignActivatedMailToAdmin(String reason,String publisherName);

    void sendPublisherCampaignStartToAdmin(PublisherCampaign publisherCampaign);

    void sendWalletFundingNotificationToAdvertiser(Advertiser advertiser, double amount, String transactionId);

  void sendAdminDailyChurnReport(File file);

  void sendAdminWeeklyChurnReportMaster(List<File> files, String period);

  void sendWeeklyPublisherChurnReportToPublisher(File file, String publisherName, String email, String period);
}
