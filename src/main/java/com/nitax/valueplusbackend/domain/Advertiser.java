package com.nitax.valueplusbackend.domain;

import java.time.Instant;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Entity
@Table(name = "advertisers")
@Data
@SQLDelete(sql = "UPDATE advertisers SET deleted = true WHERE id=?")
@Where(clause = "deleted=false")
public class Advertiser extends BaseEntity {
  @Column(name = "first_name", nullable = false)
  private String firstName;

  @Column(name = "last_name", nullable = false)
  private String lastName;

  @Column(nullable = false)
  private String email;

  @Column(name = "business_name", nullable = false)
  private String businessName;

  @Column(name = "advertiser_id", nullable = false)
  private String advertiserId;

  @Column(nullable = false)
  @JsonIgnore
  private String password;

  @Column(nullable = false)
  private String country;

  @Column(name = "postback_url")
  private String postbackUrl;

  @Column(name = "skypeId")
  private String skype;

  @Column(name = "isEmailVerified")
  private Boolean isEmailVerified = false;

  @Column(name = "isAccountActive")
  private Boolean isAccountActive = true;

  @Enumerated(EnumType.STRING)
  private AdvertiserStatus status;

  @Column(name = "unverified_date", nullable = true)
  @Setter(AccessLevel.NONE)
  private Instant unverifiedDate = Instant.now();


  @Column(name = "rejected_date", nullable = true)
  @Setter(AccessLevel.NONE)
  private Instant rejectedDate;

  @Column(name = "deleted", columnDefinition = "boolean default false")
  private boolean deleted = Boolean.FALSE;

  @Column(name = "isBulkSmsEnabled", columnDefinition = "boolean default false")
  private boolean isBulkSmsEnabled;
  @Column(name = "isMarketingAgencyEnabled", columnDefinition = "boolean default false")
  private boolean isMarketingAgencyEnabled;


  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "role_id", referencedColumnName = "id", nullable = false)
  @JsonIgnore
  private AppRoles role;

  @PrePersist
  public void prepersist(){
    if(this.status == null){
      this.setStatus(AdvertiserStatus.AWAIT_APPROVAL);
    }
  }

  public void setStatus(AdvertiserStatus status) {
    if(status.equals(AdvertiserStatus.UNVERIFIED)){
      this.unverifiedDate = Instant.now();
    }
    if(status.equals(AdvertiserStatus.REJECTED)){
      this.rejectedDate = Instant.now();
    }
    this.status = status;
  }
}
