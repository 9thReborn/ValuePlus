package com.nitax.valueplusbackend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiResponse<T> {

  private boolean success;
  private T data;

  public ApiResponse(boolean success, T data) {
    this.success = success;
    this.data = data;
  }


}
