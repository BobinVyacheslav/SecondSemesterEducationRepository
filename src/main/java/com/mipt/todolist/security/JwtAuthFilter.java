package com.mipt.todolist.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthFilter extends OncePerRequestFilter {
  private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

  private final JwtUtils jwtUtils;
  private final UserDetailsService userDetailsService;
  private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;

  public JwtAuthFilter(
      JwtUtils jwtUtils,
      UserDetailsService userDetailsService,
      RestAuthenticationEntryPoint restAuthenticationEntryPoint) {
    this.jwtUtils = jwtUtils;
    this.userDetailsService = userDetailsService;
    this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String authorizationHeader = request.getHeader("Authorization");

    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = authorizationHeader.substring(7);

    try {
      String username = jwtUtils.extractUsername(token);

      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!jwtUtils.isTokenValid(token, userDetails)) {
          throw new BadCredentialsException("Invalid JWT token");
        }

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }

      filterChain.doFilter(request, response);
    } catch (Exception exception) {
      SecurityContextHolder.clearContext();
      log.warn("JWT validation failed for token={}", maskToken(token));
      restAuthenticationEntryPoint.commence(
          request,
          response,
          new BadCredentialsException("Invalid JWT token", exception));
    }
  }

  private String maskToken(String token) {
    if (token == null || token.isBlank()) {
      return "null";
    }
    if (token.length() <= 12) {
      return "****";
    }
    return token.substring(0, 6) + "..." + token.substring(token.length() - 6);
  }
}
