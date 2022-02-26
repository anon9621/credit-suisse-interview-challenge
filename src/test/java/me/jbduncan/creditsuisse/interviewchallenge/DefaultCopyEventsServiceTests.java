package me.jbduncan.creditsuisse.interviewchallenge;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.common.io.MoreFiles;
import com.google.common.io.Resources;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.squareup.moshi.Moshi;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class DefaultCopyEventsServiceTests extends SkeletalTests {

  @Test
  void givenNullRepository_whenExecuting_thenThrowsNpe() {
    NullPointerException nullPointerException =
        assertThrows(
            NullPointerException.class,
            () -> new DefaultCopyEventsService(null, "some-file", new Moshi.Builder().build()));
    assertThat(nullPointerException).hasMessageThat().contains("saveToDatabaseRepository");
  }

  @Test
  void givenNullEventsDbFile_whenExecuting_thenThrowsNpe() {
    NullPointerException nullPointerException =
        assertThrows(
            NullPointerException.class,
            () ->
                new DefaultCopyEventsService(
                    mock(SaveToDatabaseRepository.class), null, new Moshi.Builder().build()));
    assertThat(nullPointerException).hasMessageThat().contains("eventsDbFile");
  }

  @Test
  void givenNullMoshi_whenExecuting_thenThrowsNpe() {
    NullPointerException nullPointerException =
        assertThrows(
            NullPointerException.class,
            () ->
                new DefaultCopyEventsService(
                    mock(SaveToDatabaseRepository.class), "some-file", null));
    assertThat(nullPointerException).hasMessageThat().contains("moshi");
  }

  @Test
  void givenNullLogFile_whenExecuting_thenThrowsNpe() {
    DefaultCopyEventsService copyEventsService =
        new DefaultCopyEventsService(
            mock(SaveToDatabaseRepository.class), "some-file", new Moshi.Builder().build());
    NullPointerException nullPointerException =
        assertThrows(NullPointerException.class, () -> copyEventsService.execute(null));
    assertThat(nullPointerException).hasMessageThat().contains("logFile");
  }

  @Test
  void givenLogFileWithOneEvent_whenExecuting_thenEventIsPassedToRepository() throws Exception {
    Path logFile = logFileWithSingleEvent();
    SaveToDatabaseRepository saveToDatabaseRepository = mock(SaveToDatabaseRepository.class);
    String eventsDbFile = "some-file";
    Moshi moshi = new Moshi.Builder().build();

    new DefaultCopyEventsService(saveToDatabaseRepository, eventsDbFile, moshi).execute(logFile);

    verify(saveToDatabaseRepository)
        .save(Event.builder().id("a").duration(Duration.ofMillis(3)).alert(false).build());
  }

  @Test
  void givenLogFileWithOneReversedEvent_whenExecuting_thenEventIsPassedToRepository()
      throws Exception {
    Path logFile = logFileWithSingleReversedEvent();
    SaveToDatabaseRepository saveToDatabaseRepository = mock(SaveToDatabaseRepository.class);
    String eventsDbFile = "some-file";
    Moshi moshi = new Moshi.Builder().build();

    new DefaultCopyEventsService(saveToDatabaseRepository, eventsDbFile, moshi).execute(logFile);

    verify(saveToDatabaseRepository)
        .save(Event.builder().id("a").duration(Duration.ofMillis(3)).alert(false).build());
  }

  @Test
  void givenLogFileWithOneLargeEvent_whenExecuting_thenEventIsPassedToRepository()
      throws Exception {
    Path logFile = logFileWithSingleLargeEvent();
    SaveToDatabaseRepository saveToDatabaseRepository = mock(SaveToDatabaseRepository.class);
    String eventsDbFile = "some-file";
    Moshi moshi = new Moshi.Builder().build();

    new DefaultCopyEventsService(saveToDatabaseRepository, eventsDbFile, moshi).execute(logFile);

    verify(saveToDatabaseRepository)
        .save(Event.builder().id("a").duration(Duration.ofMillis(5)).alert(true).build());
  }

  @Test
  void givenLogFileWithEventWithTypeAndHost_whenExecuting_thenEventIsPassedToRepository()
      throws Exception {
    Path logFile = logFileWithSingleEventWithTypeAndHost();
    SaveToDatabaseRepository saveToDatabaseRepository = mock(SaveToDatabaseRepository.class);
    String eventsDbFile = "some-file";
    Moshi moshi = new Moshi.Builder().build();

    new DefaultCopyEventsService(saveToDatabaseRepository, eventsDbFile, moshi).execute(logFile);

    verify(saveToDatabaseRepository)
        .save(
            Event.builder()
                .id("a")
                .type("APPLICATION_LOG")
                .host("123")
                .duration(Duration.ofMillis(3))
                .alert(false)
                .build());
  }

  @Test
  void givenLogFileWithTwoIntertwiningEvents_whenExecuting_thenEventsArePassedToRepository()
      throws Exception {
    Path logFile = logFileWithTwoIntertwiningEvents();
    SaveToDatabaseRepository saveToDatabaseRepository = mock(SaveToDatabaseRepository.class);
    String eventsDbFile = "some-file";
    Moshi moshi = new Moshi.Builder().build();

    new DefaultCopyEventsService(saveToDatabaseRepository, eventsDbFile, moshi).execute(logFile);

    verify(saveToDatabaseRepository)
        .save(Event.builder().id("a").duration(Duration.ofMillis(2)).alert(false).build());
    verify(saveToDatabaseRepository)
        .save(Event.builder().id("b").duration(Duration.ofMillis(3)).alert(false).build());
  }

  @Test
  void givenLogFilePathThatPointsToNoFile_whenExecuting_thenUsageErrorAppExceptionIsThrown() {
    Path logFile = Paths.get("nonsensical", "path");
    SaveToDatabaseRepository saveToDatabaseRepository = mock(SaveToDatabaseRepository.class);
    String eventsDbFile = "some-file";
    Moshi moshi = new Moshi.Builder().build();

    AppException exception =
        assertThrows(
            AppException.class,
            () ->
                new DefaultCopyEventsService(saveToDatabaseRepository, eventsDbFile, moshi)
                    .execute(logFile));
    assertThat(exception)
        .hasMessageThat()
        .isEqualTo(
            String.format(
                "Cannot open '%s'. Try to make it readable with 'chmod u+r %s'.",
                logFile, logFile));
    assertThat(exception.exitCode()).isEqualTo(ExitCode.USAGE_ERROR);
  }

  @Test
  void givenLogFileThatThrowsIoeOnRead_whenExecuting_thenItIsWrappedInInternalErrorAppException()
      throws Exception {
    // TODO: Is there a better way to do this? For example, injecting a custom FileSystem into
    //       the DefaultCopyEventsService?
    try (MockedStatic<Files> mockedStatic = Mockito.mockStatic(Files.class);
        FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
      Path logFile = fileSystem.getPath("some", "path");
      MoreFiles.touch(logFile);
      mockedStatic
          .when(() -> Files.lines(logFile))
          .thenThrow(new IOException("some-exception-message"));
      SaveToDatabaseRepository saveToDatabaseRepository = mock(SaveToDatabaseRepository.class);
      Moshi moshi = new Moshi.Builder().build();
      String eventsDbFile = "some-file";

      AppException exception =
          assertThrows(
              AppException.class,
              () ->
                  new DefaultCopyEventsService(saveToDatabaseRepository, eventsDbFile, moshi)
                      .execute(logFile));

      assertThat(exception).hasMessageThat().isEqualTo(String.format("Cannot read '%s'", logFile));
      assertThat(exception).hasCauseThat().isInstanceOf(IOException.class);
      assertThat(exception).hasCauseThat().hasMessageThat().isEqualTo("some-exception-message");
      assertThat(exception.exitCode()).isEqualTo(ExitCode.INTERNAL_ERROR);
    }
  }

  @Test
  void
      givenLogFileThatThrowsUioeOnRead_whenExecuting_thenCauseIsWrappedInInternalErrorAppException()
          throws Exception {
    // TODO: Is there a better way to do this? For example, injecting a custom FileSystem into
    //       the DefaultCopyEventsService?
    try (MockedStatic<Files> mockedStatic = Mockito.mockStatic(Files.class);
        FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
      Path logFile = fileSystem.getPath("some", "path");
      MoreFiles.touch(logFile);
      mockedStatic
          .when(() -> Files.lines(logFile))
          .thenThrow(new UncheckedIOException(new IOException("some-exception-message")));
      SaveToDatabaseRepository saveToDatabaseRepository = mock(SaveToDatabaseRepository.class);
      Moshi moshi = new Moshi.Builder().build();
      String eventsDbFile = "some-file";

      AppException exception =
          assertThrows(
              AppException.class,
              () ->
                  new DefaultCopyEventsService(saveToDatabaseRepository, eventsDbFile, moshi)
                      .execute(logFile));

      assertThat(exception).hasMessageThat().isEqualTo(String.format("Cannot read '%s'", logFile));
      assertThat(exception).hasCauseThat().isInstanceOf(IOException.class);
      assertThat(exception).hasCauseThat().hasMessageThat().isEqualTo("some-exception-message");
      assertThat(exception.exitCode()).isEqualTo(ExitCode.INTERNAL_ERROR);
    }
  }

  private Path logFileWithSingleEvent() throws URISyntaxException {
    return Paths.get(Resources.getResource("singleEventLogFile.txt").toURI());
  }

  private Path logFileWithSingleReversedEvent() throws URISyntaxException {
    return Paths.get(Resources.getResource("singleReversedEventLogFile.txt").toURI());
  }

  private Path logFileWithSingleLargeEvent() throws URISyntaxException {
    return Paths.get(Resources.getResource("singleLargeEventLogFile.txt").toURI());
  }

  private Path logFileWithSingleEventWithTypeAndHost() throws URISyntaxException {
    return Paths.get(Resources.getResource("singleEventWithTypeAndHostLogFile.txt").toURI());
  }

  private Path logFileWithTwoIntertwiningEvents() throws URISyntaxException {
    return Paths.get(Resources.getResource("twoIntertwiningEventsLogFile.txt").toURI());
  }
}
