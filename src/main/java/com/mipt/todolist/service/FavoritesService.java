package com.mipt.todolist.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class FavoritesService {
  public static final String FAVORITE_TASK_IDS = "favoriteTaskIds";

  public Set<Long> getFavoriteTaskIds(HttpSession session) {
    Object attribute = session.getAttribute(FAVORITE_TASK_IDS);
    if (attribute instanceof Set<?> ids) {
      LinkedHashSet<Long> result = new LinkedHashSet<>();
      for (Object id : ids) {
        result.add(Long.valueOf(id.toString()));
      }
      return result;
    }
    LinkedHashSet<Long> ids = new LinkedHashSet<>();
    session.setAttribute(FAVORITE_TASK_IDS, ids);
    return ids;
  }

  public void addFavorite(Long taskId, HttpSession session) {
    Set<Long> ids = getFavoriteTaskIds(session);
    ids.add(taskId);
    session.setAttribute(FAVORITE_TASK_IDS, ids);
  }

  public void removeFavorite(Long taskId, HttpSession session) {
    Set<Long> ids = getFavoriteTaskIds(session);
    ids.remove(taskId);
    session.setAttribute(FAVORITE_TASK_IDS, ids);
  }
}
