package com.mipt.todolist.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.slf4j.MDC;

import static com.mipt.todolist.logging.TraceIdFilter.TRACE_HEADER;
import static com.mipt.todolist.logging.TraceIdFilter.TRACE_ID_KEY;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class RestClientConfig {

  @Bean
  public RestClient externalApiRestClient(
      RestClient.Builder builder,
      @Value("${external.api.base-url}") String baseUrl,
      @Value("${external.api.connect-timeout}") Duration connectTimeout,
      @Value("${external.api.read-timeout}") Duration readTimeout) {
    HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(connectTimeout)
        .build();

    JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
    requestFactory.setReadTimeout(readTimeout);

    return builder
        .baseUrl(baseUrl)
        .defaultHeader(HttpHeaders.USER_AGENT, "study-http-gateway/1.0")
        .requestInterceptor((request, body, execution) -> {
          String traceId = MDC.get(TRACE_ID_KEY);
          if (traceId != null && !traceId.isBlank()) {
            request.getHeaders().set(TRACE_HEADER, traceId);
          }
          return execution.execute(request, body);
        })
        .requestFactory(requestFactory)
        .build();
  }
}
