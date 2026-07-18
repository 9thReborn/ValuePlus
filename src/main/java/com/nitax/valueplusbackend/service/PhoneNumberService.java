package com.nitax.valueplusbackend.service;

import com.nitax.valueplusbackend.domain.PhoneNumber;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Async;

import java.io.IOException;
import java.util.List;

public interface PhoneNumberService {
  void loadPhoneNumberFileToDB() throws IOException;

  void sendPendingSMS();

  @Async
  void addPhoneNumberFromCampaign(String msisdn, String campaignId);

  List<PhoneNumber> getSystemPhoneNumber(String state, String originatingCity, String originatingLga, int limit);

  List<PhoneNumber> getSystemPhoneNumberByExcludedNumbers(String state, String originatingCity, String originatingLga, int limit, List<String> excludedNumbers);

  List<String> getCities();
  List<String> getStates();
  List<String> getSectors();
  List<String> getLgas();

}
