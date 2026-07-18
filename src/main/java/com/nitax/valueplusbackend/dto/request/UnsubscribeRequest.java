package com.nitax.valueplusbackend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UnsubscribeRequest {
  @JsonProperty("msisdn")
  private String msisdn;

  @JsonProperty("unsubscribeDateTime")
  private String unsubscribeDateTime;

  @JsonProperty("clickId")
  private String clickId;

  private static final List<DateTimeFormatter> FORMATTERS = List.of(
      // ISO with optional fractional seconds and optional offset: 2026-05-28T07:58:35.080749479
      new DateTimeFormatterBuilder()
          .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
          .optionalStart().appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true).optionalEnd()
          .optionalStart().appendOffsetId().optionalEnd()
          .toFormatter(),
      // Space-separated with optional fractional seconds: 2026-05-28 07:58:35.123
      new DateTimeFormatterBuilder()
          .appendPattern("yyyy-MM-dd HH:mm:ss")
          .optionalStart().appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true).optionalEnd()
          .toFormatter(),
      // Plain date only: 2026-05-28
      DateTimeFormatter.ofPattern("yyyy-MM-dd")
  );

  public Instant getFormattedUnsubscribeDateTime() {
    if (unsubscribeDateTime == null || unsubscribeDateTime.isBlank()) {
      return Instant.now();
    }
    String value = unsubscribeDateTime.trim();

    // Try Instant.parse first for fully-qualified UTC strings (e.g. with Z or +00:00)
    try {
      return Instant.parse(value);
    } catch (Exception ignored) {}

    for (DateTimeFormatter formatter : FORMATTERS) {
      try {
        LocalDateTime ldt = LocalDateTime.parse(value, formatter);
        return ldt.toInstant(ZoneOffset.UTC);
      } catch (Exception ignored) {}
    }

    throw new IllegalArgumentException("Unable to parse unsubscribeDateTime: " + value);
  }
}
