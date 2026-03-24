package com.mipt.todolist.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PreferencesController.class)
@Import({ValidationExceptionHandler.class, com.mipt.todolist.config.ApiVersionFilter.class})
class PreferencesControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void getViewPreferenceShouldReturnDefaultMode() throws Exception {
    mockMvc.perform(get("/api/preferences/view"))
        .andExpect(status().isOk())
        .andExpect(cookie().value("viewPreference", "compact"))
        .andExpect(jsonPath("$.mode").value("compact"));
  }

  @Test
  void updateViewPreferenceShouldReturnUpdatedMode() throws Exception {
    mockMvc.perform(post("/api/preferences/view").param("mode", "detailed"))
        .andExpect(status().isOk())
        .andExpect(cookie().value("viewPreference", "detailed"))
        .andExpect(jsonPath("$.mode").value("detailed"));
  }

  @Test
  void updateViewPreferenceShouldReturnBadRequestForInvalidMode() throws Exception {
    mockMvc.perform(post("/api/preferences/view").param("mode", "широкий"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Параметр mode должен быть compact или detailed"));
  }

  @Test
  void updateViewPreferenceShouldReturnBadRequestWhenModeParameterMissing() throws Exception {
    mockMvc.perform(post("/api/preferences/view"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.details.parameter").value("mode"));
  }
}
