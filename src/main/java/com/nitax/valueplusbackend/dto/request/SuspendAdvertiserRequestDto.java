package com.nitax.valueplusbackend.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class SuspendAdvertiserRequestDto {
  @NotEmpty private String suspensionReason;
}
