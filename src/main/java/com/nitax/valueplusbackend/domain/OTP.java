package com.nitax.valueplusbackend.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "otp")
@Data
public class OTP extends BaseEntity {
  @Column(name = "code", nullable = false)
  private String code;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "expire_at", nullable = false)
  private Instant expireAt;

  public OTP() {}

  public OTP(String code, Long userId) {
    this.code = code;
    this.userId = userId;
    this.expireAt = Instant.now().plus(Duration.ofMinutes(10));
  }

  @Transient
  public boolean isValid() {
    LocalDateTime now = LocalDateTime.now();
    return now.isBefore(LocalDateTime.ofInstant(expireAt, ZoneId.systemDefault()));
  }
}
