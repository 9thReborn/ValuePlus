package com.nitax.valueplusbackend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ContactRequestDto {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;


    @NotEmpty(message = "Skype id is required")
    private String skypeId;


    @NotBlank(message = "Company Name is required")
    private String company;

    @NotBlank(message = "Industry name is required")
    private String industryName;
}
