package com.mipt.todolist.config;

import com.mipt.todolist.repository.TaskRepository;
import com.mipt.todolist.service.TaskService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * Компонент для перехвата и логирования этапов инициализации бинов управления задачами
 */
@Component
public class TaskLifecycleProcessor implements BeanPostProcessor {
  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
    if (bean instanceof TaskService || bean instanceof TaskRepository) {
      System.out.println("[LOG] Пред-инициализация бина: " + beanName);
    }
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    if (bean instanceof TaskService || bean instanceof TaskRepository) {
      System.out.println("[LOG] Пост-инициализация бина: " + beanName);
    }
    return bean;
  }
}
