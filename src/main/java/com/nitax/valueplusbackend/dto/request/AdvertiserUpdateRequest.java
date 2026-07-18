package com.nitax.valueplusbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NonNull;

@Data
public class AdvertiserUpdateRequest {
    @NotBlank @NotEmpty
    private String firstName;
    @NotEmpty @NotBlank
    private String lastName;
    @NotEmpty @NotBlank
    private String email;
    @NotEmpty @NotBlank
    private String country;
    @NotEmpty @NotBlank
    private String businessName;
    @NotEmpty @NotBlank
    private String skype;
    @NotEmpty @NotBlank
    private String status;
}
