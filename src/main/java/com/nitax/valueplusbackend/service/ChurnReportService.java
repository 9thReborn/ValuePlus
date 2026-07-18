package com.nitax.valueplusbackend.service;

import com.nitax.valueplusbackend.dto.ChurnType;
import com.nitax.valueplusbackend.dto.response.ChurnReport;
import java.time.LocalDateTime;
import java.util.List;

public interface ChurnReportService {
    public List<? extends ChurnReport> fetchReports(List<String> campaigns,
                                                    List<String> publishers,
                                                    LocalDateTime startDate,
                                                    LocalDateTime endDate,
                                                    ChurnType duration,
                                                    boolean includeSourceId);
}
