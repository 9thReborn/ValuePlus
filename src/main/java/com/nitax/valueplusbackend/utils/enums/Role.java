package com.nitax.valueplusbackend.utils.enums;

public enum Role {
  ADMIN("ADMIN"),
  SUPER_ADMIN("SUPER_ADMIN"), ADVERTISER("ADVERTISER"), PUBLISHER("PUBLISHER");
  ;



  private final String authority;

  Role(String authority) {
    this.authority = authority;
  }

  public String getAuthority() {
    return authority;
  }
}
