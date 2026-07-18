package com.nitax.valueplusbackend.service.impl;

import com.nitax.valueplusbackend.domain.AppRoles;
import com.nitax.valueplusbackend.domain.Campaign;
import com.nitax.valueplusbackend.domain.Notification;
import com.nitax.valueplusbackend.domain.Publisher;
import com.nitax.valueplusbackend.domain.PublisherCampaign;
import com.nitax.valueplusbackend.domain.PublisherStatus;
import com.nitax.valueplusbackend.dto.PublisherCampaignMetricsDto;
import com.nitax.valueplusbackend.dto.PublisherCampaignUrlDto;
import com.nitax.valueplusbackend.dto.request.ChurnReportRequestDTO;
import com.nitax.valueplusbackend.dto.request.PublisherChurnReportRequestDTO;
import com.nitax.valueplusbackend.dto.request.CreatePublisherDTO;
import com.nitax.valueplusbackend.dto.request.PublisherConversionRequestDTO;
import com.nitax.valueplusbackend.dto.request.UpdatePublisherDTO;
import com.nitax.valueplusbackend.dto.response.ApiResponse;
import com.nitax.valueplusbackend.dto.response.AutoFillDTO;
import com.nitax.valueplusbackend.dto.response.ChurnReport;
import com.nitax.valueplusbackend.dto.response.PublisherChurnRecordDTO;
import com.nitax.valueplusbackend.dto.response.PublisherCampaignConversionsDTO;
import com.nitax.valueplusbackend.dto.response.PublisherCampaignDashboardResponse;
import com.nitax.valueplusbackend.dto.response.PublisherCampaignDto;
import com.nitax.valueplusbackend.dto.response.PublisherCampaignResponseDTO;
import com.nitax.valueplusbackend.dto.response.PublisherConversionsDTO;
import com.nitax.valueplusbackend.exception.AppException;
import com.nitax.valueplusbackend.exception.DuplicatePublisherException;
import com.nitax.valueplusbackend.exception.PublisherNotFoundException;
import com.nitax.valueplusbackend.repository.PublisherRepository;
import com.nitax.valueplusbackend.repository.RoleRepository;
import com.nitax.valueplusbackend.service.CampaignService;
import com.nitax.valueplusbackend.service.ChurnReportService;
import com.nitax.valueplusbackend.service.NotificationService;
import com.nitax.valueplusbackend.service.ProductService;
import com.nitax.valueplusbackend.service.PublisherCampaignService;
import com.nitax.valueplusbackend.service.PublisherService;
import com.nitax.valueplusbackend.utils.AppUtils;
import com.nitax.valueplusbackend.utils.enums.Role;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublisherServiceImpl implements PublisherService {

  private final PublisherRepository publisherRepository;
  private final AppUtils appUtils;
  private final CampaignService campaignService;
  private final ProductService productService;
  private final RestService restService;
  private final NotificationService notificationService;
  private final PublisherCampaignService publisherCampaignService;
  private final ChurnReportService churnReportService;
  private final PasswordEncoder encoder;
  private final RoleRepository roleRepository;

  @Value("${app.clicks-url}")
  private String clicksUrl;

  @Override
  @Transactional
  public Publisher savePublisher(CreatePublisherDTO publisherDTO) {
    // Check if a publisher with the same email already exists
    if (publisherRepository.existsByEmail(publisherDTO.getEmail())) {
      throw new DuplicatePublisherException(
          "Publisher with email " + publisherDTO.getEmail() + " already exists");
    }

    // Create a Publisher entity from the DTO
    Publisher publisher = new Publisher();
    publisher.setName(publisherDTO.getName());
    publisher.setPubUrl(publisherDTO.getPubUrl());
    publisher.setPubId(appUtils.generatePubId());
    publisher.setEmail(publisherDTO.getEmail());
    publisher.setWebsite(publisherDTO.getWebsite());
    publisher.setPostbackUrl(appUtils.generatePublisherPostbackUrl(publisher.getPubId()));
    publisher.setIdentifier(publisherDTO.getIdentifier());
    publisher.setClickIdParameter(publisherDTO.getClickIdParameter());
    publisher.setSourceIdParameter(publisherDTO.getSourceIdParameter());
    publisher.setPassword(encoder.encode(publisherDTO.getPassword()));
    publisher.setRole(setPublisherRole());

    // Save the publisher
    Publisher savedPublisher = publisherRepository.save(publisher);
    // map publisher to all campaigns with the default cpa
    this.campaignService
        .getAllCampaigns()
        .forEach(
            campaign -> {
              this.publisherCampaignService.createPublisherCampaign(
                  savedPublisher, campaign, campaign.getCpaCostPerUser());
            });
    // map the publisher to all campaigns;

    return savedPublisher;
  }

  private AppRoles setPublisherRole() {
    return roleRepository
        .findByName(Role.PUBLISHER)
        .orElseGet(
            () -> {
              AppRoles appRoles = new AppRoles();
              appRoles.setName(Role.PUBLISHER);
              return roleRepository.save(appRoles);
            });
  }

  @Override
  public Publisher savePublisher(Publisher publisher) {
    return publisherRepository.save(publisher);
  }

  @Override
  public String updatePublisher(String pubId, UpdatePublisherDTO publisherDTO) {
    // Check if against the existence of the publisher with the Id
    if (!publisherRepository.existsByPubId(pubId)) {
      throw new PublisherNotFoundException(
          "Publisher with Id " + pubId + " doesn't exist on our record");
    }
    // get the publisher
    Publisher currentPublisher = publisherRepository.findByPubId(pubId).get();

    // update existing Publisher entity from the DTO

    currentPublisher.setName(
        Optional.ofNullable(publisherDTO.getName())
            .filter(name -> !name.isEmpty())
            .orElse(currentPublisher.getName()));

    currentPublisher.setPubUrl(
        Optional.ofNullable(publisherDTO.getPubUrl())
            .filter(url -> !url.isEmpty())
            .orElse(currentPublisher.getPubUrl()));

    currentPublisher.setEmail(
        Optional.ofNullable(publisherDTO.getEmail())
            .filter(email -> !email.isEmpty())
            .orElse(currentPublisher.getEmail()));

    currentPublisher.setWebsite(
        Optional.ofNullable(publisherDTO.getWebsite())
            .filter(website -> !website.isEmpty())
            .orElse(currentPublisher.getWebsite()));

    // postbackUrl doesn't change(deprecated)
    /*currentPublisher.setPostbackUrl(Optional.ofNullable(publisherDTO.getPostbackUrl())
    .filter(postback -> !postback.isEmpty())
    .orElse(currentPublisher.getPostbackUrl()));*/
    currentPublisher.setIdentifier(
        Optional.ofNullable(publisherDTO.getIdentifier())
            .filter(identifier -> !identifier.isEmpty())
            .orElse(currentPublisher.getIdentifier()));

    currentPublisher.setClickIdParameter(
        Optional.ofNullable(publisherDTO.getClickIdParameter())
            .filter(clickId -> !clickId.isEmpty())
            .orElse(currentPublisher.getClickIdParameter()));

    currentPublisher.setSourceIdParameter(
        Optional.ofNullable(publisherDTO.getSourceIdParameter())
            .filter(sourceId -> !sourceId.isEmpty())
            .orElse(currentPublisher.getSourceIdParameter()));

    // Save the publisher
    Publisher savedPublisher = publisherRepository.save(currentPublisher);

    return savedPublisher.getPostbackUrl();
  }

  @Override
  public String approvePublisher(String id) {
    var publisher = this.publisherRepository.findByPubId(id);
    if (publisher.isPresent()) {
      publisher.get().setStatus(PublisherStatus.APPROVED);
      this.publisherRepository.save(publisher.get());
      return "APPROVED";
    } else {
      return "REJECTED";
    }
  }

  @Override
  public String suspendPublisher(String id) {
    var publisher = this.publisherRepository.findByPubId(id);
    if (publisher.isPresent()) {
      publisher.get().setStatus(PublisherStatus.SUSPENDED);
      this.publisherRepository.save(publisher.get());
      return "SUSPENDED";
    } else {
      return "REJECTED";
    }
  }

  @Override
  @Transactional
  public String deletePublisher(String id) {
    var publisher = this.publisherRepository.findByPubId(id);
    if (publisher.isPresent()) {
      // Delete associated publisher campaign records first
      publisherCampaignService.deleteAllByPublisherId(id);
      this.publisherRepository.delete(publisher.get());
      return "DONE";
    } else {
      return "REJECTED";
    }
  }

  @Override
  public List<AutoFillDTO> autoFillPublisher(String keys) {
    return publisherRepository.findPublishersByQuery(keys).stream()
        .map(
            s ->
                new AutoFillDTO(
                    (String) s[0], // name
                    (String) s[1]))
        .toList();
  }

  @Override
  public Publisher findByEmail(String email) {
    Optional<Publisher> optionalPublisher = publisherRepository.findByEmail(email);
    if (optionalPublisher.isEmpty()) {
      throw new PublisherNotFoundException("Publisher with email " + email + " not found");
    }
    return optionalPublisher.get();
  }

  @Override
  public void emailExist(String email) {
    Optional<Publisher> optionalPublisher = publisherRepository.findByEmail(email);
    if (optionalPublisher.isPresent()) {
      throw new AppException("Publisher with email " + email + " already exist");
    }
  }

  @Override
  public PublisherCampaignDashboardResponse getTotalCampaignStats(String username) {

    Publisher publisher = findByEmail(username);
    //        long id = 19265802;
    long id = publisher.getId();

    long totalNumberOfCampaign =
        publisherCampaignService.getTotalNumberOfPublisherCampaignByPublisherId(id);
    long totalNumberOfActiveCampaign =
        publisherCampaignService.getTotalNumberOfActiveCampaignsByPublisherId(id);
    long totalNumberOfPausedCampaign =
        publisherCampaignService.getTotalNumberOfPausedCampaignsByPublisherId(id);
    long totalNumberOfDisabledCampaign =
        publisherCampaignService.getTotalNumberOfDisabledCampaignsByPublisherId(id);

    return PublisherCampaignDashboardResponse.builder()
        .totalNumberOfCampaign(totalNumberOfCampaign)
        .totalNumberOfActiveCampaign(totalNumberOfActiveCampaign)
        .totalNumberOfPausedCampaign(totalNumberOfPausedCampaign)
        .totalNumberOfDisabledCampaign(totalNumberOfDisabledCampaign)
        .build();
  }

  @Override
  public List<PublisherCampaignDto> getPublisherCampaigns(String publisherId) {

    return publisherCampaignService.getPublisherCampaigns(publisherId);
  }

  @Override
  public String pausePublisherCampaign(long publisherCampaignId, String reason) {
    publisherCampaignService.pauseSinglePublisherCampaign(publisherCampaignId, reason);
    return "Campaign paused successfully";
  }

  @Override
  public String deletePublisherCampaign(long campaignId, String reason) {
    publisherCampaignService.deleteSinglePublisherCampaign(campaignId, reason);
    return "Campaign deleted successfully";
  }

  @Override
  public String activatePublisherCampaign(long campaignId, String reason) {
    publisherCampaignService.activateSinglePublisherCampaign(campaignId, reason);
    return "Campaign activated successfully";
  }

  @Override
  public List<PublisherCampaignResponseDTO> getPublisherCampaignsStats(String publisherId) {
    return null;
  }

  @Override
  public ApiResponse<?> getTopThreePublisherCampaigns(String pubId) {
    return publisherCampaignService.getTopThreePublisherCampaign(pubId);
  }

  @Override
  public ApiResponse<?> getTopThreeNewCampaign() {
    return publisherCampaignService.getTopThreeNewCampaigns();
  }

  @Override
  public ApiResponse<?> getNewAvaialableCampaign(int page, int size, String publisherId) {
    return publisherCampaignService.getNewAvaialableCampaign(page, size, publisherId);
  }

  @Override
  public ApiResponse<?> startCampaign(String campaignId, String publisherEmail) {
    return publisherCampaignService.startCampaign(campaignId, publisherEmail);
  }

  @Override
  public List<PublisherCampaignMetricsDto> fetchPublisherCampaignMetrics(
      PublisherConversionRequestDTO dto) {
    //        String publisherId = "O1q32H8q7j";
    List<Object[]> results =
        notificationService.fetchPublisherMetrics(
            //                publisherId,
            dto.getPublisherId(),
            dto.getStartDate().atZone(ZoneId.systemDefault()).toInstant(),
            dto.getEndDate().atZone(ZoneId.systemDefault()).toInstant(),
            calculateChurnPeriod(dto.getChurnPeriod()),
            dto.isSourceId());

    return results.stream().map(this::mapToDto).collect(Collectors.toList());
  }

  private int calculateChurnPeriod(String churnPeriod) {
    if (churnPeriod.equals("THIRTY_MINUTES")) {
      return 30; // 30 minutes
    }

    if (churnPeriod.equals("ONE_HOUR")) {
      return 60; // 1 hour in minutes
    }

    if (churnPeriod.equals("THREE_HOURS")) {
      return 180; // 3 hours in minutes
    }

    if (churnPeriod.equals("SIX_HOURS")) {
      return 360; // 6 hours in minutes
    }

    if (churnPeriod.equals("TWELVE_HOURS")) {
      return 720; // 12 hours in minutes
    }

    if (churnPeriod.equals("DAILY")) {
      return 1440; // 1 day in minutes
    }

    if (churnPeriod.equals("TWO_DAYS")) {
      return 2880; // 2 days in minutes
    }

    if (churnPeriod.equals("THREE_DAYS")) {
      return 4320; // 3 days in minutes
    }

    if (churnPeriod.equals("WEEKLY")) {
      return 10080; // 7 days in minutes
    }

    return 0; // Default case
  }

  private PublisherCampaignMetricsDto mapToDto(Object[] result) {
    PublisherCampaignMetricsDto dto = new PublisherCampaignMetricsDto();

    dto.setCampaignName(result[0] != null ? (String) result[0] : "");
    dto.setCampaignCountry(result[1] != null ? (String) result[1] : "");
    dto.setSourceId(result[2] != null ? (String) result[2] : "");
    dto.setTotalClicks(result[3] != null ? ((Number) result[3]).intValue() : 0);
    dto.setTotalConversions(result[4] != null ? ((Number) result[4]).intValue() : 0);
    dto.setChurnCount(result[5] != null ? ((Number) result[5]).intValue() : 0);
    dto.setCr(result[6] != null ? ((Number) result[6]).doubleValue() : 0.0);
    dto.setEcpm(result[7] != null ? ((Number) result[7]).doubleValue() : 0.0);
    dto.setTotalAmountSpent(result[8] != null ? ((Number) result[8]).doubleValue() : 0.0);
    dto.setTotalChurnHours(result[9] != null ? ((Number) result[9]).doubleValue() : 0.0);

    return dto;
  }

  //    @Override
  //    public List<PublisherCampaignResponseDTO> getPublisherCampaignsStats(String publisherId) {
  //        PublisherCampaignResponseDTO dto = new PublisherCampaignResponseDTO();
  //        dto.setPublisherId(publisherId);
  //        List<PublisherCampaignConversionsDTO> campaignConversions =
  // getPublishersCampaignConversions(dto);
  //    }

  @Override
  public List<PublisherCampaignConversionsDTO> getPublishersCampaignConversions(
      PublisherConversionRequestDTO dto) {
    List<PublisherCampaignConversionsDTO> conversions =
        notificationService.getPublishersCampaignConversions(dto);
    for (PublisherCampaignConversionsDTO conversion : conversions) {
      long amountSpent = (long) ((conversion.getCPA() * conversion.getConversions()) * 100);
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
    dto.setStartDate(LocalDateTime.now());
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
  public List<PublisherCampaignConversionsDTO> getCampaignPublisherStats(
      PublisherConversionRequestDTO dto) {
    List<PublisherCampaignConversionsDTO> conversions =
        notificationService.getPublishersCampaignConversions(dto);
    for (PublisherCampaignConversionsDTO conversion : conversions) {
      long amountSpent = (long) ((conversion.getCPA() * conversion.getConversions()) * 100);
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

    conversions.sort(
        Comparator.comparingLong(PublisherCampaignConversionsDTO::getConversions).reversed());

    int rank = 1;
    for (PublisherCampaignConversionsDTO conversion : conversions) {
      conversion.setRank(rank++);
    }

    return conversions;
  }

  private List<PublisherCampaignResponseDTO> mapToPublisherCampaignResponseDTO(
      List<PublisherCampaign> publisherCampaigns) {
    return publisherCampaigns.stream()
        .map(
            publisherCampaign -> {
              Campaign campaign = publisherCampaign.getCampaign();
              String clickId = publisherCampaign.getPublisher().getClickIdParameter();
              return new PublisherCampaignResponseDTO(
                  publisherCampaign.getId(),
                  campaign.getName(),
                  setPublisherCampaignStatus(publisherCampaign),
                  campaign.getAcquisition(),
                  replaceClickId(campaign.getUrl(), clickId),
                  publisherCampaign.getPublisher().getName(),
                  publisherCampaign.getPublisherCpa());
            })
        .toList();
  }

  private String setPublisherCampaignStatus(PublisherCampaign publisherCampaign) {
    if (publisherCampaign.isActive()) {
      return "ACTIVE";
    }
    if (publisherCampaign.isPaused()) {
      return "PAUSED";
    }

    if (publisherCampaign.isDeleted()) {
      return "DELETED";
    }

    return "INACTIVE";
  }

  private String replaceClickId(String url, String clickId) {
    if (url == null || clickId == null) {
      throw new AppException("URL and clickId must not be null");
    }
    return url.replace("{click_id}", clickId);
  }

  //  @Async
  //  public void handlePublisherCallBack(String publisherId, String campaignId, String trxId) {
  //    // create a new notification
  //    Notification notification = new Notification();
  //    notification.setTransactionId(trxId);
  //    notification.setCampaignId(campaignId);
  //    notification.setStatus(Notification.NotificationStatus.PUBLISHER_HOOK_RECEIVED);
  //    notification.setPublisherId(publisherId);
  //
  //    Optional<Publisher> publisher = publisherRepository.findByPubId(publisherId);
  //    if (publisher.isPresent()) {
  //      Optional<Campaign> campaign = campaignService.findByCampaignIdWithProduct(campaignId);
  //      if (campaign.isPresent()) {
  //        Optional<Product> product =
  //            productService.findByProdId(campaign.get().getProduct().getProdId());
  //        product.ifPresent(
  //            value -> {
  //              restService.sendGetRequest(
  //                  value.getPostbackUrl() + "/?trxId=" + trxId + "&campaignId=" + campaignId);
  //              notification.setProductId(value.getProdId());
  //            });
  //
  //        notification.setStatus(Notification.NotificationStatus.ADVERTISER_HOOK_SENT);
  //      }
  //    }
  //
  //    notification.setMessage("publisher callback received");
  //    notificationService.saveNotification(notification);
  //  }

  @Override
  public Publisher findByPubId(String publisherId) {
    return publisherRepository.findByPubId(publisherId).orElse(null);
  }

  @Override
  @Async
  public void handlePublisherPostBack(Notification notification) {
    String clickId =
        notification
            .getTransactionId()
            .trim()
            .replace("valueplus_" + notification.getPublisherId() + "_", "")
            .replace("valueplus2_" + notification.getPublisherId() + "_", "")
            .replace("vpbcairtel_" + notification.getPublisherId() + "_", "")
            .replace("vpmpesa_" + notification.getPublisherId() + "_", "")
            .replace(notification.getCampaignId() + "_" + notification.getPublisherId() + "_", "")
            .split("SRCID")[0];

    log.info("Click ID to be sent to publisher: {}", clickId);

    Optional<Publisher> publisher = publisherRepository.findByPubId(notification.getPublisherId());

    if (publisher.isPresent()) {
      String path = String.format(publisher.get().getPubUrl(), clickId);
      restService.sendGetRequest(path);
      notification.setTransactionId(clickId);
      notification.setStatus(Notification.NotificationStatus.PUBLISHER_HOOK_SENT);
      notificationService.saveNotification(notification);
    }
  }

  @Override
  public Optional<Publisher> findByIdentifier(String s) {
    return publisherRepository.findByIdentifier(s);
  }

  public void calculateTotalDueCost() {
    Map<String, Double> publisherRevenueMap =
        notificationService.findAllMonthly().stream()
            .collect(
                Collectors.groupingBy(
                    Notification::getPublisherId,
                    Collectors.summingDouble(Notification::getCpaRevenue)));

    List<Publisher> publishers = publisherRepository.findAll();
    for (Publisher publisher : publishers) {
      String publisherId = publisher.getPubId();
      Double totalDueCost = publisherRevenueMap.getOrDefault(publisherId, 0.0);
      publisher.setTotalDueAmount(publisher.getTotalDueAmount() + totalDueCost);
      publisherRepository.save(publisher);
    }
  }

  @Override
  public List<Publisher> findAll() {
    return publisherRepository.findAll();
  }

  @Override
  public List<Publisher> getAllPublishers() {
    return publisherRepository.findAllByOrderByCreatedDateDesc();
  }

  @Override
  public List<PublisherCampaignUrlDto> getPublisherUrls() {
    List<Publisher> publishers = getAllPublishers();

    List<PublisherCampaignUrlDto> publisherCampaignUrlDtos = new ArrayList<>();

    List<Campaign> campaigns = campaignService.findAll();

    for (Campaign campaign : campaigns) {
      if (Objects.equals(campaign.getStatus(), "ACTIVE")) {
        if (campaign.getAdvertiser().getBusinessName().contains("Betacare")) {
          for (Publisher publisher : publishers) {
            String url =
                clicksUrl
                    + "/redirect/"
                    + campaign.getCampaignId()
                    + "?trxId=valueplus_"
                    + publisher.getPubId()
                    + "_{click_id}SRCID{source_id}"
                    + "&trfsrc="
                    + publisher.getIdentifier();
            PublisherCampaignUrlDto publisherCampaignUrlDto = new PublisherCampaignUrlDto();
            publisherCampaignUrlDto.setPublisherName(publisher.getName());
            publisherCampaignUrlDto.setCampaignUrl(url);
            publisherCampaignUrlDto.setCampaignName(campaign.getName());
            publisherCampaignUrlDtos.add(publisherCampaignUrlDto);
          }
        } else if (campaign.getAdvertiser().getBusinessName().contains("YellowDot")
            || campaign.getAdvertiser().getBusinessName().contains("MSR")) {
          for (Publisher publisher : publishers) {
            String url =
                clicksUrl
                    + "/redirect/"
                    + campaign.getCampaignId()
                    + "?trxId="
                    + campaign.getCampaignId()
                    + "_"
                    + publisher.getPubId()
                    + "_{click_id}SRCID{source_id}"
                    + "&trfsrc="
                    + publisher.getIdentifier();
            PublisherCampaignUrlDto publisherCampaignUrlDto = new PublisherCampaignUrlDto();
            publisherCampaignUrlDto.setPublisherName(publisher.getName());
            publisherCampaignUrlDto.setCampaignUrl(url);
            publisherCampaignUrlDto.setCampaignName(campaign.getName());

            publisherCampaignUrlDtos.add(publisherCampaignUrlDto);
          }
        } else {
          for (Publisher publisher : publishers) {
            String url =
                clicksUrl
                    + "/redirect/"
                    + campaign.getCampaignId()
                    + "?trxId=valueplus_"
                    + publisher.getPubId()
                    + "_"
                    + publisher.getClickIdParameter()
                    + "&sourceId="
                    + publisher.getSourceIdParameter();
            PublisherCampaignUrlDto publisherCampaignUrlDto = new PublisherCampaignUrlDto();
            publisherCampaignUrlDto.setPublisherName(publisher.getName());
            publisherCampaignUrlDto.setCampaignUrl(url);
            publisherCampaignUrlDto.setCampaignName(campaign.getName());

            publisherCampaignUrlDtos.add(publisherCampaignUrlDto);
          }
        }
      }
    }

    return publisherCampaignUrlDtos;
  }

  @Override
  public List<AutoFillDTO> autoFillCampaigns(String keys) {
    return campaignService.autoFillCampaign(keys);
  }

  @Override
  public List<? extends ChurnReport> generateReport(ChurnReportRequestDTO reportRequestDTO) {
    if (reportRequestDTO.getCampaigns().isEmpty()) {
      throw new AppException("Campaign cannot be empty");
    }
    return churnReportService.fetchReports(
        reportRequestDTO.getCampaigns(),
        reportRequestDTO.getPublishers(),
        reportRequestDTO.getStartDate(),
        reportRequestDTO.getEndDate(),
        reportRequestDTO.getChurnTypes(),
        reportRequestDTO.isIncludeSourceId());
  }

  @Override
  public List<PublisherChurnRecordDTO> generatePublisherApiChurnReport(PublisherChurnReportRequestDTO requestDTO) {
    Publisher publisher = publisherRepository
        .findByApiKey(requestDTO.getApiKey())
        .orElseThrow(() -> new AppException("Invalid API key"));

    return notificationService.fetchPublisherApiReport(
        publisher.getPubId(),
        requestDTO.getStartDate(),
        requestDTO.getEndDate());
  }

  @Override
  public List<PublisherChurnRecordDTO> generatePublisherApiChurnReport48hrs(PublisherChurnReportRequestDTO requestDTO) {
    Publisher publisher = publisherRepository
        .findByApiKey(requestDTO.getApiKey())
        .orElseThrow(() -> new AppException("Invalid API key"));

    return notificationService.fetchPublisherApiReport48hrs(
        publisher.getPubId(),
        requestDTO.getStartDate(),
        requestDTO.getEndDate());
  }

  @Override
  public List<PublisherChurnRecordDTO> generatePublisherConversionsReport(PublisherChurnReportRequestDTO requestDTO) {
    Publisher publisher = publisherRepository
        .findByApiKey(requestDTO.getApiKey())
        .orElseThrow(() -> new AppException("Invalid API key"));

    return notificationService.fetchPublisherConversions(
        publisher.getPubId(),
        requestDTO.getStartDate(),
        requestDTO.getEndDate());
  }

  @Override
  public String generateApiKey(String pubId) {
    Publisher publisher = publisherRepository
        .findByPubId(pubId)
        .orElseThrow(() -> new AppException("Publisher not found"));

    String apiKey = java.util.UUID.randomUUID().toString().replace("-", "");
    publisher.setApiKey(apiKey);
    publisherRepository.save(publisher);
    return apiKey;
  }
}
