package com.nitax.valueplusbackend.service.impl;

import com.nitax.valueplusbackend.domain.SmsProvider;
import com.nitax.valueplusbackend.repository.SmsProviderRepository;
import com.nitax.valueplusbackend.service.SmsProviderService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Provider;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class SmsProviderServiceImpl implements SmsProviderService {
    private final SmsProviderRepository smsProviderRepository;

    @Override
    public List<SmsProvider> findAll() {
        return smsProviderRepository.findAll();
    }

    @Override
    public SmsProvider getCurrentProvider() {
        SmsProvider smsProviderOptional = smsProviderRepository.findSmsProviderByActive(true);
        return smsProviderOptional;
    }

    @Override
    public SmsProvider setCurrentProvider(Long providerId) {
        SmsProvider newProvider = smsProviderRepository.findById(providerId).orElse(null);
        if (newProvider == null) {
            return null;
        }
        if (newProvider.isActive()) {
            return newProvider;
        }
        SmsProvider currentProvider = smsProviderRepository.findSmsProviderByActive(true);
        if (currentProvider != null) {
            currentProvider.setActive(false);
            smsProviderRepository.save(currentProvider);
        }
        newProvider.setActive(true);
        return smsProviderRepository.save(newProvider);
    }
}
