package com.nitax.valueplusbackend.service.impl;

import com.nitax.valueplusbackend.config.DoConfig;
import com.nitax.valueplusbackend.domain.Notification;
import com.nitax.valueplusbackend.domain.PhoneNumber;
import com.nitax.valueplusbackend.dto.PhoneNumberTemplateDto;
import com.nitax.valueplusbackend.repository.PhoneNumberRepository;
import com.nitax.valueplusbackend.service.CampaignService;
import com.nitax.valueplusbackend.service.NotificationService;
import com.nitax.valueplusbackend.service.PhoneNumberService;
import com.nitax.valueplusbackend.service.SMSService;
import com.univocity.parsers.common.processor.BeanListProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

@Data
@RequiredArgsConstructor
@Service
@Slf4j
public class PhoneNumberServiceImpl implements PhoneNumberService {

  private final S3Client s3Client;
  private final DoConfig doConfig;
  private final PhoneNumberRepository phoneNumberRepository;
  private final SMSService smsService;
  private final NotificationService notificationService;
  private final CampaignService campaignService;

  @Override
  public void loadPhoneNumberFileToDB() throws IOException {

    File phoneNumberFile = null;

    List<String> files = listFiles("phoneNumbers/new/");

    if (files.isEmpty()) {
      log.info("No phone number files found in the S3 bucket");
      return;
    }

    for (String file : files) {
      log.info("Processing file: {}", file);
      phoneNumberFile = downloadFile(file);
      String state = phoneNumberFile.getName().split("_")[1];
      try (Reader inputReader =
          new InputStreamReader(Files.newInputStream(phoneNumberFile.toPath()))) {
        BeanListProcessor<PhoneNumberTemplateDto> rowProcessor =
            new BeanListProcessor<>(PhoneNumberTemplateDto.class);
        CsvParserSettings settings = new CsvParserSettings();
        settings.setHeaderExtractionEnabled(true);
        settings.setProcessor(rowProcessor);
        final int batchSize = 500;
        CsvParser parser = new CsvParser(settings);
        parser.beginParsing(inputReader);

        int count = 0;

        while (parser.parseNext() != null) {
          count++;
          if (count % batchSize == 0) {
            getBeansFromRowProcessor(state, rowProcessor);
          }
        }

        getBeansFromRowProcessor(state, rowProcessor);
      } catch (IOException e) {
        log.error("Error while processing phone number file", e);
      } finally {
        boolean isDeleted = phoneNumberFile.delete();
        if (isDeleted) {
          log.info("File deleted successfully: {}", phoneNumberFile.getName());
          moveFileToProcessedFolder(file);
        }
      }
    }
  }

  private void getBeansFromRowProcessor(
      String state, BeanListProcessor<PhoneNumberTemplateDto> rowProcessor) {
    List<PhoneNumberTemplateDto> phoneNumberTemplateDtos = rowProcessor.getBeans();
    List<PhoneNumber> phoneNumbersToSave = new ArrayList<>();

    for (PhoneNumberTemplateDto phoneNumberTemplateDto : phoneNumberTemplateDtos) {
      List<Notification> notifications =
          notificationService.findByMsisdn(phoneNumberTemplateDto.getSubMsisdn());
      PhoneNumber phoneNumber = new PhoneNumber();

      if (notifications == null || notifications.isEmpty()) {
        log.info(
            "No notifications found for phone number: {}", phoneNumberTemplateDto.getSubMsisdn());
        PhoneNumber existingPhoneNumber =
            phoneNumberRepository.findByNumberAndSector(
                phoneNumberTemplateDto.getSubMsisdn(), "others");

        if (existingPhoneNumber != null) {
          log.info("Phone number already exists: {}", phoneNumberTemplateDto.getSubMsisdn());
          continue;
        }
        phoneNumber.setSector("others");
        phoneNumber.setOriginatingLga(phoneNumberTemplateDto.getOriginatingLga());
        phoneNumber.setOriginatingCity(phoneNumberTemplateDto.getOriginatingCity());
        phoneNumber.setState(state);
        phoneNumber.setNumber(phoneNumberTemplateDto.getSubMsisdn());
        phoneNumbersToSave.add(phoneNumber);
      } else {
        for (Notification notif : notifications) {
          String sector = campaignService.getSectorByCampaignId(notif.getCampaignId());
          PhoneNumber existingPhoneNumber =
              phoneNumberRepository.findByNumberAndSector(
                  phoneNumberTemplateDto.getSubMsisdn(), sector);

          if (existingPhoneNumber != null) {
            log.info("Phone number already exists: {}", phoneNumberTemplateDto.getSubMsisdn());
            continue;
          }
          phoneNumber.setSector(sector);
          phoneNumber.setOriginatingLga(phoneNumberTemplateDto.getOriginatingLga());
          phoneNumber.setOriginatingCity(phoneNumberTemplateDto.getOriginatingCity());
          phoneNumber.setState(state);
          phoneNumber.setNumber(phoneNumberTemplateDto.getSubMsisdn());
          phoneNumbersToSave.add(phoneNumber);
        }
      }

      if (phoneNumbersToSave.size() >= 500) {
        phoneNumberRepository.saveAll(phoneNumbersToSave);
        phoneNumbersToSave.clear();
      }
    }

    if (!phoneNumbersToSave.isEmpty()) {
      phoneNumberRepository.saveAll(phoneNumbersToSave);
    }

    rowProcessor.getBeans().clear();
  }

  @Override
  public void sendPendingSMS() {
    //    List<PhoneNumber> pendingPhoneNumbers =
    //        phoneNumberRepository.findByDeliveryStatus(
    //            PhoneNumber.Status.PENDING, PageRequest.of(0, 100));
    //
    //    String text = "Hello, this is a test message";
    //    for (PhoneNumber phoneNumber : pendingPhoneNumbers) {
    //      PISISendSMSResponse response = smsService.sendSMS(phoneNumber.getNumber(), text);
    //      if (Objects.nonNull(response)
    //          && response.getSuccess()
    //          && response.getStatusCode().equalsIgnoreCase("1000")) {
    //        phoneNumber.setDeliveryStatus(PhoneNumber.Status.PROCESSED);
    //        phoneNumber.setDeliveryDate(LocalDateTime.now());
    //        phoneNumber.setSmsText(text);
    //        phoneNumberRepository.save(phoneNumber);
    //      }
    //      log.info("SMS sent to: {}", phoneNumber.getNumber());
    //      log.info("Response: {}", response);
    //    }
  }

  @Override
  @Async
  public void addPhoneNumberFromCampaign(String msisdn, String campaignId) {
    //    PhoneNumber existingPhoneNumber = phoneNumberRepository.findByNumber(msisdn).orElse(null);
    //    if (existingPhoneNumber != null &&
    // existingPhoneNumber.getSector().equalsIgnoreCase("others")) {
    //      String sector = campaignService.getSectorByCampaignId(campaignId);
    //      existingPhoneNumber.setSector(sector);
    //      phoneNumberRepository.save(existingPhoneNumber);
    //    } else if (existingPhoneNumber == null) {
    //      String sector = campaignService.getSectorByCampaignId(campaignId);
    //      PhoneNumber phoneNumber = new PhoneNumber();
    //      phoneNumber.setSector(sector);
    //      phoneNumber.setNumber(msisdn);
    //      phoneNumber.setOriginatingCity("others");
    //      phoneNumber.setOriginatingLga("others");
    //      phoneNumber.setState("others");
    //      phoneNumberRepository.save(phoneNumber);
    //    }
  }

  @Override
  public List<PhoneNumber> getSystemPhoneNumber(
      String state, String originatingCity, String originatingLga, int limit) {
    Pageable pageable = PageRequest.of(0, limit); // 0 is the page number, target is the limit
    return phoneNumberRepository.findByStateAndOriginatingCityAndOriginatingLga(
        state, originatingCity, originatingLga, pageable);
  }

  @Override
  public List<PhoneNumber> getSystemPhoneNumberByExcludedNumbers(
      String state,
      String originatingCity,
      String originatingLga,
      int limit,
      List<String> excludedNumbers) {
    Pageable pageable = PageRequest.of(0, limit); // 0 is the page number, target is the limit
    return phoneNumberRepository.findByStateAndOriginatingCityAndOriginatingLgaExcludingNumbers(
        state, originatingCity, originatingLga, excludedNumbers, pageable);
  }

  @Override
  public List<String> getCities() {
    return phoneNumberRepository.getDistinctCity();
  }

  @Override
  public List<String> getStates() {
    return phoneNumberRepository.getDistinctStates();
  }

  @Override
  public List<String> getSectors() {
    return phoneNumberRepository.getDistinctSector();
  }

  @Override
  public List<String> getLgas() {
    return phoneNumberRepository.getDistinctLgas();
  }

  private File downloadFile(String key) throws IOException {
    Path localFilePath = Path.of("./reports", key);

    Files.createDirectories(localFilePath.getParent());

    File localFile = localFilePath.toFile();
    if (localFile.exists()) {
      log.info("File already exists locally: " + localFilePath);
      localFile.delete();
    }

    GetObjectRequest getObjectRequest =
        GetObjectRequest.builder().bucket(doConfig.getAwsBucketName()).key(key).build();

    s3Client.getObject(getObjectRequest, ResponseTransformer.toFile(localFilePath.toFile()));
    return localFilePath.toFile();
  }

  public List<String> listFiles(String folderKey) throws IOException {
    if (!folderKey.endsWith("/")) {
      folderKey += "/";
    }

    ListObjectsV2Request listRequest =
        ListObjectsV2Request.builder()
            .bucket(doConfig.getAwsBucketName())
            .prefix(folderKey)
            .build();

    ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);

    List<String> files = new ArrayList<>();

    for (S3Object s3Object : listResponse.contents()) {
      String key = s3Object.key();

      if (key.endsWith("/")) {
        continue;
      }
      files.add(key);
    }
    return files;
  }

  private void moveFileToProcessedFolder(String key) {

    String destinationKey = "phoneNumbers/processed/" + key.substring(key.lastIndexOf("/") + 1);

    CopyObjectRequest copyObjectRequest =
        CopyObjectRequest.builder()
            .sourceBucket(doConfig.getAwsBucketName())
            .sourceKey(key)
            .destinationBucket(doConfig.getAwsBucketName())
            .destinationKey(destinationKey)
            .build();
    s3Client.copyObject(copyObjectRequest);

    log.info("File copied to: " + destinationKey);

    DeleteObjectRequest deleteObjectRequest =
        DeleteObjectRequest.builder().bucket(doConfig.getAwsBucketName()).key(key).build();
    s3Client.deleteObject(deleteObjectRequest);

    log.info("Original file deleted: " + key);
  }
}
