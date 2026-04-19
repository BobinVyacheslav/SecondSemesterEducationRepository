package com.mipt.todolist.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PepperPasswordEncoder implements PasswordEncoder {
  private final String pepper;
  private final BCryptPasswordEncoder delegate;

  public PepperPasswordEncoder(String pepper, int strength) {
    this.pepper = pepper;
    this.delegate = new BCryptPasswordEncoder(strength);
  }

  @Override
  public String encode(CharSequence rawPassword) {
    return delegate.encode(withPepper(rawPassword));
  }

  @Override
  public boolean matches(CharSequence rawPassword, String encodedPassword) {
    return delegate.matches(withPepper(rawPassword), encodedPassword);
  }

  @Override
  public boolean upgradeEncoding(String encodedPassword) {
    return delegate.upgradeEncoding(encodedPassword);
  }

  private String withPepper(CharSequence rawPassword) {
    return String.valueOf(rawPassword) + pepper;
  }
}
