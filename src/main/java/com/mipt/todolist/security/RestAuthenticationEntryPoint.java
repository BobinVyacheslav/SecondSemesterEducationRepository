package com.mipt.todolist.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mipt.todolist.dto.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
  private final ObjectMapper objectMapper;

  public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException) throws IOException, ServletException {
    ErrorResponse errorResponse = new ErrorResponse();
    errorResponse.setTimestamp(Instant.now());
    errorResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
    errorResponse.setError(HttpStatus.UNAUTHORIZED.getReasonPhrase());
    errorResponse.setMessage("Unauthorized");
    errorResponse.setPath(request.getRequestURI());
    errorResponse.setDetails(Map.of());

    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(response.getOutputStream(), errorResponse);
  }
}
