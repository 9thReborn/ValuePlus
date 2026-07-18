package com.nitax.valueplusbackend.dto.request;

import com.nitax.valueplusbackend.utils.enums.CampaignTypes;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import lombok.Data;

@Data
public class CampaignFilter {
  @NotNull private String name;
  private CampaignTypes campaignType;
  private String country;
  private String status;
  @PastOrPresent private LocalDate startDate;
  @PastOrPresent private LocalDate endDate;

  public void setName(String name) {
    if (name != null && !name.isEmpty()) {
      this.name = name;
    }
  }

  private static boolean isValidDateFormat(String dateString) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    sdf.setLenient(false);
    try {
      sdf.parse(dateString);
      return true;
    } catch (ParseException e) {
      return false;
    }
  }
}
