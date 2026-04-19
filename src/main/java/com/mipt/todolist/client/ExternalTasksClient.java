package com.mipt.todolist.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mipt.todolist.dto.ExternalProblemDetails;
import com.mipt.todolist.dto.ExternalTaskCreateRequest;
import com.mipt.todolist.dto.ExternalTaskCreatedResult;
import com.mipt.todolist.dto.ExternalTaskResponse;
import com.mipt.todolist.exception.ExternalApiException;
import com.mipt.todolist.exception.TaskNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.UnknownContentTypeException;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.List;

@Component
public class ExternalTasksClient {
  private static final Logger log = LoggerFactory.getLogger(ExternalTasksClient.class);
  private static final int LOG_BODY_LIMIT = 300;

  private final RestClient restClient;
  private final ObjectMapper objectMapper;

  public ExternalTasksClient(RestClient externalApiRestClient, ObjectMapper objectMapper) {
    this.restClient = externalApiRestClient;
    this.objectMapper = objectMapper;
  }

  public ExternalTaskCreatedResult createTask(ExternalTaskCreateRequest request) {
    try {
      ResponseEntity<ExternalTaskResponse> entity = restClient.post()
          .uri("/tasks")
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
          .body(request)
          .retrieve()
          .toEntity(ExternalTaskResponse.class);
      return new ExternalTaskCreatedResult(entity.getBody(), entity.getHeaders().getLocation());
    } catch (RestClientResponseException exception) {
      throw mapResponseException(exception);
    } catch (UnknownContentTypeException exception) {
      throw mapUnknownContentType(exception);
    } catch (ResourceAccessException exception) {
      throw new ExternalApiException("External API is unavailable", exception);
    }
  }

  public ExternalTaskResponse getTask(Long id) {
    try {
      return restClient.get()
          .uri("/tasks/{id}", id)
          .accept(MediaType.APPLICATION_JSON)
          .retrieve()
          .body(ExternalTaskResponse.class);
    } catch (RestClientResponseException exception) {
      throw mapResponseException(exception);
    } catch (UnknownContentTypeException exception) {
      throw mapUnknownContentType(exception);
    } catch (ResourceAccessException exception) {
      throw new ExternalApiException("External API is unavailable", exception);
    }
  }

  public List<ExternalTaskResponse> getTasks(Boolean completed, Integer limit) {
    try {
      return restClient.get()
          .uri(uriBuilder -> {
            var builder = uriBuilder.path("/tasks");
            if (completed != null) {
              builder.queryParam("completed", completed);
            }
            if (limit != null) {
              builder.queryParam("limit", limit);
            }
            return builder.build();
          })
          .accept(MediaType.APPLICATION_JSON)
          .retrieve()
          .body(new ParameterizedTypeReference<List<ExternalTaskResponse>>() {
          });
    } catch (RestClientResponseException exception) {
      throw mapResponseException(exception);
    } catch (UnknownContentTypeException exception) {
      throw mapUnknownContentType(exception);
    } catch (ResourceAccessException exception) {
      throw new ExternalApiException("External API is unavailable", exception);
    }
  }

  public void deleteTask(Long id) {
    try {
      restClient.delete()
          .uri("/tasks/{id}", id)
          .accept(MediaType.APPLICATION_JSON)
          .retrieve()
          .toBodilessEntity();
    } catch (RestClientResponseException exception) {
      throw mapResponseException(exception);
    } catch (UnknownContentTypeException exception) {
      throw mapUnknownContentType(exception);
    } catch (ResourceAccessException exception) {
      throw new ExternalApiException("External API is unavailable", exception);
    }
  }

  private RuntimeException mapResponseException(RestClientResponseException exception) {
    int status = exception.getStatusCode().value();

    if (status == 404) {
      ExternalProblemDetails problemDetails = parseProblemDetails(exception.getResponseBodyAsByteArray());
      String message = problemDetails != null && problemDetails.detail() != null
          ? problemDetails.detail()
          : "Task not found";
      return new TaskNotFoundException(message);
    }

    if (status >= 500) {
      logUnexpectedBody(exception.getResponseBodyAsByteArray(), exception.getResponseHeaders().getContentType());
      return new ExternalApiException("External API server error: HTTP " + status, exception);
    }

    if (status == 429) {
      return new ExternalApiException("External API rate limit exceeded", exception);
    }

    return new ExternalApiException("External API request failed: HTTP " + status, exception);
  }

  private RuntimeException mapUnknownContentType(UnknownContentTypeException exception) {
    logUnexpectedBody(exception.getResponseBody(), exception.getContentType());
    return new ExternalApiException("Unexpected content type from external API: " + exception.getContentType(), exception);
  }

  private ExternalProblemDetails parseProblemDetails(byte[] body) {
    if (body == null || body.length == 0) {
      return null;
    }

    try {
      return objectMapper.readValue(body, ExternalProblemDetails.class);
    } catch (IOException exception) {
      return null;
    }
  }

  private void logUnexpectedBody(byte[] body, MediaType contentType) {
    String bodyPreview = truncateBody(body);
    log.warn("Unexpected response from external API: contentType={} body={}", contentType, bodyPreview);
  }

  private String truncateBody(byte[] body) {
    if (body == null || body.length == 0) {
      return "";
    }
    String text = new String(body, StandardCharsets.UTF_8);
    if (text.length() <= LOG_BODY_LIMIT) {
      return text;
    }
    return text.substring(0, LOG_BODY_LIMIT) + "...";
  }
}
