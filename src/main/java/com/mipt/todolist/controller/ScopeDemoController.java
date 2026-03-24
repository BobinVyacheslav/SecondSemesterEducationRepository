package com.mipt.todolist.controller;

import com.mipt.todolist.model.PrototypeScopedBean;
import com.mipt.todolist.model.RequestScopedBean;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер для демонстрации различий между Prototype и Request Scopes.
 */
@RestController
@RequestMapping("/api/debug/scopes")
@Tag(name = "Debug", description = "Scope demonstration endpoint")
public class ScopeDemoController {

  /**
   * Используем ObjectProvider для Prototype бина, чтобы получать новый экземпляр при каждом вызове,
   * так как контроллер является Singleton.
   */
  private final ObjectProvider<PrototypeScopedBean> prototypeProvider;

  /**
   * RequestScopedBean внедряется как прокси и будет обновляться при каждом новом HTTP-запросе.
   */
  private final RequestScopedBean requestScopedBean;

  public ScopeDemoController(ObjectProvider<PrototypeScopedBean> prototypeProvider,
                             RequestScopedBean requestScopedBean) {
    this.prototypeProvider = prototypeProvider;
    this.requestScopedBean = requestScopedBean;
  }

  /**
   * Демонстрирует поведение бинов при вызове эндпоинта.
   * @return отчет о состоянии экземпляров бинов.
   */
  @GetMapping
  @Operation(summary = "Compare request and prototype scopes")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Scope data returned")
  })
  public ResponseEntity<Map<String, Object>> checkScopes() {
    Map<String, Object> report = new HashMap<>();

    PrototypeScopedBean p1 = prototypeProvider.getObject();
    PrototypeScopedBean p2 = prototypeProvider.getObject();

    String requestId = requestScopedBean.getRequestId();

    report.put("request_scope_id", requestId);
    report.put("prototype_instance_1_id", p1.generateId());
    report.put("prototype_instance_2_id", p2.generateId());
    report.put("message", "Сравните ID: Prototype всегда разные, Request одинаковый в рамках одного JSON, но разный между нажатиями F5");

    return ResponseEntity.ok(report);
  }
}
