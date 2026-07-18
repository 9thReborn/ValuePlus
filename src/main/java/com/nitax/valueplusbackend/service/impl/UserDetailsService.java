package com.nitax.valueplusbackend.service.impl;

import com.nitax.valueplusbackend.domain.Admin;
import com.nitax.valueplusbackend.domain.Advertiser;
import com.nitax.valueplusbackend.domain.Publisher;
import com.nitax.valueplusbackend.exception.AdvertiserNotFoundException;
import com.nitax.valueplusbackend.exception.AppAuthException;
import com.nitax.valueplusbackend.repository.AdminRepository;
import com.nitax.valueplusbackend.repository.AdvertiserRepository;
import java.util.Optional;

import com.nitax.valueplusbackend.repository.PublisherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserDetailsService
    implements org.springframework.security.core.userdetails.UserDetailsService {
  private final AdvertiserRepository advertiserRepository;
  private final AdminRepository adminRepository;
  private final PublisherRepository publisherRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws AdvertiserNotFoundException {
    Optional<Advertiser> advertiser = advertiserRepository.findByEmail(email);
    Optional<Publisher> publisher = publisherRepository.findByEmail(email);

    if (advertiser.isPresent()) {
      switch (advertiser.get().getStatus()){
        case REJECTED -> throw new AppAuthException("Sorry, you can't login. Your account has been rejected");
        case UNVERIFIED -> throw new AppAuthException("Sorry you can't login. Your account is unverified. Please check your email and verify your account.");
        case AWAIT_APPROVAL -> throw new AppAuthException("Sorry you can't login. Your account is awaiting approval. Please wait or contact the administrator");
        case SUSPENDED -> throw new AppAuthException("Sorry you can't login. Your account is suspended. Please contact the administrator");
      }
      return UserDetailsImpl.build(advertiser.get());
    }else if (publisher.isPresent()){
      switch (publisher.get().getStatus()){
        case AWAIT_APPROVAL -> throw new AppAuthException("Sorry you can't login. Your account is awaiting approval. Please wait or contact the administrator");
        case SUSPENDED -> throw new AppAuthException("Sorry you can't login. Your account is suspended. Please contact the administrator");
      }
      return UserDetailsImpl.build(publisher.get());

    }


    Optional<Admin> admin = adminRepository.findByEmail(email);

    if (admin.isPresent()) {
      return UserDetailsImpl.build(admin.get());
    }
    throw new AppAuthException("Bad credentials");
  }

  public Advertiser getAdvertiserFromSecurityContext() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    if (principal instanceof UserDetailsImpl) {
      UserDetailsImpl userDetails = (UserDetailsImpl) principal;
      return advertiserRepository
          .findByEmail(userDetails.getEmail())
          .orElseThrow(() -> new AdvertiserNotFoundException("Kindly login to continue."));
    } else {
      throw new AdvertiserNotFoundException("unauthenticated");
    }
  }
  public Publisher getPublisherFromSecurityContext() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    if (principal instanceof UserDetailsImpl) {
      UserDetailsImpl userDetails = (UserDetailsImpl) principal;
      return publisherRepository
              .findByEmail(userDetails.getEmail())
              .orElseThrow(() -> new AdvertiserNotFoundException("Kindly login to continue."));
    } else {
      throw new AdvertiserNotFoundException("unauthenticated");
    }
  }

  public Admin getAdminFromSecurityContext() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    if (principal instanceof UserDetailsImpl) {
      UserDetailsImpl userDetails = (UserDetailsImpl) principal;
      return adminRepository
          .findByEmail(userDetails.getEmail())
          .orElseThrow(() -> new AdvertiserNotFoundException("Kindly login to continue."));
    } else {
      throw new AdvertiserNotFoundException("unauthenticated");
    }
  }
}
