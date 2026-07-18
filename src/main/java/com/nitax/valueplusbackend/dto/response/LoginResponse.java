package com.nitax.valueplusbackend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {

  private String accessToken;
  private String refreshToken;

  public LoginResponse(String accessToken) {
    this.accessToken = accessToken;
  }

  public LoginResponse(String accessToken, String refreshToken) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
  }
}
