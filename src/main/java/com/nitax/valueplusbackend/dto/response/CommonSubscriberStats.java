package com.nitax.valueplusbackend.dto.response;

import lombok.Data;

@Data
public class CommonSubscriberStats {
  private long numberOfCommonSubscribers;
  private long numberOfUniqueSubscribers;
  private double percentageOfCommonSubscribers;
}
