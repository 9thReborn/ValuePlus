package com.nitax.valueplusbackend.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "cpa_settings")
@Data
public class CPASettings extends BaseEntity{
    @Column(name = "cpa_Id", nullable = false)
    private String cpaId;

    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "mobile_network_operator", nullable = false)
    private String mobileNetworkOperator;

    @Column(name = "flow", nullable = false)
    private String flow;

    @Enumerated(EnumType.STRING)
    private FlowType flowType;

    @Column(name = "pub_cpa")
    private Double pubCpa;

    @Column(name = "adv_cpa")
    private Double advCpa;
}
