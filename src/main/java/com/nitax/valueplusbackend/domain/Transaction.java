package com.nitax.valueplusbackend.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "transaction")
@Data
public class Transaction extends BaseEntity{
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name="advertiser_id")
    private Advertiser advertiser;
    private Date transactionDate;
    private BigDecimal amount;
    private double costPerSms;
    private double pointAssigned;
    private String transactionType; // CREDIT or DEBIT
    private double pointDeducted;
    private double pointRefunded;
    private String transactionId;
}
