package com.nitax.valueplusbackend.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "contact_request")
@Data
@NoArgsConstructor
public class ContactRequest extends BaseEntity{
    private String name;
    private String email;
    private String skypeId;
    private String company;
    private String industryName;
}
