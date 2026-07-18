package com.nitax.valueplusbackend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nitax.valueplusbackend.config.JwtUtils;
import com.nitax.valueplusbackend.domain.Advertiser;
import com.nitax.valueplusbackend.domain.AdvertiserStatus;
import com.nitax.valueplusbackend.domain.AppRoles;
import com.nitax.valueplusbackend.domain.Campaign;
import com.nitax.valueplusbackend.domain.Notification;
import com.nitax.valueplusbackend.domain.Publisher;
import com.nitax.valueplusbackend.dto.AuthenticationResponse;
import com.nitax.valueplusbackend.dto.CampaignDetailsDTO;
import com.nitax.valueplusbackend.dto.ReportingChartDto;
import com.nitax.valueplusbackend.dto.SecureDNotificationDto;
import com.nitax.valueplusbackend.dto.request.AdvertiserChurnReportRequestDTO;
import com.nitax.valueplusbackend.dto.request.AdvertiserConversionReportRequestDTO;
import com.nitax.valueplusbackend.dto.request.AdvertiserSignInDTO;
import com.nitax.valueplusbackend.dto.request.AdvertiserSignupDTO;
import com.nitax.valueplusbackend.dto.request.CampaignFilter;
import com.nitax.valueplusbackend.dto.request.CreateCampaignDTO;
import com.nitax.valueplusbackend.dto.request.ReportChartRequestDto;
import com.nitax.valueplusbackend.dto.request.ReportSummaryRequestDto;
import com.nitax.valueplusbackend.dto.request.UnsubscribeRequest;
import com.nitax.valueplusbackend.dto.request.UpdateCampaignDTO;
import com.nitax.valueplusbackend.dto.response.AdvertiserChurnReportDTO;
import com.nitax.valueplusbackend.dto.response.AdvertiserConversionDTO;
import com.nitax.valueplusbackend.dto.response.AdvertiserNameResponse;
import com.nitax.valueplusbackend.dto.response.ApiResponse;
import com.nitax.valueplusbackend.dto.response.AutoFillDTO;
import com.nitax.valueplusbackend.dto.response.CampaignAnalyticsResponseDto;
import com.nitax.valueplusbackend.dto.response.CampaignSummaryDTO;
import com.nitax.valueplusbackend.dto.response.LoginResponse;
import com.nitax.valueplusbackend.dto.response.ReportingSummaryDto;
import com.nitax.valueplusbackend.dto.response.SpendSummaryDto;
import com.nitax.valueplusbackend.exception.AdvertiserNotFoundException;
import com.nitax.valueplusbackend.exception.AppException;
import com.nitax.valueplusbackend.exception.DuplicateAdvertiserException;
import com.nitax.valueplusbackend.repository.AdvertiserRepository;
import com.nitax.valueplusbackend.repository.RoleRepository;
import com.nitax.valueplusbackend.service.AdvertiserService;
import com.nitax.valueplusbackend.service.CampaignService;
import com.nitax.valueplusbackend.service.EmailService;
import com.nitax.valueplusbackend.service.NotificationService;
import com.nitax.valueplusbackend.service.PublisherCampaignService;
import com.nitax.valueplusbackend.service.PublisherService;
import com.nitax.valueplusbackend.service.WalletService;
import com.nitax.valueplusbackend.utils.AppUtils;
import com.nitax.valueplusbackend.utils.enums.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdvertiserServiceImpl implements AdvertiserService {

  private final AdvertiserRepository advertiserRepository;
  private final ModelMapper modelMapper;
  private final AppUtils appUtils;
  private final PasswordEncoder encoder;
  private final AuthenticationManager authenticationManager;
  private final JwtUtils jwtUtils;
  private final NotificationService notificationService;
  private final CampaignService campaignService;
  private final PublisherService publisherService;
  private final EmailService emailService;
  private final PublisherCampaignService publisherCampaignService;
  private final RoleRepository roleRepository;
  private final WalletService walletService;

  @Value("${app.frontend-server-url}")
  private String frontendServerUrl;

  @Override
  public List<Advertiser> getAllUnverifiedUsers() {
    return advertiserRepository.findAllByStatus(AdvertiserStatus.UNVERIFIED);
  }

  @Override
  public List<Advertiser> getAllRejectUsers() {
    return advertiserRepository.findAllByStatus(AdvertiserStatus.REJECTED);
  }

  @Override
  public String createAdvertiser(AdvertiserSignupDTO advertiserSignupDTO) {
    if (advertiserRepository.existsByEmail(advertiserSignupDTO.getEmail())) {
      throw new DuplicateAdvertiserException(
          "An advertiser with the provided email already exists.");
    }

    Advertiser advertiser = modelMapper.map(advertiserSignupDTO, Advertiser.class);

    if (advertiserSignupDTO.getSignUpChannel().equalsIgnoreCase("BULK_SMS")) {
      advertiser.setRole(roleRepository.findByName(Role.ADVERTISER).get());
      advertiser.setAdvertiserId(appUtils.generateAdvId());
      advertiser.setPassword(encoder.encode(advertiser.getPassword()));
      advertiser.setPostbackUrl(
          appUtils.generateAdvertiserPostbackUrl(advertiser.getAdvertiserId()));
      advertiser.setStatus(AdvertiserStatus.AWAIT_APPROVAL);
      advertiser.setBulkSmsEnabled(true);
      advertiser = advertiserRepository.save(advertiser);
      walletService.createAdvertiserWallet(advertiser);

      String authToken = jwtUtils.generateJwtToken(advertiser);
      //      added type to token
      String verifyUrl = frontendServerUrl + "?token=" + authToken + "&type=bulksms";
      emailService.sendAdvertiserVerificationMail(advertiser, verifyUrl);
      return authToken;
    }
    advertiser.setRole(roleRepository.findByName(Role.ADVERTISER).get());
    advertiser.setAdvertiserId(appUtils.generateAdvId());
    advertiser.setPassword(encoder.encode(advertiser.getPassword()));
    advertiser.setPostbackUrl(appUtils.generateAdvertiserPostbackUrl(advertiser.getAdvertiserId()));
    advertiser.setStatus(AdvertiserStatus.AWAIT_APPROVAL);
    advertiser.setMarketingAgencyEnabled(true);
    advertiser = advertiserRepository.save(advertiser);
    walletService.createAdvertiserWallet(advertiser);

    String authToken = jwtUtils.generateJwtToken(advertiser);
    String verifyUrl = frontendServerUrl + "?token=" + authToken + "type=advertiser";
    emailService.sendAdvertiserVerificationMail(advertiser, verifyUrl);
    return authToken;
  }

  @Override
  public ApiResponse<LoginResponse> loginAdvertiser(AdvertiserSignInDTO advertiserSignInDTO) {
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                advertiserSignInDTO.getEmail(), advertiserSignInDTO.getPassword()));

    String jwt = jwtUtils.generateJwtToken(authentication);
    String refreshToken = jwtUtils.generateRefreshToken(authentication);

    LoginResponse loginResponse = new LoginResponse(jwt, refreshToken);
    return ApiResponse.<LoginResponse>builder().success(true).data(loginResponse).build();
  }

  @Override
  @Async
  public void handleAdvertiserCallBack(
      String advertiserId,
      String campaignId,
      String trxId,
      String sourceId,
      String msisdn,
      String message,
      String activation) {
    // log all parameter
    log.info(
        "advertiserId: {}, campaignId: {}, trxId: {}, sourceId: {}, msisdn: {}, message: {}",
        advertiserId,
        campaignId,
        trxId,
        sourceId,
        msisdn,
        message);

    //    List<Notification> notifications = notificationService.findNotifcationByTrxId(trxId);
    //    Notification notification = null;

    //    if (Objects.isNull(notifications) || notifications.isEmpty()) {
    Notification notification = new Notification();
    //      log.info("Advertiser is not tracking click");
    //    } else {
    //      notification = notifications.get(0);
    //    }
    notification.setCampaignId(campaignId);
    notification.setTransactionId(trxId);
    notification.setSourceId(sourceId);
    notification.setStatus(Notification.NotificationStatus.ADVERTISER_HOOK_RECEIVED);
    notification.setMsisdn(msisdn);
    notification.setMessage(message);
    notification.setActivation(activation);
    notification = notificationService.saveNotification(notification);

    Campaign campaign = campaignService.findCampaignById(campaignId).orElse(null);

    if (campaign != null && campaign.getStatus().equalsIgnoreCase("ACTIVE")) {
      notification.setCampaignId(campaign.getCampaignId());
      notification.setCpaRevenue(campaign.getCpaCostPerUser());
      Publisher publisher = publisherService.findByPubId(trxId.trim().split("_")[1]);
      if (publisher != null) {
        notification.setPublisherId(publisher.getPubId());
        Notification savedNotification = notificationService.saveNotification(notification);
        if (activation.equalsIgnoreCase("1")) {
          publisherService.handlePublisherPostBack(savedNotification);
        }
      } else {
        log.info("Publisher not found");
      }
    } else if (campaign != null && campaign.getStatus().equalsIgnoreCase("INACTIVE")) {
      notification.setMessage("Campaign is not active");
      notificationService.saveNotification(notification);
    } else if (campaign == null) {
      notification.setMessage("Campaign not found");
      notificationService.saveNotification(notification);
    }
  }

  @Override
  @Async
  public void handleUnsubscription(UnsubscribeRequest unsubscribeRequest) {
    // log request
    log.info(
        "Unsubscribe request received: msisdn: {}, clickId: {}, unsubscribeDateTime: {}",
        unsubscribeRequest.getMsisdn(),
        unsubscribeRequest.getClickId(),
        unsubscribeRequest.getUnsubscribeDateTime());

    //    String publisher = unsubscribeRequest.getClickId().trim().split("_")[1];
    //    String campaignId = unsubscribeRequest.getClickId().trim().split("_")[0];
    //    String clickId =
    //        unsubscribeRequest
    //            .getClickId()
    //            .trim()
    //            .replace("valueplus_" + publisher + "_", "")
    //            .replace("valueplus2_" + publisher + "_", "")
    //            .replace("vpbcairtel_" + publisher + "_", "")
    //            .replace("vpmpesa_" + publisher + "_", "")
    //            .replace(campaignId + "_" + publisher + "_", "");
    //    clickId = clickId.split("SRCID")[0];
    Notification notification =
        notificationService.findTopByMsisdnOrderByCreatedAtDesc(unsubscribeRequest.getMsisdn());

    if (Objects.nonNull(notification)) {
      //      if (notificationService.existsUnsubscriptionByTransactionIdAndMsisdn(
      //          notification.getTransactionId(), notification.getMsisdn())) {
      //        log.info(
      //            "Duplicate unsubscription request ignored for transactionId: {}, msisdn: {}",
      //            notification.getTransactionId(),
      //            notification.getMsisdn());
      //        return;
      //      }

      Duration duration =
          Duration.between(
              notification.getCreatedDate(), unsubscribeRequest.getFormattedUnsubscribeDateTime());

      Notification newNotification = new Notification();
      newNotification.setStatus(Notification.NotificationStatus.UNSUBSCRIBED);
      newNotification.setDuration(duration.toSeconds());
      newNotification.setUnsubscribeTimestamp(unsubscribeRequest.getFormattedUnsubscribeDateTime());
      newNotification.setMessage("User unsubscribed from campaign");
      newNotification.setCampaignId(notification.getCampaignId());
      newNotification.setPublisherId(notification.getPublisherId());
      newNotification.setProductId(notification.getProductId());
      newNotification.setTransactionId(notification.getTransactionId());
      newNotification.setSourceId(notification.getSourceId());
      newNotification.setMsisdn(notification.getMsisdn());
      newNotification.setCpaRevenue(notification.getCpaRevenue());
      newNotification.setActivation(notification.getActivation());
      newNotification.setVpRevenue(notification.getVpRevenue());
      newNotification.setMonth(LocalDate.now().getMonthValue());
      newNotification.setYear(LocalDate.now().getYear());
      newNotification.setDay(LocalDate.now().getDayOfMonth());
      newNotification.setCreatedDate(notification.getCreatedDate());

      notificationService.saveNotification(newNotification);
      log.info("Unsubscription handled successfully");
    } else {
      log.info("Notification not found");
    }
  }

  @Override
  public CampaignDetailsDTO getTotalAmountOwedByAdvertiser(String advertiserId) {
    Advertiser advertiser =
        advertiserRepository
            .findByAdvertiserId(advertiserId)
            .orElseThrow(() -> new AdvertiserNotFoundException("Advertiser not found"));
    return campaignService.getTotalAmountOwedByAdvertiser(advertiser.getId());
  }

  @Override
  public void handleSecureDWebhook(SecureDNotificationDto secureDNotificationDto) {
    try {
      if (secureDNotificationDto.getActivation().equalsIgnoreCase("1")) {
        boolean isTrxIdValid =
            secureDNotificationDto.getTrxId() != null
                && secureDNotificationDto.getTrxId().trim().split("SRCID").length > 0;
        if (!isTrxIdValid) {
          log.error("Invalid TrxId: {}", secureDNotificationDto.getTrxId());
          return;
        }
        String clickId = secureDNotificationDto.getTrxId().trim().split("SRCID")[0];
        String source =
            secureDNotificationDto.getTrxId().trim().split("SRCID").length > 1
                ? secureDNotificationDto.getTrxId().trim().split("SRCID")[1]
                : "";
        String advertiserId = secureDNotificationDto.getAdvertiserId();
        String campaignId = secureDNotificationDto.getCampaignId();
        String trxId = clickId;
        String sourceId = source;
        String msisdn = secureDNotificationDto.getMsisdn();
        String message = secureDNotificationDto.getDescription();
        handleAdvertiserCallBack(
            advertiserId,
            campaignId,
            trxId,
            sourceId,
            msisdn,
            message,
            secureDNotificationDto.getActivation());
      }
    } catch (Exception e) {
      log.error("Error in handleSecureDWebhook: ", e);
    }
  }

  @Override
  public List<Advertiser> findAll() {
    return advertiserRepository.findAll();
  }

  @Override
  public List<Campaign> getCampaigns(Long advertiserId) {
    return campaignService.findCampaignByAdvertiserId(advertiserId);
  }

  @Override
  public Long getNumberOfActiveCampaigns(Advertiser advertiserId) {
    return campaignService.getNumberOfActiveCampaigns(advertiserId);
  }

  @Override
  public Long getNumberOfCampaigns(Advertiser advertiser) {
    return campaignService.getNumberOfCampaigns(advertiser);
  }

  @Override
  public List<Campaign> getActiveCampaigns(Advertiser advertiser) {
    return campaignService.getActiveCampaigns(advertiser);
  }

  @Override
  public List<Advertiser> getAllAdvertiser() {
    return advertiserRepository.findAll();
  }

  @Override
  public String verifySignupToken(String jwtToken) {
    boolean isValid = jwtUtils.validateJwtToken(jwtToken);

    if (!isValid) {
      throw new AppException(
          "Cannot verify signup, please request for a new verification or contact support.");
    }

    String email = jwtUtils.getUserNameFromJwtToken(jwtToken);

    Advertiser advertiser = advertiserRepository.findByEmail(email).orElseThrow();

    if (advertiser.getIsEmailVerified()) {
      throw new AppException("Email already verified");
    }

    advertiser.setIsEmailVerified(true);
    advertiser.setStatus(AdvertiserStatus.AWAIT_APPROVAL);
    emailService.sendNotificationToAdmin(advertiser);
    emailService.sendNotificationToAdvertiser(advertiser);

    advertiserRepository.save(advertiser);

    return jwtToken;
  }

  @Override
  public CampaignSummaryDTO getTotalCampaignStats(Object username) {
    Advertiser advertiser = advertiserRepository.findByEmail(username.toString()).orElse(null);
    long totalCampaignCountToday = campaignService.getNumberOfCampaigns(advertiser);

    long totalCampaignCountYesterday = campaignService.getCampaignCountForYesterday(advertiser);

    long[] chartNumbers = campaignService.getTotalCampaignsStats(advertiser);

    double percentageChange = 0.0;
    if (totalCampaignCountYesterday != 0) {
      percentageChange =
          ((double) (totalCampaignCountToday - totalCampaignCountYesterday)
                  / totalCampaignCountYesterday)
              * 100;
    }

    return CampaignSummaryDTO.builder()
        .totalCampaignCount(totalCampaignCountToday)
        .percentageChange(percentageChange)
        .chartNumbers(chartNumbers)
        .build();
  }

  @Override
  public CampaignSummaryDTO getActiveCampaignStats(String username) {
    Advertiser advertiser = advertiserRepository.findByEmail(username).orElse(null);
    long totalCampaignCountToday = campaignService.getNumberOfActiveCampaigns(advertiser);

    long totalCampaignCountYesterday =
        campaignService.getActiveCampaignCountForYesterday(advertiser);

    long[] chartNumbers = campaignService.getActiveCampaignsStats(advertiser);

    double percentageChange = 0.0;
    if (totalCampaignCountYesterday != 0) {
      percentageChange =
          ((double) (totalCampaignCountToday - totalCampaignCountYesterday)
                  / totalCampaignCountYesterday)
              * 100;
    }

    return CampaignSummaryDTO.builder()
        .totalCampaignCount(totalCampaignCountToday)
        .percentageChange(percentageChange)
        .chartNumbers(chartNumbers)
        .build();
  }

  @Override
  public SpendSummaryDto getSpendStats(String username) {
    Advertiser advertiser = advertiserRepository.findByEmail(username).orElse(null);
    long totalSpendLastMonth =
        campaignService.getTotalSpendForPreviousMonthForAdvertiser(advertiser);

    long totalSpendForCurrentMonth =
        campaignService.getTotalSpendForCurrentMonth(advertiser.getId());

    // calculate the percentage difference
    double percentageChange = 0.0;
    if (totalSpendLastMonth != 0) {
      percentageChange =
          ((double) (totalSpendForCurrentMonth - totalSpendLastMonth) / totalSpendLastMonth) * 100;
    }

    long[] chartNumbers = campaignService.getTotalCampaignsSpendStats(advertiser);

    return SpendSummaryDto.builder()
        .totalAmountSpent(totalSpendForCurrentMonth)
        .totalAmountSpentLastMonth(totalSpendLastMonth)
        .percentageChange(percentageChange)
        .chartNumbers(chartNumbers)
        .build();
  }

  @Override
  public List<CampaignAnalyticsResponseDto> getAnalyticsCampaignStats(
      String username, Integer year) {
    Advertiser advertiser = advertiserRepository.findByEmail(username).orElse(null);

    long[] clickData = campaignService.getClickData(advertiser.getId(), year);

    long[] conversionData = campaignService.getConversionData(advertiser.getId(), year);

    int[] availableMonths = campaignService.getMonthlyConversionMonths(advertiser.getId(), year);

    CampaignAnalyticsResponseDto clickAnalytics =
        CampaignAnalyticsResponseDto.builder()
            .dataLabel("Clicks")
            .data(clickData)
            .monthsLabel(availableMonths)
            .build();

    CampaignAnalyticsResponseDto conversionAnalytics =
        CampaignAnalyticsResponseDto.builder()
            .dataLabel("Conversions")
            .data(conversionData)
            .monthsLabel(availableMonths)
            .build();

    List<CampaignAnalyticsResponseDto> response = new ArrayList<>();
    response.add(clickAnalytics);
    response.add(conversionAnalytics);

    return response;
  }

  @Override
  public Page<Campaign> findAllCampaigns(
      CampaignFilter filter, Pageable pageable, String username) {

    Advertiser advertiser = advertiserRepository.findByEmail(username).orElse(null);

    return campaignService.findAllCampaigns(filter, pageable, advertiser);
  }

  @Override
  @Transactional
  public Campaign createCampaign(CreateCampaignDTO createCampaignDTO, String username)
      throws IOException {
    Advertiser advertiser = advertiserRepository.findByEmail(username).orElse(null);
    boolean hasCampaigns = campaignService.getNumberOfCampaigns(advertiser) > 0;
    Campaign campaign = campaignService.createCampaign(createCampaignDTO);

    if (Objects.nonNull(campaign) && Objects.nonNull(advertiser)) {
      emailService.sendCampaignCreatedMail(campaign, advertiser);
      // map campaign to all publishers
      List<Publisher> allPublishers = publisherService.getAllPublishers();
      for (Publisher publisher : allPublishers) {
        publisherCampaignService.createPublisherCampaign(
            publisher, campaign, campaign.getCpaCostPerUser());
      }

      // if the advertiser hasn't created any campaign : set status to active
      if (!hasCampaigns) {
        advertiser.setStatus(AdvertiserStatus.ACTIVE);
        advertiserRepository.save(advertiser);
      }
      return campaign;
    }

    throw new AppException("Error creating campaign");
  }

  @Override
  public Campaign getCampaignDetails(String campaignId, String username) {

    advertiserRepository.findByEmail(username).orElse(null);

    return campaignService.getCampaignDetails(campaignId);
  }

  @Override
  public String deactivateCampaign(String campaignId) {
    return campaignService.deactivateCampaign(campaignId);
  }

  @Override
  public String activateCampaign(String campaignId) {
    return campaignService.activateCampaign(campaignId);
  }

  @Override
  public Advertiser getAdvertiserDetails(String username) {

    Advertiser advertiser = advertiserRepository.findByEmail(username).orElse(null);
    assert advertiser != null;
    //    advertiser.setPassword("**********");

    return advertiser;
  }

  @Override
  public String uploadCampaignImage(MultipartFile file) throws IOException {

    return campaignService.uploadCampaignImage(file);
  }

  @Override
  @Transactional
  public Campaign editCampaign(UpdateCampaignDTO editCampaignDTO, String campaignId) {

    Campaign existingCampaign =
        campaignService
            .findCampaignById(campaignId)
            .orElseThrow(() -> new AppException("Campaign not found"));

    Campaign backup = new Campaign(existingCampaign);

    Campaign updatedCampaign = campaignService.editCampaign(editCampaignDTO, existingCampaign);

    if (Objects.nonNull(updatedCampaign)) {
      String changesSummary = generateChangesSummary(backup, updatedCampaign);
      emailService.sendCampaignUpdatedMail(
          updatedCampaign, updatedCampaign.getAdvertiser(), changesSummary);
      if (changesSummary.contains("cpaCostPerUser")) {
        // map campaign to all publishers if changes Summary has an update
        List<Publisher> allPublishers = publisherService.getAllPublishers();
        for (Publisher publisher : allPublishers) {
          publisherCampaignService.createPublisherCampaign(
              publisher, updatedCampaign, updatedCampaign.getCpaCostPerUser());
        }
      }
      return updatedCampaign;
    }

    throw new AppException("Error updating campaign");
  }

  private String generateChangesSummary(Campaign original, Campaign updated) {
    StringBuilder changes = new StringBuilder("The following changes were made to the campaign:\n");

    // Get the differences using reflection
    String originalString =
        ReflectionToStringBuilder.toString(original, ToStringStyle.SHORT_PREFIX_STYLE);
    String updatedString =
        ReflectionToStringBuilder.toString(updated, ToStringStyle.SHORT_PREFIX_STYLE);

    if (!originalString.equals(updatedString)) {
      String[] originalFields = originalString.split(",");
      String[] updatedFields = updatedString.split(",");

      for (int i = 0; i < originalFields.length; i++) {
        if (!originalFields[i].equals(updatedFields[i])) {
          changes
              .append(originalFields[i].split("=")[0])
              .append(" changed from '")
              .append(originalFields[i].split("=")[1])
              .append("' to '")
              .append(updatedFields[i].split("=")[1])
              .append("'\n");
        }
      }
    }

    return changes.toString();
  }

  @Override
  public String deleteCampaign(String campaignId) {
    return campaignService.deleteCampaign(campaignId);
  }

  @Override
  public List<ReportingChartDto> getAdvertiserReports(String username, ReportChartRequestDto dto) {
    Advertiser advertiser = advertiserRepository.findByEmail(username).orElse(null);

    assert advertiser != null;
    if (dto.getChartType().equalsIgnoreCase("clicks")) {
      return notificationService.getAdvertiserClicksReport(advertiser.getAdvertiserId(), dto);
    }

    if (dto.getChartType().equalsIgnoreCase("conversions")) {
      return notificationService.getAdvertiserConversionsReport(advertiser.getAdvertiserId(), dto);
    }

    if (dto.getChartType().equalsIgnoreCase("retention")) {
      return notificationService.getAdvertiserRetentionReport(advertiser.getAdvertiserId(), dto);
    }

    if (dto.getChartType().equalsIgnoreCase("churn")) {
      return notificationService.getAdvertiserChurnReport(advertiser.getAdvertiserId(), dto);
    }

    throw new AppException("Invalid chart type");
  }

  @Override
  public List<ReportingSummaryDto> getAdvertiserReportsSummary(
      String username, ReportSummaryRequestDto dto) {

    Advertiser advertiser = advertiserRepository.findByEmail(username).orElse(null);

    assert advertiser != null;
    return notificationService.getAdvertiserReportsSummary(advertiser.getAdvertiserId(), dto);
  }

  @Override
  public Advertiser findAdvertiserById(Long advertiserId) {
    return advertiserRepository.findById(advertiserId).orElseThrow(null);
  }

  @Override
  public List<AdvertiserNameResponse> getAllAdvertisersName() {
    return advertiserRepository.findAll().stream()
        .map(
            advertiser -> {
              AdvertiserNameResponse response = new AdvertiserNameResponse();
              response.setId(advertiser.getId());
              response.setName(advertiser.getBusinessName());
              return response;
            })
        .toList();
  }

  @Override
  public Advertiser createAdvertiserForAdmin(AdvertiserSignupDTO signupDTO) {
    Advertiser advertiser = modelMapper.map(signupDTO, Advertiser.class);
    advertiser.setAdvertiserId(appUtils.generateAdvId());
    advertiser.setPassword(encoder.encode(advertiser.getPassword()));
    advertiser.setIsAccountActive(true);
    advertiser.setIsEmailVerified(true);
    advertiser.setRole(createAdvertiserRole());
    advertiser.setPostbackUrl(appUtils.generateAdvertiserPostbackUrl(advertiser.getAdvertiserId()));
    advertiser.setStatus(AdvertiserStatus.AWAIT_APPROVAL);

    return advertiserRepository.save(advertiser);
  }

  private AppRoles createAdvertiserRole() {
    return roleRepository
        .findByName(Role.ADVERTISER)
        .orElseGet(
            () -> {
              AppRoles appRoles = new AppRoles();
              appRoles.setName(Role.ADVERTISER);
              return roleRepository.save(appRoles);
            });
  }

  @Override
  public Advertiser getAdvertiserDetailsById(String id) {
    return advertiserRepository.findByAdvertiserId(id).orElseThrow();
  }

  @Override
  public Page<Advertiser> getAllAdvertisers(
      String businessName, AdvertiserStatus status, Pageable pageable) {
    return advertiserRepository.getAllAdvertisers(businessName, status, pageable);
  }

  @Override
  public List<AdvertiserConversionDTO> getAdvertiserConversions(
      String username, AdvertiserConversionReportRequestDTO dto) {
    Advertiser advertiser = advertiserRepository.findByEmail(username).orElse(null);

    assert advertiser != null;

    dto.setAdvertiserName(advertiser.getBusinessName());
    List<AdvertiserConversionDTO> response =
        notificationService.getAdvertiserConversionsForAdvertiser(dto);

    for (AdvertiserConversionDTO conversion : response) {
      conversion.setAmountSpent(conversion.getAmountSpent());
      Double cr = ((double) conversion.getConversions() / conversion.getClicks()) * 100;
      if (Double.isNaN(cr)) {
        cr = 0.0;
      }
      conversion.setCr(String.format("%.2f", cr));
      Double eCPM = ((double) (conversion.getAmountSpent() / 100) / conversion.getClicks()) * 1000;
      if (Double.isNaN(eCPM)) {
        eCPM = 0.0;
      }
      conversion.setECPM(String.format("%.2f", eCPM));
    }

    return response;
  }

  @Override
  public void updateAdvertiser(Advertiser advertiser) {
    this.advertiserRepository.save(advertiser);
  }

  @Override
  public void deleteAdvertiser(Advertiser advertiser) {
    this.advertiserRepository.delete(advertiser);
  }

  @Override
  public List<AdvertiserChurnReportDTO> generateChurnReport(
      String username, AdvertiserChurnReportRequestDTO reportRequestDTO) {
    Advertiser advertiser = advertiserRepository.findByEmail(username).orElse(null);
    return notificationService.getAdvertiserChurnAndAcquisition(advertiser, reportRequestDTO);
  }

  @Override
  public List<AutoFillDTO> getCampaignsForAdvertiser(String username) {
    Advertiser advertiser = advertiserRepository.findByEmail(username).orElse(null);
    return campaignService.getActiveCampaigns(advertiser).stream()
        .map(s -> new AutoFillDTO(s.getName(), s.getCampaignId()))
        .toList();
  }

  @Override
  public void refreshToken(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String refreshToken = authHeader.substring(7);
      String userEmail = this.jwtUtils.extractUsername(refreshToken);
      if (userEmail != null) {
        Advertiser advertiser = advertiserRepository.findByEmail(userEmail).orElseThrow();
        if (this.jwtUtils.validateJwtToken(refreshToken)) {
          String accessToken = jwtUtils.generateJwtToken(advertiser);
          AuthenticationResponse authResponse =
              AuthenticationResponse.builder()
                  .token(accessToken)
                  .refreshToken(refreshToken)
                  .build();
          (new ObjectMapper()).writeValue(response.getOutputStream(), authResponse);
        }
      }
    }
  }

  @Override
  public Advertiser save(Advertiser advertiser) {
    return advertiserRepository.save(advertiser);
  }

  @Override
  public long totalBulkSMSAdvertisers() {
    return advertiserRepository.countAdvertisersWithBulkSmsEnabled();
  }

  @Override
  public long totalActiveBulkSMSAdvertisers() {
    return advertiserRepository.countAdvertisersWithBulkSmsEnabledAndActive();
  }

  @Override
  public List<Advertiser> getAllBulkSMSAdvertisers(Pageable pageable) {
    return advertiserRepository.findAllByBulkSmsEnabledAdvertiser(pageable).getContent();
  }
}
