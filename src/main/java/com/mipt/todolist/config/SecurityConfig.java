package com.mipt.todolist.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mipt.todolist.dto.ErrorResponse;
import com.mipt.todolist.security.JwtUtils;
import com.mipt.todolist.security.JwtAuthFilter;
import com.mipt.todolist.security.PepperPasswordEncoder;
import com.mipt.todolist.security.RestAuthenticationEntryPoint;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.Instant;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      JwtAuthFilter jwtAuthFilter,
      RestAuthenticationEntryPoint restAuthenticationEntryPoint,
      ObjectMapper objectMapper) throws Exception {
    return http
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(exceptionHandling -> exceptionHandling
            .authenticationEntryPoint(restAuthenticationEntryPoint)
            .accessDeniedHandler((request, response, accessDeniedException) -> {
              ErrorResponse errorResponse = new ErrorResponse();
              errorResponse.setTimestamp(Instant.now());
              errorResponse.setStatus(HttpStatus.FORBIDDEN.value());
              errorResponse.setError(HttpStatus.FORBIDDEN.getReasonPhrase());
              errorResponse.setMessage("Forbidden");
              errorResponse.setPath(request.getRequestURI());
              errorResponse.setDetails(Map.of());

              response.setStatus(HttpServletResponse.SC_FORBIDDEN);
              response.setContentType(MediaType.APPLICATION_JSON_VALUE);
              objectMapper.writeValue(response.getOutputStream(), errorResponse);
            }))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/v1/auth/**").permitAll()
            .requestMatchers("/api/v1/profile").hasRole("USER")
            .requestMatchers("/api/v1/docs").hasAuthority("READ_PRIVILEGE")
            .requestMatchers("/api/v1/**").authenticated()
            .requestMatchers(
                "/external/**",
                "/actuator/health",
                "/actuator/metrics/**",
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/v3/api-docs/**")
            .permitAll()
            .anyRequest().permitAll())
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  @Bean
  public PasswordEncoder passwordEncoder(@Value("${security.pepper}") String pepper) {
    return new PepperPasswordEncoder(pepper, 12);
  }

  @Bean
  public JwtAuthFilter jwtAuthFilter(
      JwtUtils jwtUtils,
      UserDetailsService userDetailsService,
      RestAuthenticationEntryPoint restAuthenticationEntryPoint) {
    return new JwtAuthFilter(jwtUtils, userDetailsService, restAuthenticationEntryPoint);
  }

  @Bean
  public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
    UserDetails user = User.builder()
        .username("user")
        .password(passwordEncoder.encode("password"))
        .authorities("ROLE_USER")
        .build();

    UserDetails reader = User.builder()
        .username("reader")
        .password(passwordEncoder.encode("password"))
        .authorities("ROLE_USER", "READ_PRIVILEGE")
        .build();

    return new InMemoryUserDetailsManager(user, reader);
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
    return configuration.getAuthenticationManager();
  }
}
