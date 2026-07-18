package com.nitax.valueplusbackend.exception;

import org.springframework.security.core.AuthenticationException;

public class AppAuthException extends AuthenticationException {

  public AppAuthException(String msg) {
    super(msg);
  }
}
