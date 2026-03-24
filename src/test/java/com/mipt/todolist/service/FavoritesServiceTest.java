package com.mipt.todolist.service;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FavoritesServiceTest {

  private final FavoritesService favoritesService = new FavoritesService();

  @Test
  void getFavoriteTaskIdsShouldReturnEmptySetForNewSession() {
    MockHttpSession session = new MockHttpSession();

    Set<Long> ids = favoritesService.getFavoriteTaskIds(session);

    assertThat(ids).isEmpty();
    assertThat(session.getAttribute(FavoritesService.FAVORITE_TASK_IDS)).isNotNull();
  }

  @Test
  void addFavoriteShouldStoreTaskIdInSession() {
    MockHttpSession session = new MockHttpSession();

    favoritesService.addFavorite(7L, session);

    assertThat(favoritesService.getFavoriteTaskIds(session)).containsExactly(7L);
  }

  @Test
  void removeFavoriteShouldDeleteTaskIdFromSession() {
    MockHttpSession session = new MockHttpSession();
    favoritesService.addFavorite(7L, session);
    favoritesService.addFavorite(8L, session);

    favoritesService.removeFavorite(7L, session);

    assertThat(favoritesService.getFavoriteTaskIds(session)).containsExactly(8L);
  }

  @Test
  void addFavoriteShouldNotDuplicateSameTaskId() {
    MockHttpSession session = new MockHttpSession();

    favoritesService.addFavorite(5L, session);
    favoritesService.addFavorite(5L, session);

    assertThat(favoritesService.getFavoriteTaskIds(session)).containsExactly(5L);
  }
}
