package com.nitax.valueplusbackend.service;

import com.nitax.valueplusbackend.domain.Notification;
import com.nitax.valueplusbackend.domain.Publisher;
import com.nitax.valueplusbackend.dto.PublisherCampaignMetricsDto;
import com.nitax.valueplusbackend.dto.PublisherCampaignUrlDto;
import com.nitax.valueplusbackend.dto.request.*;
import com.nitax.valueplusbackend.dto.response.*;

import java.util.List;
import java.util.Optional;

public interface PublisherService {
  Publisher savePublisher(CreatePublisherDTO publisherDTO);
  Publisher savePublisher(Publisher publisher);

  //  void handlePublisherCallBack(String publisherId, String campaignId, String trxId);

  List<PublisherCampaignConversionsDTO> getPublishersCampaignConversions(PublisherConversionRequestDTO dto);

  List<PublisherConversionsDTO> getPublishersConversions(PublisherConversionRequestDTO dto);

  List<PublisherCampaignConversionsDTO> getCampaignPublisherStats(PublisherConversionRequestDTO dto);

  Publisher findByPubId(String publisherId);

  void handlePublisherPostBack(Notification notification);

  Optional<Publisher> findByIdentifier(String s);

  public void calculateTotalDueCost();

  List<Publisher> findAll();

  List<Publisher> getAllPublishers();

  List<PublisherCampaignUrlDto> getPublisherUrls();

  String updatePublisher(String pubId,UpdatePublisherDTO publisherDTO);

  String approvePublisher(String id);
  String suspendPublisher(String id);
  String deletePublisher(String id);

    List<AutoFillDTO> autoFillPublisher(String keys);

  Publisher findByEmail(String email);

  void emailExist(String email);

  PublisherCampaignDashboardResponse getTotalCampaignStats(String username);

   List<PublisherCampaignDto> getPublisherCampaigns(String publisherId);

   String pausePublisherCampaign(long campaignId,String reason);
   String deletePublisherCampaign(long campaignId,String reason);
   String activatePublisherCampaign(long campaignId, String reason);

  List<PublisherCampaignResponseDTO> getPublisherCampaignsStats(String publisherId);

  ApiResponse<?> getTopThreePublisherCampaigns(String pubId);

  ApiResponse<?> getTopThreeNewCampaign();

  ApiResponse<?> getNewAvaialableCampaign(int page, int size, String publisherId);

  ApiResponse<?> startCampaign(String campaignId, String publisherEmail);

  List<PublisherCampaignMetricsDto> fetchPublisherCampaignMetrics(PublisherConversionRequestDTO dto);


  List<AutoFillDTO> autoFillCampaigns(String keys);

  List<? extends ChurnReport> generateReport(ChurnReportRequestDTO reportRequestDTO);

  List<PublisherChurnRecordDTO> generatePublisherApiChurnReport(PublisherChurnReportRequestDTO requestDTO);

  List<PublisherChurnRecordDTO> generatePublisherApiChurnReport48hrs(PublisherChurnReportRequestDTO requestDTO);

  List<PublisherChurnRecordDTO> generatePublisherConversionsReport(PublisherChurnReportRequestDTO requestDTO);

  String generateApiKey(String pubId);
}
