package com.nitax.valueplusbackend.service.impl;

import com.nitax.valueplusbackend.domain.*;
import com.nitax.valueplusbackend.dto.request.CreateBulkSmsCampaignRequest;
import com.nitax.valueplusbackend.dto.request.GeminiSendBulkSmsRequest;
import com.nitax.valueplusbackend.dto.request.PisiSendBulkSmsRequest;
import com.nitax.valueplusbackend.dto.response.*;
import com.nitax.valueplusbackend.exception.BulkSmsCampaignException;
import com.nitax.valueplusbackend.repository.BulkSmsCampaignRepository;
import com.nitax.valueplusbackend.repository.BulkSmsMessageRepository;
import com.nitax.valueplusbackend.repository.CampaignRepository;
import com.nitax.valueplusbackend.service.*;
import com.nitax.valueplusbackend.utils.AppUtils;
import com.nitax.valueplusbackend.utils.CsvUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.nitax.valueplusbackend.utils.Constants.POINT_VALUE_NAIRA;


@Service
@AllArgsConstructor
@Slf4j
public class BulkSmsCampaignServiceImpl implements BulkSmsCampaignService {
    private final BulkSmsCampaignRepository bulkSmsCampaignRepository;
    private final WalletService walletService;
    private final PhoneNumberService phoneNumberService;
    private final AdvertiserService advertiserService;
    private final CsvStorageService csvStorageService;
    private final PisiBulkSmsService pisiBulkSmsService;
    private final GeminiSmsService geminiSmsService;
    private final AppUtils appUtils;
    private final ModelMapper modelMapper;
    private final ProhibitedWordService prohibitedWordService;
    private final CampaignRepository campaignRepository;
    private final BulkSmsMessageRepository bulkSmsMessageRepository;
    private final SystemExcludedNumberService systemExcludedNumberService;
    private final SmsProviderService smsProviderService;

    @Override
    public CreateBulkSmsCampaignResponse createBulkSms(CreateBulkSmsCampaignRequest request, MultipartFile csv) throws IOException {
        checkForProhibitedWords(request.getCampaignContent());

        Advertiser advertiser = getCurrentAdvertiser();
        if (null ==  advertiser){
            advertiser =  advertiserService.getAdvertiserDetailsById(request.getAdvertiserId());
            if(advertiser == null){
                log.error("Advertiser not found");
                throw new BulkSmsCampaignException("You must be logged in to create a campaign");
            }
        }
        double walletBalance = walletService.fetchWalletPointBalanceByAdvertiserId(advertiser.getId());

        BulkSmsCampaign campaign = createCampaignFromRequest(request, csv);
        campaign.setAdvertiser(advertiser);

        double requiredPoints = campaign.getTotalNumbers();
        if (walletBalance < requiredPoints) {
            throw new BulkSmsCampaignException("Insufficient wallet points. Please fund your wallet or contact admin.");
        }

        List<String> phoneNumberList = parsePhoneNumbers(campaign.getPhoneNumbers());
        phoneNumberList = removeSystemExcludedPhoneNumbers(phoneNumberList);
        if (phoneNumberList.size() > 1000){
            throw new BulkSmsCampaignException("You can only send bulk SMS to a maximum of 1000 phone numbers at a time.");
        }


        if (campaign.getScheduledDate() == null) {
            SmsProvider currentSMSProvider =  smsProviderService.getCurrentProvider();

            if (currentSMSProvider.getName().equalsIgnoreCase("GEMINI")) {
                GeminiSendBulkSmsRequest smsRequest = new GeminiSendBulkSmsRequest();
                smsRequest.setText(campaign.getContent());
                smsRequest.setSource(campaign.getSenderId());
                smsRequest.setDestinations(phoneNumberList);

                GeminiSendBulkSmsResponse response = geminiSmsService.sendBulkSms(smsRequest);
                campaign.setTransactionId(response.getTransactionID());
                campaign.setStatus(BulkSmsCampaignStatus.IN_PROGRESS);
                campaign.setProcessor("GEMINI");
                campaign.setPhoneNumbers("");

                campaign = bulkSmsCampaignRepository.save(campaign);

                storeMessageDetails(response.getMessageID(), campaign, phoneNumberList);
                walletService.deductWalletPointAndBalanceByAdvertiserId(advertiser.getId(), requiredPoints);
            } else {
                // Default to PISI
//                PisiSendBulkSmsRequest smsRequest = new PisiSendBulkSmsRequest();
//                smsRequest.setMessage(campaign.getContent());
//                smsRequest.setSenderId(campaign.getSenderId());
//                smsRequest.setRecipients(phoneNumberList);
//
//                PisiSendBulkSmsResponse response = pisiBulkSmsService.sendBulkSms(smsRequest);
//                String transactionId = extractTransactionId(response.getMessage());
//                campaign.setTransactionId(transactionId);
//                campaign.setStatus(BulkSmsCampaignStatus.IN_PROGRESS);
//                campaign.setProcessor("PISI");
//                campaign.setPhoneNumbers("");
            }
        }else{
            campaign = bulkSmsCampaignRepository.save(campaign);
        }
        BulkSmsCampaignResponse responseBody = modelMapper.map(campaign, BulkSmsCampaignResponse.class);
        return new CreateBulkSmsCampaignResponse("Bulk SMS campaign created successfully", true, responseBody);
    }
    private List<String> parsePhoneNumbers(String numbers) {
        return Arrays.stream(numbers.split(","))
                .map(String::trim)
                .filter(num -> !num.isEmpty())
                .toList();
    }

    private List<String> removeSystemExcludedPhoneNumbers(List<String> phoneNumbers) {
        String excludedNumbersStr = systemExcludedNumberService.getAllExcludedNumbers();
        List<String> excludedNumbers = Arrays.stream(excludedNumbersStr.split(","))
                .map(String::trim)
                .filter(num -> !num.isEmpty())
                .toList();

        return phoneNumbers.stream()
                .filter(num -> !excludedNumbers.contains(num))
                .collect(Collectors.toList());
    }

    private void storeMessageDetails(List<String> messageIds, BulkSmsCampaign bulkSmsCampaign, List<String> phoneNumbers) {
        List<BulkSmsMessage> messagesToSave = new ArrayList<>();
        for (int i = 0; i < messageIds.size(); i++) {
            BulkSmsMessage message = new BulkSmsMessage();
            message.setCampaign(bulkSmsCampaign);
            message.setPhoneNumber(phoneNumbers.get(i));
            message.setMessageId(messageIds.get(i));
            message.setStatus(SmsDeliveryStatus.PENDING); // initial state
            messagesToSave.add(message);
        }
        // Perform a single save operation for the entire list
        bulkSmsMessageRepository.saveAll(messagesToSave);
    }
    private void checkForProhibitedWords(String content) {
        String prohibitedWord = prohibitedWordService.getProhibitedWords();
        if (prohibitedWord != null && !prohibitedWord.isEmpty() ) {
            // Split the prohibited words into a list
            String[] prohibitedWords = prohibitedWord.split(",\\s*");

            // Check if the content contains any prohibited word
            for (String word : prohibitedWords) {
                if (content.toLowerCase().contains(word.toLowerCase())) {
                    throw new BulkSmsCampaignException("This content contains prohibited words: " + word);
                }
            }
        }
    }


    private  String extractTransactionId(String message) {
        // Define the regex pattern to match the transaction ID
        String regex = "Sms transaction ID :: ([a-f0-9\\-]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);

        // Check if the pattern matches and extract the transaction ID
        if (matcher.find()) {
            return matcher.group(1); // Group 1 contains the transaction ID
        } else {
            throw new IllegalArgumentException("Transaction ID not found in the message");
        }
    }

    private BulkSmsCampaign createCampaignFromRequest(CreateBulkSmsCampaignRequest request, MultipartFile csv) throws IOException {
        BulkSmsCampaign bulkSmsCampaign =  new BulkSmsCampaign();
        bulkSmsCampaign.setName(request.getCampaignName());
        bulkSmsCampaign.setSenderId(request.getSenderId());
        bulkSmsCampaign.setContent(request.getCampaignContent());
        bulkSmsCampaign.setCountry(request.getCountry());
        bulkSmsCampaign.setActive(true);
        bulkSmsCampaign.setBulkSmsCampaignId(appUtils.generateBulkSmsCampaignId());
        if (null == csv || csv.isEmpty()){
            bulkSmsCampaign.setPhoneNumbers(getSystemNumbers(request));
            bulkSmsCampaign.setTotalNumbers(request.getNumberOfTarget());
            bulkSmsCampaign.setLga(request.getLga());
            bulkSmsCampaign.setState(request.getState());
        }else{
            bulkSmsCampaign.setCsv(csvStorageService.uploadCsvFile(csv));
            bulkSmsCampaign.setTotalNumbers(CsvUtils.countPhoneNumbersInCsv(csv));
            bulkSmsCampaign.setPhoneNumbers(CsvUtils.readPhoneNumbersFromCsv(csv));
        }


        if ( null != request.getScheduledDate() && !request.getScheduledDate().isBlank()){
            bulkSmsCampaign.setStatus(BulkSmsCampaignStatus.SCHEDULED);
             Date scheduledDate = Date.from(OffsetDateTime.parse(request.getScheduledDate()).toInstant());
            bulkSmsCampaign.setScheduledDate(scheduledDate);
        }
        return bulkSmsCampaign;
    }



    @Override
    public GetBulkSmsCostEstimate getSmsPointCostEstimateAndValidate(MultipartFile csv, String advertiserId) throws IOException {
        Advertiser advertiser = null;
        if (null ==  advertiserId || advertiserId.isEmpty()) {
            advertiser = getCurrentAdvertiser();
        }else{
            advertiser = advertiserService.getAdvertiserDetailsById(advertiserId);
        }

        if (csv == null || csv.isEmpty()) {
            throw new BulkSmsCampaignException("CSV file must not be empty.");
        }

        // Check content type
        String contentType = csv.getContentType();
        if (!"text/csv".equalsIgnoreCase(contentType) && !"application/vnd.ms-excel".equalsIgnoreCase(contentType)) {
            throw new BulkSmsCampaignException("Uploaded file must be a CSV");
        }

        // Optionally, also check extension
        String filename = csv.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            throw new BulkSmsCampaignException("Invalid file extension. Only CSV files are allowed.");
        }

        double pointsBalance = walletService.fetchWalletPointBalanceByAdvertiserId(advertiser.getId());

         long contactCount = CsvUtils.countPhoneNumbersInCsv(csv);

        GetBulkSmsCostEstimate estimate =  new GetBulkSmsCostEstimate();
        estimate.setAvailablePoints(pointsBalance);
        estimate.setPointsRequired((double) contactCount);
        estimate.setContactCount(contactCount);
        estimate.setStatus("Valid");

        return estimate;
    }

    @Override
    public GetBulkSmsCostEstimate getSmsPointCostEstimate(long targetNumbers, String advertiserId) {
        Advertiser advertiser = null;
        if (null ==  advertiserId || advertiserId.isEmpty()) {
            advertiser = getCurrentAdvertiser();
        }else{
            advertiser = advertiserService.getAdvertiserDetailsById(advertiserId);
        }

        if (targetNumbers <= 0) {
            throw new BulkSmsCampaignException("Target Numbers is required");
        }

        double pointsBalance = walletService.fetchWalletPointBalanceByAdvertiserId(advertiser.getId());


        GetBulkSmsCostEstimate estimate =  new GetBulkSmsCostEstimate();
        estimate.setAvailablePoints(pointsBalance);
        estimate.setPointsRequired((double) targetNumbers);
        estimate.setContactCount(targetNumbers);


        return estimate;
    }

    @Override
    public List<BulkSmsCampaign> findByStatus(BulkSmsCampaignStatus bulkSmsCampaignStatus) {
        return bulkSmsCampaignRepository.findByStatus(bulkSmsCampaignStatus);
    }

    @Override
    public List<BulkSmsCampaignResponse> findAll(int page, int size) {
        List<BulkSmsCampaign> bulkSmsCampaigns = bulkSmsCampaignRepository.findAll(PageRequest.of(page, size)).getContent();
        if (bulkSmsCampaigns.isEmpty()) {
            return Collections.emptyList();
        }
        return mapToBulkSmsCampaign(bulkSmsCampaigns);
    }

    @Override
    public List<BulkSmsCampaign> findByStatusAndAdveritserId(BulkSmsCampaignStatus bulkSmsCampaignStatus, long advertiserId) {
        return bulkSmsCampaignRepository.findByStatus(bulkSmsCampaignStatus);
    }


    @Override
    public BulkSmsCampaign save(BulkSmsCampaign campaign) {
        return bulkSmsCampaignRepository.save(campaign);
    }

    @Override
    public BulkSmsDashboardSummaryDto getDashboardSummary() {
        Advertiser currentAdvertiser = getCurrentAdvertiser();

        // The repository method now returns a List<Object[]>
        List<Object[]> queryResult = bulkSmsCampaignRepository.getBulkSmsDashboardSummaryData(currentAdvertiser.getId());

        // Handle case where no data is found for the advertiser
        if (queryResult == null || queryResult.isEmpty()) {
            return new BulkSmsDashboardSummaryDto(0.0, 0L, 0L, Collections.emptyList(), 0L, null, 0.0, 0.0);
        }

        // Get the first (and likely only) row from the result list
        Object[] row = queryResult.get(0);

        // Extract values with safe casting and null checks based on expected types and indices
        BigDecimal totalPointsBigDecimal = (row[0] != null ? (BigDecimal) row[0] : BigDecimal.ZERO);
        Long completedCampaigns = (row[1] != null ? ((Number) row[1]).longValue() : 0L);
        Long upcomingCampaigns = (row[2] != null ? ((Number) row[2]).longValue() : 0L);
        String upcomingCampaignNamesRaw = (row[3] != null ? (String) row[3] : "");
        Long totalSmsSentForTheMonth = (row[4] != null ? ((Number) row[4]).longValue() : 0L);
        Instant lastCampaignDay = (row[5] != null ? (Instant) row[5] : null);
        Double pointUsedForTheMonth = (row[6] != null ? ((Number) row[6]).doubleValue() : 0.0);

        // Extract raw counts for percentage calculation
        Long currentMonthDeliveredSmsCount = (row[7] != null ? ((Number) row[7]).longValue() : 0L);
        Long previousMonthDeliveredSmsCount = (row[8] != null ? ((Number) row[8]).longValue() : 0L);


        // **** Calculate monthlyAveragePercentage in Java ****
        double monthlyAveragePercentage;
        if (previousMonthDeliveredSmsCount == 0) {
            if (currentMonthDeliveredSmsCount > 0) {
                monthlyAveragePercentage = 100.0; // 100% increase from zero
            } else {
                monthlyAveragePercentage = 0.0; // Both are zero, no change
            }
        } else {
            monthlyAveragePercentage = ((double) (currentMonthDeliveredSmsCount - previousMonthDeliveredSmsCount) / previousMonthDeliveredSmsCount) * 100.0;
        }

        // Construct the DTO
        return new BulkSmsDashboardSummaryDto(
                totalPointsBigDecimal.doubleValue(), // Convert BigDecimal to double for DTO
                completedCampaigns,
                upcomingCampaigns,
                upcomingCampaignNamesRaw != null ? Collections.singletonList(upcomingCampaignNamesRaw) : Collections.emptyList(),
                totalSmsSentForTheMonth,
                lastCampaignDay,
                pointUsedForTheMonth,
                monthlyAveragePercentage
        );
    }

    @Override
    public BulkSmsCampaignResponse getBulkSmsCampaignDetailsByCampaignId(String bulkSmsCampaignId) {
        Optional<BulkSmsCampaign> optionalBulkSmsCampaign = bulkSmsCampaignRepository.findByBulkSmsCampaignId(bulkSmsCampaignId);
        if (optionalBulkSmsCampaign.isPresent()){
            BulkSmsCampaign bulkSmsCampaign =  optionalBulkSmsCampaign.get();
            BulkSmsCampaignResponse bulkSmsCampaignResponse =  new BulkSmsCampaignResponse();
            ModelMapper modelMapper =  new ModelMapper();
            modelMapper.map(bulkSmsCampaign,bulkSmsCampaignResponse);

            // **** CRITICAL CHANGES HERE ****
            // If "Points" truly means "number of SMS/Points" (1 SMS = 1 Point)
            bulkSmsCampaignResponse.setTotalPoints(bulkSmsCampaign.getTotalNumbers()); // Total SMS = Total Points
            bulkSmsCampaignResponse.setPointUsed(bulkSmsCampaign.getTotalDelivered()); // Total Delivered SMS = Points Used
            bulkSmsCampaignResponse.setPointRemaining(bulkSmsCampaign.getTotalNumbers() - bulkSmsCampaign.getTotalDelivered()); // Remaining SMS = Points Remaining
//            bulkSmsCampaignResponse.setAdvertiserName(bulkSmsCampaign.getAdvertiser().getBusinessName());
            bulkSmsCampaignResponse.setTotalAmountSpent(bulkSmsCampaign.getTotalDelivered()*POINT_VALUE_NAIRA.doubleValue());
            // If you need to expose the MONETARY cost, you'd add new fields to BulkSmsCampaignResponse
            // For example:
//             bulkSmsCampaignResponse.setTotalCost(bulkSmsCampaign.getTotalNumbers() * POINT_VALUE_NAIRA.doubleValue());
//             bulkSmsCampaignResponse.setCostUsed(bulkSmsCampaign.getTotalDelivered() * POINT_VALUE_NAIRA.doubleValue());
//             bulkSmsCampaignResponse.setCostRemaining((bulkSmsCampaign.getTotalNumbers() - bulkSmsCampaign.getTotalDelivered()) * POINT_VALUE_NAIRA.doubleValue());

            return bulkSmsCampaignResponse;
        }
        throw new BulkSmsCampaignException("There is no campaign with this id: "+bulkSmsCampaignId);
    }

//    @Override
//    public BulkSmsCampaignManagementResponse getAdvertiserCampaignStats(Instant startDate, Instant endDate) {
//        Advertiser currentAdv = getCurrentAdvertiser();
////        Object [] summaryData =  bulkSmsCampaignRepository.getBulkSmsDashboardSummaryData(curentAdvertiser.getId());
////        Object[] row = (Object[]) summaryData[0];
//        Object[] advertiserCampaignStats = bulkSmsCampaignRepository.getAdvertiserCampaignStats(currentAdv.getId(),startDate,endDate);
//        Object[] row = (Object[]) advertiserCampaignStats[0];
//        log.info("Stats ", advertiserCampaignStats);
//        BulkSmsCampaignManagementResponse response = new BulkSmsCampaignManagementResponse();
//
//        response.setTotalCampaigns(row[0] != null ? ((long) row[0]) :0L);
//        response.setActiveCampaigns(row[1] != null ? ((long) row[1]) :0L);
//        response.setScheduledCampaigns(row[2] != null ? ((long) row[2]) :0L);
//        response.setMessagesSent(row[3] != null ? ((BigDecimal) row[3]) :BigDecimal.ZERO);
//        response.setOverallSuccessRate(row[4] != null ? ((BigDecimal) row[4]) : BigDecimal.ZERO);
//        response.setProhibitedWords(prohibitedWordService.getProhibitedWords());
//
//        return response;
//    }


    @Override
    public BulkSmsCampaignManagementResponse getAdvertiserCampaignStats(Instant startDate, Instant endDate) {
        Advertiser currentAdv = getCurrentAdvertiser();

        // Pass null to the repository if no date range is provided
        if (startDate == null && endDate == null) {
            return mapStatsResponse(bulkSmsCampaignRepository.getAdvertiserCampaignStatsWithoutDateFilter(currentAdv.getId()));
        }

        // Use the existing query if dates are provided
        Object[] advertiserCampaignStats = bulkSmsCampaignRepository.getAdvertiserCampaignStats(currentAdv.getId(), startDate, endDate);
        return mapStatsResponse(advertiserCampaignStats);
    }

    private BulkSmsCampaignManagementResponse mapStatsResponse(Object[] stats) {
        Object[] row = (Object[]) stats[0];
        BulkSmsCampaignManagementResponse response = new BulkSmsCampaignManagementResponse();

        response.setTotalCampaigns(row[0] != null ? ((long) row[0]) : 0L);
        response.setActiveCampaigns(row[1] != null ? ((long) row[1]) : 0L);
        response.setScheduledCampaigns(row[2] != null ? ((long) row[2]) : 0L);
        response.setMessagesSent(row[3] != null ? ((BigDecimal) row[3]) : BigDecimal.ZERO);
        response.setOverallSuccessRate(row[4] != null ? ((BigDecimal) row[4]) : BigDecimal.ZERO);
        response.setProhibitedWords(prohibitedWordService.getProhibitedWords());

        return response;
    }

    @Override
    public List<BulkSmsCampaignResponse> getCampaigns(int page, int size, Instant startDate, Instant endDate, String name) {
        Advertiser currentAdv =  getCurrentAdvertiser();
        Pageable pageable = PageRequest.of(page,size);
        Page<BulkSmsCampaign> bulkSmsCampaigns;
        if (name.equalsIgnoreCase("undefined")){
            name = "";
        }

        if (null == startDate && null == endDate){
            bulkSmsCampaigns = bulkSmsCampaignRepository.findAllByAdvertiser_Id(currentAdv.getId(),pageable);
        }else
        if (!name.isEmpty() || !name.isBlank()){
            bulkSmsCampaigns = bulkSmsCampaignRepository.findAllByAdvertiser_IdAndFilterName(currentAdv.getId(), name, pageable);
        }else{
            bulkSmsCampaigns = bulkSmsCampaignRepository.findAllByAdvertiser_IdAndCreatedDateBetween(currentAdv.getId(), startDate,
                    endDate,pageable);
        }
        return mapToBulkSmsCampaign(bulkSmsCampaigns.getContent());
    }

    @Override
    public GeographicResponse getSystemNumbersGeographicDetails() {
        GeographicResponse geographicResponse = new GeographicResponse();
        geographicResponse.setSectors(phoneNumberService.getSectors());
        geographicResponse.setLgas(phoneNumberService.getLgas());
        geographicResponse.setCities(phoneNumberService.getCities());
        geographicResponse.setStates(phoneNumberService.getStates());
        return geographicResponse;
    }

    @Override
    public void saveAll(Iterable<BulkSmsCampaign> campaigns) {
        bulkSmsCampaignRepository.saveAll(campaigns);
    }

    @Override
    public BulkSmsCampaign getBulkSmsCampaignById(long id) {
        return bulkSmsCampaignRepository.findById(id).orElseThrow(()-> new BulkSmsCampaignException("Campaign not found!"));
    }

    @Override
    public AdminCampaignSummaryResponse getAdminCampaignSummary(String startDateString, String endDateString) {
        if (null == startDateString || null == endDateString) {
            Map<Object, Object> getAdminCampaignSummary = bulkSmsCampaignRepository.getAdminCampaignSummary();

            Long totalCampaigns = ((Number) getAdminCampaignSummary.get("totalCampaign")).longValue();
            Long currentlySending = ((Number) getAdminCampaignSummary.get("currentlySending")).longValue();
            Long scheduled = ((Number) getAdminCampaignSummary.get("scheduled")).longValue();
            Long messagesSent = ((Number) getAdminCampaignSummary.get("messagesSent")).longValue();
            Long totalMessages = ((Number) getAdminCampaignSummary.get("totalMessages")).longValue();
            BigDecimal deliveryRate = (BigDecimal) getAdminCampaignSummary.get("deliveryRate");

            String prohibitedWords = prohibitedWordService.getProhibitedWords();

            AdminCampaignSummaryResponse response = new AdminCampaignSummaryResponse();
            response.setTotalCampaigns(totalCampaigns);
            response.setActiveCampaigns(currentlySending);
            response.setScheduledCampaigns(scheduled);
            response.setTotalMessagesSent(messagesSent);
            response.setTotalMessagesSent(totalMessages);
            response.setDeliveryStatistics(deliveryRate);
            response.setProhibitedWords(prohibitedWords);

            return response;
        }
        Instant startDate = LocalDateTime.parse(startDateString).atZone(ZoneId.of("UTC")).toInstant();
        Instant endDate = LocalDateTime.parse(endDateString).atZone(ZoneId.of("UTC")).toInstant();

        Map<Object, Object> getAdminCampaignSummary = bulkSmsCampaignRepository.getAdminCampaignSummary(startDate, endDate);

        Long totalCampaigns = ((Number) getAdminCampaignSummary.get("totalCampaign")).longValue();
        Long currentlySending = ((Number) getAdminCampaignSummary.get("currentlySending")).longValue();
        Long scheduled = ((Number) getAdminCampaignSummary.get("scheduled")).longValue();
        Long messagesSent = ((Number) getAdminCampaignSummary.get("messagesSent")).longValue();
        Long totalMessages = ((Number) getAdminCampaignSummary.get("totalMessages")).longValue();
        BigDecimal deliveryRate = (BigDecimal) getAdminCampaignSummary.get("deliveryRate");

        String prohibitedWords = prohibitedWordService.getProhibitedWords();

        AdminCampaignSummaryResponse response = new AdminCampaignSummaryResponse();
        response.setTotalCampaigns(totalCampaigns);
        response.setActiveCampaigns(currentlySending);
        response.setScheduledCampaigns(scheduled);
        response.setTotalMessagesSent(messagesSent);
        response.setTotalMessagesSent(totalMessages);
        response.setDeliveryStatistics(deliveryRate);
        response.setProhibitedWords(prohibitedWords);

        return response;
    }

    @Override
    public List<AdminAdvertiserCampaignResponse> getAdminAdvertiserCampaigns(int page, int size, String startDateString, String endDateString) {
        Pageable pageable = PageRequest.of(page, size);

        Instant startDate = LocalDateTime.parse(startDateString).atZone(ZoneId.of("UTC")).toInstant();
        Instant endDate = LocalDateTime.parse(endDateString).atZone(ZoneId.of("UTC")).toInstant();
        Page<BulkSmsCampaign> campaigns = bulkSmsCampaignRepository.findAllByCreatedDateBetween(startDate, endDate, pageable);
        if (campaigns.hasContent()) {
            List<BulkSmsCampaign> bulkSmsCampaigns = campaigns.getContent();
            List<AdminAdvertiserCampaignResponse> responses = new ArrayList<>();
            for (BulkSmsCampaign campaign : bulkSmsCampaigns) {
                AdminAdvertiserCampaignResponse response = new AdminAdvertiserCampaignResponse();
                response.setCampaignId(campaign.getBulkSmsCampaignId());
                response.setAdvertiserName(campaign.getAdvertiser().getBusinessName());
                response.setCampaignName(campaign.getName());
                response.setStatus(campaign.getStatus().name());
                response.setCreatedDate(String.valueOf(campaign.getCreatedDate()));
                response.setScheduledDate(String.valueOf(null == campaign.getScheduledDate()? campaign.getCreatedDate() : campaign.getScheduledDate()));
                responses.add(response);
            }
            return responses;
        }
        return List.of();
    }

    @Override
    public List<HourlyDeliveryRate> getTop3DeliveryRatesByHour() {
        return bulkSmsCampaignRepository.findTop3DeliveryRatesByHour();
//        return List.of();
    }

    @Override
    public List<CampaignDeliveryRate> getCampaignDeliveryRates() {
        return bulkSmsCampaignRepository.findCampaignDeliveryRates();
    }

    @Override
    public List<SMSDeliveryStatusRes> getLiveDeliveryStatus(int page,int size,String startDateString, String endDateString) {

        Pageable pageable = PageRequest.of(page, size);
        Instant startDate = LocalDateTime.parse(startDateString).atZone(ZoneId.of("UTC")).toInstant();
        Instant endDate = LocalDateTime.parse(endDateString).atZone(ZoneId.of("UTC")).toInstant();
        Page<SMSDeliveryStatusRes> deliveryStatusPage = bulkSmsMessageRepository.getDeliveryStatusByCampaignCreatedDate(pageable, startDate, endDate);
        if (deliveryStatusPage.hasContent()) {
            return deliveryStatusPage.getContent();
        }
        // If no content is found, return an empty list
        return List.of();
    }

    @Override
    public List<BulkSMSReportResponse> generateReports(String startDateString, String endDateString) {
        Instant startDate = LocalDateTime.parse(startDateString).atZone(ZoneId.of("UTC")).toInstant();
        Instant endDate = LocalDateTime.parse(endDateString).atZone(ZoneId.of("UTC")).toInstant();
        List<BulkSmsCampaign> responses = bulkSmsCampaignRepository.findAllByCreatedDateBetween(startDate,endDate);
        List<BulkSMSReportResponse>  re = mapToReports(responses);
        if (!responses.isEmpty()) {
            return re;
        }
        return List.of();
    }

    private List<BulkSMSReportResponse> mapToReports(List<BulkSmsCampaign> responses) {
        List<BulkSMSReportResponse> reports = new ArrayList<>();
        for (BulkSmsCampaign response : responses) {
            BulkSMSReportResponse res =  new BulkSMSReportResponse();
            res.setCampaignName(response.getName());
            res.setPublisherApi(response.getProcessor());
            res.setDate(response.getCreatedDate().toString());
            res.setDeliveryBreakdown(response.getStatus().name());
            res.setAdvertiserName(response.getAdvertiser().getBusinessName());
            res.setTotalSmsTarget(response.getTargetNumbers()<=0? response.getTotalNumbers():response.getTargetNumbers());
            res.setTotalAmountSpent((response.getTargetNumbers()<=0? response.getTotalNumbers():response.getTargetNumbers()) * POINT_VALUE_NAIRA.doubleValue());
            reports.add(res);
        }

        return reports;
    }
    private List<BulkSmsCampaignResponse> mapToBulkSmsCampaign(List<BulkSmsCampaign> bulkSmsCampaigns) {
        List<BulkSmsCampaignResponse> responses = new ArrayList<>();
        ModelMapper modelMapper = new ModelMapper();
        bulkSmsCampaigns.forEach(bulkSmsCampaign -> {
            BulkSmsCampaignResponse response = modelMapper.map(bulkSmsCampaign, BulkSmsCampaignResponse.class);
            responses.add(response);
        });
        return responses;
    }

//    private List<BulkSmsCampaignResponse> mapToBulkSmsCampaign(List<BulkSmsCampaign> bulkSmsCampaigns) {
//        List<BulkSmsCampaignResponse> responses = new ArrayList<>();
//        ModelMapper modelMapper = new ModelMapper();
//
//        // Explicit mapping rule
//        modelMapper.typeMap(BulkSmsCampaign.class, BulkSmsCampaignResponse.class)
//                .addMappings(mapper -> mapper.map(
//                        src -> src.getAdvertiser().getBusinessName(), // pick businessName (or firstName/lastName if you prefer)
//                        BulkSmsCampaignResponse::setAdvertiserName
//                ));
//
//        bulkSmsCampaigns.forEach(bulkSmsCampaign -> {
//            BulkSmsCampaignResponse response = modelMapper.map(bulkSmsCampaign, BulkSmsCampaignResponse.class);
//            responses.add(response);
//        });
//        return responses;
//    }



    private String getSystemNumbers(CreateBulkSmsCampaignRequest request) {
        List<PhoneNumber> phoneNumbersList;
        if (null != request.getExcludedNumbers() && !request.getExcludedNumbers().isEmpty()){
            phoneNumbersList =  phoneNumberService.getSystemPhoneNumberByExcludedNumbers(
                    request.getState().toLowerCase(),
                    request.getCity().toUpperCase(),
                    request.getLga().toUpperCase(),
                    request.getNumberOfTarget(),
                    request.getExcludedNumbers()
                    );

            return phoneNumbersList.stream()
                    .map(PhoneNumber::getNumber)
                    .collect(Collectors.joining(","));
        }
        phoneNumbersList = phoneNumberService.getSystemPhoneNumber(
                request.getState().toLowerCase(),
                request.getCity().toUpperCase(),
                request.getLga().toUpperCase(),
                request.getNumberOfTarget()
        );

        return phoneNumbersList.stream()
                .map(PhoneNumber::getNumber)
                .collect(Collectors.joining(","));
    }


    private Advertiser getCurrentAdvertiser(){
        String advertiserEmail = "";
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetailsImpl userDetails) {
            advertiserEmail =  userDetails.getEmail();
        }
        return advertiserService.getAdvertiserDetails(advertiserEmail);
    }
}
