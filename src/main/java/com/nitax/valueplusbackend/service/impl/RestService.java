package com.nitax.valueplusbackend.service.impl;

import com.nitax.valueplusbackend.exception.ExternalAPIException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class RestService {

  private final WebClient webClient;

  public RestService() {
    this.webClient = WebClient.create();
  }

  public boolean sendGetRequest(String path) {
    log.info("Sending postback to publisher at: {}", path);
    ResponseEntity<Void> response = webClient.get().uri(path).retrieve().toBodilessEntity().block();
    log.info("Publisher postback sent to: {}", path);
    return response != null && response.getStatusCode().is2xxSuccessful();
  }

  public <T> T sendPostRequest(
      String path, HttpEntity<?> requestEntity, Class<T> responseType, HttpHeaders headers)
      throws ExternalAPIException {
    log.info("Sending POST request to: {}", path);

    Mono<T> responseMono =
        webClient
            .post()
            .uri(path)
            .headers(httpHeaders -> httpHeaders.addAll(headers))
            .bodyValue(requestEntity.getBody())
            .retrieve()
            .onStatus(
                status -> status.is4xxClientError() || status.is5xxServerError(),
                response -> {
                  response
                      .bodyToMono(String.class)
                      .subscribe(body -> log.error("Error response body: {}", body));
                  return Mono.error(new ExternalAPIException("Error returned from external API"));
                })
            .bodyToMono(responseType);

    return responseMono.block();
  }
}
