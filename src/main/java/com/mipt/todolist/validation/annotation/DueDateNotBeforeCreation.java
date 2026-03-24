package com.mipt.todolist.validation.annotation;


import com.mipt.todolist.validation.validator.DueDateNotBeforeCreationValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DueDateNotBeforeCreationValidator.class)
public @interface DueDateNotBeforeCreation {
  String message() default "Дата окончания должна быть после даты начала.";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};

}
