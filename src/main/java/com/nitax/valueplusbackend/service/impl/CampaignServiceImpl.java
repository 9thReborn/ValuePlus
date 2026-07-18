package com.nitax.valueplusbackend.service.impl;

import com.nitax.valueplusbackend.domain.Advertiser;
import com.nitax.valueplusbackend.domain.Campaign;
import com.nitax.valueplusbackend.domain.PublisherCampaign;
import com.nitax.valueplusbackend.dto.CampaignDetailsDTO;
import com.nitax.valueplusbackend.dto.CampaignMetricsDTO;
import com.nitax.valueplusbackend.dto.request.*;
import com.nitax.valueplusbackend.dto.response.AdvertiserConversionDTOForTop;
import com.nitax.valueplusbackend.dto.response.AutoFillDTO;
import com.nitax.valueplusbackend.dto.response.PublisherCampaignDetailsDTO;
import com.nitax.valueplusbackend.dto.response.RetentionStatsResponseDto;
import com.nitax.valueplusbackend.exception.ResourceNotFoundException;
import com.nitax.valueplusbackend.repository.CampaignRepository;
import com.nitax.valueplusbackend.service.*;
import com.nitax.valueplusbackend.utils.AppUtils;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CampaignServiceImpl implements CampaignService {

  private final CampaignRepository campaignRepository;
  private final UserDetailsService userDetailsService;
  private final NotificationService notificationService;
  private final AppUtils appUtils;
  private final ImageStorageService imageStorageService;
  private final EmailService emailService;
  private final PublisherCampaignService publisherCampaignService;

  @Override
  public Campaign createCampaign(CreateCampaignDTO createCampaignDTO) throws IOException {
    Advertiser advertiser = userDetailsService.getAdvertiserFromSecurityContext();

    Campaign campaign = new Campaign();
    campaign.setAdvertiser(advertiser);
    campaign.setName(createCampaignDTO.getCampaignName().trim());
    campaign.setUrl(createCampaignDTO.getCampaignUrl().trim());
    campaign.setReach(0L);
    campaign.setCampaignId(appUtils.generateCampaignId());
    campaign.setCampaignCost(0.0);
    campaign.setCpaCampaignCost(0.0);
    campaign.setCpaCostPerUser(0.3);
    campaign.setCostPerUser(0.25);
    campaign.setAcquisition(0L);
    campaign.setAgeRange(createCampaignDTO.getAgeRange());
    campaign.setGender(createCampaignDTO.getPreferredGender());
    campaign.setBudget(createCampaignDTO.getCampaignBudget());
    campaign.setTrafficQuality(createCampaignDTO.getTrafficQuality());
    campaign.setCarrierConnection(createCampaignDTO.getCarrierConnection());
    campaign.setDailyCap(createCampaignDTO.getDailyBudget());
    campaign.setInterest(createCampaignDTO.getInterests());
    campaign.setCountry(createCampaignDTO.getCountry());
    campaign.setImage(createCampaignDTO.getCampaignImage());
    campaign.setObjective(createCampaignDTO.getObjective());
    campaign.setStartDate(createCampaignDTO.getStartDate());
    campaign.setEndDate(createCampaignDTO.getEndDate());
    campaign.setType(createCampaignDTO.getCampaignType());
    campaign.setStatus(createCampaignDTO.getStatus());
    campaign.setClickFlow(createCampaignDTO.getClickFlow());
    campaign.setConnectionType(createCampaignDTO.getConnectionType());
    campaign.setPayoutModel(createCampaignDTO.getPayoutModel());
    campaign.setRestrictionType(createCampaignDTO.getRestrictionType());
    return campaignRepository.save(campaign);
  }

  @Override
  @Cacheable(value = "campaigns", key = "#campaignId")
  public Optional<Campaign> findCampaignById(String campaignId) {
    return campaignRepository.findByCampaignId(campaignId);
  }

  @Override
  public void saveCampaign(Campaign campaign) {
    campaignRepository.save(campaign);
  }

  public void calculateCampaignCostForCurrentMonth() {
    List<Campaign> campaigns = campaignRepository.findAll();

    for (Campaign campaign : campaigns) {
      //      long conversions =
      //          notificationService.getTotalConversionsForCurrentMonth(campaign.getCampaignId());
      //
      //      long clicks =
      // notificationService.getTotalClicksForCurrentMonth(campaign.getCampaignId());
      //
      //      double campaignCost = campaign.getCostPerUser() * conversions;
      //      double cpaCost = notificationService.getCpaCostForCampaign(campaign.getCampaignId());

      CampaignMetricsDTO campaignMetricsDTO =
          notificationService.calculateCampaignCostForCurrentMonth(campaign.getCampaignId());
      campaign.setCampaignCost(campaignMetricsDTO.getVpCost() * 100);
      campaign.setCpaCampaignCost(campaignMetricsDTO.getCpaCost() * 100);
      campaign.setAcquisition(campaignMetricsDTO.getConversions());
      campaign.setReach(campaignMetricsDTO.getClicks());
      saveCampaign(campaign);
    }
  }

  @Async
  public void sendBudgetUsageReminders() {
    List<Campaign> campaigns = campaignRepository.findAll();

    for (Campaign campaign : campaigns) {
      // Get today's date
      LocalDate today = LocalDate.now(ZoneId.of("UTC"));

      Instant lastTime50ReminderSent = campaign.getLast50PercentReminderAt();
      LocalDate last50ReminderDate = null;
      if (lastTime50ReminderSent != null) {
        last50ReminderDate = lastTime50ReminderSent.atZone(ZoneId.of("UTC")).toLocalDate();
      }
      Instant lastTime75ReminderSent = campaign.getLast75PercentReminderAt();
      LocalDate last75ReminderDate = null;
      if (lastTime75ReminderSent != null) {
        last75ReminderDate = lastTime75ReminderSent.atZone(ZoneId.of("UTC")).toLocalDate();
      }
      Instant lastTime90ReminderSent = campaign.getLast90PercentReminderAt();
      LocalDate last90ReminderDate = null;
      if (lastTime90ReminderSent != null) {
        last90ReminderDate = lastTime90ReminderSent.atZone(ZoneId.of("UTC")).toLocalDate();
      }
      Instant lastTime100ReminderSent = campaign.getLast100PercentReminderAt();
      LocalDate last100ReminderDate = null;
      if (lastTime100ReminderSent != null) {
        last100ReminderDate = lastTime100ReminderSent.atZone(ZoneId.of("UTC")).toLocalDate();
      }

      if (Objects.isNull(lastTime50ReminderSent) || !last50ReminderDate.isEqual(today)) {
        if ((campaign.getCampaignCost() / 100) >= (campaign.getBudget() * 0.5)
            && campaign.getCampaignCost() < (campaign.getBudget() * 0.75)) {
          emailService.send50PercentUsageNotificationToAdvertiser(
              campaign.getAdvertiser(), campaign);
          campaign.setLast50PercentReminderAt(Instant.now());
          campaignRepository.save(campaign);
        }
      }
      if (Objects.isNull(lastTime75ReminderSent) || !last75ReminderDate.isEqual(today)) {
        if ((campaign.getCampaignCost() / 100) >= (campaign.getBudget() * 0.75)
            && campaign.getCampaignCost() < (campaign.getBudget() * 0.9)) {
          emailService.send75PercentUsageNotificationToAdvertiser(
              campaign.getAdvertiser(), campaign);
          campaign.setLast75PercentReminderAt(Instant.now());
          campaignRepository.save(campaign);
        }
      }
      if (Objects.isNull(lastTime90ReminderSent) || !last90ReminderDate.isEqual(today)) {
        if ((campaign.getCampaignCost() / 100) >= (campaign.getBudget() * 0.9)
            && campaign.getCampaignCost() < (campaign.getBudget() * 1.0)) {
          emailService.send90PercentUsageNotificationToAdvertiser(
              campaign.getAdvertiser(), campaign);
          campaign.setLast90PercentReminderAt(Instant.now());
          campaignRepository.save(campaign);
        }
      }
      if (Objects.isNull(lastTime100ReminderSent) || !last100ReminderDate.isEqual(today)) {
        if ((campaign.getCampaignCost() / 100) >= (campaign.getBudget() * 1.0)) {
          emailService.send100PercentUsageNotificationToAdvertiser(
              campaign.getAdvertiser(), campaign);
          campaign.setLast100PercentReminderAt(Instant.now());
          campaignRepository.save(campaign);
        }
      }
    }
  }

  @Override
  public List<Campaign> findAll() {
    return campaignRepository.findAll();
  }

  @Override
  public Long getActiveCampaignsForAdmin() {
    return campaignRepository.getActiveCampaignsForAdmin();
  }

  @Override
  public Long getPausedCampaignsForAdmin() {
    return campaignRepository.getPausedCampaignsForAdmin();
  }

  @Override
  public Long getDisabledCampaignsForAdmin() {
    return campaignRepository.getDisabledCampaignsForAdmin();
  }

  @Override
  public List<AdvertiserConversionDTOForTop> getTopFiveCampaignsForAdmin() {
    List<AdvertiserConversionDTOForTop> advertiserConversionDTO =
        campaignRepository.findTop5ByAcquisitionTest(PageRequest.of(0, 5));

    for (AdvertiserConversionDTOForTop conversion : advertiserConversionDTO) {
      long amountSpent = (long) ((conversion.getCpa() * conversion.getConversions()) * 100);
      conversion.setAmountSpent(Long.toString(amountSpent));
      Double cr = ((double) conversion.getConversions() / conversion.getClicks()) * 100;
      conversion.setCr(String.format("%.2f", cr));
      Double eCPM = ((double) (amountSpent / 100) / conversion.getClicks()) * 1000;
      conversion.setECPM(String.format("%.2f", eCPM));
    }

    return advertiserConversionDTO;
  }

  @Override
  public List<AdvertiserConversionDTOForTop> findLeast5ByAcquisition(
      List<String> top5CampaignNames) {
    List<AdvertiserConversionDTOForTop> advertiserConversionDTO =
        campaignRepository.findLeast5ByAcquisitionTest(top5CampaignNames, PageRequest.of(0, 5));

    for (AdvertiserConversionDTOForTop conversion : advertiserConversionDTO) {
      long amountSpent = (long) ((conversion.getCpa() * conversion.getConversions()) * 100);
      conversion.setAmountSpent(Long.toString(amountSpent));
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

    return advertiserConversionDTO;
    //    return campaignRepository.findLeast5ByAcquisition(top5CampaignIds, PageRequest.of(0, 5));
  }

  @Override
  public Page<Campaign> getAllCampaignsForAdmin(CampaignFilter filter, Pageable pageable) {
    Instant startDate = AppUtils.localDateToInstant(filter.getStartDate());
    Instant endDate = AppUtils.localDateToInstant(filter.getEndDate());

    // I can't figure out why the query won't work when dates are null, so I'm hardcoding the start
    // and end date values if they're null
    if (Objects.isNull(startDate)) {
      startDate =
          LocalDate.of(LocalDate.now().getYear(), 1, 1)
              .atStartOfDay(ZoneId.systemDefault())
              .toInstant();
    }

    if (Objects.isNull(endDate)) {
      endDate =
          LocalDate.of(LocalDate.now().getYear(), 12, 31)
              .atStartOfDay(ZoneId.systemDefault())
              .plusDays(1)
              .minusNanos(1)
              .toInstant();
    } else {
      // set endDate to the end of the day
      endDate =
          LocalDate.of(
                  filter.getEndDate().getYear(),
                  filter.getEndDate().getMonth(),
                  filter.getEndDate().getDayOfMonth())
              .atStartOfDay(ZoneId.of("UTC"))
              .plusDays(1)
              .minusNanos(1)
              .toInstant();
    }
    return campaignRepository.getAllCampaignsWithFiltersForAdmin(
        filter.getName(),
        filter.getCountry(),
        filter.getCampaignType(),
        filter.getStatus(),
        pageable);
  }

  @Override
  @CachePut(value = "campaigns", key = "#existingCampaign.campaignId")
  @CacheEvict(value = "campaigns", key = "#existingCampaign.campaignId", condition = "#result == null")
  public Campaign editCampaignForAdmmin(UpdateCampaignForAdminDTO dto, Campaign existingCampaign) {
    if (existingCampaign == null) {
      throw new ResourceNotFoundException("Campaign not found");
    }

    existingCampaign.setName(dto.getCampaignName().trim());
    existingCampaign.setUrl(dto.getCampaignUrl().trim());
    existingCampaign.setAgeRange(dto.getAgeRange());
    existingCampaign.setGender(dto.getPreferredGender());
    existingCampaign.setBudget(dto.getCampaignBudget());
    existingCampaign.setTrafficQuality(dto.getTrafficQuality());
    existingCampaign.setCarrierConnection(dto.getCarrierConnection());
    existingCampaign.setDailyCap(dto.getDailyBudget());
    existingCampaign.setInterest(dto.getInterests());
    existingCampaign.setCountry(dto.getCountry());
    existingCampaign.setImage(dto.getCampaignImage());
    existingCampaign.setObjective(dto.getObjective());
    existingCampaign.setStartDate(dto.getStartDate());
    existingCampaign.setEndDate(dto.getEndDate());
    existingCampaign.setType(dto.getCampaignType());
    existingCampaign.setCostPerUser(dto.getCostPerUser());
    existingCampaign.setCpaCostPerUser(dto.getCpaCostPerUser());
    existingCampaign.setClickFlow(dto.getFlow());

    existingCampaign.setConnectionType(dto.getConnectionType());
    existingCampaign.setPayoutModel(dto.getPayoutModel());
    existingCampaign.setRestrictionType(dto.getRestrictionType());
    return campaignRepository.save(existingCampaign);
  }

  @Override
  public Campaign createCampaignForAdmin(CreateCampaignForAdminDTO dto, Advertiser advertiser) {
    Campaign campaign = new Campaign();
    campaign.setAdvertiser(advertiser);
    campaign.setName(dto.getCampaignName().trim());
    campaign.setUrl(dto.getCampaignUrl().trim());
    campaign.setReach(0L);
    campaign.setCampaignId(appUtils.generateCampaignId());
    campaign.setCampaignCost(0.0);
    campaign.setCpaCampaignCost(0.0);
    campaign.setCpaCostPerUser(dto.getCpaCostPerUser());
    campaign.setCostPerUser(dto.getCostPerUser());
    campaign.setAcquisition(0L);
    campaign.setAgeRange(dto.getAgeRange());
    campaign.setGender(dto.getPreferredGender());
    campaign.setBudget(dto.getCampaignBudget());
    campaign.setTrafficQuality(dto.getTrafficQuality());
    campaign.setCarrierConnection(dto.getCarrierConnection());
    campaign.setDailyCap(dto.getDailyBudget());
    campaign.setInterest(dto.getInterests());
    campaign.setCountry(dto.getCountry());
    campaign.setImage(dto.getCampaignImage());
    campaign.setObjective(dto.getObjective());
    campaign.setStartDate(dto.getStartDate());
    campaign.setEndDate(dto.getEndDate());
    campaign.setType(dto.getCampaignType());
    campaign.setStatus(dto.getStatus());
    campaign.setClickFlow(dto.getFlow());
    campaign.setRestrictionType(dto.getRestrictionType());
    campaign.setPayoutModel(dto.getPayoutModel());
    campaign.setConnectionType(dto.getConnectionType());

    return campaignRepository.save(campaign);
  }

  @Override
  public Campaign pauseCampaign(CampaignPauseRequest dto, String campaignId) {
    Campaign existingCampaign = campaignRepository.findByCampaignId(campaignId).orElse(null);

    if (existingCampaign == null || existingCampaign.isDeleted()) {
      throw new ResourceNotFoundException("Campaign not found");
    }

    existingCampaign.setStatus("INACTIVE");
    existingCampaign.setPauseReason(dto.getPauseReason());

    return campaignRepository.save(existingCampaign);
  }

  @Override
  public Campaign enableCampaign(String campaignId) {
    Campaign existingCampaign = campaignRepository.findByCampaignId(campaignId).orElse(null);

    if (existingCampaign == null || existingCampaign.isDeleted()) {
      throw new ResourceNotFoundException("Campaign not found");
    }

    existingCampaign.setStatus("ACTIVE");

    return campaignRepository.save(existingCampaign);
  }

  @Override
  @CacheEvict(value = "campaigns", key = "#campaignId")
  public Campaign deleteCampaignForAdmin(DeleteCampaignRequestDto dto, String campaignId) {
    Campaign existingCampaign = campaignRepository.findByCampaignId(campaignId).orElse(null);

    if (existingCampaign == null) {
      throw new ResourceNotFoundException("Campaign not found");
    }

    existingCampaign.setDeleted(true);
    existingCampaign.setStatus("DELETED");
    existingCampaign.setDeletedAt(Instant.now());
    existingCampaign.setDeleteReason(dto.getDeleteReason());

    return campaignRepository.save(existingCampaign);
  }

  @Override
  public Campaign disableCampaign(String campaignId, CampaignDsableRequest dto) {
    Campaign existingCampaign = campaignRepository.findByCampaignId(campaignId).orElse(null);

    if (existingCampaign == null || existingCampaign.isDeleted()) {
      throw new ResourceNotFoundException("Campaign not found");
    }

    existingCampaign.setIsDisabled(true);
    existingCampaign.setStatus("INACTIVE");
    existingCampaign.setDisableReason(dto.getDisableReason());
    existingCampaign.setDisabledAt(Instant.now());

    return campaignRepository.save(existingCampaign);
  }

  @Override
  public List<Campaign> getAllEmptyMappedCampaigned() {
    return campaignRepository.getAllUnMappedCampaigns();
  }

  @Override
  public String getSectorByCampaignId(String campaignId) {
    Campaign campaign = campaignRepository.findByCampaignId(campaignId).orElse(null);

    if (campaign == null) {
      throw new ResourceNotFoundException("Campaign not found");
    }

    return campaign.getInterest() == null ? "Others" : campaign.getInterest();
  }

  @Override
  public List<AutoFillDTO> autoFillCampaign(String keys) {
    return campaignRepository.findCampaignByQuery(keys)
            .stream().map(s -> new AutoFillDTO((String) s[0],//name
                    (String) s[1])).toList();
  }

  @Override
  public PublisherCampaignDetailsDTO fetchCampaignByCampaignName(String campaignName) {
    Optional<Campaign> optionalCampaign = campaignRepository.findByName(campaignName);
    if (optionalCampaign.isEmpty()){
      throw new ResourceNotFoundException("Campaign not found");
    }
    PublisherCampaignDetailsDTO publisherCampaignDetailsDTO =  new PublisherCampaignDetailsDTO();
    publisherCampaignDetailsDTO.setCampaignName(campaignName);
    publisherCampaignDetailsDTO.setCampaignStatus(optionalCampaign.get().getStatus());
    publisherCampaignDetailsDTO.setCampaignLink(optionalCampaign.get().getUrl());
    publisherCampaignDetailsDTO.setCampaignId(optionalCampaign.get().getCampaignId());
    publisherCampaignDetailsDTO.setMNO(optionalCampaign.get().getCarrierConnection());
    publisherCampaignDetailsDTO.setGender(optionalCampaign.get().getGender());
    publisherCampaignDetailsDTO.setCountry(optionalCampaign.get().getCountry());
    publisherCampaignDetailsDTO.setAgeRange(optionalCampaign.get().getAgeRange());
    publisherCampaignDetailsDTO.setInterest(optionalCampaign.get().getInterest());
    publisherCampaignDetailsDTO.setStartDate(String.valueOf(optionalCampaign.get().getStartDate()));
    publisherCampaignDetailsDTO.setEndDate(String.valueOf(optionalCampaign.get().getEndDate()));

    return publisherCampaignDetailsDTO;
  }

  @Override
  public long getTotalSpendForPreviousMonthForAdvertiser(Advertiser advertiser) {
    long totalSpend = 0;

    long conversionCount =
        notificationService.getTotalSpendForPreviousMonthForAdvertiser(advertiser);

    return conversionCount * 100;
  }

  @Override
  public long getTotalSpendForCurrentMonthForAdvertiser(Advertiser advertiser) {
    List<Campaign> campaigns = campaignRepository.findByAdvertiserId(advertiser.getId());
    long totalSpend = 0;
    for (Campaign campaign : campaigns) {
      totalSpend += (long) (campaign.getCampaignCost() * 1);
    }
    return totalSpend;
  }

  @Override
  public CampaignDetailsDTO getTotalAmountOwedByAdvertiser(Long advertiserId) {
    List<Campaign> campaigns = campaignRepository.findByAdvertiserId(advertiserId);

    double totalCampaignCost = 0.0;
    double totalCpaCampaignCost = 0.0;

    List<CampaignDetailsDTO> campaignDetailsList =
        campaigns.stream()
            .map(
                campaign -> {
                  CampaignDetailsDTO campaignDetails = new CampaignDetailsDTO();
                  campaignDetails.setCampaignCost(campaign.getCampaignCost());
                  campaignDetails.setCpaCampaignCost(campaign.getCpaCampaignCost());
                  return campaignDetails;
                })
            .toList();

    for (CampaignDetailsDTO campaignDetails : campaignDetailsList) {
      totalCampaignCost += campaignDetails.getCampaignCost();
      totalCpaCampaignCost += campaignDetails.getCpaCampaignCost();
    }

    CampaignDetailsDTO totalAmountOwed = new CampaignDetailsDTO();
    totalAmountOwed.setCampaignCost(totalCampaignCost);
    totalAmountOwed.setCpaCampaignCost(totalCpaCampaignCost);

    return totalAmountOwed;
  }

  @Override
  public List<Campaign> findCampaignByAdvertiserId(Long advertiserId) {
    return campaignRepository.findByAdvertiserId(advertiserId);
  }

  @Override
  public Long getNumberOfActiveCampaigns(Advertiser advertiser) {
    return campaignRepository.getNumberOfActiveCampaigns(advertiser);
  }

  @Override
  public Long getNumberOfCampaigns(Advertiser advertiser) {
    return campaignRepository.getNumberOfCampaigns(advertiser);
  }


  @Override
  public List<Campaign> getActiveCampaigns(Advertiser advertiser) {
    return campaignRepository.getActiveCampaigns(advertiser);
  }

  @Override
  public List<Campaign> getAllCampaigns() {
    return campaignRepository.findAll();
  }

  @Override
  public RetentionStatsResponseDto getRetentionStatsForDays(RetentionStatDto retentionStatDto) {
    int numberOfDays = retentionStatDto.getNumberOfDays();

    Instant endDate = Instant.now().atOffset(ZoneOffset.UTC).toInstant();

    Instant startDate = endDate.minus(Duration.ofDays(numberOfDays));

    long numberOfUnsubscribers =
        notificationService.getCountOfUnsubscribersWithDateRangeAndOptionalCampaignId(
            startDate, endDate, retentionStatDto.getCampaignId());

    long numberOfSubscribers =
        notificationService.getCountOfTotalSubscribersWithDateRangeAndOptionalCampaignId(
            startDate, endDate, retentionStatDto.getCampaignId());

    double percentageOfUnSubscribers = 0.0;

    if (numberOfUnsubscribers > 0) {
      percentageOfUnSubscribers = ((double) numberOfUnsubscribers / numberOfSubscribers) * 100;
    }

    DecimalFormat df = new DecimalFormat("#.##");
    String formattedPercentageCommon = df.format(percentageOfUnSubscribers);

    RetentionStatsResponseDto stats = new RetentionStatsResponseDto();
    stats.setNumberOfUnsubscribers(numberOfUnsubscribers);
    stats.setNumberOfAllSubscribers(numberOfSubscribers);
    stats.setPercentageOfUnsubscribers(Double.parseDouble(formattedPercentageCommon));

    return stats;
  }

  @Override
  public long[] getTotalCampaignsStats(Advertiser advertiser) {
    return campaignRepository.getTotalCampaignsStats(advertiser.getId());
  }

  @Override
  public long getCampaignCountForYesterday(Advertiser advertiser) {
    Instant today = Instant.now();

    return campaignRepository.getNumberOfCampaignsPreviousDays(advertiser, today);
  }

  @Override
  public long getActiveCampaignCountForYesterday(Advertiser advertiser) {
    Instant today = Instant.now();

    return campaignRepository.getNumberOfActiveCampaignsPreviousDays(advertiser, today);
  }

  @Override
  public long[] getActiveCampaignsStats(Advertiser advertiser) {
    return campaignRepository.getActiveCampaignsStats(advertiser.getId());
  }

  @Override
  public long getTotalSpendForPreviousMonth(Long advertiserId) {
    List<Campaign> campaigns = campaignRepository.findByAdvertiserId(advertiserId);
    long totalSpend = 0;
    for (Campaign campaign : campaigns) {
      long conversionCount =
          notificationService.getTotalSpendForPreviousMonth(campaign.getCampaignId());

      double costPerUser = campaign.getCostPerUser();
      long campaignCost = (long) ((costPerUser * conversionCount) * 100);

      totalSpend += campaignCost;
    }
    return totalSpend;
  }

  @Override
  public long getTotalSpendForCurrentMonth(Long advertiserId) {
    List<Campaign> campaigns = campaignRepository.findByAdvertiserId(advertiserId);
    long totalSpend = 0;
    for (Campaign campaign : campaigns) {
      totalSpend += campaign.getCampaignCost();
    }
    return totalSpend;
  }

  @Override
  public long[] getTotalCampaignsSpendStats(Advertiser advertiser) {
    ZoneId zoneId = ZoneId.of("UTC");

    LocalDate firstDayOf4MonthsAgo = LocalDate.now(zoneId).minusMonths(4).withDayOfMonth(1);

    LocalDateTime startOfMonth = firstDayOf4MonthsAgo.atTime(0, 0, 0);
    ZonedDateTime startOfMonthZoned = startOfMonth.atZone(zoneId);

    int currentYear = LocalDate.now(zoneId).getYear();
    double[] rawStats =
        campaignRepository.getTotalCampaignsSpendStats(
            advertiser.getId(), currentYear, startOfMonthZoned.toInstant(), Instant.now());
    long[] convertedStats = new long[rawStats.length];

    // Convert each cost_per_user value to long by multiplying by 100
    for (int i = 0; i < rawStats.length; i++) {
      convertedStats[i] = Math.round(rawStats[i] * 100);
    }

    return convertedStats;
  }

  @Override
  public long[] getConversionData(Long advertiserId, Integer year) {
    return campaignRepository.getMonthlyConversionData(advertiserId, year);
  }

  @Override
  public long[] getClickData(Long id, Integer year) {
    return campaignRepository.getClickData(id, year);
  }

  @Override
  public int[] getMonthlyConversionMonths(Long id, Integer year) {
    return campaignRepository.getMonthlyConversionMonths(id, year);
  }

  @Override
  public Page<Campaign> findAllCampaigns(
      CampaignFilter filter, Pageable pageable, Advertiser advertiser) {
    String name = filter.getName();

    return campaignRepository.findAllByAdvertiserWithFilters(
        advertiser,
        name,
        filter.getCountry(),
        filter.getCampaignType(),
        filter.getStatus(),
        pageable);
  }

  @Override
  public Campaign getCampaignDetails(String campaignId) {
    Optional<Campaign> campaign = campaignRepository.findByCampaignId(campaignId);
    if (campaign.isPresent()){
      return campaign.get();
    }
    throw new ResourceNotFoundException("Campaign not found");
  }

  @Override
  public String deactivateCampaign(String campaignId) {
    String reason = "Campaign paused by advertiser";
    campaignRepository.deactivateCampaign(campaignId);
    Campaign campaign = campaignRepository.findByCampaignId(campaignId).orElse(null);
    List<PublisherCampaign> publishersPushingCampaign = publisherCampaignService.getAllPublisherCampaignForCampaign(campaignId);
    emailService.sendCampaignPausedMail(campaign, reason);
    emailService .sendCampaignPausedMailToPublishers(publishersPushingCampaign,reason);

    return "Campaign Deactivated";
  }

  @Override
  public String activateCampaign(String campaignId) {
    campaignRepository.activateCampaign(campaignId);
    Campaign campaign = campaignRepository.findByCampaignId(campaignId).orElse(null);
    List<PublisherCampaign> publishersPushingCampaign = publisherCampaignService.getAllPublisherCampaignForCampaign(campaignId);
    emailService.sendCampaignEnabledMail(campaign);
    emailService .sendCampaignActivatedMailToPublishers(publishersPushingCampaign,"Advertiser activated campaigned");
    return "Campaign Activated";
  }

  @Override
  public String uploadCampaignImage(MultipartFile file) throws IOException {
    return imageStorageService.saveFile(file);
  }

  @Override
  @CachePut(value = "campaigns", key = "#campaign.campaignId")
  @CacheEvict(value = "campaigns", key = "#campaign.campaignId", condition = "#result != null")
  public Campaign editCampaign(UpdateCampaignDTO editCampaignDTO, Campaign campaign) {
    if (campaign == null) {
      throw new ResourceNotFoundException("Campaign not found");
    }

    campaign.setName(editCampaignDTO.getCampaignName().trim());
    campaign.setUrl(editCampaignDTO.getCampaignUrl().trim());
    campaign.setAgeRange(editCampaignDTO.getAgeRange());
    campaign.setGender(editCampaignDTO.getPreferredGender());
    campaign.setBudget(editCampaignDTO.getCampaignBudget());
    campaign.setTrafficQuality(editCampaignDTO.getTrafficQuality());
    campaign.setCarrierConnection(editCampaignDTO.getCarrierConnection());
    campaign.setDailyCap(editCampaignDTO.getDailyBudget());
    campaign.setInterest(editCampaignDTO.getInterests());
    campaign.setCountry(editCampaignDTO.getCountry());
    campaign.setImage(editCampaignDTO.getCampaignImage());
    campaign.setObjective(editCampaignDTO.getObjective());
    campaign.setStartDate(editCampaignDTO.getStartDate());
    campaign.setEndDate(editCampaignDTO.getEndDate());
    campaign.setType(editCampaignDTO.getCampaignType());
    campaign.setApproved(false);
    campaign.setClickFlow(editCampaignDTO.getClickFlow());
    return campaignRepository.save(campaign);
  }

  @Override
  @CacheEvict(value = "campaigns", key = "#campaignId")
  public String deleteCampaign(String campaignId) {
    Campaign campaign = campaignRepository.findByCampaignId(campaignId).orElse(null);

    if (campaign == null) {
      throw new ResourceNotFoundException("Campaign not found");
    }

    campaign.setDeleted(true);
    campaign.setStatus("DELETED");
    campaign.setDeletedAt(Instant.now());

    campaignRepository.save(campaign);

    return "Campaign Deleted";
  }
}
