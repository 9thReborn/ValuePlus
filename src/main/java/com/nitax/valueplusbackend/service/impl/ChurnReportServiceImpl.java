package com.nitax.valueplusbackend.service.impl;

import com.nitax.valueplusbackend.dto.ChurnType;
import com.nitax.valueplusbackend.dto.response.ChurnReport;
import com.nitax.valueplusbackend.service.ChurnReportService;
import com.nitax.valueplusbackend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChurnReportServiceImpl implements ChurnReportService {

    @Autowired
    private NotificationService service;


    public List<? extends ChurnReport> fetchReports(List<String> campaigns,
                                                    List<String> publishers,
                                                    LocalDateTime startDate,
                                                    LocalDateTime endDate,
                                                    ChurnType churnType,
                                                    boolean includeSourceId) {
        return this.service.fetchReports(campaigns, publishers, startDate, endDate, churnType, includeSourceId);
    }

}
