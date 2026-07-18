package com.nitax.valueplusbackend.dto.response;

import lombok.Data;

@Data
public class MonthlyConversionCount {
  private int month;
  private int year;
  private long count;

  public MonthlyConversionCount(int year, int month, long count) {
    this.month = month;
    this.year = year;
    this.count = count;
  }
}
