package com.nitax.valueplusbackend.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(
    name = "phone_numbers",
    indexes = {
      @Index(name = "idx_number", columnList = "number"),
      @Index(name = "idx_number_sector", columnList = "number, sector"),
      @Index(
          name = "idx_all_fields",
          columnList = "number, originatingLga, originatingCity, state, sector")
    })
@Data
public class PhoneNumber extends BaseEntity {

  private String number;

  private String originatingLga;

  private String originatingCity;

  private String state;

  private String sector;
}
