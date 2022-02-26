package me.jbduncan.creditsuisse.interviewchallenge;

import static com.google.common.truth.Truth.assertThat;
import static me.jbduncan.creditsuisse.interviewchallenge.Databases.eventMapper;
import static me.jbduncan.creditsuisse.interviewchallenge.Databases.jdbi;
import static me.jbduncan.creditsuisse.interviewchallenge.Databases.removeEventsDb;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;

class AppIntegrationTests {
  @Test
  void givenALogFile_whenTheAppParsesIt_thenTheLogsAreWrittenToAnHsqldbDatabase() throws Exception {
    removeEventsDb();
    StringWriter err = new StringWriter();

    ExitCode exitCode = App.execute(ImmutableList.of(logFile().toString()), err);

    assertThat(exitCode).isEqualTo(ExitCode.SUCCESS);
    assertThat(rows())
        .containsExactly(
            Event.builder()
                .id("a")
                .type("APPLICATION_LOG")
                .host("123")
                .duration(Duration.ofMillis(5))
                .alert(true)
                .build(),
            Event.builder() //
                .id("c")
                .duration(Duration.ofMillis(8))
                .alert(true)
                .build(),
            Event.builder() //
                .id("b")
                .duration(Duration.ofMillis(3))
                .alert(false)
                .build());
    assertThat(err.toString()).isEmpty();
  }

  private static Path logFile() throws URISyntaxException {
    return Paths.get(Resources.getResource("logFile.txt").toURI());
  }

  private List<Event> rows() {
    Jdbi jdbi = jdbi();
    return jdbi.inTransaction(
        handle ->
            handle
                .createQuery("select id, type, host, duration, alert from events")
                .map(eventMapper())
                .list());
  }
}
