package com.nitax.valueplusbackend.service;

import com.nitax.valueplusbackend.domain.SmsProvider;

import java.security.Provider;
import java.util.List;

public interface SmsProviderService {
    List<SmsProvider> findAll();
    SmsProvider getCurrentProvider();
    SmsProvider setCurrentProvider(Long providerId);
}
