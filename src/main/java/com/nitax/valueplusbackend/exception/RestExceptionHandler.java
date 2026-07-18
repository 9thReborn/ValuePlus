package com.nitax.valueplusbackend.exception;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.nitax.valueplusbackend.dto.response.ApiResponse;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(DuplicatePublisherException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ApiResponse<String>> handleDuplicatePublisherException(
      DuplicatePublisherException ex) {
    ApiResponse<String> apiResponse =
        ApiResponse.<String>builder().success(false).data(ex.getMessage()).build();
    return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(DuplicateAdvertiserException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ApiResponse<String>> handleDuplicateAdvertiserException(
      DuplicateAdvertiserException ex) {
    ApiResponse<String> apiResponse =
        ApiResponse.<String>builder().success(false).data(ex.getMessage()).build();
    return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(AppException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ApiResponse<String>> handleDuplicateAppException(AppException ex) {
    ApiResponse<String> apiResponse =
        ApiResponse.<String>builder().success(false).data(ex.getMessage()).build();
    return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(AdvertiserNotFoundException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ApiResponse<String>> handleAdvertiserNotFoundException(
      AdvertiserNotFoundException ex) {
    ApiResponse<String> apiResponse =
        ApiResponse.<String>builder().success(false).data(ex.getMessage()).build();
    return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ApiResponse<String>> handleResourcerNotFoundException(
      ResourceNotFoundException ex) {
    ApiResponse<String> apiResponse =
        ApiResponse.<String>builder().success(false).data(ex.getMessage()).build();
    return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(AppAuthException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ResponseEntity<ApiResponse<String>> handleAppAuthException(AppAuthException ex) {
    ApiResponse<String> apiResponse =
        ApiResponse.<String>builder().success(false).data(ex.getMessage()).build();
    return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ApiResponse<String>> handleAuthenticationException(
      AuthenticationException ex) {
    ApiResponse<String> apiResponse =
        ApiResponse.<String>builder().success(false).data(ex.getMessage()).build();
    return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(BulkSmsCampaignException.class)
  public ResponseEntity<ApiResponse<String>> handleBulkSmsCampaignException(
      BulkSmsCampaignException ex) {
    ApiResponse<String> apiResponse =
        ApiResponse.<String>builder().success(false).data(ex.getMessage()).build();
    return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(WalletServiceException.class)
  public ResponseEntity<ApiResponse<String>> handleWalletServiceException(
      WalletServiceException ex) {
    ApiResponse<String> apiResponse =
        ApiResponse.<String>builder().success(false).data(ex.getMessage()).build();
    return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    List<String> errors = new ArrayList<>();
    for (FieldError error : ex.getBindingResult().getFieldErrors()) {
      errors.add(error.getField() + " " + error.getDefaultMessage());
    }
    ApiResponse<List<String>> apiResponse =
        ApiResponse.<List<String>>builder().success(false).data(errors).build();

    return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolationException(
      ConstraintViolationException ex) {
    Map<String, String> errors = new HashMap<>();
    for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
      errors.put(violation.getPropertyPath().toString(), violation.getMessage());
    }
    ApiResponse<Map<String, String>> apiResponse =
        ApiResponse.<Map<String, String>>builder().success(false).data(errors).build();

    return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(QueryTimeoutException.class)
  @ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
  public ResponseEntity<ApiResponse<String>> handleQueryTimeoutException(QueryTimeoutException ex) {
    String errorId = UUID.randomUUID().toString().substring(0, 8);
    log.error("[{}] Query timeout: {}", errorId, ex.getMessage());
    ApiResponse<String> apiResponse =
        ApiResponse.<String>builder()
            .success(false)
            .data("The request took too long to process. Please try with a smaller date range or contact support. (Ref: " + errorId + ")")
            .build();
    return new ResponseEntity<>(apiResponse, HttpStatus.REQUEST_TIMEOUT);
  }

  @ExceptionHandler(DataAccessException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<ApiResponse<String>> handleDataAccessException(DataAccessException ex) {
    String errorId = UUID.randomUUID().toString().substring(0, 8);
    log.error("[{}] Database error: {}", errorId, ex.getMessage(), ex);
    
    String userMessage;
    if (ex.getMessage() != null && ex.getMessage().contains("canceling statement")) {
      userMessage = "The request was cancelled due to timeout. Please try with a smaller date range. (Ref: " + errorId + ")";
    } else {
      userMessage = "A database error occurred. Please try again later or contact support. (Ref: " + errorId + ")";
    }
    
    ApiResponse<String> apiResponse =
        ApiResponse.<String>builder()
            .success(false)
            .data(userMessage)
            .build();
    return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<ApiResponse<String>> handleGeneralException(Exception ex) {
    String errorId = UUID.randomUUID().toString().substring(0, 8);
    log.error("[{}] Unexpected error: {}", errorId, ex.getMessage(), ex);
    ApiResponse<String> apiResponse =
        ApiResponse.<String>builder()
            .success(false)
            .data("An unexpected error occurred. Please try again later or contact support. (Ref: " + errorId + ")")
            .build();
    return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
