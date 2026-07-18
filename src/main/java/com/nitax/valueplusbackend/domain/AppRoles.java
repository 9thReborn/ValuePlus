package com.nitax.valueplusbackend.domain;

import jakarta.persistence.*;
import lombok.Data;

@Table(name = "roles")
@Entity
@Data
public class AppRoles extends BaseEntity {

  @Column(unique = true, nullable = false)
  @Enumerated(EnumType.STRING)
  private com.nitax.valueplusbackend.utils.enums.Role name;

  @Column(nullable = false)
  private String description;
}
