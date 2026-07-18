package com.nitax.valueplusbackend.service;

import com.nitax.valueplusbackend.domain.CPASettings;
import com.nitax.valueplusbackend.dto.request.CPASettingRequest;

import java.util.List;

public interface CPAService {
    CPASettings create(CPASettingRequest request);

    CPASettings update(String cpaId, CPASettingRequest request);

    List<CPASettings> getAll(String cpaId);

    void deleteById(String cpaId);

    List<CPASettings> getAll(String country, String mno, String flow, String flowType);
}
