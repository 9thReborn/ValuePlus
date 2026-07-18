package com.nitax.valueplusbackend.service.impl;

import com.nitax.valueplusbackend.domain.CPASettings;
import com.nitax.valueplusbackend.domain.FlowType;
import com.nitax.valueplusbackend.dto.request.CPASettingRequest;
import com.nitax.valueplusbackend.repository.CpaSettingsRepository;
import com.nitax.valueplusbackend.service.CPAService;
import com.nitax.valueplusbackend.utils.AppUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CPAServiceImpl implements CPAService {
    private final CpaSettingsRepository repository;
    private final AppUtils appUtils;

    @Override
    public CPASettings create(CPASettingRequest request) {
        CPASettings cpa = new CPASettings();
        cpa.setCountry(request.getCountry());
        cpa.setFlow(request.getFlow());
        FlowType cpaFlowType = null;
        try {
            cpaFlowType = FlowType.valueOf(request.getFlowType());
        } catch (IllegalArgumentException exception) {
            cpaFlowType = FlowType.BILLABLE;
        }
        cpa.setFlowType(cpaFlowType);
        cpa.setCpaId(appUtils.generateCPAId());
        cpa.setMobileNetworkOperator(request.getMno());
        cpa.setPubCpa(request.getPubCpa());
        cpa.setAdvCpa(request.getAdvCpa());
        return this.repository.save(cpa);
    }

    @Override
    public CPASettings update(String cpaId, CPASettingRequest request) {
        CPASettings oldCPA = this.repository.findCPASettingsByCpaId(cpaId);

        oldCPA.setCountry((request.getCountry() != null && (!request.getCountry().trim().isEmpty())) ? request.getCountry() : oldCPA.getCountry());
        oldCPA.setFlow((request.getFlow() != null && (!request.getFlow().trim().isEmpty())) ? request.getFlow() : oldCPA.getFlow());
        FlowType cpaFlowType = (request.getFlowType().toLowerCase().contains("non")) ? FlowType.NON_BILLABLE : FlowType.BILLABLE;
        oldCPA.setFlowType((request.getFlowType() != null && (!request.getFlowType().trim().isEmpty())) ? cpaFlowType : oldCPA.getFlowType());

        oldCPA.setAdvCpa((request.getAdvCpa() != null && (!request.getAdvCpa().isNaN())) ? request.getAdvCpa() : oldCPA.getAdvCpa());



        oldCPA.setMobileNetworkOperator((request.getMno() != null && (!request.getMno().trim().isEmpty())) ?
                request.getMno() : oldCPA.getMobileNetworkOperator());
        oldCPA.setPubCpa((request.getPubCpa() != null && (!request.getPubCpa().isNaN())) ? request.getPubCpa() : oldCPA.getPubCpa());


        return this.repository.save(oldCPA);
    }

    @Override
    public List<CPASettings> getAll(String cpaId) {
        if (cpaId != null) {
            return List.of(this.repository.findCPASettingsByCpaId(cpaId));
        } else {
            return this.repository.findAll();
        }
    }

    @Override
    @Transactional
    public void deleteById(String cpaId) {
        if (cpaId != null)
            this.repository.deleteByCpaId(cpaId);
    }

    @Override
    public List<CPASettings> getAll(String country, String mno, String flow, String flowType) {
        FlowType type;
        if (flowType == null) {
            type = null;
        } else {
            type = flowType.toLowerCase().contains("non") ? FlowType.NON_BILLABLE : FlowType.BILLABLE;
        }
        if (country != null || mno != null || flow != null || flowType != null) {
            return this.repository.findCpaByFilters(country, flow, mno, type);
        }
        return List.of();
    }
}
