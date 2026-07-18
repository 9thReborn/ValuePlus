package com.nitax.valueplusbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class CPASettingRequest implements Serializable {

    @NotBlank(message = "Country cannot be blank")
    private String country;

    @NotBlank(message = "mobileNetworkOperator cannot be blank")
    private String mno;

    @NotBlank(message = "flow cannot be blank")
    private String flow;

    @NotBlank(message = "FlowType cannot be blank")
    private String flowType;

    @NotNull(message = "pubcpa cannot be blank")
    private Double pubCpa;

    @NotNull(message = "advCPa cannot be blank")
    private Double advCpa;
}
