package com.mipt.todolist.model;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

/**
 * Бин с областью видимости request.
 * Создается заново для каждого входящего HTTP-запроса.
 */
@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RequestScopedBean {
  private final String requestId;
  private final long creationTime;

  /**
   * Инициализирует идентификатор запроса и время его начала.
   */
  public RequestScopedBean() {
    this.requestId = UUID.randomUUID().toString();
    this.creationTime = System.currentTimeMillis();
  }

  public String getRequestId() { return requestId; }
  public long getCreationTime() { return creationTime; }
}