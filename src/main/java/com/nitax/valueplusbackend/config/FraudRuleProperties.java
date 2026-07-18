package com.nitax.valueplusbackend.config;

import lombok.Data;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "valueplus.fraud")
@Data
public class FraudRuleProperties {

    private int sameServiceCooldownHours;
    private int churnFrequencyWindowHours;
    private int tempBlockDurationHours;
}
