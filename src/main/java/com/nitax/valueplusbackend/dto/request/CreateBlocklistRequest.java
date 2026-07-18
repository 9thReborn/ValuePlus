package com.nitax.valueplusbackend.dto.request;

import com.nitax.valueplusbackend.domain.Blocklist;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateBlocklistRequest {
    @NotEmpty
    private String msisdn;
    @NotNull
    private Blocklist.Scope scope;
    private String serviceId;
    private Integer durationHours;
}

