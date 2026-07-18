package com.nitax.valueplusbackend.service;

import com.nitax.valueplusbackend.domain.Advertiser;
import com.nitax.valueplusbackend.domain.Campaign;
import com.nitax.valueplusbackend.domain.Publisher;
import com.nitax.valueplusbackend.dto.CampaignDetailsDTO;
import com.nitax.valueplusbackend.dto.request.*;
import com.nitax.valueplusbackend.dto.response.AdvertiserConversionDTOForTop;
import com.nitax.valueplusbackend.dto.response.AutoFillDTO;
import com.nitax.valueplusbackend.dto.response.PublisherCampaignDetailsDTO;
import com.nitax.valueplusbackend.dto.response.RetentionStatsResponseDto;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface CampaignService {

  Campaign createCampaign(CreateCampaignDTO createCampaignDTO) throws IOException;

  Optional<Campaign> findCampaignById(String campaignId);

  void saveCampaign(Campaign campaign);

  public void calculateCampaignCostForCurrentMonth();

  CampaignDetailsDTO getTotalAmountOwedByAdvertiser(Long advertiserId);

  List<Campaign> findCampaignByAdvertiserId(Long advertiserId);

  Long getNumberOfActiveCampaigns(Advertiser advertiser);

  Long getNumberOfCampaigns(Advertiser advertiser);
  List<Campaign> getActiveCampaigns(Advertiser advertiser);

  List<Campaign> getAllCampaigns();

  RetentionStatsResponseDto getRetentionStatsForDays(RetentionStatDto retentionStatDto);

  long[] getTotalCampaignsStats(Advertiser advertiser);

  long getCampaignCountForYesterday(Advertiser advertiser);

  long getActiveCampaignCountForYesterday(Advertiser advertiser);

  long[] getActiveCampaignsStats(Advertiser advertiser);

  long getTotalSpendForPreviousMonth(Long id);

  long getTotalSpendForCurrentMonth(Long id);

  long[] getTotalCampaignsSpendStats(Advertiser advertiser);

  long[] getConversionData(Long advertiserId, Integer year);

  long[] getClickData(Long id, Integer year);

  int[] getMonthlyConversionMonths(Long id, Integer year);

  Page<Campaign> findAllCampaigns(CampaignFilter filter, Pageable pageable, Advertiser advertiser);

  Campaign getCampaignDetails(String campaignId);

  String deactivateCampaign(String campaignId);

  String activateCampaign(String campaignId);

  String uploadCampaignImage(MultipartFile file) throws IOException;

  Campaign editCampaign(UpdateCampaignDTO editCampaignDTO, Campaign campaignId);

  String deleteCampaign(String campaignId);

  void sendBudgetUsageReminders();

  List<Campaign> findAll();

  Long getActiveCampaignsForAdmin();

  Long getPausedCampaignsForAdmin();

  Long getDisabledCampaignsForAdmin();

  List<AdvertiserConversionDTOForTop> getTopFiveCampaignsForAdmin();

  List<AdvertiserConversionDTOForTop> findLeast5ByAcquisition(List<String> top5CampaignNames);

  Page<Campaign> getAllCampaignsForAdmin(CampaignFilter filter, Pageable pageable);

  Campaign editCampaignForAdmmin(UpdateCampaignForAdminDTO dto, Campaign existingCampaign);

  Campaign createCampaignForAdmin(CreateCampaignForAdminDTO dto, Advertiser advertiser);

  Campaign pauseCampaign(CampaignPauseRequest dto, String campaignId);

  Campaign enableCampaign(String campaignId);

  Campaign deleteCampaignForAdmin(DeleteCampaignRequestDto dto, String campaignId);

  Campaign disableCampaign(String campaignId, CampaignDsableRequest dto);

  List<Campaign> getAllEmptyMappedCampaigned();

  String getSectorByCampaignId(String campaignId);

  long getTotalSpendForPreviousMonthForAdvertiser(Advertiser advertiser);

  long getTotalSpendForCurrentMonthForAdvertiser(Advertiser advertiser);

  //  Optional<Campaign> findByCampaignIdWithProduct(String campaignId);
    List<AutoFillDTO> autoFillCampaign(String keys);

  PublisherCampaignDetailsDTO fetchCampaignByCampaignName(String campaignName);

  //  Optional<Campaign> findByCampaignIdWithProduct(String campaignId);
}
