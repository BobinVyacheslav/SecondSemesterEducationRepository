package com.mipt.todolist.api;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class InternalSecurityDemoController {

  @GetMapping("/profile")
  public ProfileResponse profile(Authentication authentication) {
    List<String> authorities = authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .toList();
    return new ProfileResponse(authentication.getName(), authorities);
  }

  @GetMapping("/docs")
  public DocsResponse docs() {
    return new DocsResponse("Documentation access granted");
  }

  public record ProfileResponse(String username, List<String> authorities) {
  }

  public record DocsResponse(String message) {
  }
}
