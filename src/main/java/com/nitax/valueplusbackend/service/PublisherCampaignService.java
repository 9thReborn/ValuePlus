package com.nitax.valueplusbackend.service;

import java.util.List;

import com.nitax.valueplusbackend.domain.Campaign;
import com.nitax.valueplusbackend.domain.Publisher;
import com.nitax.valueplusbackend.domain.PublisherCampaign;
import com.nitax.valueplusbackend.dto.request.PublisherCampaignRequest;
import com.nitax.valueplusbackend.dto.response.ApiResponse;
import com.nitax.valueplusbackend.dto.response.PublisherCampaignConversionsDTO;
import com.nitax.valueplusbackend.dto.response.PublisherCampaignDetailsDTO;
import com.nitax.valueplusbackend.dto.response.PublisherCampaignDto;

public interface PublisherCampaignService {
  PublisherCampaign createPublisherCampaign(PublisherCampaignRequest request);

  PublisherCampaign createPublisherCampaign(Publisher publisher, Campaign campaign, Double cpa);
  PublisherCampaign updatePublisherCampaign(Publisher publisher, Campaign campaign, Double cpa);
  boolean existsByPublisherAndCampaign(Publisher publisher, Campaign campaign);

  PublisherCampaign updatePublisherCampaign(String pubCampId, PublisherCampaignRequest request);

  List<PublisherCampaign> getPublisherCampaigns(String publisherId, String campaignId);
  List<PublisherCampaignDto> getPublisherCampaigns(String publisherId);

  List<PublisherCampaign> getAllPublisherCampaignForCampaign(String campaignId);

  void deletePublisherCampaign(String pubCampId);

  PublisherCampaign findByPublisherIdAndCampaignId(String campaignId, String publisherId);

  List<PublisherCampaign> findAll();

  Long getTotalNumberOfPublisherCampaignByPublisherId(long id);
  Long getTotalNumberOfActiveCampaignsByPublisherId(long publisherId);
  Long getTotalNumberOfPausedCampaignsByPublisherId(long publisherId);
  Long getTotalNumberOfDisabledCampaignsByPublisherId(long publisherId);

  PublisherCampaign pauseSinglePublisherCampaign(long pubCampId,String reason);

  PublisherCampaign deleteSinglePublisherCampaign(long pubCampId,String reason);
  PublisherCampaign activateSinglePublisherCampaign(long pubCampId,String reason);

  void deleteAllByPublisherId(String publisherId);

  PublisherCampaign findById(long id);

  ApiResponse<List<PublisherCampaignConversionsDTO>> getTopThreePublisherCampaign(String publisherId);

  ApiResponse<?> getTopThreeNewCampaigns();

  ApiResponse<?> getNewAvaialableCampaign(int page, int size,String publisherId);

  ApiResponse<?> startCampaign(String campaignId, String publisherEmail);

  PublisherCampaignDetailsDTO fetchPublisherCampaignDetailsByCampaignName(String campaignName);
}
