package com.mipt.todolist.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Сервис для извлечения метаданных с защитой от отсутствующих ключей конфигурации.
 */
@Service
public class AppInfoService {

  @Value("${spring.application.name:Default-App-Name}")
  private String appName;

  @Value("${app.version:0.0.1}")
  private String appVersion;

  @Value("${app.description:No description provided}")
  private String appDescription;

  @Value("${app.tasks.default-status:UNKNOWN}")
  private String defaultStatus;

  @PostConstruct
  public void init() {
    System.out.println("=== Configuration Loaded Successfully ===");
    System.out.println("App Name: " + appName);
    System.out.println("Version:  " + appVersion);
    System.out.println("Description:  " + appDescription);
  }
}