package com.mipt.todolist.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mipt.todolist.dto.ExternalTaskCreateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.endsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExternalApiControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void createTaskShouldReturnCreatedAndLocation() throws Exception {
    mockMvc.perform(post("/external/v1/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new ExternalTaskCreateRequest("Read lecture", "HTTP"))))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", endsWith("/external/v1/tasks/1")))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.title").value("Read lecture"))
        .andExpect(jsonPath("$.completed").value(false));
  }

  @Test
  void missingTaskShouldReturnProblemDetail() throws Exception {
    mockMvc.perform(get("/external/v1/tasks/999999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Task 999999 not found"));
  }

  @Test
  void unstable429ShouldReturnRetryAfter() throws Exception {
    mockMvc.perform(get("/external/v1/unstable").param("mode", "429"))
        .andExpect(status().isTooManyRequests())
        .andExpect(header().string("Retry-After", "5"))
        .andExpect(jsonPath("$.title").value("Too Many Requests"));
  }
}
