package com.nitax.valueplusbackend.service.impl;

import com.nitax.valueplusbackend.domain.Advertiser;
import com.nitax.valueplusbackend.domain.Notification;
import com.nitax.valueplusbackend.dto.CampaignMetricsDTO;
import com.nitax.valueplusbackend.dto.ChurnType;
import com.nitax.valueplusbackend.dto.NotificationDto;
import com.nitax.valueplusbackend.dto.PubChurnReportDto;
import com.nitax.valueplusbackend.dto.PublisherConversionReportDto;
import com.nitax.valueplusbackend.dto.ReportingChartDto;
import com.nitax.valueplusbackend.dto.RetentionReportDto;
import com.nitax.valueplusbackend.dto.request.AdvertiserChurnReportRequestDTO;
import com.nitax.valueplusbackend.dto.request.AdvertiserConversionReportRequestDTO;
import com.nitax.valueplusbackend.dto.request.AdvertiserConversionRequestDTO;

import com.nitax.valueplusbackend.dto.request.PublisherConversionRequestDTO;
import com.nitax.valueplusbackend.dto.request.ReportChartRequestDto;
import com.nitax.valueplusbackend.dto.request.ReportSummaryRequestDto;
import com.nitax.valueplusbackend.dto.response.AdvertiserChurnReportDTO;
import com.nitax.valueplusbackend.dto.response.AdvertiserConversionCpaBreakdownDTO;
import com.nitax.valueplusbackend.dto.response.AdvertiserConversionDTO;
import com.nitax.valueplusbackend.dto.response.ChurnReport;
import com.nitax.valueplusbackend.dto.response.ChurnReportDTO;
import com.nitax.valueplusbackend.dto.response.ChurnReportDTOSourced;
import com.nitax.valueplusbackend.dto.AdminChurnReportDto;
import com.nitax.valueplusbackend.dto.response.PublisherChurnRecordDTO;
import com.nitax.valueplusbackend.dto.response.MonthlyConversionCount;
import com.nitax.valueplusbackend.dto.response.PublisherCampaignConversionsDTO;
import com.nitax.valueplusbackend.dto.response.PublisherConversionsDTO;
import com.nitax.valueplusbackend.dto.response.ReportingSummaryDto;
import com.nitax.valueplusbackend.dto.response.SearchPostbackDto;
import com.nitax.valueplusbackend.repository.NotificationRepository;
import com.nitax.valueplusbackend.service.NotificationService;
import com.nitax.valueplusbackend.utils.AppUtils;
import java.sql.Date;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

  private final NotificationRepository notificationRepository;

  //  private final notificationRepository notificationRepository;

  @Override
  public Notification saveNotification(Notification notification) {
    return notificationRepository.save(notification);
  }

  @Override
  public Optional<Notification> findClickByShortTrxId(String shortTrxId) {
    if (shortTrxId == null || shortTrxId.isEmpty() || shortTrxId.contains("_")) {
      return Optional.empty();
    }
    return notificationRepository.findFirstByShortTrxIdAndStatusOrderByCreatedDateDesc(
        shortTrxId, Notification.NotificationStatus.PUBLISHER_HOOK_RECEIVED);
  }

  @Override
  public boolean existsByTransactionId(String transactionId) {
    return notificationRepository.existsByTransactionIdAndStatusIn(
        transactionId,
        List.of(
            Notification.NotificationStatus.ADVERTISER_HOOK_RECEIVED,
            Notification.NotificationStatus.PUBLISHER_HOOK_SENT));
  }

  @Override
  public boolean existsByMsisdn(String msisdn) {
    return notificationRepository.existsByMsisdnAndStatusIn(
        msisdn,
        List.of(
            Notification.NotificationStatus.ADVERTISER_HOOK_RECEIVED,
            Notification.NotificationStatus.PUBLISHER_HOOK_SENT));
  }

  @Override
  public boolean existsUnsubscriptionByTransactionIdAndMsisdn(String transactionId, String msisdn) {
    return notificationRepository.existsByTransactionIdAndMsisdnAndStatus(
        transactionId, msisdn, Notification.NotificationStatus.UNSUBSCRIBED);
  }

  @Override
  public Notification findByTrxIdAndProductId(String trxId, String productId) {
    return notificationRepository.findBytransactionIdAndProductId(trxId, productId).orElse(null);
  }

  @Override
  public Notification findTopByMsisdnOrderByCreatedAtDesc(String msisdn) {
    Notification notification =
        notificationRepository
            .findTopByMsisdnAndDurationIsNullAndTransactionIdOrderByCreatedDateDescFromArchive(
                msisdn);

    return notification;
  }

  @Override
  public long getTotalSpendForPreviousMonth(String campaignId) {
    ZoneId zoneId = ZoneId.of("UTC");

    LocalDate firstDayOfPreviousMonth = LocalDate.now(zoneId).withDayOfMonth(1).minusMonths(1);
    LocalDate lastDayOfPreviousMonth =
        firstDayOfPreviousMonth.withDayOfMonth(firstDayOfPreviousMonth.lengthOfMonth());

    LocalDateTime startOfMonth = firstDayOfPreviousMonth.atTime(0, 0, 0);
    LocalDateTime endOfMonth = lastDayOfPreviousMonth.atTime(23, 59, 59);

    ZonedDateTime startOfMonthZoned = startOfMonth.atZone(zoneId);
    ZonedDateTime endOfMonthZoned = endOfMonth.atZone(zoneId);

    return notificationRepository.countConversionsByCampaignIdWithDateRange(
        campaignId, startOfMonthZoned.toInstant(), endOfMonthZoned.toInstant());
  }

  @Override
  public List<Notification> findAllMonthly() {
    ZoneId zoneId = ZoneId.of("UTC");

    LocalDate firstDayOfPreviousMonth = LocalDate.now(zoneId).withDayOfMonth(1).minusMonths(1);
    LocalDate lastDayOfPreviousMonth =
        firstDayOfPreviousMonth.withDayOfMonth(firstDayOfPreviousMonth.lengthOfMonth());

    LocalDateTime startOfMonth = firstDayOfPreviousMonth.atTime(0, 0, 0);
    LocalDateTime endOfMonth = lastDayOfPreviousMonth.atTime(23, 59, 59);

    ZonedDateTime startOfMonthZoned = startOfMonth.atZone(zoneId);
    ZonedDateTime endOfMonthZoned = endOfMonth.atZone(zoneId);
    return notificationRepository.findByStatusWithDateRange(
        startOfMonthZoned.toInstant(), endOfMonthZoned.toInstant());
  }

  @Override
  public List<NotificationDto> getWeeklyUnsubscribersForPublisher(String pubId) {
    ZoneId zoneId = ZoneId.of("UTC");

    LocalDate now = LocalDate.now(zoneId);
    LocalDate startOfWeek =
        now.minusDays(now.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue());
    LocalDate endOfWeek = startOfWeek.plusDays(6);

    LocalDateTime startDate = startOfWeek.atStartOfDay();
    LocalDateTime endDate = endOfWeek.atTime(23, 59, 59);

    ZonedDateTime startOfDayZoned = startDate.atZone(zoneId);
    ZonedDateTime endOfDayZoned = endDate.atZone(zoneId);

    return notificationRepository.findUnsubscribersForPublisherWithDateRange(
        startOfDayZoned.toInstant(), endOfDayZoned.toInstant(), pubId);
  }

  @Override
  public List<NotificationDto> getDailyUnsubscribersForPublisher(String pubId) {
    ZoneId zoneId = ZoneId.of("UTC");

    LocalDate yesterday = LocalDate.now(zoneId).minusDays(1);

    LocalDateTime startOfDay = yesterday.atStartOfDay();
    LocalDateTime endOfDay = yesterday.atTime(23, 59, 59);

    ZonedDateTime startOfDayZoned = startOfDay.atZone(zoneId);
    ZonedDateTime endOfDayZoned = endOfDay.atZone(zoneId);

    return notificationRepository.findUnsubscribersForPublisherWithDateRange(
        startOfDayZoned.toInstant(), endOfDayZoned.toInstant(), pubId);
  }

  @Override
  public List<NotificationDto> getMonthlyNotificationsForCampaign(String campaignId) {
    ZoneId zoneId = ZoneId.of("UTC");

    LocalDate firstDayOfPreviousMonth = LocalDate.now(zoneId).withDayOfMonth(1).minusMonths(1);
    LocalDate lastDayOfPreviousMonth =
        firstDayOfPreviousMonth.withDayOfMonth(firstDayOfPreviousMonth.lengthOfMonth());

    LocalDateTime startOfMonth = firstDayOfPreviousMonth.atTime(0, 0, 0);
    LocalDateTime endOfMonth = lastDayOfPreviousMonth.atTime(23, 59, 59);

    ZonedDateTime startOfMonthZoned = startOfMonth.atZone(zoneId);
    ZonedDateTime endOfMonthZoned = endOfMonth.atZone(zoneId);

    return notificationRepository.findNotificationsForCampaignWithDateRange(
        startOfMonthZoned.toInstant(),
        endOfMonthZoned.toInstant(),
        Notification.NotificationStatus.ADVERTISER_HOOK_RECEIVED,
        campaignId);
  }

  @Override
  public List<PublisherConversionReportDto> getMonthlyNotificationsForPublishers(String pubId) {
    ZoneId zoneId = ZoneId.of("UTC");

    LocalDate firstDayOfPreviousMonth = LocalDate.now(zoneId).withDayOfMonth(1).minusMonths(1);
    LocalDate lastDayOfPreviousMonth =
        firstDayOfPreviousMonth.withDayOfMonth(firstDayOfPreviousMonth.lengthOfMonth());

    LocalDateTime startOfMonth = firstDayOfPreviousMonth.atTime(0, 0, 0);
    LocalDateTime endOfMonth = lastDayOfPreviousMonth.atTime(23, 59, 59);

    ZonedDateTime startOfMonthZoned = startOfMonth.atZone(zoneId);
    ZonedDateTime endOfMonthZoned = endOfMonth.atZone(zoneId);

    return notificationRepository.findNotificationsForPublishersWithDateRange(
        startOfMonthZoned.toInstant(), endOfMonthZoned.toInstant(), pubId);
  }

  @Override
  public Long getCountOfDailyNotificationsForAdvertisers(Advertiser advertiser) {
    ZoneId zoneId = ZoneId.of("UTC");

    LocalDate yesterday = LocalDate.now(zoneId).minusDays(1);

    LocalDateTime startOfDay = yesterday.atStartOfDay();
    LocalDateTime endOfDay = yesterday.atTime(23, 59, 59);

    ZonedDateTime startOfDayZoned = startOfDay.atZone(zoneId);
    ZonedDateTime endOfDayZoned = endOfDay.atZone(zoneId);
    return notificationRepository.getCountOfNotificationsForAdvertisersWithDateRange(
        advertiser, startOfDayZoned.toInstant(), endOfDayZoned.toInstant());
  }

  @Override
  public List<NotificationDto> getDailyNotificationsForCampaign(String campaignId) {
    ZoneId zoneId = ZoneId.of("UTC");

    LocalDate yesterday = LocalDate.now(zoneId).minusDays(1);

    LocalDateTime startOfDay = yesterday.atStartOfDay();
    LocalDateTime endOfDay = yesterday.atTime(23, 59, 59);

    ZonedDateTime startOfDayZoned = startOfDay.atZone(zoneId);
    ZonedDateTime endOfDayZoned = endOfDay.atZone(zoneId);

    return notificationRepository.findNotificationsForCampaignWithDateRange(
        startOfDayZoned.toInstant(),
        endOfDayZoned.toInstant(),
        Notification.NotificationStatus.ADVERTISER_HOOK_RECEIVED,
        campaignId);
  }

  @Override
  public Long getCountOfMonthlyNotificationsForAdvertisers(Advertiser advertiser) {
    ZoneId zoneId = ZoneId.of("UTC");

    LocalDate firstDayOfPreviousMonth = LocalDate.now(zoneId).withDayOfMonth(1).minusMonths(1);
    LocalDate lastDayOfPreviousMonth =
        firstDayOfPreviousMonth.withDayOfMonth(firstDayOfPreviousMonth.lengthOfMonth());

    LocalDateTime startOfMonth = firstDayOfPreviousMonth.atTime(0, 0, 0);
    LocalDateTime endOfMonth = lastDayOfPreviousMonth.atTime(23, 59, 59);

    ZonedDateTime startOfMonthZoned = startOfMonth.atZone(zoneId);
    ZonedDateTime endOfMonthZoned = endOfMonth.atZone(zoneId);

    return notificationRepository.getCountOfNotificationsForAdvertisersWithDateRange(
        advertiser, startOfMonthZoned.toInstant(), endOfMonthZoned.toInstant());
  }

  @Override
  public List<NotificationDto> getWeeklyNotificationsForCampaign(String campaignId) {
    ZoneId zoneId = ZoneId.of("UTC");

    LocalDate now = LocalDate.now(zoneId);
    LocalDate startOfWeek =
        now.minusDays(now.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue());
    LocalDate endOfWeek = startOfWeek.plusDays(6);

    LocalDateTime startDate = startOfWeek.atStartOfDay();
    LocalDateTime endDate = endOfWeek.atTime(23, 59, 59);

    ZonedDateTime startOfDayZoned = startDate.atZone(zoneId);
    ZonedDateTime endOfDayZoned = endDate.atZone(zoneId);

    return notificationRepository.findNotificationsForCampaignWithDateRange(
        startOfDayZoned.toInstant(),
        endOfDayZoned.toInstant(),
        Notification.NotificationStatus.ADVERTISER_HOOK_RECEIVED,
        campaignId);
  }

  @Override
  public long getCountOfCommonSubscribersForCampaigns() {
    return notificationRepository.getCountOfCommonSubscribers();
  }

  @Override
  public long getCountOfUniqueSubscribersForCampaigns() {
    return notificationRepository.countDistinctByMsisdnIsNotNull();
  }

  @Override
  public long getCountOfCommonSubscribersForPublishers() {
    return notificationRepository.getCountOfCommonSubscribersForPublishers();
  }

  @Override
  public long getCountOfUniqueSubscribersForPublishers() {
    return notificationRepository.countByMsisdnIsNotNull();
  }

  @Override
  public long getCountOfUnsubscribersWithDateRangeAndOptionalCampaignId(
      Instant startDate, Instant endDate, String campaignId) {
    return notificationRepository.getCountOfUnsubscribersWithDateRangeAndOptionalCampginId(
        startDate, endDate, campaignId);
  }

  @Override
  public long getCountOfTotalSubscribersWithDateRangeAndOptionalCampaignId(
      Instant startDate, Instant endDate, String campaignId) {
    return notificationRepository.getCountOfTotalSubscribersWithDateRangeAndOptionalCampaignId(
        startDate, endDate, campaignId);
  }

  @Override
  @Async
  public void recordClickEvent(
      String campaignId,
      String trxId,
      String shortTrxId,
      String sourceId,
      Double cpaRevenue,
      Double vpRevenue) {
    try {
      if (campaignId == null || trxId == null) {
        log.error("Error recording click event: campaignId or trxId is null");
        log.error("campaignId: {}, trxId: {}", campaignId, trxId);
        return;
      }

      String[] parts = trxId.trim().split("_");
      String publisherId = parts.length > 1 ? parts[1] : null;
      Notification notification = new Notification();
      notification.setCampaignId(campaignId);
      notification.setStatus(Notification.NotificationStatus.PUBLISHER_HOOK_RECEIVED);
      notification.setTransactionId(trxId);
      notification.setShortTrxId(shortTrxId);
      notification.setSourceId(sourceId);
      notification.setCpaRevenue(cpaRevenue);
      notification.setVpRevenue(vpRevenue);
      notification.setPublisherId(publisherId != null ? publisherId : "unknown");
      notification.setYear(LocalDate.now().getYear());
      notification.setMonth(LocalDate.now().getMonthValue());
      notification.setDay(LocalDate.now().getDayOfMonth());
      notificationRepository.save(notification);
    } catch (Exception e) {
      log.error("Error recording click event: {}", e.getMessage());
    }
  }

  @Override
  public List<ReportingChartDto> getAdvertiserClicksReport(
      String advertiserId, ReportChartRequestDto dto) {

    Instant startDate = dto.getStartDate();
    Instant endDate = dto.getEndDate();

    // I can't figure out why the query won't work when dates are null, so I'm hardcoding the start
    // and end date values if they're null
    if (Objects.isNull(startDate)) {
      startDate =
          LocalDate.of(LocalDate.now().getYear(), 1, 1).atStartOfDay(ZoneId.of("UTC")).toInstant();
    }

    if (Objects.isNull(endDate)) {
      endDate =
          LocalDate.of(LocalDate.now().getYear(), 12, 31)
              .atStartOfDay(ZoneId.of("UTC"))
              .plusDays(1)
              .minusNanos(1)
              .toInstant();
    } else {
      // set endDate to the end of the day
      endDate =
          LocalDate.of(
                  AppUtils.InstantToLocalDate(dto.getEndDate()).getYear(),
                  AppUtils.InstantToLocalDate(dto.getEndDate()).getMonth(),
                  AppUtils.InstantToLocalDate(dto.getEndDate()).getDayOfMonth())
              .atStartOfDay(ZoneId.of("UTC"))
              .plusDays(1)
              .minusNanos(1)
              .toInstant();
    }
    List<Object[]> objects =
        notificationRepository.getAdvertiserClicksReportNative(advertiserId, startDate, endDate);

    List<ReportingChartDto> dtoList = new ArrayList<>();
    for (Object[] obj : objects) {
      ReportingChartDto summaryDto =
          ReportingChartDto.builder()
              .day(LocalDateTime.now().withDayOfMonth((Integer) obj[0]).toInstant(ZoneOffset.UTC))
              .count(((Long) obj[1]).longValue())
              .build();
      dtoList.add(summaryDto);
    }
    return dtoList;
  }

  @Override
  public List<ReportingChartDto> getAdvertiserConversionsReport(
      String advertiserId, ReportChartRequestDto dto) {

    Instant startDate = dto.getStartDate();
    Instant endDate = dto.getEndDate();

    // I can't figure out why the query won't work when dates are null, so I'm hardcoding the start
    // and end date values if they're null
    if (Objects.isNull(startDate)) {
      startDate =
          LocalDate.of(LocalDate.now().getYear(), 1, 1).atStartOfDay(ZoneId.of("UTC")).toInstant();
    }

    if (Objects.isNull(endDate)) {
      endDate =
          LocalDate.of(LocalDate.now().getYear(), 12, 31)
              .atStartOfDay(ZoneId.of("UTC"))
              .plusDays(1)
              .minusNanos(1)
              .toInstant();
    } else {
      // set endDate to the end of the day
      endDate =
          LocalDate.of(
                  AppUtils.InstantToLocalDate(dto.getEndDate()).getYear(),
                  AppUtils.InstantToLocalDate(dto.getEndDate()).getMonth(),
                  AppUtils.InstantToLocalDate(dto.getEndDate()).getDayOfMonth())
              .atStartOfDay(ZoneId.of("UTC"))
              .plusDays(1)
              .minusNanos(1)
              .toInstant();
    }
    List<Object[]> objects =
        notificationRepository.getAdvertiserConversionsReportArchiveNative(
            advertiserId, startDate, endDate);

    List<ReportingChartDto> dtoList = new ArrayList<>();
    for (Object[] obj : objects) {
      ReportingChartDto summaryDto =
          ReportingChartDto.builder()
              .day(LocalDateTime.now().withDayOfMonth((Integer) obj[0]).toInstant(ZoneOffset.UTC))
              .count(((Long) obj[1]).longValue())
              .build();
      dtoList.add(summaryDto);
    }

    return dtoList;
  }

  @Override
  public List<ReportingChartDto> getAdvertiserChurnReport(
      String advertiserId, ReportChartRequestDto dto) {

    Instant startDate = dto.getStartDate();
    Instant endDate = dto.getEndDate();

    // I can't figure out why the query won't work when dates are null, so I'm hardcoding the start
    // and end date values if they're null
    if (Objects.isNull(startDate)) {
      startDate =
          LocalDate.of(LocalDate.now().getYear(), 1, 1).atStartOfDay(ZoneId.of("UTC")).toInstant();
    }

    if (Objects.isNull(endDate)) {
      endDate =
          LocalDate.of(LocalDate.now().getYear(), 12, 31)
              .atStartOfDay(ZoneId.of("UTC"))
              .plusDays(1)
              .minusNanos(1)
              .toInstant();
    } else {
      // set endDate to the end of the day
      endDate =
          LocalDate.of(
                  AppUtils.InstantToLocalDate(dto.getEndDate()).getYear(),
                  AppUtils.InstantToLocalDate(dto.getEndDate()).getMonth(),
                  AppUtils.InstantToLocalDate(dto.getEndDate()).getDayOfMonth())
              .atStartOfDay(ZoneId.of("UTC"))
              .plusDays(1)
              .minusNanos(1)
              .toInstant();
    }
    List<Object[]> objects =
        notificationRepository.getAdvertiserChurnReportArchiveNative(
            advertiserId, startDate, endDate);

    List<ReportingChartDto> dtoList = new ArrayList<>();
    for (Object[] obj : objects) {
      ReportingChartDto summaryDto =
          ReportingChartDto.builder()
              .day(LocalDateTime.now().withDayOfMonth((Integer) obj[0]).toInstant(ZoneOffset.UTC))
              .count(((Long) obj[1]).longValue())
              .build();
      dtoList.add(summaryDto);
    }

    return dtoList;
  }

  @Override
  public List<ReportingChartDto> getAdvertiserRetentionReport(
      String advertiserId, ReportChartRequestDto dto) {
    List<ReportingChartDto> churnReport = getAdvertiserChurnReport(advertiserId, dto);

    List<ReportingChartDto> conversionReport = getAdvertiserConversionsReport(advertiserId, dto);

    List<ReportingChartDto> retentionReport = new ArrayList<>();

    for (int i = 0; i < conversionReport.size(); i++) {
      ReportingChartDto retention = new ReportingChartDto();
      retention.setDay(conversionReport.get(i).getDay());
      retention.setCount(
          conversionReport.get(i).getCount()
              - (i < churnReport.size() ? churnReport.get(i).getCount() : 0));
      retentionReport.add(retention);
    }

    return retentionReport;
  }

  @Override
  public List<ReportingSummaryDto> getAdvertiserReportsSummary(
      String advertiserId, ReportSummaryRequestDto dto) {

    Instant startDate = dto.getStartDate();
    Instant endDate = dto.getEndDate();

    // I can't figure out why the query won't work when dates are null, so I'm hardcoding the start
    // and end date values if they're null
    if (Objects.isNull(startDate)) {
      startDate =
          LocalDate.of(LocalDate.now().getYear(), 1, 1).atStartOfDay(ZoneId.of("UTC")).toInstant();
    }

    if (Objects.isNull(endDate)) {
      endDate =
          LocalDate.of(LocalDate.now().getYear(), 12, 31)
              .atStartOfDay(ZoneId.of("UTC"))
              .plusDays(1)
              .minusNanos(1)
              .toInstant();
    } else {
      // set endDate to the end of the day
      endDate =
          LocalDate.of(
                  AppUtils.InstantToLocalDate(dto.getEndDate()).getYear(),
                  AppUtils.InstantToLocalDate(dto.getEndDate()).getMonth(),
                  AppUtils.InstantToLocalDate(dto.getEndDate()).getDayOfMonth())
              .atStartOfDay(ZoneId.of("UTC"))
              .plusDays(1)
              .minusNanos(1)
              .toInstant();
    }
    List<Object[]> objects =
        notificationRepository.getAdvertiserReportsSummaryArchiveNative(
            advertiserId, startDate, endDate);

    List<ReportingSummaryDto> dtoList = new ArrayList<>();
    for (Object[] obj : objects) {
      ReportingSummaryDto summaryDto =
          new ReportingSummaryDto(
              (Integer) obj[0], // Year
              (Integer) obj[1], // Month
              (Double) obj[2], // vp campaignCost
              ((Number) obj[3]).longValue(), // ConversionCount
              ((Number) obj[4]).longValue(), // clickCount
              ((String) obj[5]), // Campaign Name
              ((Number) obj[6]).longValue()); // Campaign Budget
      dtoList.add(summaryDto);
    }
    return dtoList;
  }

  @Override
  public List<MonthlyConversionCount> getCampaignPerformanceOverview() {
    //    return notificationRepository.getCampaignPerformanceOverview();
    List<Object[]> results = notificationRepository.getCampaignPerformanceOverview();
    List<MonthlyConversionCount> monthlyCounts = new ArrayList<>();

    for (Object[] result : results) {
      int year = ((Number) result[0]).intValue();
      int month = ((Number) result[1]).intValue();
      long count = ((Number) result[2]).longValue();

      MonthlyConversionCount dto = new MonthlyConversionCount(year, month, count);
      monthlyCounts.add(dto);
    }

    return monthlyCounts;
  }

  @Override
  public List<MonthlyConversionCount> getCampaignPerformanceOverviewClicks() {
    List<Object[]> results = notificationRepository.getCampaignPerformanceOverviewClicks();
    List<MonthlyConversionCount> monthlyCounts = new ArrayList<>();

    for (Object[] result : results) {
      int year = ((Number) result[0]).intValue();
      int month = ((Number) result[1]).intValue();
      long count = ((Number) result[2]).longValue();

      MonthlyConversionCount dto = new MonthlyConversionCount(year, month, count);
      monthlyCounts.add(dto);
    }

    return monthlyCounts;
  }

  @Override
  public List<AdvertiserConversionDTO> getAdvertiserConversionsForAdmin(
      AdvertiserConversionRequestDTO dto) {
    Instant startDate = dto.getStartDate().toInstant(ZoneOffset.UTC);
    Instant endDate = dto.getEndDate().toInstant(ZoneOffset.UTC);

    // I can't figure out why the query won't work when dates are null, so I'm hardcoding the start
    // and end date values if they're null
    if (Objects.isNull(startDate)) {
      startDate =
          LocalDate.of(LocalDate.now().getYear(), 1, 1).atStartOfDay(ZoneId.of("UTC")).toInstant();
    }

    if (Objects.isNull(endDate)) {
      endDate =
          LocalDate.of(LocalDate.now().getYear(), 12, 31)
              .atStartOfDay(ZoneId.of("UTC"))
              .plusDays(1)
              .minusNanos(1)
              .toInstant();
    } else {
      // set endDate to the end of the day
      endDate =
          LocalDate.of(
                  dto.getEndDate().getYear(),
                  dto.getEndDate().getMonth(),
                  dto.getEndDate().getDayOfMonth())
              .atStartOfDay(ZoneId.of("UTC"))
              .plusDays(1)
              .minusNanos(1)
              .toInstant();
    }
    List<Object[]> objects;

    if (dto.getAdvertiserName() == null || dto.getAdvertiserName().isEmpty()) {
      objects = notificationRepository.getAdvertiserConversionsForAdmin(startDate, endDate);
    } else {
      objects =
          notificationRepository.getAdvertiserConversionsForAdminArchiveWithAdvertiser(
              dto.getAdvertiserName(), startDate, endDate);
    }

    List<AdvertiserConversionDTO> dtoList = new ArrayList<>();
    for (Object[] obj : objects) {
      AdvertiserConversionDTO advertiserConversionDTO =
          new AdvertiserConversionDTO(
              (String) obj[0], // c.status
              (String) obj[1], // a.business_name
              (String) obj[2], // c.name
              ((Number) obj[3]).longValue(), // nonPublisherHookCount
              ((Number) obj[4]).longValue(), // publisherHookReceivedCount
              ((Number) obj[5]).longValue(), // unsubscribedCount
              ((Number) obj[6]).longValue(), // c.budget
              (String) obj[7], // c.country,
              ((Number) obj[8]).doubleValue(), // c.costperuser
              ((Number) obj[9]).doubleValue()); // vp revenue or campaign cost
      dtoList.add(advertiserConversionDTO);
    }

    return dtoList;
  }

  @Override
  public List<PublisherCampaignConversionsDTO> getPublishersCampaignConversions(
      PublisherConversionRequestDTO dto) {
    Instant startDate = dto.getStartDate().toInstant(ZoneOffset.UTC);
    Instant endDate = dto.getEndDate().toInstant(ZoneOffset.UTC);
    boolean poolCurrent =
        (dto.getEndDate().getYear() == LocalDate.now().getYear())
            && (dto.getEndDate().getMonth() == LocalDate.now().getMonth());

    if (Objects.isNull(startDate)) {
      startDate =
          LocalDate.of(LocalDate.now().getYear(), 1, 1).atStartOfDay(ZoneId.of("UTC")).toInstant();
    }

    if (Objects.isNull(endDate)) {
      endDate =
          LocalDate.of(LocalDate.now().getYear(), 12, 31)
              .atStartOfDay(ZoneId.of("UTC"))
              .plusDays(1)
              .minusNanos(1)
              .toInstant();
    } else {
      endDate =
          LocalDate.of(
                  dto.getEndDate().getYear(),
                  dto.getEndDate().getMonth(),
                  dto.getEndDate().getDayOfMonth())
              .atStartOfDay(ZoneId.of("UTC"))
              .plusDays(1)
              .minusNanos(1)
              .toInstant();
    }

    List<Object[]> objects;

    if (dto.getPublisherName() == null || dto.getPublisherName().isEmpty()) {
      objects = notificationRepository.getPublisherConversionsForAdminArchive(startDate, endDate);
    } else {
      objects =
          notificationRepository.getPublisherConversionsForAdminArchiveWithPublisher(
              dto.getPublisherName(), startDate, endDate);
    }

    List<PublisherCampaignConversionsDTO> dtoList = new ArrayList<>();
    for (Object[] obj : objects) {
      PublisherCampaignConversionsDTO publisherCampaignConversionsDTO =
          new PublisherCampaignConversionsDTO(
              (String) obj[0], // publisherName
              (String) obj[1], // campaignName
              ((Number) obj[2]).longValue(), // publisherHookSentOrDurationGreaterThan7Count
              ((Number) obj[3]).longValue(), // publisherHookReceivedCount
              ((Number) obj[4]).longValue(), // durationLessThan8Count
              ((Number) obj[5]).doubleValue(), // cpaCostPerUser
              ((Number) obj[7]).doubleValue() // amountSpent (cpa_revenue sum)
              );
      dtoList.add(publisherCampaignConversionsDTO);
    }

    return dtoList;
  }

  @Override
  public List<Notification> getPendingNotifications() {
    Pageable pageable = Pageable.ofSize(250);
    List<Notification> notifications = notificationRepository.findUnsentConversions(pageable);

    return notifications;
  }

  @Override
  public List<PublisherConversionsDTO> getPublishersConversions(PublisherConversionRequestDTO dto) {
    Instant startDate = dto.getStartDate().toInstant(ZoneOffset.UTC);
    Instant endDate = dto.getEndDate().toInstant(ZoneOffset.UTC);
    boolean poolCurrent =
        (dto.getEndDate().getYear() == LocalDate.now().getYear())
            && (dto.getEndDate().getMonth() == LocalDate.now().getMonth());

    if (Objects.isNull(startDate)) {
      startDate =
          LocalDate.of(LocalDate.now().getYear(), 1, 1).atStartOfDay(ZoneId.of("UTC")).toInstant();
    }

    if (Objects.isNull(endDate)) {
      endDate =
          LocalDate.of(LocalDate.now().getYear(), 12, 31)
              .atStartOfDay(ZoneId.of("UTC"))
              .plusDays(1)
              .minusNanos(1)
              .toInstant();
    } else {
      endDate =
          LocalDate.of(
                  dto.getEndDate().getYear(),
                  dto.getEndDate().getMonth(),
                  dto.getEndDate().getDayOfMonth())
              .atStartOfDay(ZoneId.of("UTC"))
              .plusDays(1)
              .minusNanos(1)
              .toInstant();
    }

    List<Object[]> objects = null;
    if (dto.getPublisherName() == null || dto.getPublisherName().isEmpty()) {
      objects = notificationRepository.getPublishersConversionsForAdminArchive(startDate, endDate);
    } else {
      objects =
          notificationRepository.getPublishersConversionsForAdminArchiveWithPublisherName(
              dto.getPublisherName(), startDate, endDate);
    }

    List<PublisherConversionsDTO> conversionsList = new ArrayList<>();

    for (Object[] row : objects) {
      String publisherName = (String) row[0];
      Long publisherHookSentOrDurationGreaterThan7Count = ((Number) row[1]).longValue();
      Long publisherHookReceivedCount = ((Number) row[2]).longValue();
      Long durationLessThan8Count = ((Number) row[3]).longValue();
      Double cpaCostPerUser = ((Number) row[4]).doubleValue();

      // Create a DTO or other object to hold these values
      PublisherConversionsDTO publisherConversionsDTO =
          new PublisherConversionsDTO(
              publisherName,
              publisherHookSentOrDurationGreaterThan7Count,
              publisherHookReceivedCount,
              durationLessThan8Count,
              cpaCostPerUser);
      conversionsList.add(publisherConversionsDTO);
    }

    return conversionsList;
  }

  @Override
  public List<SearchPostbackDto> getPostbacks(String transactionId, Pageable pageable) {
    return notificationRepository.searchByTransactionId(transactionId, pageable);
  }

  @Override
  public List<AdvertiserConversionDTO> getAdvertiserConversionsForAdvertiser(
      AdvertiserConversionReportRequestDTO dto) {
    Instant startDate = AppUtils.localDateToInstant(dto.getStartDate());
    Instant endDate = AppUtils.localDateToInstant(dto.getEndDate());

    // I can't figure out why the query won't work when dates are null, so I'm hardcoding the start
    // and end date values if they're null
    if (Objects.isNull(startDate)) {
      startDate =
          LocalDate.of(LocalDate.now().getYear(), 1, 1).atStartOfDay(ZoneId.of("UTC")).toInstant();
    }

    if (Objects.isNull(endDate)) {
      endDate =
          LocalDate.of(LocalDate.now().getYear(), 12, 31)
              .atStartOfDay(ZoneId.of("UTC"))
              .plusDays(1)
              .minusNanos(1)
              .toInstant();
    } else {
      // set endDate to the end of the day
      endDate =
          LocalDate.of(
                  dto.getEndDate().getYear(),
                  dto.getEndDate().getMonth(),
                  dto.getEndDate().getDayOfMonth())
              .atStartOfDay(ZoneId.of("UTC"))
              .plusDays(1)
              .minusNanos(1)
              .toInstant();
    }

    List<Object[]> objects =
        notificationRepository.getAdvertiserConversionsForAdvertiser(
            dto.getAdvertiserName(), dto.getCampaignName(), startDate, endDate);

    List<AdvertiserConversionDTO> dtoList = new ArrayList<>();
    for (Object[] obj : objects) {
      AdvertiserConversionDTO advertiserConversionDTO =
          new AdvertiserConversionDTO(
              (String) obj[0], // c.status
              (String) obj[1], // a.business_name
              (String) obj[2], // c.name
              ((Number) obj[3]).longValue(), // nonPublisherHookCount
              ((Number) obj[4]).longValue(), // publisherHookReceivedCount
              ((Number) obj[5]).longValue(), // unsubscribedCount
              ((Number) obj[6]).longValue(), // c.budget
              (String) obj[7], // c.country,
              ((Number) obj[8]).doubleValue(), // c.costperuser
              ((Number) obj[9]).doubleValue()); // vp revenue or campaign cost
      dtoList.add(advertiserConversionDTO);
    }

    return dtoList;
  }

  @Override
  public CampaignMetricsDTO calculateCampaignCostForCurrentMonth(String campaignId) {

    ZoneId zoneId = ZoneId.of("UTC");

    LocalDate firstDayOfCurrentMonth = LocalDate.now(zoneId).withDayOfMonth(1);
    LocalDate lastDayOfCurrentMonth =
        firstDayOfCurrentMonth.withDayOfMonth(firstDayOfCurrentMonth.lengthOfMonth());

    LocalDateTime startOfMonth = firstDayOfCurrentMonth.atTime(0, 0, 0);
    LocalDateTime endOfMonth = lastDayOfCurrentMonth.atTime(23, 59, 59);

    ZonedDateTime startOfMonthZoned = startOfMonth.atZone(zoneId);
    ZonedDateTime endOfMonthZoned = endOfMonth.atZone(zoneId);

    List<Object[]> rows = notificationRepository.getCampaignMetrics(
        campaignId, startOfMonthZoned.toInstant(), endOfMonthZoned.toInstant());

    if (rows.isEmpty()) {
      return new CampaignMetricsDTO(0L, 0.0, 0L, 0.0);
    }

    Object[] row = rows.get(0);
    return new CampaignMetricsDTO(
        ((Number) row[0]).longValue(),
        ((Number) row[1]).doubleValue(),
        ((Number) row[2]).longValue(),
        ((Number) row[3]).doubleValue());
  }

  @Override
  public Notification getLastConversionForPublisher(String pubId) {
    return notificationRepository.getLastConversionForPublisher(pubId);
  }

  @Override
  @Transactional
  public void deleteOldRecords() {
    LocalDateTime cutoffDate =
        LocalDateTime.now()
            .minusMonths(1)
            .withDayOfMonth(30)
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0);

    // Call the repository method to delete old records
    notificationRepository.deleteBefore(Instant.from(cutoffDate.atZone(ZoneId.of("UTC"))));
  }

  @Override
  public List<NotificationDto> getDailyNotificationsForPublisher(String pubId) {
    ZoneId zoneId = ZoneId.of("UTC");

    LocalDate yesterday = LocalDate.now(zoneId).minusDays(1);

    LocalDateTime startOfDay = yesterday.atStartOfDay();
    LocalDateTime endOfDay = yesterday.atTime(23, 59, 59);

    ZonedDateTime startOfDayZoned = startOfDay.atZone(zoneId);
    ZonedDateTime endOfDayZoned = endOfDay.atZone(zoneId);

    return notificationRepository.findNotificationsForPublisherWithDateRange(
        startOfDayZoned.toInstant(), endOfDayZoned.toInstant(), pubId);
  }

  @Override
  public List<NotificationDto> getWeeklyNotificationsForPublisher(String pubId) {
    ZoneId zoneId = ZoneId.of("UTC");

    LocalDate now = LocalDate.now(zoneId);
    LocalDate startOfWeek =
        now.minusDays(now.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue());
    LocalDate endOfWeek = startOfWeek.plusDays(6);

    LocalDateTime startDate = startOfWeek.atStartOfDay();
    LocalDateTime endDate = endOfWeek.atTime(23, 59, 59);

    ZonedDateTime startOfDayZoned = startDate.atZone(zoneId);
    ZonedDateTime endOfDayZoned = endDate.atZone(zoneId);

    return notificationRepository.findNotificationsForPublisherWithDateRange(
        startOfDayZoned.toInstant(), endOfDayZoned.toInstant(), pubId);
  }

  @Override
  public List<RetentionReportDto> getWeeklyRetentionForPublisher(String pubId) {
    ZoneId zoneId = ZoneId.of("UTC");

    LocalDate now = LocalDate.now(zoneId);
    LocalDate startOfWeek =
        now.minusDays(now.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue());
    LocalDate endOfWeek = startOfWeek.plusDays(6);

    LocalDateTime startDate = startOfWeek.atStartOfDay();
    LocalDateTime endDate = endOfWeek.atTime(23, 59, 59);

    ZonedDateTime startOfDayZoned = startDate.atZone(zoneId);
    ZonedDateTime endOfDayZoned = endDate.atZone(zoneId);

    return notificationRepository.findPubSourceRetentionCount(
        startOfDayZoned.toInstant(), endOfDayZoned.toInstant(), pubId);
  }

  @Override
  public List<PubChurnReportDto> getWeeklyChurnForValueplus() {
    ZoneId zoneId = ZoneId.of("UTC");

    LocalDate now = LocalDate.now(zoneId);
    LocalDate startOfWeek =
        now.minusDays(now.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue());
    LocalDate endOfWeek = startOfWeek.plusDays(6);

    LocalDateTime startDate = startOfWeek.atStartOfDay();
    LocalDateTime endDate = endOfWeek.atTime(23, 59, 59);

    ZonedDateTime startOfDayZoned = startDate.atZone(zoneId);
    ZonedDateTime endOfDayZoned = endDate.atZone(zoneId);

    return notificationRepository.getPubChurnReportPerCampaign(
        startOfDayZoned.toInstant(), endOfDayZoned.toInstant());
  }

  @Override
  public List<PubChurnReportDto> getDailyChurnForPublisher() {
    ZoneId zoneId = ZoneId.of("UTC");

    LocalDate now = LocalDate.now(zoneId);
    LocalDate yesterday = now.minusDays(1);

    LocalDateTime startOfDay = yesterday.atStartOfDay();
    LocalDateTime endOfDay = yesterday.atTime(23, 59, 59);

    ZonedDateTime startOfDayZoned = startOfDay.atZone(zoneId);
    ZonedDateTime endOfDayZoned = endOfDay.atZone(zoneId);

    return notificationRepository.getPubChurnReportPerCampaign(
        startOfDayZoned.toInstant(), endOfDayZoned.toInstant());
  }

  @Override
  public List<Notification> findByMsisdn(String subMsisdn) {
    List<Notification> notifications = notificationRepository.findTopByMsisdn(subMsisdn);

    if (Objects.isNull(notifications) || notifications.isEmpty()) {
      notifications = notificationRepository.findTopByMsisdnFromArchive(subMsisdn);
    }

    return notifications;
  }

  @Override
  public List<? extends ChurnReport> fetchReports(
      List<String> campaigns,
      List<String> publishers,
      LocalDateTime startDate,
      LocalDateTime endDate,
      ChurnType churnType,
      boolean includeSourceId) {
    validateInputs(startDate, endDate);
    if (includeSourceId) {
      return notificationRepository
          .findWithSourceIdFromCurrent(
              campaigns,
              publishers,
              AppUtils.localDateTimeToInstantUTC(startDate),
              AppUtils.localDateTimeToInstantUTC(endDate),
              churnType.getDurationInMinutes())
          .stream()
          .map(this::convertToChurnWithSourceId)
          .toList();
    } else {
      return notificationRepository
          .findWithoutSourceIdFromCurrent(
              campaigns,
              publishers,
              AppUtils.localDateTimeToInstantUTC(startDate),
              AppUtils.localDateTimeToInstantUTC(endDate),
              churnType.getDurationInMinutes())
          .stream()
          .map(this::convertToChurnWithoutSourceId)
          .toList();
    }
  }

  private ChurnReportDTOSourced convertToChurnWithSourceId(Object[] objects) {
    return new ChurnReportDTOSourced(
        ((Date) objects[0]).toLocalDate(), // ReportDate
        (String) objects[1], // PublisherName
        ((String) objects[2]), // CampaignName
        ((String) objects[3]), // SourceId
        ((Number) objects[4]).intValue(), // acquisition
        ((Number) objects[5]).intValue() // chrun
        );
  }

  private ChurnReportDTO convertToChurnWithoutSourceId(Object[] objects) {
    return new ChurnReportDTO(
        ((Date) objects[0]).toLocalDate(), // ReportDate
        (String) objects[1], // PublisherName
        ((String) objects[2]), // CampaignName
        ((Number) objects[3]).intValue(), // acquisition
        ((Number) objects[4]).intValue() // chrun
        );
  }

  private AdvertiserChurnReportDTO convertToAdvertiserChurn(Object[] objects) {
    return new AdvertiserChurnReportDTO(
        ((Date) objects[0]).toLocalDate(), // ReportDate
        ((String) objects[1]), // CampaignName
        ((Number) objects[2]).intValue(), // acquisition
        ((Number) objects[3]).intValue() // churn
        );
  }

  @Override
  public List<AdminChurnReportDto> fetchAdminChurnReport(Instant startDate, Instant endDate) {
    return notificationRepository.findAdminChurnReport(startDate, endDate)
        .stream()
        .map(this::toAdminChurnRow)
        .toList();
  }

  @Override
  public List<AdminChurnReportDto> fetchAdminChurnReport(Instant startDate, Instant endDate, int churnDurationHours) {
    return notificationRepository.findAdminChurnReportWithDuration(startDate, endDate, churnDurationHours)
        .stream()
        .map(this::toAdminChurnRow)
        .toList();
  }

  private AdminChurnReportDto toAdminChurnRow(Object[] row) {
    return new AdminChurnReportDto(
        ((java.sql.Date) row[0]).toLocalDate(),
        (String) row[1],
        (String) row[2],
        (String) row[3],
        (String) row[4],
        ((Number) row[5]).longValue(),
        ((Number) row[6]).longValue(),
        ((Number) row[7]).longValue(),
        ((Number) row[8]).doubleValue(),
        ((Number) row[9]).doubleValue());
  }

  @Override
  public List<PublisherChurnRecordDTO> fetchPublisherApiReport(String publisherId,
                                                               LocalDateTime startDate,
                                                               LocalDateTime endDate) {
    validateInputs(startDate, endDate);
    return notificationRepository
        .findPublisherChurnReport(
            publisherId,
            AppUtils.localDateTimeToInstantUTC(startDate),
            AppUtils.localDateTimeToInstantUTC(endDate))
        .stream()
        .map(this::toPublisherRecord)
        .toList();
  }

  @Override
  public List<PublisherChurnRecordDTO> fetchPublisherApiReport48hrs(String publisherId,
                                                                     LocalDateTime startDate,
                                                                     LocalDateTime endDate) {
    validateInputs(startDate, endDate);
    return notificationRepository
        .findPublisherChurnReport48hrs(
            publisherId,
            AppUtils.localDateTimeToInstantUTC(startDate),
            AppUtils.localDateTimeToInstantUTC(endDate))
        .stream()
        .map(this::toPublisherRecord)
        .toList();
  }

  @Override
  public List<PublisherChurnRecordDTO> fetchPublisherConversions(String publisherId,
                                                                 LocalDateTime startDate,
                                                                 LocalDateTime endDate) {
    validateInputs(startDate, endDate);
    return notificationRepository
        .findPublisherConversions(
            publisherId,
            AppUtils.localDateTimeToInstantUTC(startDate),
            AppUtils.localDateTimeToInstantUTC(endDate))
        .stream()
        .map(this::toPublisherRecord)
        .toList();
  }

  private PublisherChurnRecordDTO toPublisherRecord(Object[] row) {
    return new PublisherChurnRecordDTO(
        ((java.sql.Date) row[0]).toLocalDate(),
        (String) row[1],
        (String) row[2],
        (String) row[3]);
  }



  private void validateInputs(LocalDateTime startDate, LocalDateTime endDate) {
    if (startDate == null || endDate == null) {
      throw new IllegalArgumentException("Start date and end date must be provided.");
    }
  }

  @Override
  public long getTotalSpendForPreviousMonthForAdvertiser(Advertiser advertiser) {
    ZoneId zoneId = ZoneId.of("UTC");

    LocalDate firstDayOfPreviousMonth = LocalDate.now(zoneId).withDayOfMonth(1).minusMonths(1);
    LocalDate lastDayOfPreviousMonth =
        firstDayOfPreviousMonth.withDayOfMonth(firstDayOfPreviousMonth.lengthOfMonth());

    LocalDateTime startOfMonth = firstDayOfPreviousMonth.atTime(0, 0, 0);
    LocalDateTime endOfMonth = lastDayOfPreviousMonth.atTime(23, 59, 59);

    ZonedDateTime startOfMonthZoned = startOfMonth.atZone(zoneId);
    ZonedDateTime endOfMonthZoned = endOfMonth.atZone(zoneId);

    return notificationRepository.countConversionsByCampaignIdWithDateRangeForAdvertiser(
        advertiser, startOfMonthZoned.toInstant(), endOfMonthZoned.toInstant());
  }

  @Override
  public List<AdvertiserChurnReportDTO> getAdvertiserChurnAndAcquisition(
      Advertiser advertiser, AdvertiserChurnReportRequestDTO reportRequestDTO) {
    List<Object[]> churnObjects =
        notificationRepository.findAdvertiserChurn(
            reportRequestDTO.getCampaigns(),
            advertiser.getAdvertiserId(),
            reportRequestDTO.getStartDate().toInstant(ZoneOffset.UTC),
            reportRequestDTO.getEndDate().toInstant(ZoneOffset.UTC),
            reportRequestDTO.getChurnTypes().getDurationInMinutes());
    return churnObjects.stream().map(this::convertToAdvertiserChurn).toList();
  }

  @Override
  public List<Object[]> fetchPublisherMetrics(
      String publisherId, Instant startDate, Instant endDate, int churnPeriod, boolean sourceId) {
    ZoneId utcZone = ZoneId.of("UTC");

    LocalDate start = startDate.atZone(utcZone).toLocalDate();
    LocalDate end = endDate.atZone(utcZone).toLocalDate();

    Instant startOfDay = start.atStartOfDay(utcZone).toInstant();
    Instant endOfDay;

    if (start.isEqual(end)) {
      // Same day: make end time 23:59:59.999999999
      endOfDay = end.atTime(LocalTime.MAX).atZone(utcZone).toInstant();
    } else {
      // Use endDate as-is (assuming already UTC)
      endOfDay = endDate;
    }

    if (sourceId) {
      return notificationRepository.getPublisherCampaignMetricsWithSourceId(
          publisherId, startOfDay, endOfDay);
    } else {
      return notificationRepository.getPublisherCampaignMetricsWithoutSourceId(
          publisherId, startOfDay, endOfDay);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<AdvertiserConversionCpaBreakdownDTO> getAdvertiserConversionsCpaBreakdown(
      AdvertiserConversionRequestDTO dto) {

    LocalDateTime startDateTime =
        dto.getStartDate() != null ? dto.getStartDate() : LocalDateTime.now().minusMonths(1);
    LocalDateTime endDateTime = dto.getEndDate() != null ? dto.getEndDate() : LocalDateTime.now();

    Instant startDate = startDateTime.atZone(ZoneId.systemDefault()).toInstant();
    Instant endDate = endDateTime.atZone(ZoneId.systemDefault()).toInstant();

    List<Object[]> results;
    if (dto.getAdvertiserName() != null && !dto.getAdvertiserName().isEmpty()) {
      results =
          notificationRepository.getAdvertiserConversionsCpaBreakdownByAdvertiser(
              dto.getAdvertiserName(), startDate, endDate);
    } else {
      results = notificationRepository.getAdvertiserConversionsCpaBreakdown(startDate, endDate);
    }

    List<AdvertiserConversionCpaBreakdownDTO> dtoList = new ArrayList<>();
    for (Object[] row : results) {
      AdvertiserConversionCpaBreakdownDTO breakdown = new AdvertiserConversionCpaBreakdownDTO();
      breakdown.setAdvertiserName((String) row[0]);
      breakdown.setCampaignName((String) row[1]);
      breakdown.setCampaignId((String) row[2]);
      breakdown.setStatus((String) row[3]);
      breakdown.setCountry((String) row[4]);
      breakdown.setBudget(row[5] != null ? ((Number) row[5]).longValue() : 0L);
      breakdown.setConversions(row[6] != null ? ((Number) row[6]).longValue() : 0L);
      breakdown.setClicks(row[7] != null ? ((Number) row[7]).longValue() : 0L);
      breakdown.setChurn(row[8] != null ? ((Number) row[8]).longValue() : 0L);
      breakdown.setAmountSpent(row[9] != null ? ((Number) row[9]).doubleValue() : 0.0);
      dtoList.add(breakdown);
    }

    return dtoList;
  }
}
