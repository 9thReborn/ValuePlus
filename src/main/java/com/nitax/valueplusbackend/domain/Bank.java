package com.nitax.valueplusbackend.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bank")
@Data
@NoArgsConstructor
public class Bank extends BaseEntity{
    private String bankName;
    private String accountNumber;
    private String accountName;
    private boolean isActive;
    @OneToOne
    private Admin addedBy;
    @OneToOne
    private Admin updatedBy;
}
