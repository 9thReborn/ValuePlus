package com.nitax.valueplusbackend.service.impl;

import com.nitax.valueplusbackend.domain.*;
import com.nitax.valueplusbackend.dto.request.CreateBulkSmsCampaignRequest;
import com.nitax.valueplusbackend.dto.request.GeminiSendBulkSmsRequest;
import com.nitax.valueplusbackend.dto.request.external.SendExternalBulkSmsRequest;
import com.nitax.valueplusbackend.dto.response.BulkSmsCampaignResponse;
import com.nitax.valueplusbackend.dto.response.CreateBulkSmsCampaignResponse;
import com.nitax.valueplusbackend.dto.response.GeminiSendBulkSmsResponse;
import com.nitax.valueplusbackend.dto.response.external.*;
import com.nitax.valueplusbackend.exception.BulkSmsCampaignException;
import com.nitax.valueplusbackend.exception.ExternalServiceBadRequestException;
import com.nitax.valueplusbackend.exception.ExternalServiceInternalServiceException;
import com.nitax.valueplusbackend.exception.WalletServiceException;
import com.nitax.valueplusbackend.repository.AdvertiserRepository;
import com.nitax.valueplusbackend.repository.BulkSmsCampaignRepository;
import com.nitax.valueplusbackend.repository.BulkSmsMessageRepository;
import com.nitax.valueplusbackend.service.*;
import com.nitax.valueplusbackend.utils.AppUtils;
import com.nitax.valueplusbackend.utils.CsvUtils;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
public class ExternalServiceImpl implements com.nitax.valueplusbackend.service.ExternalService {
    private final AdvertiserRepository advertiserService;
    private final WalletService walletService;
    private final BulkSmsCampaignRepository bulkSmsCampaignRepository;
    private final ProhibitedWordService prohibitedWordService;
    private final CsvStorageService csvStorageService;
    private final ModelMapper modelMapper;
    private final GeminiSmsService geminiSmsService;
    private final AppUtils appUtils;
    private final PhoneNumberService phoneNumberService;
    private final BulkSmsMessageRepository bulkSmsMessageRepository;
    private final SystemExcludedNumberService systemExcludedNumberService;
    private final SmsProviderService smsProviderService;


    @Override
    public GetWalletBalanceResponse getExternalWalletBalance() {
        Advertiser adv = getCurrentAdvertiser();
        try{
            Wallet wallet = walletService.getWalletByAdvertiserId(adv.getId());
            GetWalletBalanceResponse response = new GetWalletBalanceResponse();
            response.setBalance(wallet.getBalance());
            response.setSmsPoints(wallet.getPointsBalance());
            response.setCurrency("NGN");
            response.setMessage("Wallet balance retrieved successfully");
            response.setStatus("success");
            return response;
        }catch (Exception ex){
            throw new ExternalServiceInternalServiceException("Something went wrong on our end, please try again or reach out to us");
        }
    }

    @Override
    public SendSmsResponse sendExternalBulkSms(SendExternalBulkSmsRequest request, MultipartFile csvFile) throws IOException {
        checkForProhibitedWords(request.getContent());
        Advertiser advertiser = getCurrentAdvertiser();
        double walletBalance = walletService.fetchWalletPointBalanceByAdvertiserId(advertiser.getId());

        BulkSmsCampaign campaign = createCampaignFromRequest(request, csvFile);
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

//        if (campaign.getScheduledDate() == null) {
        SmsProvider currentProvider = smsProviderService.getCurrentProvider();

        if (currentProvider.getName().equalsIgnoreCase("GEMINI")) {
            GeminiSendBulkSmsRequest smsRequest = new GeminiSendBulkSmsRequest();
            smsRequest.setText(campaign.getContent());
            smsRequest.setSource(campaign.getSenderId());
            smsRequest.setDestinations(phoneNumberList);

            GeminiSendBulkSmsResponse response = geminiSmsService.sendBulkSms(smsRequest);
            campaign.setTransactionId(response.getTransactionID());
            campaign.setStatus(BulkSmsCampaignStatus.IN_PROGRESS);
            campaign.setProcessor("GEMINI");
            campaign.setProcessChannel("API");

            campaign = bulkSmsCampaignRepository.save(campaign);

            List<BulkSmsMessage> messagesDetails = storeMessageDetails(response.getMessageID(), campaign, phoneNumberList);
            walletService.deductWalletPointAndBalanceByAdvertiserId(advertiser.getId(), requiredPoints);
//        }else{
//            campaign = bulkSmsCampaignRepository.save(campaign);
//        }
            BulkSmsCampaignResponse responseBody = modelMapper.map(campaign, BulkSmsCampaignResponse.class);
            return new SendSmsResponse(campaign.getBulkSmsCampaignId(), "SMS sent successfully", "success", campaign.getStatus(),
                    messagesDetails.stream().map(BulkSmsMessage::getMessageId).collect(Collectors.toList()));
        }else{
//            //Todo: Integrate other providers here
            throw new ExternalServiceBadRequestException("No SMS provider configured. Please contact admin.");
        }
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

    @Override
    public StatusQueryResponse querySmsStatus(String messageId) {
        Optional<BulkSmsMessage> bulkSmsMessage =  bulkSmsMessageRepository.findByMessageId(messageId);
        if (bulkSmsMessage.isEmpty()){
            throw new ExternalServiceBadRequestException("Message ID not found");
        }

        BulkSmsMessage message = bulkSmsMessage.get();
        StatusQueryResponse response =  new StatusQueryResponse();
        response.setError(message.getError());
        response.setMessage("Status retrieved successfully");
        response.setStatus(message.getStatus().name());
        return response;
    }

    @Override
    public GetProhibitedWordsListResponse getProhibitedWordsList() {
        String prohibitedWords = prohibitedWordService.getProhibitedWords();

        return new GetProhibitedWordsListResponse("success",
                "Prohibited words retrieved successfully",
               prohibitedWords);
    }

    @Override
    public AvailableNumbersGeographyResponse getAvailableGeograpyhyList() {
        AvailableNumbersGeographyResponse response = new AvailableNumbersGeographyResponse();
        response.setCity(phoneNumberService.getCities());
//        response.setCountry(new ArrayList<String>()["NIGERIA"]);
        response.setSector(phoneNumberService.getSectors());
        response.setLga(phoneNumberService.getLgas());
        response.setMessage("Geographic data retrieved successfully");
        response.setStatus("success");
        return response;
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

    private  List<BulkSmsMessage > storeMessageDetails(List<String > messageIds, BulkSmsCampaign bulkSmsCampaign,List<String > phoneNumbers){
        List<BulkSmsMessage > messages =  new ArrayList<>();
        for (int i = 0; i < messageIds.size(); i++) {
            BulkSmsMessage message = new BulkSmsMessage();
            message.setCampaign(bulkSmsCampaign);
            message.setPhoneNumber(phoneNumbers.get(i));
            message.setMessageId(messageIds.get(i));
            message.setStatus(SmsDeliveryStatus.PENDING); // initial state
           messages.add(  bulkSmsMessageRepository.save(message));
        }

        return messages;

    }

    private BulkSmsCampaign createCampaignFromRequest(SendExternalBulkSmsRequest request, MultipartFile csv) throws IOException {
        BulkSmsCampaign bulkSmsCampaign =  new BulkSmsCampaign();
        bulkSmsCampaign.setName(request.getName());
        bulkSmsCampaign.setSenderId(request.getSource());
        bulkSmsCampaign.setContent(request.getContent());
        bulkSmsCampaign.setCountry(request.getCountry());
        bulkSmsCampaign.setActive(true);
        bulkSmsCampaign.setBulkSmsCampaignId(appUtils.generateBulkSmsCampaignId());
        if ((null == csv || csv.isEmpty() )&& (null == request.getDestinations() || request.getDestinations().isEmpty())){
            bulkSmsCampaign.setPhoneNumbers(getSystemNumbers(request));
            bulkSmsCampaign.setTotalNumbers(request.getNumberOfTarget());
        }else if(!request.getDestinations().isEmpty()){
            bulkSmsCampaign.setPhoneNumbers(String.join(",", request.getDestinations()));
        }
        else{
            bulkSmsCampaign.setCsv(csvStorageService.uploadCsvFile(csv));
            bulkSmsCampaign.setTotalNumbers(CsvUtils.countPhoneNumbersInCsv(csv));
            bulkSmsCampaign.setPhoneNumbers(CsvUtils.readPhoneNumbersFromCsv(csv));
        }

        return bulkSmsCampaign;
    }


    private Advertiser getCurrentAdvertiser(){
        String advertiserEmail = "";
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetailsImpl userDetails) {
            advertiserEmail =  userDetails.getEmail();
        }
        return advertiserService.findByEmail(advertiserEmail).get();
    }


    private String getSystemNumbers(SendExternalBulkSmsRequest request) {

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

    private List<String> parsePhoneNumbers(String numbers) {
        return Arrays.stream(numbers.split(","))
                .map(String::trim)
                .filter(num -> !num.isEmpty())
                .toList();
    }
}
