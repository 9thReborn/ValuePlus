package com.nitax.valueplusbackend.service.impl;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.nitax.valueplusbackend.domain.Campaign;
import com.nitax.valueplusbackend.domain.Publisher;
import com.nitax.valueplusbackend.domain.PublisherCampaign;
import com.nitax.valueplusbackend.domain.PublisherStatus;
import com.nitax.valueplusbackend.dto.request.PublisherCampaignRequest;
import com.nitax.valueplusbackend.dto.response.ApiResponse;
import com.nitax.valueplusbackend.dto.response.PublisherCampaignConversionsDTO;
import com.nitax.valueplusbackend.dto.response.PublisherCampaignDetailsDTO;
import com.nitax.valueplusbackend.dto.response.PublisherCampaignDto;
import com.nitax.valueplusbackend.exception.AppException;
import com.nitax.valueplusbackend.exception.PublisherNotFoundException;
import com.nitax.valueplusbackend.exception.ResourceNotFoundException;
import com.nitax.valueplusbackend.repository.CampaignRepository;
import com.nitax.valueplusbackend.repository.PublisherCampaignRepository;
import com.nitax.valueplusbackend.repository.PublisherRepository;
import com.nitax.valueplusbackend.service.EmailService;
import com.nitax.valueplusbackend.service.PublisherCampaignService;
import com.nitax.valueplusbackend.utils.AppUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PublisherCampaignServiceImpl implements PublisherCampaignService {
    private final PublisherRepository publisherRepository;
    private final CampaignRepository campaignRepository;
    private final PublisherCampaignRepository publisherCampaignRepository;
    private final AppUtils appUtils;
    private final EmailService emailService;

    @Value("${app.clicks-url}")
    private String clicksUrl;

    @Override
    public PublisherCampaign createPublisherCampaign(PublisherCampaignRequest request) {
        Publisher publisher =
                publisherRepository
                        .findByPubId(request.getPublisherId())
                        .orElseThrow(() -> new IllegalArgumentException("Publisher not found"));
        Campaign campaign =
                campaignRepository
                        .findByCampaignId(request.getCampaignId())
                        .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));

        PublisherCampaign publisherCampaign = new PublisherCampaign();
        publisherCampaign.setPubCampId(appUtils.generatePubSubId());
        publisherCampaign.setPublisher(publisher);
        publisherCampaign.setCampaign(campaign);
        publisherCampaign.setPublisherCpa(request.getPublisherCpa());
        publisherCampaign.setPublisherCampaignLink(getPublisherUrl(publisher, campaign));

        return publisherCampaignRepository.save(publisherCampaign);
    }

    @Override
    public PublisherCampaign createPublisherCampaign(
            Publisher publisher, Campaign campaign, Double cpa) {
        PublisherCampaign publisherCampaign = new PublisherCampaign();
        publisherCampaign.setPubCampId(appUtils.generatePubSubId());
        publisherCampaign.setPublisher(publisher);
        publisherCampaign.setCampaign(campaign);
        publisherCampaign.setPublisherCpa(cpa);
        publisherCampaign.setPublisherCampaignLink(getPublisherUrl(publisher, campaign));
        return publisherCampaignRepository.save(publisherCampaign);
    }

    @Override
    public PublisherCampaign updatePublisherCampaign(Publisher publisher, Campaign campaign, Double cpa) {
        // find if exits update
        Optional<PublisherCampaign> getCP = publisherCampaignRepository.getByPublisherIdAndCampaignId(publisher.getPubId(), campaign.getCampaignId());
        if (getCP.isPresent()) {
            publisherCampaignRepository.updateCPAByPubCamId(getCP.get().getPubCampId(), cpa);
            return getCP.get();
        } else {
            // if not exist create
            return createPublisherCampaign(publisher, campaign, cpa);
        }
    }

    @Override
    public boolean existsByPublisherAndCampaign(Publisher publisher, Campaign campaign) {
        return publisherCampaignRepository.existsByPublisherIdAndCampaignId(publisher.getPubId(), campaign.getCampaignId());
    }

    @Override
    public PublisherCampaign updatePublisherCampaign(String id, PublisherCampaignRequest request) {
        PublisherCampaign publisherCampaign = publisherCampaignRepository.getByPubSubId(id);
        Publisher publisher =
                publisherRepository
                        .findByPubId(request.getPublisherId())
                        .orElseThrow(() -> new IllegalArgumentException("Publisher not found"));
        Campaign campaign =
                campaignRepository
                        .findByCampaignId(request.getCampaignId())
                        .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));

        publisherCampaign.setPublisher(publisher);
        publisherCampaign.setCampaign(campaign);
        publisherCampaign.setPublisherCpa(request.getPublisherCpa());
        publisherCampaign.setPublisherCampaignLink(getPublisherUrl(publisher, campaign));

        return publisherCampaignRepository.save(publisherCampaign);
    }

    @Override
    public List<PublisherCampaign> getPublisherCampaigns(String publisherId, String campaignId) {
        if (publisherId != null && campaignId != null) {
            return Collections.singletonList(
                    publisherCampaignRepository.getByPublisherIdAndCampaignId(publisherId, campaignId).get());
        } else if (publisherId != null) {
            return publisherCampaignRepository.getByPublisherId(publisherId);
        } else if (campaignId != null) {
            return publisherCampaignRepository.getByCampaignId(campaignId);
        } else {
            return publisherCampaignRepository.findAll();
        }
    }

    @Override
    public List<PublisherCampaignDto> getPublisherCampaigns(String publisherId) {
        if (null == publisherId  || publisherId.isEmpty()) {
            throw new PublisherNotFoundException("Publisher not found");
        }

        return mapToDto(publisherCampaignRepository.getByPublisherId(publisherId));
    }

//    private List<PublisherCampaignDto> mapToDto(List<PublisherCampaign> publisherCampaigns){
//        List<PublisherCampaignDto> response  =  new ArrayList<>();
//        for (PublisherCampaign publisherCampaign : publisherCampaigns){
//            String url =
//                    clicksUrl
//                            + publisherCampaign.getCampaign().getCampaignId()
//                            + "?trxId="
//                            +publisherCampaign.getCampaign().getCampaignId()
//                            + "_"
//                            + publisherCampaign.getPublisher().getPubId()
//                            + "_"
//                            + publisherCampaign.getPublisher().getClickIdParameter()
//                            + "&sourceId="
//                            + publisherCampaign.getPublisher().getSourceIdParameter()
//                            + "&trfsrc="
//                            + publisherCampaign.getPublisher().getIdentifier();
//
//                PublisherCampaignDto dto  =  new PublisherCampaignDto();
//                dto.setCampaignLink(url);
//                dto.setStatus(publisherCampaign.getCampaign().getStatus());
//                dto.setCampaignName(publisherCampaign.getCampaign().getName());
//                dto.setId(publisherCampaign.getCampaign().getCampaignId());
//                dto.setCpa(publisherCampaign.getPublisherCpa());
//                response.add(dto);
//        }
//        return response;
//    }

    private List<PublisherCampaignDto> mapToDto(List<PublisherCampaign> publisherCampaigns) {
        return publisherCampaigns.stream().map(pc -> {
            Campaign campaign = pc.getCampaign();
            Publisher publisher = pc.getPublisher();

            String campaignId = campaign.getCampaignId();
            String pubId = publisher.getPubId();
            String clickParam = publisher.getClickIdParameter();
            String sourceParam = publisher.getSourceIdParameter();
            String identifier = publisher.getIdentifier();

            String url = String.format("%s%s?trxId=%s_%s_%s&sourceId=%s&trfsrc=%s",
                    clicksUrl,
                    campaignId,
                    campaignId,
                    pubId,
                    clickParam,
                    sourceParam,
                    identifier
            );

            PublisherCampaignDto dto = new PublisherCampaignDto();
            dto.setCampaignLink(url);
            dto.setStatus(campaign.getStatus());
            dto.setCampaignName(campaign.getName());
            dto.setId(campaignId);
            dto.setCpa(pc.getPublisherCpa());

            return dto;
        }).collect(Collectors.toList());
    }


    @Override
    public List<PublisherCampaign> getAllPublisherCampaignForCampaign(String campaignId) {

        return publisherCampaignRepository.findAllByCampaign_CampaignId(campaignId);
    }

    @Override
    @Transactional
    public void deletePublisherCampaign(String pubCampId) {
        this.publisherCampaignRepository.deleteByPubCampId(pubCampId);
    }

    @Override
    @Transactional
    public void deleteAllByPublisherId(String publisherId) {
        this.publisherCampaignRepository.deleteByPublisherId(publisherId);
    }

    @Override
    public PublisherCampaign findByPublisherIdAndCampaignId(String campaignId, String publisherId) {
        return publisherCampaignRepository
                .getByPublisherIdAndCampaignId(publisherId, campaignId)
                .orElse(null);
    }

    @Override
    public List<PublisherCampaign> findAll() {
        return publisherCampaignRepository.findAll();
    }

    @Override
    public Long getTotalNumberOfPublisherCampaignByPublisherId(long id) {
        return publisherCampaignRepository.getAllPublisherCampaignsCountByPublisherId(id);
    }

    @Override
    public Long getTotalNumberOfActiveCampaignsByPublisherId(long publisherId) {
        return  campaignRepository.getActiveCampaignsForAdmin();
//        return publisherCampaignRepository.getTotalNumberOfActiveCampaignsByPublisherId(publisherId);
    }

    @Override
    public Long getTotalNumberOfPausedCampaignsByPublisherId(long publisherId) {
        return  campaignRepository.getPausedCampaignsForAdmin();
//        return publisherCampaignRepository.getTotalNumberOfPausedCampaignsByPublisherId(publisherId);
    }

    @Override
    public Long getTotalNumberOfDisabledCampaignsByPublisherId(long publisherId) {
        return  campaignRepository.getDisabledCampaignsForAdmin();
//        return publisherCampaignRepository.getTotalNumberOfDisabledCampaignsByPublisherId(publisherId);
    }

    @Override
    public PublisherCampaign pauseSinglePublisherCampaign(long pubCampId,String reason) {
        PublisherCampaign publisherCampaign =  findById(pubCampId);
        publisherCampaign.setActive(false);
        publisherCampaign.setPaused(true);
        publisherCampaign.setDeleted(false);
        publisherCampaign.setPauseReason(reason);
        publisherCampaign.setDeleteReason("");
        publisherCampaign.setActivationReason("");
        emailService.sendPublisherCampaignPausedMailTOAdmin(publisherCampaign,reason);
        return  publisherCampaignRepository.save(publisherCampaign);

    }

    @Override
    public PublisherCampaign deleteSinglePublisherCampaign(long pubCampId,String reason) {
        PublisherCampaign publisherCampaign =  findById(pubCampId);
        publisherCampaign.setActive(false);
        publisherCampaign.setDeleted(true);
        publisherCampaign.setPaused(false);
        publisherCampaign.setPauseReason("");
        publisherCampaign.setDeleteReason(reason);
        publisherCampaign.setActivationReason("");
        return  publisherCampaignRepository.save(publisherCampaign);
    }

    @Override
    public PublisherCampaign activateSinglePublisherCampaign(long pubCampId,String reason) {
        PublisherCampaign publisherCampaign =  findById(pubCampId);
        publisherCampaign.setActive(true);
        publisherCampaign.setDeleted(false);
        publisherCampaign.setPaused(false);
        publisherCampaign.setPauseReason("");
        publisherCampaign.setDeleteReason("");
        publisherCampaign.setActivationReason(reason);
        emailService.sendCampaignActivatedMailToAdmin(reason,publisherCampaign.getPublisher().getName());
        return  publisherCampaignRepository.save(publisherCampaign);
    }

    @Override
    public PublisherCampaign findById(long id) {
        Optional<PublisherCampaign> optionalPublisherCampaign =  publisherCampaignRepository.findById(id);
        if (optionalPublisherCampaign.isEmpty()){
            throw new AppException("Publisher Campaign not found");
        }

        return  optionalPublisherCampaign.get();
    }

    @Override
    public ApiResponse<List<PublisherCampaignConversionsDTO>> getTopThreePublisherCampaign(String publisherId) {
        // Calculate the start and end dates for the current year
        ZoneId zoneId = ZoneId.of("UTC");
        Instant startDate = LocalDate.of(LocalDate.now(zoneId).getYear(), 1, 1)
                .atStartOfDay(zoneId).toInstant();
        Instant endDate = LocalDate.of(LocalDate.now(zoneId).getYear(), 12, 31)
                .atTime(23, 59, 59).atZone(zoneId).toInstant();

        // Fetch data from the repository
        List<Object[]> results = publisherCampaignRepository.getTopCampaignsForPublisher(publisherId);

        // Process results into DTOs
        List<PublisherCampaignConversionsDTO> campaigns = new ArrayList<>();
        for (Object[] result : results) {
            String campaignId = (String) result[0];
            String campaignName = (String) result[1];
            String status = (String) result[2];
            long conversions = ((Number) result[3]).longValue();
            long clicks = ((Number) result[4]).longValue();
            long churn = ((Number) result[5]).longValue();
            double cpaCostPerUser = ((Number) result[6]).doubleValue();
            double totalCost = ((Number) result[7]).doubleValue();


            // Calculate metrics
            long amountSpent = (long) (cpaCostPerUser * conversions * 100);
            double cr = clicks > 0 ? ((double) conversions / clicks) * 100 : 0.0;
            double eCPM = clicks > 0 ? ((double) (amountSpent / 100) / clicks) * 1000 : 0.0;

            // Create DTO
            PublisherCampaignConversionsDTO dto = new PublisherCampaignConversionsDTO();
            dto.setCampaignId(campaignId);
            dto.setStatus(status);
            dto.setCampaignName(campaignName);
            dto.setConversions(conversions);
            dto.setClicks(clicks);
            dto.setChurn(churn);
            dto.setCPA(cpaCostPerUser);
            dto.setAmountSpent(Long.toString(amountSpent));
            dto.setCr(String.format("%.2f", cr));
            dto.setECPM(String.format("%.2f", eCPM));

            campaigns.add(dto);
        }

        // Rank campaigns
        campaigns.sort(Comparator.comparingLong(PublisherCampaignConversionsDTO::getConversions).reversed());
        int rank = 1;
        for (PublisherCampaignConversionsDTO campaign : campaigns) {
            campaign.setRank(rank++);
        }

        return new ApiResponse<>(true,campaigns);
    }

    @Override
    public ApiResponse<?> getTopThreeNewCampaigns() {
        Publisher publisher =  getCurrentPublisher();
        PageRequest pageRequest = PageRequest.of(0, 3);
        List<Object[]> campaigns =  publisherCampaignRepository.getPublisherTop3AvailableCampaigns(publisher.getPubId());
//        List<Campaign> campaigns =  campaignRepository.findTopThreeNewCampaigns();
//        List<PublisherCampaignDetailsDTO> publisherCampaignDetails = new ArrayList<>();
//        campaigns.stream()
//                .map(this::mapTo)
//                .forEach(publisherCampaignDetails::add);


        return new ApiResponse<>(true,campaigns);

    }

    @Override
    public ApiResponse<?> getNewAvaialableCampaign(int page, int size, String publisherId) {
        Pageable pageable = PageRequest.of(page,size);
        Page<Campaign> campaigns =  campaignRepository.findNewAvailableCampaignForPublisher(publisherId,pageable);
        List<PublisherCampaignDetailsDTO> publisherCampaignDetails = new ArrayList<>();
        campaigns.stream()
                .map(this::mapTo)
                .forEach(publisherCampaignDetails::add);
        return new ApiResponse<>(true,publisherCampaignDetails);
    }

    @Override
    public ApiResponse<?> startCampaign(String campaignId, String publisherEmail) {
        Publisher publisher =
                publisherRepository
                        .findByEmail(publisherEmail)
                        .orElseThrow(() -> new IllegalArgumentException("Publisher not found"));
        if (!publisher.getStatus().equals(PublisherStatus.APPROVED)) {
            throw new AppException("You are not approved yet");
        }
        Campaign campaign =
                campaignRepository
                        .findByCampaignId(campaignId)
                        .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));

        PublisherCampaign publisherCampaign = new PublisherCampaign();
        publisherCampaign.setPubCampId(appUtils.generatePubSubId());
        publisherCampaign.setPublisher(publisher);
        publisherCampaign.setCampaign(campaign);
        publisherCampaign.setPublisherCpa(campaign.getCpaCostPerUser());
        publisherCampaign.setPublisherCampaignLink(getPublisherUrl(publisher, campaign));
        //Send email to publisher and admin
        publisherCampaign = publisherCampaignRepository.save(publisherCampaign);

        emailService.sendPublisherCampaignStartToAdmin(publisherCampaign);
        emailService.sendPublisherCampaignStartToPublisher(publisherCampaign);

        return new ApiResponse<>(true,publisherCampaign );
    }

    private PublisherCampaignDetailsDTO mapTo(Campaign campaign){
        PublisherCampaignDetailsDTO publisherCampaignDetailsDTO = new PublisherCampaignDetailsDTO();
        publisherCampaignDetailsDTO.setCampaignName(campaign.getName());
        publisherCampaignDetailsDTO.setCampaignId(campaign.getCampaignId());
        publisherCampaignDetailsDTO.setCountry(campaign.getCountry());
        publisherCampaignDetailsDTO.setCpa(campaign.getCpaCostPerUser());
        publisherCampaignDetailsDTO.setFlow(campaign.getClickFlow());
        publisherCampaignDetailsDTO.setMNO(campaign.getCarrierConnection());
        return publisherCampaignDetailsDTO;
    }
    private String getPublisherUrl(Publisher publisher, Campaign campaign) {

        return clicksUrl
               + "/redirect/"
               + campaign.getCampaignId()
               + "?trxId=valueplus_"
               + publisher.getPubId()
               + "_"
               + publisher.getClickIdParameter()
               + "&sourceId="
               + publisher.getSourceIdParameter();
    }

    private Publisher getCurrentPublisher(){
        String publisherEmail = "";
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetailsImpl userDetails) {
            publisherEmail =  userDetails.getEmail();
        }
       return publisherRepository.findByEmail(publisherEmail).orElseThrow(()-> new PublisherNotFoundException("Publisher not found"));
    }

    @Override
    public PublisherCampaignDetailsDTO fetchPublisherCampaignDetailsByCampaignName(String campaignName) {

        Optional<Campaign> optionalCampaign = campaignRepository.findByName(campaignName);
        if (optionalCampaign.isEmpty()){
            throw new ResourceNotFoundException("Campaign not found");
        }
        PublisherCampaignDetailsDTO publisherCampaignDetailsDTO =  new PublisherCampaignDetailsDTO();
        publisherCampaignDetailsDTO.setCampaignName(campaignName);
        publisherCampaignDetailsDTO.setCarrierConnection(optionalCampaign.get().getCarrierConnection());
        publisherCampaignDetailsDTO.setCampaignStatus(optionalCampaign.get().getStatus());
        publisherCampaignDetailsDTO.setCampaignLink(getPublisherUrl(getCurrentPublisher(),optionalCampaign.get()));
        publisherCampaignDetailsDTO.setCampaignId(optionalCampaign.get().getCampaignId());
        publisherCampaignDetailsDTO.setMNO(optionalCampaign.get().getCarrierConnection());
        publisherCampaignDetailsDTO.setCpa(optionalCampaign.get().getCpaCostPerUser());
        publisherCampaignDetailsDTO.setGender(optionalCampaign.get().getGender());
        publisherCampaignDetailsDTO.setCountry(optionalCampaign.get().getCountry());
        publisherCampaignDetailsDTO.setAgeRange(optionalCampaign.get().getAgeRange());
        publisherCampaignDetailsDTO.setInterest(optionalCampaign.get().getInterest());
        publisherCampaignDetailsDTO.setStartDate(String.valueOf(optionalCampaign.get().getStartDate()));
        publisherCampaignDetailsDTO.setEndDate(String.valueOf(optionalCampaign.get().getEndDate()));
        publisherCampaignDetailsDTO.setBudget(String.valueOf(optionalCampaign.get().getBudget()));
        publisherCampaignDetailsDTO.setFlow(optionalCampaign.get().getClickFlow());

        return publisherCampaignDetailsDTO;
    }

}
