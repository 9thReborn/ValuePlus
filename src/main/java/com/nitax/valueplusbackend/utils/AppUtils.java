package com.nitax.valueplusbackend.utils;

import com.nitax.valueplusbackend.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import java.security.SecureRandom;
import java.time.*;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppUtils {

  private final HttpServletRequest request;
  private static final String ALPHABET =
      "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
  private static final SecureRandom RANDOM = new SecureRandom();

  private final PublisherRepository publisherRepository;
  private final AdvertiserRepository advertiserRepository;
  private final ProductRepository productRepository;
  private final CampaignRepository campaignRepository;
  private final CpaSettingsRepository cpaSettingsRepository;
  private final PublisherCampaignRepository pubSubRepository;
  private final BulkSmsCampaignRepository bulkSmsCampaignRepository;
  private final TransactionRepository transactionRepository;
  private final NotificationRepository notificationRepository;

  public String generatePublisherPostbackUrl(String publisherId) {
    String scheme = request.getScheme();
    String serverName = request.getServerName();
    int serverPort = request.getServerPort();
    String contextPath = request.getContextPath();

    String baseUrl = scheme + "://" + serverName;

    return baseUrl + contextPath + "/pubs/" + publisherId + "/?campId={campaign_id}/trxId={trx_id}";
  }

  public String generatePubId() {
    StringBuilder sb = new StringBuilder(10);
    for (int i = 0; i < 10; i++) {
      int index = RANDOM.nextInt(ALPHABET.length());
      sb.append(ALPHABET.charAt(index));
    }
    boolean canUsePubId = false;
    while (!canUsePubId) {
      if (!publisherRepository.existsByPubId(sb.toString())) {
        canUsePubId = true;
      } else {
        sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
          int index = RANDOM.nextInt(ALPHABET.length());
          sb.append(ALPHABET.charAt(index));
        }
      }
    }
    return sb.toString();
  }

  public String generateCPAId() {
    StringBuilder sb = new StringBuilder(10);
    for (int i = 0; i < 10; i++) {
      int index = RANDOM.nextInt(ALPHABET.length());
      sb.append(ALPHABET.charAt(index));
    }
    boolean canUsePubId = false;
    while (!canUsePubId) {
      if (!cpaSettingsRepository.existsByCpaId(sb.toString())) {
        canUsePubId = true;
      } else {
        sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
          int index = RANDOM.nextInt(ALPHABET.length());
          sb.append(ALPHABET.charAt(index));
        }
      }
    }
    return sb.toString();
  }

  public String generatePubSubId() {
    StringBuilder sb = new StringBuilder(10);
    for (int i = 0; i < 10; i++) {
      int index = RANDOM.nextInt(ALPHABET.length());
      sb.append(ALPHABET.charAt(index));
    }
    boolean canUsePubId = false;
    while (!canUsePubId) {
      if (!pubSubRepository.existsByPubCampId(sb.toString())) {
        canUsePubId = true;
      } else {
        sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
          int index = RANDOM.nextInt(ALPHABET.length());
          sb.append(ALPHABET.charAt(index));
        }
      }
    }
    return sb.toString();
  }

  public String generateAdvId() {
    StringBuilder sb = new StringBuilder(10);
    for (int i = 0; i < 10; i++) {
      int index = RANDOM.nextInt(ALPHABET.length());
      sb.append(ALPHABET.charAt(index));
    }
    boolean canUseProdId = false;
    while (!canUseProdId) {
      if (!productRepository.existsByProdId(sb.toString())) {
        canUseProdId = true;
      } else {
        sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
          int index = RANDOM.nextInt(ALPHABET.length());
          sb.append(ALPHABET.charAt(index));
        }
      }
    }
    return sb.toString();
  }

  public String generateAdvertiserPostbackUrl(String advertiserId) {
    String scheme = request.getScheme();
    String serverName = request.getServerName();
    int serverPort = request.getServerPort();
    String contextPath = request.getContextPath();

    // Construct the base URL without the request path
    String baseUrl = scheme + "://" + serverName;

    return baseUrl
        + contextPath
        + "/advertisers/"
        + advertiserId
        + "/?campaignId={campaign_id}/trxId={trx_id}";
  }

  public String generateProdId() {
    StringBuilder sb = new StringBuilder(10);
    for (int i = 0; i < 10; i++) {
      int index = RANDOM.nextInt(ALPHABET.length());
      sb.append(ALPHABET.charAt(index));
    }
    boolean canUseAdvId = false;
    while (!canUseAdvId) {
      if (!advertiserRepository.existsByAdvertiserId(sb.toString())) {
        canUseAdvId = true;
      } else {
        sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
          int index = RANDOM.nextInt(ALPHABET.length());
          sb.append(ALPHABET.charAt(index));
        }
      }
    }
    return sb.toString();
  }

  public String generateCampaignId() {
    StringBuilder sb = new StringBuilder(10);
    for (int i = 0; i < 10; i++) {
      int index = RANDOM.nextInt(ALPHABET.length());
      sb.append(ALPHABET.charAt(index));
    }
    boolean canUseCampaignId = false;
    while (!canUseCampaignId) {
      if (!campaignRepository.existsByCampaignId(sb.toString())) {
        canUseCampaignId = true;
      } else {
        sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
          int index = RANDOM.nextInt(ALPHABET.length());
          sb.append(ALPHABET.charAt(index));
        }
      }
    }
    return sb.toString();
  }

  public String generateBulkSmsCampaignId() {
    StringBuilder sb = new StringBuilder(10);
    for (int i = 0; i < 10; i++) {
      int index = RANDOM.nextInt(ALPHABET.length());
      sb.append(ALPHABET.charAt(index));
    }
    boolean canUseCampaignId = false;
    while (!canUseCampaignId) {
      if (!bulkSmsCampaignRepository.existsByBulkSmsCampaignId(sb.toString())) {
        canUseCampaignId = true;
      } else {
        sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
          int index = RANDOM.nextInt(ALPHABET.length());
          sb.append(ALPHABET.charAt(index));
        }
      }
    }
    return sb.toString();
  }

  public String generateShortTrxId() {
    while (true) {
      StringBuilder sb = new StringBuilder(12);
      for (int i = 0; i < 12; i++) {
        sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
      }
      String candidate = sb.toString();
      if (!notificationRepository.existsByShortTrxId(candidate)) {
        return candidate;
      }
    }
  }

  public String generateTransactionId() {
    StringBuilder sb = new StringBuilder(10);
    for (int i = 0; i < 10; i++) {
      int index = RANDOM.nextInt(ALPHABET.length());
      sb.append(ALPHABET.charAt(index));
    }
    boolean canUseCampaignId = false;
    while (!canUseCampaignId) {
      if (!transactionRepository.existsByTransactionId(sb.toString())) {
        canUseCampaignId = true;
      } else {
        sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
          int index = RANDOM.nextInt(ALPHABET.length());
          sb.append(ALPHABET.charAt(index));
        }
      }
    }
    return sb.toString();
  }
  public static Instant localDateToInstant(LocalDate localDate) {
    return Objects.nonNull(localDate)
        ? localDate.atStartOfDay().atZone(ZoneId.of("UTC")).toInstant()
        : null;
  }
  public static Instant localDateTimeToInstantUTC(LocalDateTime localDateTime) {
    return localDateTime.toInstant(ZoneOffset.UTC);
  }

  public static LocalDate InstantToLocalDate(Instant instant) {
    return Objects.nonNull(instant)
            ? LocalDateTime.ofInstant(instant, ZoneId.of("UTC")).toLocalDate()
            : null;
  }
}
