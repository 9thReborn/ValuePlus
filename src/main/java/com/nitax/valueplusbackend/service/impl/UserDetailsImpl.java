package com.nitax.valueplusbackend.service.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nitax.valueplusbackend.domain.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Data
public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String email;

    @JsonIgnore
    private String password;

    private String role;

    private String postbackUrl;
    private boolean isEnabled;

    public UserDetailsImpl(Admin admin) {
        this.id = admin.getId();
        this.email = admin.getEmail();
        this.password = admin.getPassword();
        this.role = admin.getRole().getName().toString();
        this.isEnabled = true;
    }

    public UserDetailsImpl(Advertiser advertiser) {
        this.id = advertiser.getId();
        this.email = advertiser.getEmail();
        this.password = advertiser.getPassword();
        this.postbackUrl = advertiser.getPostbackUrl();
        this.isEnabled = ((advertiser.getStatus().statusCode() >= AdvertiserStatus.APPROVED.statusCode())
                          && (advertiser.getStatus() != AdvertiserStatus.SUSPENDED));
        this.role = "ADVERTISER";
    }

    public UserDetailsImpl(Publisher publisher) {
        this.id = publisher.getId();
        this.email = publisher.getEmail();
        this.password = publisher.getPassword();
        this.role = "PUBLISHER";
        this.isEnabled = (publisher.getStatus() == PublisherStatus.APPROVED);
    }

    public static UserDetailsImpl build(Advertiser user) {
        return new UserDetailsImpl(user);
    }

    public static UserDetailsImpl build(Admin user) {
        return new UserDetailsImpl(user);
    }

    public static UserDetails build(Publisher publisher) {
        return new UserDetailsImpl(publisher);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + this.role));
    }

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.isEnabled;
    }

}
