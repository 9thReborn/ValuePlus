package com.nitax.valueplusbackend.utils.enums;

public enum CampaignTypes {
  CPA("CPA"),
  CPM("CPM"),
  CPI("CPI"),
  CPV("CPV"),
  CPS("CPS"),
  CPC("CPC");

  private final String name;

  CampaignTypes(String name) {
    this.name = name;
  }
}
