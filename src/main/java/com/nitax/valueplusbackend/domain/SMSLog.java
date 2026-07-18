package com.nitax.valueplusbackend.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "SMSLogs")
@Data
public class SMSLog extends BaseEntity {

  private String smsText;

  private Long transactionId;

  @Enumerated(EnumType.STRING)
  private Status status;

  public enum Status {
    PROCESSED,
    PENDING,
    FAILED
  }
}
