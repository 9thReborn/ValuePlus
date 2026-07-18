package com.nitax.valueplusbackend.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "sms_providers")
public class SmsProvider extends BaseEntity {
    private String name;
    private String apiKey;
    private String apiSecret;
    private String apiUrl;
    private boolean isActive;


    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }

}
