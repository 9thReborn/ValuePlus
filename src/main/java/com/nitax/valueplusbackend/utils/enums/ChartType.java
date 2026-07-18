package com.nitax.valueplusbackend.utils.enums;

import lombok.Getter;

@Getter
public enum ChartType {
  CLICKS("clicks"),
  CONVERSIONS("conversions"),
  RETENTION("retention"),
  CHURN("churn");

  private final String label;

  private ChartType(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
