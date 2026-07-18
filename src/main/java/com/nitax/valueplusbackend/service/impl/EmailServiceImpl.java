package com.nitax.valueplusbackend.service.impl;

import com.nitax.valueplusbackend.domain.Advertiser;
import com.nitax.valueplusbackend.domain.Campaign;
import com.nitax.valueplusbackend.domain.Publisher;
import com.nitax.valueplusbackend.domain.PublisherCampaign;
import com.nitax.valueplusbackend.dto.request.ContactRequestDto;
import com.nitax.valueplusbackend.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

  @Autowired private JavaMailSender javaMailSender;

  @Value("${admin.email}")
  private String adminEmail;

  @Value("${contactUs.email}")
  private String contactAdminEmail;

  @Value("${spring.mail.sender}")
  private String sender;

  @Override
  public void sendSimpleMessage(String to, String subject, String text) {
    MimeMessage mimeMessage = javaMailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);

    try {
      helper.setTo(to.split(","));
      helper.setFrom("james@nxt.ng", sender);
      helper.setSubject(subject);
      helper.setBcc(adminEmail.split(","));
      helper.setText(text);

      javaMailSender.send(mimeMessage);
    } catch (MessagingException | UnsupportedEncodingException e) {
      // handle exception
      e.printStackTrace();
    }
  }

  @Override
  public void sendBulkMessages(List<String> recipients, String subject, String text) {
    MimeMessage mimeMessage = javaMailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);

    try {
      for (String recipient : recipients) {
        helper.setTo(recipient);
        helper.setFrom("james@nxt.ng", sender);
        helper.setSubject(subject);
        helper.setBcc(adminEmail.split(","));
        helper.setText(text);

        javaMailSender.send(mimeMessage);
      }
    } catch (MessagingException | UnsupportedEncodingException e) {
      // handle exception
      e.printStackTrace();
    }
  }

  @Override
  public void sendDailyChurnReport(List<File> files) {
    sendFilesToAdmin(
        files,
        "Daily Unsubscribers Report - " + new Date(),
        "Attached is today's Daily Unsubscribers Report. You'll find the detailed breakdown by publisher in the attached CSV file.\n");
  }

  @Override
  public void sendWeeklyChurnReport(List<File> files) {
    sendFilesToAdmin(
        files,
        "Weekly Unsubscribers Report - " + new Date(),
        "Attached is this week's Weekly Unsubscribers Report. You'll find the detailed breakdown by publisher in the attached CSV file.\n");
  }

  @Override
  public void sendMonthlyCostToAdvertiser(List<File> reportFiles, String email) {
    String monthName =
        LocalDateTime.now()
            .minusMonths(1)
            .getMonth()
            .getDisplayName(TextStyle.FULL, Locale.ENGLISH);

    sendFiles(
        reportFiles,
        "Monthly Cost Report - " + monthName,
        "Attached is this month's cost breakdown for your services with valueplus. You'd find cost for each of your active campaigns.\n",
        adminEmail + ",account@nxt.ng");
  }

  @Override
  public void sendAmountOwedReportToPublisherMonthly(Path reportFile, String email) {
    String monthName =
        LocalDateTime.now()
            .minusMonths(1)
            .getMonth()
            .getDisplayName(TextStyle.FULL, Locale.ENGLISH);

    sendFile(
        reportFile.toFile(),
        "Monthly Conversion Report - " + monthName,
        "Attached is this month's conversion breakdown for all active campaigns with Valueplus. You'd find cost for each of our active campaigns.\n",
        adminEmail + ",account@nxt.ng");
  }

  @Override
  public void sendDailySummaryReportToAdvertiser(Path reportFile, String email) {
    sendFile(
        reportFile.toFile(),
        "Daily Performance Summary Report - " + LocalDate.now().minusDays(1),
        "Attached is yesterday's performance report for all your campaigns. \n",
        adminEmail);
  }

  @Override
  public void sendMonthlySummaryReportToAdvertiser(Path reportFile, String email) {
    String monthName =
        LocalDateTime.now()
            .minusMonths(1)
            .getMonth()
            .getDisplayName(TextStyle.FULL, Locale.ENGLISH);

    sendFile(
        reportFile.toFile(),
        "Monthly Performance Summary Report - " + monthName,
        "Attached is last month's performance report for all your campaigns.\n",
        adminEmail + ",account@nxt.ng");
  }

  @Override
  public void sendWeeklyExecSummaryReport(Path reportFile) {
    ZoneId zoneId = ZoneId.of("UTC");

    LocalDate now = LocalDate.now(zoneId);
    LocalDate startOfWeek =
        now.minusDays(now.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue());
    LocalDate endOfWeek = startOfWeek.plusDays(6);
    sendFile(
        reportFile.toFile(),
        "Weekly Performance Summary Report for " + startOfWeek + " to " + endOfWeek,
        "Attached is this week's performance report. \n",
        adminEmail);
  }

  @Override
  public void sendMonthlyExecSummaryReport(Path reportFile) {
    String monthName =
        LocalDateTime.now()
            .minusMonths(1)
            .getMonth()
            .getDisplayName(TextStyle.FULL, Locale.ENGLISH);

    sendFile(
        reportFile.toFile(),
        "Monthly Performance Summary Report for " + monthName,
        "Attached is last month's performance report. \n",
        adminEmail + ",account@nxt.ng");
  }

  @Override
  public void sendAdvertiserVerificationMail(Advertiser advertiser, String url) {
    // create a mail for email verification and append the url

    String verificationMail =
        "Hello "
            + advertiser.getBusinessName()
            + ",\n\n"
            + "Thank you for signing up with ValuePlus. Please click the link below to verify your email address.\n\n"
            + url
            + "\n\n"
            + "If you did not sign up for an account with ValuePlus, please ignore this email.\n\n"
            + "Best Regards,\n"
            + "ValuePlus Team";

    sendSimpleMessage(advertiser.getEmail(), "Email Verification", verificationMail);
    //    sendSimpleMessage(advertiser.getEmail(), "Email Verification", url);
  }

  @Override
  public void sendPublisherVerificationMail(Publisher publisher, String url) {
    // create a mail for email verification and append the url

    String verificationMail =
        "Hello "
            + publisher.getBusinessName()
            + ",\n\n"
            + "Thank you for signing up with ValuePlus. Please click the link below to verify your email address.\n\n"
            + url
            + "\n\n"
            + "If you did not sign up for an account with ValuePlus, please ignore this email.\n\n"
            + "Best Regards,\n"
            + "ValuePlus Team";

    sendSimpleMessage(publisher.getEmail(), "Email Verification", verificationMail);
  }

  @Override
  public void sendAdminPublisherVerificationMail(Publisher publisher, MultipartFile multipartFile) {
    String verificationMail =
        "Hello Admin,\n\n"
            + "A new publisher has signed up with ValuePlus. Please find the attached signed IO form.\n\n"
            + "Publisher Name: "
            + publisher.getBusinessName()
            + "\n\n"
            + "Publisher Email: "
            + publisher.getEmail()
            + "\n\n"
            + "Best Regards,\n"
            + "ValuePlus Team";

    sendFile((File) multipartFile, "Verify New Publisher", verificationMail, adminEmail);
  }

  @Override
  public void sendNotificationToAdmin(Advertiser adv) {
    // check the Advertiser status to set status text
    String text = "";
    switch (adv.getStatus()) {
      case AWAIT_APPROVAL -> {
        text =
            """
                        Hello Admin,

                         An advertiser with business name %s and ID %s just verified their email and is waiting for approval,
                         Do not hesitate to move on with the approval checks.
                         Best regards,
                         The ValuePlus Team
                        """;
        sendSimpleMessage(
            adminEmail,
            "ACCOUNT APPROVAL",
            text.formatted(adv.getBusinessName(), adv.getAdvertiserId()));
      }
    }
  }

  @Override
  public void sendNotificationToAdmin(Publisher pub) {
    String text = "";
    switch (pub.getStatus()) {
      case AWAIT_APPROVAL -> {
        text =
            """
                            Hello Admin,

                             A Publisher with business name %s and ID %s just verified their email and is waiting for approval,
                             Do not hesitate to move on with the approval checks.
                             Best regards,
                             The ValuePlus Team
                            """;
        sendSimpleMessage(
            adminEmail, "ACCOUNT APPROVAL", text.formatted(pub.getBusinessName(), pub.getPubId()));
      }
    }
  }

  @Override
  public void sendUnVerifiedWithin25DaysMail(Advertiser adv) {
    String text =
        """
                Hello %s,

                It has been 30 days since you signed up with ValuePlus, but we have not received email verification from you. As a result, your account will be removed from our system within the next 24 hours.

                If you’d like to keep your account, please complete the email verification process by [Date].

                Best regards,
                The ValuePlus Team
                """;
    sendSimpleMessage(
        adminEmail,
        "Account Removal Due to Incomplete Verification",
        text.formatted(adv.getBusinessName(), adv.getBusinessName()));
  }

  @Override
  public void sendNotificationToAdvertiser(Advertiser adv) {
    // check the Advertiser status to set status text
    String text = "";
    switch (adv.getStatus()) {
      case AWAIT_APPROVAL -> {
        text =
            """
                        Hello %s,

                        Thank you for verifying your email. Your account is now under review by our team to ensure it meets our standards and guidelines. We will notify you shortly once the review is complete.

                        If you have any questions during this time, please don’t hesitate to contact us.

                        Best regards,
                        The ValuePlus Team
                        """;
        sendSimpleMessage(
            adv.getEmail(), "Your Account is Under Review", text.formatted(adv.getBusinessName()));
      }
      case APPROVED -> {
        text =
            """
                        Your Account Has Been Approved!
                        Hello %s,

                        We are thrilled to inform you that your account has been approved! You can now log in to the ValuePlus platform to start creating campaigns.

                        Our support team is available if you need assistance in setting up your first campaign.

                        Welcome aboard,
                        The ValuePlus Team
                        """;
        sendSimpleMessage(
            adv.getEmail(),
            "Your Account Has Been Approved",
            text.formatted(adv.getBusinessName()));
      }
      case REJECTED -> {
        text =
            """
                        Hello %s,

                        Thank you for your interest in ValuePlus. After careful review, we are unable to approve your account at this time. Unfortunately, this means you will not be able to access the platform.

                        If you would like further clarification on this decision, please feel free to contact our team.

                        Best regards,
                        The ValuePlus Team
                        """;
        sendSimpleMessage(
            adv.getEmail(),
            "Your Account Application Has Been Rejected",
            text.formatted(adv.getBusinessName()));
      }
      case INACTIVE -> {
        text =
            """
                        Hello %s,

                        We noticed that all of your campaigns are currently paused, and as a result, your account status is now set to inactive. If you’d like to resume your campaigns or start new ones, you can do so anytime.

                        If you need assistance, please reach out to our support team.

                        Thank you,
                        The ValuePlus Team
                        """;
        sendSimpleMessage(
            adv.getEmail(),
            "Your Account is Currently Inactive",
            text.formatted(adv.getBusinessName()));
      }
    }
  }

  @Override
  public void sendNotificationToPublisher(Publisher pub) {
    String text = "";
    switch (pub.getStatus()) {
      case AWAIT_APPROVAL -> {
        text =
            """
                            Hello %s,

                            Thank you for verifying your email. Your account is now under review by our team to ensure it meets our standards and guidelines. We will notify you shortly once the review is complete.

                            If you have any questions during this time, please don’t hesitate to contact us.

                            Best regards,
                            The ValuePlus Team
                            """;
        sendSimpleMessage(
            pub.getEmail(), "Your Account is Under Review", text.formatted(pub.getBusinessName()));
      }
      case APPROVED -> {
        text =
            """
                            Your Account Has Been Approved!
                            Hello %s,

                            We are thrilled to inform you that your account has been approved! You can now log in to the ValuePlus platform to start creating campaigns.

                            Our support team is available if you need assistance in setting up your first campaign.

                            Welcome aboard,
                            The ValuePlus Team
                            """;
        sendSimpleMessage(
            pub.getEmail(),
            "Your Account Has Been Approved",
            text.formatted(pub.getBusinessName()));
      }
      case REJECTED -> {
        text =
            """
                            Hello %s,

                            Thank you for your interest in ValuePlus. After careful review, we are unable to approve your account at this time. Unfortunately, this means you will not be able to access the platform.

                            If you would like further clarification on this decision, please feel free to contact our team.

                            Best regards,
                            The ValuePlus Team
                            """;
        sendSimpleMessage(
            pub.getEmail(),
            "Your Account Application Has Been Rejected",
            text.formatted(pub.getBusinessName()));
      }
      case INACTIVE -> {
        text =
            """
                            Hello %s,

                            We noticed that all of your campaigns are currently paused, and as a result, your account status is now set to inactive. If you’d like to resume your campaigns or start new ones, you can do so anytime.

                            If you need assistance, please reach out to our support team.

                            Thank you,
                            The ValuePlus Team
                            """;
        sendSimpleMessage(
            pub.getEmail(),
            "Your Account is Currently Inactive",
            text.formatted(pub.getBusinessName()));
      }
    }
  }

  @Override
  public void sendNotificationToAdvertiserReject(Advertiser adv, String text) {
    String body =
        """
                        Hello %s,

                        We regret to inform you that your account has been suspended due to the following reason(s):

                        %s

                        Please reach out to our support team if you wish to discuss this suspension or resolve any issues. We will do our best to assist you in reactivating your account if possible.

                        Best regards,
                        The ValuePlus Team
                        """;
    sendSimpleMessage(
        adminEmail, "Your Account Has Been Suspended", text.formatted(adv.getBusinessName(), text));
  }

  @Override
  @Async
  public void sendCampaignCreatedMail(Campaign campaign, Advertiser advertiser) {

    sendSimpleMessage(
        advertiser.getEmail(),
        "Campaign Created",
        "Your campaign has been created and is awaiting approval. You'll be notified once it's approved.");

    sendSimpleMessage(
        adminEmail,
        "New Campaign Created",
        "A new campaign has been created by/for "
            + advertiser.getBusinessName()
            + ".\n"
            + "Campaign Name: "
            + campaign.getName()
            + "\n"
            + "Campaign Type: "
            + campaign.getType()
            + "\n"
            + "Campaign Status: "
            + campaign.getStatus()
            + "\n"
            + "Campaign Start Date: "
            + campaign.getStartDate()
            + "\n"
            + "Campaign End Date: "
            + campaign.getEndDate()
            + "\n"
            + "Campaign Budget: "
            + campaign.getBudget()
            + "\n"
            + "Campaign URL: "
            + campaign.getUrl()
            + "Campaign Image: "
            + campaign.getImage()
            + "\n"
            + "Advertiser Name: "
            + advertiser.getBusinessName()
            + "\n"
            + "Advertiser Email: "
            + advertiser.getEmail()
            + "\n"
            + "Traffic Quality"
            + campaign.getTrafficQuality()
            + "\n"
            + "Carrier Connection: "
            + campaign.getCarrierConnection()
            + "\n"
            + "Interest: "
            + campaign.getInterest()
            + "\n"
            + "Objective: "
            + campaign.getObjective()
            + "\n"
            + "Advertiser Skype: "
            + advertiser.getSkype()
            + "\n");
  }

  @Override
  public void send50PercentUsageNotificationToAdvertiser(Advertiser advertiser, Campaign campaign) {
    sendSimpleMessage(
        advertiser.getEmail(),
        "Campaign Budget Alert",
        "Your campaign " + campaign.getName() + " has reached 50% of its budget.");
  }

  @Override
  public void send75PercentUsageNotificationToAdvertiser(Advertiser advertiser, Campaign campaign) {
    sendSimpleMessage(
        advertiser.getEmail(),
        "Campaign Budget Alert",
        "Your campaign " + campaign.getName() + " has reached 75% of its budget.");
  }

  @Override
  public void send90PercentUsageNotificationToAdvertiser(Advertiser advertiser, Campaign campaign) {
    sendSimpleMessage(
        advertiser.getEmail(),
        "Campaign Budget Alert",
        "Your campaign "
            + campaign.getName()
            + " has reached 90% of its budget. Kindly top up to continue running the campaign.");
  }

  @Override
  public void send100PercentUsageNotificationToAdvertiser(
      Advertiser advertiser, Campaign campaign) {
    sendSimpleMessage(
        advertiser.getEmail(),
        "Campaign Budget Alert",
        "Your campaign " + campaign.getName() + " has surpassed 100% of its budget.");
  }

  @Override
  @Async
  public void sendCampaignUpdatedMail(
      Campaign updatedCampaign, Advertiser advertiser, String changesSummary) {
    sendSimpleMessage(
        adminEmail,
        "Campaign Updated",
        "Your campaign has been updated. Here are the changes made: \n" + changesSummary);

    sendSimpleMessage(
        advertiser.getEmail(),
        "Campaign Updated",
        "Your campaign has been updated and is awaiting approval. You'll be notified once it's approved.");
  }

  @Override
  @Async
  public void sendCampaignPausedMail(Campaign pausedCampaign, String pauseReason) {
    sendSimpleMessage(
        pausedCampaign.getAdvertiser().getEmail(),
        "Campaign Paused",
        "Your campaign "
            + pausedCampaign.getName()
            + " has been paused. Reason: "
            + pauseReason
            + ". \n\n"
            + "Please note that your campaign can still accumulate conversions and still be billed because pause isn't instant. ");
  }

  @Override
  @Async
  public void sendCampaignPausedMailToPublishers(
      List<PublisherCampaign> pausedCampaign, String pauseReason) {
    sendBulkMessages(
        pausedCampaign.stream()
            .map(PublisherCampaign::getPublisher)
            .map(Publisher::getEmail)
            .toList(),
        "Campaign Paused",
        "This campaign has been paused due to the following Reason: "
            + pauseReason
            + ". \n\n"
            + " ");
  }

  @Override
  public void sendCampaignActivatedMailToPublishers(
      List<PublisherCampaign> pausedCampaign, String activationReason) {
    sendBulkMessages(
        pausedCampaign.stream()
            .map(PublisherCampaign::getPublisher)
            .map(Publisher::getEmail)
            .toList(),
        "Campaign Activated",
        "This campaign has been activated due to the following Reason: "
            + activationReason
            + ". \n\n"
            + " ");
  }

  @Override
  @Async
  public void sendCampaignPausedMaiToPublishers(
      List<PublisherCampaign> pausedCampaign, String pauseReason) {
    sendBulkMessages(
        pausedCampaign.stream()
            .map(PublisherCampaign::getPublisher)
            .map(Publisher::getEmail)
            .toList(),
        "Campaign Paused",
        "This campaign has been paused due to the following Reason: "
            + pauseReason
            + ". \n\n"
            + " ");
  }

  @Override
  @Async
  public void sendPublisherCampaignPausedMailTOAdmin(
      PublisherCampaign pausedCampaign, String pauseReason) {
    sendSimpleMessage(
        adminEmail,
        "Campaign Paused",
        "Publisher "
            + pausedCampaign.getPublisher().getName()
            + " just paused campaign: "
            + pausedCampaign.getCampaign().getName()
            + " Reason: "
            + pauseReason
            + ". \n\n");
  }

  @Override
  @Async
  public void sendCampaignEnabledMail(Campaign enabledCampaign) {
    sendSimpleMessage(
        enabledCampaign.getAdvertiser().getEmail(),
        "Campaign Enabled",
        "Your campaign "
            + enabledCampaign.getName()
            + " has been enabled. \n\n"
            + "Your campaign is now live and will start accumulating conversions. ");
  }

  @Override
  @Async
  public void sendCampaignEnabledMailToPublishersPushingCampaign(
      List<PublisherCampaign> publisherCampaignList, Campaign enabledCampaign) {
    sendBulkMessages(
        publisherCampaignList.stream()
            .map(PublisherCampaign::getPublisher)
            .map(Publisher::getEmail)
            .toList(),
        "Campaign Enabled",
        "Campaign "
            + enabledCampaign.getName()
            + " has been enabled. You can now continue pushing"
            + "\n\n"
            + "Your campaign is now live and will start accumulating conversions. ");
  }

  @Override
  public void sendCampaignDeletedMail(Campaign deletedCampaign, String deleteReason) {
    sendSimpleMessage(
        deletedCampaign.getAdvertiser().getEmail(),
        "Campaign Deleted",
        "Your campaign "
            + deletedCampaign.getName()
            + " has been deleted. Reason: "
            + deleteReason
            + ". \n\n"
            + "Please note that your campaign can still accumulate conversions and still be billed while deletion propagates. ");
  }

  @Override
  public void sendContactRequest(ContactRequestDto contactRequestDto) {
    sendSimpleMessage(
        contactAdminEmail,
        "Advertiser Contact Request",
        "An Advertiser has requested to contact You. Details Provided below :"
            + contactRequestDto.getName()
            + "\n Contact Name: "
            + contactRequestDto.getName()
            + "\n Contact Email: "
            + contactRequestDto.getEmail()
            + "\n Contact Company: "
            + contactRequestDto.getCompany()
            + "\n Contact SkypeId: "
            + contactRequestDto.getSkypeId()
            + "\n Contact Industry: "
            + contactRequestDto.getIndustryName()
            + ". \n\n"
            + "Please Do well to contact Them as soon as possible. ");
  }

  @Override
  public void sendDailyChurnReportToPublisher(File file, String email) {
    sendFile(
        file,
        "Daily Churn Report - " + LocalDate.now(),
        "Attached is your daily churn report.\n",
        adminEmail);
  }

  @Override
  public void sendWeeklyChurnReportToPublisher(
      File file, String publisherName, String date, String email) {
    String emailBody =
        String.format(
            "Hi %s,\n\n"
                + "Please find attached the weekly churn report for your campaigns from %s. "
                + "This report provides insights on user churn for optimized engagement and performance.\n\n"
                + "Thank you for your attention to these details.\n\n"
                + "Best regards,\n"
                + "Value Plus Team",
            publisherName, date);

    sendFile(file, "Weekly Churn Report for Your Campaigns", emailBody, email);
  }

  @Override
  public void sendWeeklyChurnReportToValueplus(File file) {
    String emailBody =
        String.format(
            "Hello Team,\n\n"
                + "Please find attached the churn report for publishers on Valueplus. "
                + "This report provides insights on churn performance on the various campaigns they're pushing.\n\n"
                + "Thank you for your attention to these details.\n\n"
                + "Best regards,\n"
                + "Value Plus Engine");

    sendFile(
        file,
        "Valueplus Weekly Churn Report - " + LocalDate.now().minusDays(6),
        emailBody,
        adminEmail);
  }

  @Override
  public void sendAdminWeeklyChurnReportMaster(List<File> files, String period) {
    String emailBody =
        String.format(
            "Hello Team,\n\n"
                + "Please find attached the weekly admin churn report for %s. "
                + "Each attachment is a separate publisher report. "
                + "This report shows acquisition vs 72h churn per publisher, campaign and source ID.\n\n"
                + "Best regards,\n"
                + "Value Plus Team",
            period);
    sendFiles(files, "Admin Weekly Churn Report - " + period, emailBody, adminEmail);
  }

  @Override
  public void sendWeeklyPublisherChurnReportToPublisher(
      File file, String publisherName, String email, String period) {
    String emailBody =
        String.format(
            "Hi %s,\n\n"
                + "Please find attached your weekly churn report for %s. "
                + "This report shows your acquisition vs 72h churn broken down by Campaign name and source ID.\n\n"
                + "Best regards,\n"
                + "Value Plus Team",
            publisherName, period);
    sendFile(file, "Weekly Churn Report - " + period, emailBody, email);
  }

  public void sendAdminDailyChurnReport(File file) {
    LocalDate weekEnd = LocalDate.now().minusDays(1);
    LocalDate weekStart = weekEnd.minusDays(6);
    String period = weekStart + " to " + weekEnd;
    String emailBody =
        String.format(
            "Hello Team,\n\n"
                + "Please find attached the weekly admin churn report for %s. "
                + "This report shows acquisition vs 48h churn per publisher and source ID.\n\n"
                + "Best regards,\n"
                + "Value Plus Team",
            period);
    sendFile(file, "Admin Weekly Churn Report - " + period, emailBody, adminEmail);
  }

  @Override
  public void sendDailyChurnReportToValueplus(File file) {
    String emailBody =
        String.format(
            "Hello Team,\n\n"
                + "Please find attached the daily churn report for publishers on Valueplus. "
                + "This report provides insights on churn performance on the various campaigns they're pushing.\n\n"
                + "Thank you for your attention to these details.\n\n"
                + "Best regards,\n"
                + "Value Plus Engine");

    sendFile(file, "Valueplus Daily Churn Report - " + LocalDate.now(), emailBody, adminEmail);
  }

  @Override
  public void sendCampaignActivatedMailToAdmin(String reason, String publisherName) {
    sendSimpleMessage(
        adminEmail,
        "Campaign Activated",
        "Publisher "
            + publisherName
            + "  just activated a campaign. "
            + " Reason: "
            + reason
            + ". \n\n");
  }

  @Override
  public void sendPublisherCampaignStartToAdmin(PublisherCampaign publisherCampaign) {
    sendSimpleMessage(
        adminEmail,
        "Publisher Campaign Started",
        "Publisher "
            + publisherCampaign.getPublisher().getName()
            + " just started a campaign. \n\n"
            + "Campaign Name: "
            + publisherCampaign.getCampaign().getName()
            + "\n"
            + "Kindly update cpa for this publisher's campaign.\n"
            + "Publisher Email: "
            + publisherCampaign.getPublisher().getEmail()
            + "\n");
  }

  @Override
  public void sendWalletFundingNotificationToAdvertiser(
      Advertiser advertiser, double amount, String transactionId) {
    String emailBody =
        String.format(
            "Hello "
                + advertiser.getFirstName()
                + " "
                + advertiser.getLastName()
                + ","
                + "We have funded your wallet with "
                + amount
                + "Here is the transaction id for the transaction "
                + transactionId
                + "Best regards,\n"
                + "Value Plus Team");

    sendSimpleMessage(advertiser.getEmail(), "Wallet Funding", emailBody);
  }

  @Override
  public void sendPublisherCampaignStartToPublisher(PublisherCampaign publisherCampaign) {
    sendSimpleMessage(
        adminEmail,
        "You started pushing this  Campaign",
        "Publisher "
            + publisherCampaign.getPublisher().getName()
            + " you started pushing a campaign. \n\n"
            + "Campaign Name: "
            + publisherCampaign.getCampaign().getName()
            + "\n");
  }

  @Override
  public void senddNoConversionNotificationToAdmin(Publisher publisher) {
    sendSimpleMessage(
        adminEmail,
        "No Conversion Notification",
        "Publisher " + publisher.getName() + " has not had any conversion in the last 24 hours.");
  }

  @Override
  public void sendRetentionReportToPublisher(File file, String publisherName, String email) {
    String emailBody =
        String.format(
            "Hi %s,\n\n"
                + "Please find attached the retention report for your campaigns. "
                + "This report provides an overview of how your sources performed, including metrics on user acquisition and churn rate. "
                + "It offers valuable insights to help you understand how your sources perform on our campaigns and can help you make informed decisions.\n\n"
                + "Thank you for your continued partnership.\n\n"
                + "Best regards,\n"
                + "Value Plus Team",
            publisherName);

    sendFile(file, "Subsource Retention Report", emailBody, email);
  }

  @Override
  public void sendCampaignDisabledMail(Campaign disabledCampaign, String disableReason) {
    sendSimpleMessage(
        disabledCampaign.getAdvertiser().getEmail(),
        "Campaign Disabled",
        "Your campaign "
            + disabledCampaign.getName()
            + " has been disabled. Reason: "
            + disableReason
            + ". \n\n");
  }

  private void sendFiles(List<File> files, String subject, String body, String recipient) {
    try {
      MimeMessage message = javaMailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);

      helper.setTo(recipient.split(","));
      helper.setFrom("james@nxt.ng", sender);
      helper.setSubject(subject);
      helper.setBcc(adminEmail.split(","));
      helper.setText(body);

      for (File file : files) {
        FileSystemResource fileResource = new FileSystemResource(file);
        helper.addAttachment(file.getName(), fileResource);
      }

      javaMailSender.send(message);
      log.info("Email sent successfully to email address(s): {}", recipient);
    } catch (MessagingException e) {
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  private void sendFile(File file, String subject, String body, String recipient) {
    try {
      MimeMessage message = javaMailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);

      helper.setTo(recipient.split(","));
      helper.setFrom("james@nxt.ng", sender);
      helper.setSubject(subject);
      helper.setBcc(adminEmail.split(","));
      helper.setText(body);

      FileSystemResource fileResource = new FileSystemResource(file);
      helper.addAttachment(file.getName(), fileResource);

      javaMailSender.send(message);
      log.info("Email sent successfully to email address(s): {}", recipient);
    } catch (MessagingException e) {
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  private void sendFilesToAdmin(List<File> files, String subject, String body) {
    sendFiles(files, subject, body, adminEmail);
  }
}
