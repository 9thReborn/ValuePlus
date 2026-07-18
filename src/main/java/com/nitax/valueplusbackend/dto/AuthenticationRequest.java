package com.nitax.valueplusbackend.dto;

import lombok.Data;

@Data
public class AuthenticationRequest {

  private String emailAddress;
  private String password;
}
