package me.jbduncan.creditsuisse.interviewchallenge;

import static com.google.common.truth.Truth.assertThat;
import static me.jbduncan.creditsuisse.interviewchallenge.Databases.createEventsTable;
import static me.jbduncan.creditsuisse.interviewchallenge.Databases.eventMapper;
import static me.jbduncan.creditsuisse.interviewchallenge.Databases.jdbi;
import static me.jbduncan.creditsuisse.interviewchallenge.Databases.removeEventsDb;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.time.Duration;
import java.util.List;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultSaveToDatabaseRepositoryTests extends SkeletalTests {
  @BeforeEach
  void beforeEach() throws Exception {
    removeEventsDb();
  }

  @Test
  void givenEvent_whenSaving_thenEventIsSavedInDatabase() throws Exception {
    createEventsTable();
    Jdbi jdbi = jdbi();
    Event event = Event.builder().id("a").duration(Duration.ofMillis(1)).alert(false).build();

    new DefaultSaveToDatabaseRepository(jdbi).save(event);

    assertThatEventsTableContains(event);
  }

  @Test
  void givenEvent_whenSavingAndDatabaseDoesntExist_thenDatabaseAndEventsTableAreCreated()
      throws Exception {
    Jdbi jdbi = jdbi();
    Event event = Event.builder().id("a").duration(Duration.ofMillis(1)).alert(false).build();

    new DefaultSaveToDatabaseRepository(jdbi).save(event);

    assertThatEventsTableContains(event);
  }

  @Test
  void givenEventWithTypeAndHost_whenSaving_thenEventIsSavedInDatabase() throws Exception {
    createEventsTable();
    Jdbi jdbi = jdbi();
    Event event =
        Event.builder()
            .id("a")
            .type("b")
            .host("c")
            .duration(Duration.ofMillis(1))
            .alert(false)
            .build();

    new DefaultSaveToDatabaseRepository(jdbi).save(event);

    assertThatEventsTableContains(event);
  }

  @Test
  void givenNullJdbi_whenInstantiating_thenThrowsNpe() {
    NullPointerException nullPointerException =
        assertThrows(NullPointerException.class, () -> new DefaultSaveToDatabaseRepository(null));
    assertThat(nullPointerException).hasMessageThat().contains("jdbi");
  }

  @Test
  void givenNullEvent_whenSaving_thenThrowsNpe() {
    DefaultSaveToDatabaseRepository saveToDatabaseRepository =
        new DefaultSaveToDatabaseRepository(jdbi());

    NullPointerException nullPointerException =
        assertThrows(NullPointerException.class, () -> saveToDatabaseRepository.save(null));
    assertThat(nullPointerException).hasMessageThat().contains("event");
  }

  @Test
  void givenJdbiThatThrows_whenSaving_thenWrapsExceptionInInternalErrorAppException() {
    Jdbi jdbi = mock(Jdbi.class);
    doThrow(new Exception("some-message")).when(jdbi).useTransaction(any());
    Event event = Event.builder().id("a").duration(Duration.ofMillis(1)).alert(false).build();
    DefaultSaveToDatabaseRepository saveToDatabaseRepository =
        new DefaultSaveToDatabaseRepository(jdbi);

    AppException exception =
        assertThrows(AppException.class, () -> saveToDatabaseRepository.save(event));
    assertThat(exception).hasMessageThat().isEqualTo(String.format("Cannot save event: %s", event));
    assertThat(exception).hasCauseThat().hasMessageThat().isEqualTo("some-message");
  }

  private void assertThatEventsTableContains(Event event) {
    Jdbi jdbi = jdbi();
    List<Event> events =
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery("select id, type, host, duration, alert from events")
                    .map(eventMapper())
                    .list());
    assertThat(events).containsExactly(event);
  }
}
