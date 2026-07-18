package com.nitax.valueplusbackend.service.impl;

import static com.nitax.valueplusbackend.utils.Constants.POINT_VALUE_NAIRA;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nitax.valueplusbackend.config.JwtUtils;
import com.nitax.valueplusbackend.domain.Admin;
import com.nitax.valueplusbackend.domain.Advertiser;
import com.nitax.valueplusbackend.domain.AdvertiserStatus;
import com.nitax.valueplusbackend.domain.AppRoles;
import com.nitax.valueplusbackend.domain.CPASettings;
import com.nitax.valueplusbackend.domain.Campaign;
import com.nitax.valueplusbackend.domain.Publisher;
import com.nitax.valueplusbackend.domain.PublisherCampaign;
import com.nitax.valueplusbackend.domain.SMSLog;
import com.nitax.valueplusbackend.domain.Wallet;
import com.nitax.valueplusbackend.dto.AuthenticationResponse;
import com.nitax.valueplusbackend.dto.CampignUrlDto;
import com.nitax.valueplusbackend.dto.request.AdminLoginRequest;
import com.nitax.valueplusbackend.dto.request.AdvertiserConversionRequestDTO;
import com.nitax.valueplusbackend.dto.request.AdvertiserSignupDTO;
import com.nitax.valueplusbackend.dto.request.AdvertiserUpdateRequest;
import com.nitax.valueplusbackend.dto.request.CPASettingRequest;
import com.nitax.valueplusbackend.dto.request.CampaignDsableRequest;
import com.nitax.valueplusbackend.dto.request.CampaignFilter;
import com.nitax.valueplusbackend.dto.request.CampaignPauseRequest;
import com.nitax.valueplusbackend.dto.request.ChurnReportRequestDTO;
import com.nitax.valueplusbackend.dto.request.CreateCampaignForAdminDTO;
import com.nitax.valueplusbackend.dto.request.CreatePublisherDTO;
import com.nitax.valueplusbackend.dto.request.DeleteCampaignRequestDto;
import com.nitax.valueplusbackend.dto.request.FundWalletRequest;
import com.nitax.valueplusbackend.dto.request.PublisherCampaignRequest;
import com.nitax.valueplusbackend.dto.request.PublisherConversionRequestDTO;
import com.nitax.valueplusbackend.dto.request.RecordStatus;
import com.nitax.valueplusbackend.dto.request.SendSMSByList;
import com.nitax.valueplusbackend.dto.request.SmsPointInfoResponse;
import com.nitax.valueplusbackend.dto.request.UpdateCampaignForAdminDTO;
import com.nitax.valueplusbackend.dto.request.UpdatePublisherDTO;
import com.nitax.valueplusbackend.dto.response.AdminBulkSmsDashboardSummary;
import com.nitax.valueplusbackend.dto.response.AdvertiserConversionCpaBreakdownDTO;
import com.nitax.valueplusbackend.dto.response.AdvertiserConversionDTO;
import com.nitax.valueplusbackend.dto.response.AdvertiserConversionDTOForTop;
import com.nitax.valueplusbackend.dto.response.AdvertiserNameResponse;
import com.nitax.valueplusbackend.dto.response.AutoFillDTO;
import com.nitax.valueplusbackend.dto.response.BulkSMSAdvertiserResponse;
import com.nitax.valueplusbackend.dto.response.ChurnReport;
import com.nitax.valueplusbackend.dto.response.CommonSubscriberStats;
import com.nitax.valueplusbackend.dto.response.LoginResponse;
import com.nitax.valueplusbackend.dto.response.MonthlyConversionCount;
import com.nitax.valueplusbackend.dto.response.PublisherCampaignConversionsDTO;
import com.nitax.valueplusbackend.dto.response.PublisherConversionsDTO;
import com.nitax.valueplusbackend.dto.response.SearchPostbackDto;
import com.nitax.valueplusbackend.exception.AppException;
import com.nitax.valueplusbackend.repository.AdminRepository;
import com.nitax.valueplusbackend.repository.RoleRepository;
import com.nitax.valueplusbackend.service.AdminService;
import com.nitax.valueplusbackend.service.AdvertiserService;
import com.nitax.valueplusbackend.service.CPAService;
import com.nitax.valueplusbackend.service.CampaignService;
import com.nitax.valueplusbackend.service.ChurnReportService;
import com.nitax.valueplusbackend.service.EmailService;
import com.nitax.valueplusbackend.service.NotificationService;
import com.nitax.valueplusbackend.service.PublisherCampaignService;
import com.nitax.valueplusbackend.service.PublisherService;
import com.nitax.valueplusbackend.service.SMSService;
import com.nitax.valueplusbackend.service.WalletService;
import com.nitax.valueplusbackend.utils.enums.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

  private final NotificationService notificationService;
  private final CampaignService campaignService;
  private final AuthenticationManager authenticationManager;
  private final JwtUtils jwtUtils;
  private final SMSService smsService;
  private final AdminRepository adminRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder encoder;
  private final PublisherService publisherService;
  private final EmailService emailService;
  private final AdvertiserService advertiserService;
  private final PublisherCampaignService publisherCampaignService;
  private final CPAService cpaService;
  private final ChurnReportService churnReportService;
  //    private final BulkSmsCampaignService bulkSmsCampaignService;
  private final WalletService walletService;

  @Value("${app.clicks-url}")
  private String clicksUrl;

  @EventListener(ApplicationReadyEvent.class)
  public void createAdminAccount() {
    AppRoles superAdmin = roleRepository.findByName(Role.SUPER_ADMIN).get();
    if (adminRepository.findByEmail("superadmin@valueplusagency.com").isEmpty()) {
      Admin admin = new Admin();
      admin.setFirstName("Super");
      admin.setLastName("Admin");
      admin.setEmail("superadmin@valueplusagency.com");
      admin.setPassword(encoder.encode("superadminpassword"));
      admin.setRole(superAdmin);
      adminRepository.save(admin);
    }
  }

  @Override
  public CommonSubscriberStats getCommonSubscribersForCampaigns() {
    long numOfCommonSubs = notificationService.getCountOfCommonSubscribersForCampaigns();
    long numOfUniqueSubs = notificationService.getCountOfUniqueSubscribersForCampaigns();

    double percentageOfCommonSubscribers = 0.0;

    if (numOfUniqueSubs > 0) {
      percentageOfCommonSubscribers = ((double) numOfCommonSubs / numOfUniqueSubs) * 100;
    }

    DecimalFormat df = new DecimalFormat("#.##");
    String formattedPercentageCommon = df.format(percentageOfCommonSubscribers);

    CommonSubscriberStats stats = new CommonSubscriberStats();
    stats.setNumberOfCommonSubscribers(numOfCommonSubs);
    stats.setNumberOfUniqueSubscribers(numOfUniqueSubs);
    stats.setPercentageOfCommonSubscribers(Double.parseDouble(formattedPercentageCommon));

    return stats;
  }

  @Override
  public CommonSubscriberStats getCommonSubscribersForPublishers() {
    long numOfCommonSubs = notificationService.getCountOfCommonSubscribersForPublishers();
    long numOfUniqueSubs = notificationService.getCountOfUniqueSubscribersForPublishers();

    double percentageOfCommonSubscribers = 0.0;

    if (numOfUniqueSubs > 0) {
      percentageOfCommonSubscribers = ((double) numOfCommonSubs / numOfUniqueSubs) * 100;
    }

    DecimalFormat df = new DecimalFormat("#.##");
    String formattedPercentageCommon = df.format(percentageOfCommonSubscribers);

    CommonSubscriberStats stats = new CommonSubscriberStats();
    stats.setNumberOfCommonSubscribers(numOfCommonSubs);
    stats.setNumberOfUniqueSubscribers(numOfUniqueSubs);
    stats.setPercentageOfCommonSubscribers(Double.parseDouble(formattedPercentageCommon));

    return stats;
  }

  @Override
  public SMSLog sendSMSByList(SendSMSByList dto) {
    return smsService.sendBulkSmsFromList(dto);
  }

  @Override
  public LoginResponse adminLogin(AdminLoginRequest dto) {
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = jwtUtils.generateJwtToken(authentication);
    String refreshToken = jwtUtils.generateRefreshToken(authentication);

    return new LoginResponse(jwt, refreshToken);
  }

  @Override
  public Long getActiveCampaigns() {
    return campaignService.getActiveCampaignsForAdmin();
  }

  @Override
  public Long getPausedCampaigns() {
    return campaignService.getPausedCampaignsForAdmin();
  }

  @Override
  public Long getDisabledCampaigns() {
    return campaignService.getDisabledCampaignsForAdmin();
  }

  @Override
  public List<AdvertiserConversionDTOForTop> getTopFiveCampaigns() {
    return campaignService.getTopFiveCampaignsForAdmin();
  }

  @Override
  public List<AdvertiserConversionDTOForTop> getLeastFiveCampaigns() {
    List<AdvertiserConversionDTOForTop> top5Campaigns = getTopFiveCampaigns();
    List<String> top5CampaignNames =
        top5Campaigns.stream().map(AdvertiserConversionDTOForTop::getCampaignId).toList();

    return campaignService.findLeast5ByAcquisition(top5CampaignNames);
  }

  @Override
  public List<MonthlyConversionCount> getCampaignPerformanceOverview() {
    List<MonthlyConversionCount> rawCounts = notificationService.getCampaignPerformanceOverview();
    Map<YearMonth, Long> countsMap = new HashMap<>();

    for (MonthlyConversionCount count : rawCounts) {
      YearMonth yearMonth = YearMonth.of(count.getYear(), count.getMonth());
      countsMap.put(yearMonth, count.getCount());
    }

    List<MonthlyConversionCount> completeCounts = new ArrayList<>();
    YearMonth currentMonth = YearMonth.now();
    YearMonth startMonth = YearMonth.of(currentMonth.getYear(), 1);
    YearMonth endMonth = YearMonth.of(YearMonth.now().getYear(), 12);

    for (YearMonth month = startMonth; !month.isAfter(endMonth); month = month.plusMonths(1)) {
      long count = countsMap.getOrDefault(month, 0L);
      completeCounts.add(new MonthlyConversionCount(month.getYear(), month.getMonthValue(), count));
    }

    return completeCounts;
  }

  @Override
  public List<MonthlyConversionCount> getCampaignPerformanceOverviewClicks() {
    List<MonthlyConversionCount> rawCounts =
        notificationService.getCampaignPerformanceOverviewClicks();
    Map<YearMonth, Long> countsMap = new HashMap<>();

    for (MonthlyConversionCount count : rawCounts) {
      YearMonth yearMonth = YearMonth.of(count.getYear(), count.getMonth());
      countsMap.put(yearMonth, count.getCount());
    }

    List<MonthlyConversionCount> completeCounts = new ArrayList<>();
    YearMonth currentMonth = YearMonth.now();
    YearMonth startMonth = YearMonth.of(currentMonth.getYear(), 1);
    YearMonth endMonth = YearMonth.of(YearMonth.now().getYear(), 12);

    for (YearMonth month = startMonth; !month.isAfter(endMonth); month = month.plusMonths(1)) {
      long count = countsMap.getOrDefault(month, 0L);
      completeCounts.add(new MonthlyConversionCount(month.getYear(), month.getMonthValue(), count));
    }

    return completeCounts;
  }

  @Override
  public Page<Campaign> getAllCampaigns(CampaignFilter filter, Pageable pageable) {
    return campaignService.getAllCampaignsForAdmin(filter, pageable);
  }

  @Override
  public Campaign getCampaignById(String id) {
    return campaignService.getCampaignDetails(id);
  }

  @Override
  public List<AdvertiserConversionDTO> getAdvertiserConversions(
      AdvertiserConversionRequestDTO dto) {
    List<AdvertiserConversionDTO> conversions =
        notificationService.getAdvertiserConversionsForAdmin(dto);
    for (AdvertiserConversionDTO conversion : conversions) {
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

    return conversions.stream()
        .filter(
            s -> {
              if (dto.getConversionType() == RecordStatus.ALL) {
                return true;
              } else if (dto.getConversionType() == RecordStatus.ACTIVE) {
                return s.getStatus().equalsIgnoreCase("ACTIVE");
              } else if (dto.getConversionType() == RecordStatus.INACTIVE) {
                return s.getStatus().equalsIgnoreCase("INACTIVE");
              }
              return true;
            })
        .collect(Collectors.toList());
  }

  @Override
  public List<PublisherCampaignConversionsDTO> getPublishersCampaignConversions(
      PublisherConversionRequestDTO dto) {
    List<PublisherCampaignConversionsDTO> conversions =
        notificationService.getPublishersCampaignConversions(dto);
    for (PublisherCampaignConversionsDTO conversion : conversions) {
      long amountSpent = (long) ((conversion.getCPA() * conversion.getConversions()) * 100);
      conversion.setAmountSpent(String.format("%.2f", amountSpent / 100.0));
      Double cr = ((double) conversion.getConversions() / conversion.getClicks()) * 100;
      if (Double.isNaN(cr)) {
        cr = 0.0;
      }
      conversion.setCr(String.format("%.2f", cr));

      Double eCPM = ((double) (amountSpent / 100) / conversion.getClicks()) * 1000;
      if (Double.isNaN(eCPM)) {
        eCPM = 0.0;
      }
      conversion.setECPM(String.format("%.2f", eCPM));
    }

    conversions.sort(
        Comparator.comparingLong(PublisherCampaignConversionsDTO::getConversions).reversed());

    int rank = 1;
    for (PublisherCampaignConversionsDTO conversion : conversions) {
      conversion.setRank(rank++);
    }

    return conversions;
  }

  @Override
  public List<PublisherConversionsDTO> getPublishersConversions(PublisherConversionRequestDTO dto) {
    List<PublisherConversionsDTO> conversions = notificationService.getPublishersConversions(dto);

    for (PublisherConversionsDTO conversion : conversions) {
      long amountSpent = (long) ((conversion.getCPA() * conversion.getConversions()) * 100);
      conversion.setAmountSpent(Long.toString(amountSpent));
      Double cr = ((double) conversion.getConversions() / conversion.getClicks()) * 100;
      if (Double.isNaN(cr)) {
        cr = 0.0;
      }
      conversion.setCr(cr);
      Double eCPM = ((double) (amountSpent / 100) / conversion.getClicks()) * 1000;
      if (Double.isNaN(eCPM)) {
        eCPM = 0.0;
      }
      conversion.setECPM(eCPM);
    }
    Map<String, PublisherConversionsDTO> publisherMap = new HashMap<>();
    for (PublisherConversionsDTO conversion : conversions) {
      if (publisherMap.containsKey(conversion.getPublisherName())) {
        PublisherConversionsDTO existingConversion =
            publisherMap.get(conversion.getPublisherName());
        existingConversion.setAmountSpent(
            Long.toString(
                Long.parseLong(existingConversion.getAmountSpent())
                    + Long.parseLong(conversion.getAmountSpent())));
        existingConversion.setClicks(existingConversion.getClicks() + conversion.getClicks());
        existingConversion.setChurn(existingConversion.getChurn() + conversion.getChurn());
        existingConversion.setConversions(
            existingConversion.getConversions() + conversion.getConversions());
        existingConversion.setCr(existingConversion.getCr() + conversion.getCr());
        existingConversion.setECPM(existingConversion.getECPM() + conversion.getECPM());
      } else {
        publisherMap.put(conversion.getPublisherName(), conversion);
      }
    }

    List<PublisherConversionsDTO> finalConversions = new ArrayList<>(publisherMap.values());

    finalConversions.sort(
        Comparator.comparingLong(PublisherConversionsDTO::getConversions).reversed());

    int rank = 1;
    for (PublisherConversionsDTO conversion : finalConversions) {
      conversion.setRank(rank++);
    }

    return finalConversions;
  }

  @Override
  public List<CampignUrlDto> getCampaignUrls(String campaignId) {
    List<Publisher> publishers = publisherService.getAllPublishers();

    List<CampignUrlDto> campignUrlDtos = new ArrayList<>();

    Optional<Campaign> campaign = campaignService.findCampaignById(campaignId);

    if (campaign.isPresent()) {
      for (Publisher publisher : publishers) {
        String url =
            clicksUrl
                + campaign.get().getCampaignId()
                + "?trxId="
                + campaign.get().getCampaignId()
                + "_"
                + publisher.getPubId()
                + "_"
                + publisher.getClickIdParameter()
                + "&sourceId="
                + publisher.getSourceIdParameter();
        CampignUrlDto campignUrlDto = new CampignUrlDto();
        campignUrlDto.setPublisherName(publisher.getName());
        campignUrlDto.setPublisherUrl(url);
        campignUrlDto.setCampaignUrl(campaign.get().getUrl());
        campignUrlDtos.add(campignUrlDto);
      }
      return campignUrlDtos;
    }

    throw new AppException("Campaign not found");
  }

  @Override
  @Transactional
  public Campaign updateCampaign(String campaignId, UpdateCampaignForAdminDTO dto) {
    Campaign existingCampaign =
        campaignService
            .findCampaignById(campaignId)
            .orElseThrow(() -> new AppException("Campaign not found"));

    Campaign backup = new Campaign(existingCampaign);

    Campaign updatedCampaign = campaignService.editCampaignForAdmmin(dto, existingCampaign);

    if (Objects.nonNull(updatedCampaign)) {
      String changesSummary = generateChangesSummary(backup, updatedCampaign);
      emailService.sendCampaignUpdatedMail(
          updatedCampaign, updatedCampaign.getAdvertiser(), changesSummary);
      if (changesSummary.contains("cpaCostPerUser")) {
        // map campaign to all publishers if changes Summary has an update
        List<Publisher> allPublishers = publisherService.getAllPublishers();
        for (Publisher publisher : allPublishers) {
          publisherCampaignService.updatePublisherCampaign(
              publisher, updatedCampaign, dto.getCpaCostPerUser());
        }
      }
      return updatedCampaign;
    }

    throw new AppException("Error updating campaign");
  }

  @Override
  public String uploadCampaignImage(MultipartFile file) throws IOException {
    return campaignService.uploadCampaignImage(file);
  }

  @Override
  public Campaign createCampaign(CreateCampaignForAdminDTO dto) {
    Advertiser adveriser = advertiserService.findAdvertiserById(dto.getAdvertiserId());
    if (Objects.isNull(adveriser)) {
      throw new AppException("Advertiser not found");
    }
    Campaign campaign = campaignService.createCampaignForAdmin(dto, adveriser);
    // map campaign to all publishers
    List<Publisher> allPublishers = publisherService.getAllPublishers();
    for (Publisher publisher : allPublishers) {
      publisherCampaignService.createPublisherCampaign(
          publisher, campaign, campaign.getCpaCostPerUser());
    }
    //    emailService.sendCampaignCreatedMail(campaign, campaign.getAdvertiser());
    return campaign;
  }

  @Override
  public Campaign pauseCampaign(CampaignPauseRequest dto, String campaignId) {
    Campaign pausedCampaign = campaignService.pauseCampaign(dto, campaignId);
    List<PublisherCampaign> publishers =
        publisherCampaignService.getAllPublisherCampaignForCampaign(campaignId);
    emailService.sendCampaignPausedMail(pausedCampaign, dto.getPauseReason());
    emailService.sendCampaignPausedMaiToPublishers(publishers, dto.getPauseReason());
    return pausedCampaign;
  }

  @Override
  public Campaign enableCampaign(String campaignId) {
    Campaign enabledCampaign = campaignService.enableCampaign(campaignId);
    List<PublisherCampaign> publishers =
        publisherCampaignService.getAllPublisherCampaignForCampaign(campaignId);
    //        emailService.sendCampaignEnabledMail(enabledCampaign);
    //
    // emailService.sendCampaignEnabledMailToPublishersPushingCampaign(publishers,enabledCampaign);
    return enabledCampaign;
  }

  @Override
  public String deleteCampaign(DeleteCampaignRequestDto dto, String campaignId) {
    Campaign deletedCampaign = campaignService.deleteCampaignForAdmin(dto, campaignId);
    emailService.sendCampaignDeletedMail(deletedCampaign, dto.getDeleteReason());
    return "Campaign deleted successfully";
  }

  @Override
  public Campaign disableCampaign(String campaignId, CampaignDsableRequest dto) {
    Campaign disabledCampaign = campaignService.disableCampaign(campaignId, dto);

    emailService.sendCampaignDisabledMail(disabledCampaign, dto.getDisableReason());
    return disabledCampaign;
  }

  @Override
  public List<AdvertiserNameResponse> getAdvertiserNames() {
    return advertiserService.getAllAdvertisersName();
  }

  @Override
  public String createAdvertiser(AdvertiserSignupDTO signupDTO) {
    Advertiser advertiser = advertiserService.createAdvertiserForAdmin(signupDTO);

    return "Advertiser created successfully";
  }

  @Override
  public Advertiser getAdvertiserInfo(String id) {
    return advertiserService.getAdvertiserDetailsById(id);
  }

  @Override
  public List<SearchPostbackDto> getPostbacks(String transactionId, Pageable pageable) {
    return notificationService.getPostbacks(transactionId, pageable);
  }

  @Override
  public Page<Advertiser> getAllAdvertisers(
      String businessName, AdvertiserStatus status, Pageable pageable) {
    return advertiserService.getAllAdvertisers(businessName, status, pageable);
  }

  private String generateChangesSummary(Campaign original, Campaign updated) {
    StringBuilder changes = new StringBuilder("The following changes were made to the campaign:\n");

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
  public String approveAdvertiser(String id) {
    var advertiser = this.advertiserService.getAdvertiserDetailsById(id);
    if (advertiser != null) {
      advertiser.setStatus(AdvertiserStatus.APPROVED);
      this.advertiserService.updateAdvertiser(advertiser);
      this.emailService.sendNotificationToAdvertiser(advertiser);
      return "APPROVED";
    } else {
      return "REJECTED";
    }
  }

  @Override
  public String rejectAdvertiser(String id) {
    var advertiser = this.advertiserService.getAdvertiserDetailsById(id);
    if (advertiser != null) {
      advertiser.setStatus(AdvertiserStatus.REJECTED);
      this.advertiserService.updateAdvertiser(advertiser);
      this.emailService.sendNotificationToAdvertiser(advertiser);
      return "REJECTED";
    } else {
      return "NOT AVAILABLE";
    }
  }

  @Override
  public String suspendAdvertiser(String id, String suspensionReason) {
    var advertiser = this.advertiserService.getAdvertiserDetailsById(id);
    if (advertiser != null) {
      advertiser.setStatus(AdvertiserStatus.SUSPENDED);
      this.advertiserService.updateAdvertiser(advertiser);
      this.emailService.sendNotificationToAdvertiserReject(advertiser, suspensionReason);
      return "SUSPENDED";
    } else {
      return "REJECTED";
    }
  }

  @Override
  public String deleteAdvertiser(String id) {
    var advertiser = this.advertiserService.getAdvertiserDetailsById(id);
    if (advertiser != null) {
      this.advertiserService.deleteAdvertiser(advertiser);
      return "DONE";
    } else {
      return "REJECTED";
    }
  }

  @Override
  public String savePublisher(CreatePublisherDTO publisherDTO) {
    // publisher created
    return this.publisherService.savePublisher(publisherDTO).getPostbackUrl();
  }

  @Override
  public String updatePublisher(String pubId, UpdatePublisherDTO publisherDTO) {
    return this.publisherService.updatePublisher(pubId, publisherDTO);
  }

  // publisher Campaign
  @Override
  public List<Publisher> getAllPublishers() {
    return this.publisherService.getAllPublishers();
  }

  @Override
  public Publisher findByPubId(String pubId) {
    return this.publisherService.findByPubId(pubId);
  }

  @Override
  public String approvePublisher(String id) {
    return this.publisherService.approvePublisher(id);
  }

  @Override
  public String suspendPublisher(String id) {
    return this.publisherService.suspendPublisher(id);
  }

  @Override
  public String deletePublisher(String id) {
    return this.publisherService.deletePublisher(id);
  }

  @Override
  public PublisherCampaign createPublisherCampaign(PublisherCampaignRequest request) {
    return publisherCampaignService.createPublisherCampaign(request);
  }

  @Override
  public PublisherCampaign updatePublisherCampaign(
      String pubCampId, PublisherCampaignRequest request) {
    return publisherCampaignService.updatePublisherCampaign(pubCampId, request);
  }

  @Override
  public List<PublisherCampaign> getPublisherCampaigns(String publisherId, String campaignId) {
    return publisherCampaignService.getPublisherCampaigns(publisherId, campaignId);
  }

  @Override
  public void deletePublisherCampaign(String pubCampId) {
    publisherCampaignService.deletePublisherCampaign(pubCampId);
  }

  // cpa
  @Override
  public CPASettings createCPASetting(CPASettingRequest request) {
    return cpaService.create(request);
  }

  @Override
  public CPASettings updateCPASetting(String cpaId, CPASettingRequest request) {
    return cpaService.update(cpaId, request);
  }

  @Override
  public List<CPASettings> getCPAList(String cpaId) {
    return cpaService.getAll(cpaId);
  }

  @Override
  public void deleteCPA(String cpaId) {
    cpaService.deleteById(cpaId);
  }

  @Override
  public List<CPASettings> getCPAList(String country, String mno, String flow, String flowType) {
    return cpaService.getAll(country, mno, flow, flowType);
  }

  @Override
  public List<? extends ChurnReport> generateReport(ChurnReportRequestDTO reportRequestDTO) {
    return churnReportService.fetchReports(
        reportRequestDTO.getCampaigns(),
        reportRequestDTO.getPublishers(),
        reportRequestDTO.getStartDate(),
        reportRequestDTO.getEndDate(),
        reportRequestDTO.getChurnTypes(),
        reportRequestDTO.isIncludeSourceId());
  }

  @Override
  public List<AutoFillDTO> autoFillPublisher(String keys) {
    return publisherService.autoFillPublisher(keys);
  }

  @Override
  public List<AutoFillDTO> autoFillCampaigns(String keys) {
    return campaignService.autoFillCampaign(keys);
  }

  @Override
  public Admin getAdminByEmail(String email) {
    return adminRepository.findByEmail(email).orElseThrow();
  }

  @Override
  public void refreshToken(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String refreshToken = authHeader.substring(7);
      String userEmail = this.jwtUtils.extractUsername(refreshToken);
      if (userEmail != null) {
        Admin user = adminRepository.findByEmail(userEmail).orElseThrow();
        if (this.jwtUtils.validateJwtToken(refreshToken)) {
          String accessToken = jwtUtils.generateJwtToken(user);
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
  public AdminBulkSmsDashboardSummary getBulkSmsDashboardSummary() {
    long allBulkSmsAdvertiser = advertiserService.totalActiveBulkSMSAdvertisers();
    long allActiveBulkSmsAdvertiser = advertiserService.totalActiveBulkSMSAdvertisers();
    BigDecimal pointBalance = walletService.getTotalWalletPoints();
    BigDecimal cpa = POINT_VALUE_NAIRA;

    AdminBulkSmsDashboardSummary summary = new AdminBulkSmsDashboardSummary();
    summary.setTotalBulkSMSAdvertisers(allBulkSmsAdvertiser);
    summary.setAverageCpa(cpa);
    summary.setActiveBulkSMSAdvertisers(allActiveBulkSmsAdvertiser);
    summary.setTotalBulkSMSPoints(pointBalance);
    return summary;
  }

  @Override
  public List<BulkSMSAdvertiserResponse> getBulkSMSAdvertisers(int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    List<Advertiser> advertisers = advertiserService.getAllBulkSMSAdvertisers(pageable);
    List<BulkSMSAdvertiserResponse> res = new ArrayList<>();

    advertisers.forEach(
        advertiser -> {
          BulkSMSAdvertiserResponse response = new BulkSMSAdvertiserResponse();
          response.setStatus(advertiser.getStatus().name());
          response.setEmail(advertiser.getEmail());
          response.setBusinessName(advertiser.getBusinessName());
          response.setSkype(advertiser.getSkype());
          response.setPointBalance(
              walletService.fetchWalletPointBalanceByAdvertiserId(advertiser.getId()));
          response.setCpa(POINT_VALUE_NAIRA.doubleValue());
          response.setAdvertiserId(advertiser.getAdvertiserId());
          res.add(response);
        });

    return res;
  }

  @Override
  public SmsPointInfoResponse getSmsPointInfo(String advertiserId, BigDecimal amount) {
    SmsPointInfoResponse smsPointInfoResponse = new SmsPointInfoResponse();
    smsPointInfoResponse.setCostPerSms(POINT_VALUE_NAIRA);
    smsPointInfoResponse.setPointsToBeAssigned(amount.multiply(POINT_VALUE_NAIRA));
    return smsPointInfoResponse;
  }

  @Override
  public void fundWallet(FundWalletRequest request) {
    walletService.fundWallet(request);
  }

  @Override
  public SmsPointInfoResponse getAdvertiserSmsPointInfo(String advertiserId) {
    Wallet wallet = walletService.getWalletByAdvertiserId(advertiserId);
    SmsPointInfoResponse smsPointInfoResponse = new SmsPointInfoResponse();
    smsPointInfoResponse.setCostPerSms(POINT_VALUE_NAIRA);
    smsPointInfoResponse.setPointBalance(wallet.getPointsBalance());
    return smsPointInfoResponse;
  }

  @Override
  public Advertiser updateAdvertiser(String advertiserId, AdvertiserUpdateRequest request) {
    Advertiser advertiser = advertiserService.getAdvertiserDetailsById(advertiserId);
    advertiser.setFirstName(request.getFirstName());
    advertiser.setLastName(request.getLastName());
    advertiser.setBusinessName(request.getBusinessName());
    advertiser.setEmail(request.getEmail());
    advertiser.setCountry(request.getCountry());
    advertiser.setSkype(request.getSkype());
    advertiser.setStatus(Objects.requireNonNull(checkStatus(request.getStatus())));
    return advertiserService.save(advertiser);
  }

  private AdvertiserStatus checkStatus(String status) {

    if (status.equalsIgnoreCase("APPROVED")) {
      return AdvertiserStatus.APPROVED;
    } else if (status.equalsIgnoreCase("SUSPENDED")) {
      return AdvertiserStatus.SUSPENDED;
    } else if (status.equalsIgnoreCase("REJECTED")) {
      return AdvertiserStatus.REJECTED;
    } else if (status.equalsIgnoreCase("UNVERIFIED")) {
      return AdvertiserStatus.UNVERIFIED;
    } else if (status.equalsIgnoreCase("AWAIT_APPROVAL")) {
      return AdvertiserStatus.AWAIT_APPROVAL;
    } else if (status.equalsIgnoreCase("ACTIVE")) {
      return AdvertiserStatus.ACTIVE;
    } else if (status.equalsIgnoreCase("INACTIVE")) {
      return AdvertiserStatus.INACTIVE;
    }

    return null;
  }

  @Override
  public List<AdvertiserConversionCpaBreakdownDTO> getAdvertiserConversionsCpaBreakdown(
      AdvertiserConversionRequestDTO dto) {
    return notificationService.getAdvertiserConversionsCpaBreakdown(dto);
  }
}
