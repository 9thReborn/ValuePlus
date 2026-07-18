package com.nitax.valueplusbackend.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "system_excluded_numbers")
public class SystemExcludedNumber extends BaseEntity {
    private  String number;
    private String addedBy;
}
