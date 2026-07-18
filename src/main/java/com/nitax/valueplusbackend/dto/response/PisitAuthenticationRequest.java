package com.nitax.valueplusbackend.dto.response;

import lombok.Data;

@Data
public class PisitAuthenticationRequest {
    private String password;
    private String vaspid;
}
