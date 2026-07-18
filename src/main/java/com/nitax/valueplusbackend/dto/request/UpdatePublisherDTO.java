package com.nitax.valueplusbackend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdatePublisherDTO {

  @Size(max = 255, message = "Name cannot exceed 255 characters")
  private String name;

  //  @Size(max = 255, message = "Publisher URL cannot exceed 255 characters")
  private String pubUrl;

  @Email(message = "Invalid email format")
  private String email;

  @Size(max = 255, message = "Website cannot exceed 255 characters")
  private String website;

  //  @Size(max = 255, message = "Website cannot exceed 255 characters")
  private String identifier;

  private String postbackUrl;

  @Size(max = 255, message = "ClickId cannot exceed 255 characters")
  private String clickIdParameter;

  @Size(max = 255, message = "SourceId cannot exceed 255 characters")
  private String sourceIdParameter;
}
