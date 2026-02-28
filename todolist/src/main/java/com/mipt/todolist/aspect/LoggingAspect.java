package com.mipt.todolist.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Аспект для автоматического мониторинга выполнения методов в сервисном слое
 */
@Aspect
@Component
public class LoggingAspect {

  /**
   * Окружает выполнение методов в пакете service для фиксации входных данных и результат
   * @param joinPoint точка соприкосновения с исполняемым кодом
   * @return результат работы целевого метода
   * @throws Throwable ошибки, возникшие при выполнении метода
   */
  @Around("execution(* com.mipt.todolist.service.*.*(..))")
  public Object logServiceActivity(ProceedingJoinPoint joinPoint) throws Throwable {
    String methodName = joinPoint.getSignature().getName();
    Object[] args = joinPoint.getArgs();

    System.out.println("[AOP Start] Метод: " + methodName + " | Аргументы: " + Arrays.toString(args));

    Object result;
    try {
      result = joinPoint.proceed();
    } catch (Throwable throwable) {
      System.err.println("[AOP Error] В методе " + methodName + " произошел сбой: " + throwable.getMessage());
      throw throwable;
    }

    String output;
    if (result != null) {
      output = result.toString();
    } else {
      output = "void/null";
    }
    System.out.println("[AOP End] Метод: " + methodName + " | Результат: " + output);

    return result;
  }
}