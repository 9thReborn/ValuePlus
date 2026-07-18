package com.nitax.valueplusbackend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PublisherSignUpRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Business name is required")
    private String businessName;

    @NotBlank(message = "SkypeID is required")
    private String skypeId;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Country is required")
    private String country;

    @NotBlank(message = "Publisher Url  is required")
    private String pubUrl;

    @NotBlank(message = "Postback Url  is required")
    private String postBackUrl;

}
