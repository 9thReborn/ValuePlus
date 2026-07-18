package com.nitax.valueplusbackend.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreatePublisherDTO {

  @NotBlank(message = "Name is required")
  @Size(max = 255, message = "Name cannot exceed 255 characters")
  private String name;

  @NotBlank(message = "Publisher URL is required")
  @Size(max = 255, message = "Publisher URL cannot exceed 255 characters")
  private String pubUrl;

  @NotBlank(message = "Email is required")
  @Email(message = "Invalid email format")
  private String email;

  @Size(max = 255, message = "Website cannot exceed 255 characters")
  private String website;

  @Size(max = 255, message = "Website cannot exceed 255 characters")
  private String identifier;

  @NotBlank(message = "PostbackURL of Publisher is required")
  private String postbackUrl;

  @NotBlank(message = "ClickId is required")
  @Size(max = 255, message = "ClickId cannot exceed 255 characters")
  private String clickIdParameter;

  @NotBlank(message = "SourceId is required")
  @Size(max = 255, message = "SourceId cannot exceed 255 characters")
  private String sourceIdParameter;

  @NotBlank(message = "Password is required")
  @Size(max = 255, message = "Password cannot exceed 255 characters")
  private String password;
}
