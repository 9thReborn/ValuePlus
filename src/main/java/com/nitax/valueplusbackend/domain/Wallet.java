package com.nitax.valueplusbackend.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "wallet")
@Data
public class Wallet extends BaseEntity{
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name="advertiser_id")
    private Advertiser advertiser;
    private BigDecimal balance;
    private BigDecimal pointsBalance;
    @OneToMany
    private List<Transaction> transactions;
}
