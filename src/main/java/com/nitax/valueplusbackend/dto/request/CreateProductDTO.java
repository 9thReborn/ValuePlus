package com.nitax.valueplusbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateProductDTO {

  @NotBlank(message = "Product name is required")
  private String name;

  @NotBlank(message = "Postback URL is required")
  private String postbackUrl;
}
