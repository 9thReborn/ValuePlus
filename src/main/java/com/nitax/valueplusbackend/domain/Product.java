package com.nitax.valueplusbackend.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "products")
@Data
public class Product extends BaseEntity {

  @Column(name = "prod_id", nullable = false)
  private String prodId;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "postback_url", nullable = false)
  private String postbackUrl;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "advertiser_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JsonIgnore
  private Advertiser advertiser;
}
