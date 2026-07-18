package com.nitax.valueplusbackend.dto.request;

import com.nitax.valueplusbackend.dto.ChurnType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ChurnReportRequestDTO implements Serializable {
    @NotNull
    ChurnType churnTypes;
    @NotNull
    private List<String> campaigns;
    @NotNull
    private List<String> publishers;
    @PastOrPresent
    private LocalDateTime startDate;
    @PastOrPresent
    private LocalDateTime endDate;
    private boolean includeSourceId;
}
