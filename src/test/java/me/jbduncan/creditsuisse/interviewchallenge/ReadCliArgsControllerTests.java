package me.jbduncan.creditsuisse.interviewchallenge;

import static com.google.common.truth.Truth.assertThat;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.common.base.Throwables;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

class ReadCliArgsControllerTests extends SkeletalTests {

  @Test
  void givenNullService_whenInstantiating_thenThrowsNpe() {
    NullPointerException nullPointerException =
        assertThrows(
            NullPointerException.class,
            () -> new ReadCliArgsController(null, singletonList("some-arg"), new StringWriter()));
    assertThat(nullPointerException).hasMessageThat().contains("copyEventsService");
  }

  @Test
  void givenNullArgs_whenInstantiating_thenThrowsNpe() {
    NullPointerException nullPointerException =
        assertThrows(
            NullPointerException.class,
            () ->
                new ReadCliArgsController(mock(CopyEventsService.class), null, new StringWriter()));
    assertThat(nullPointerException).hasMessageThat().contains("args");
  }

  @Test
  void givenArgsWithANullElement_whenInstantiating_thenThrowsNpe() {
    assertThrows(
        NullPointerException.class,
        () ->
            new ReadCliArgsController(
                mock(CopyEventsService.class), singletonList(null), new StringWriter()));
  }

  @Test
  void givenNullErrWriter_whenInstantiating_thenThrowsNpe() {
    NullPointerException nullPointerException =
        assertThrows(
            NullPointerException.class,
            () ->
                new ReadCliArgsController(
                    mock(CopyEventsService.class),
                    singletonList(Paths.get("some", "path").toString()),
                    null));
    assertThat(nullPointerException).hasMessageThat().contains("err");
  }

  @Test
  void givenArgsWithPathToLogFile_whenExecuting_thenPassesPathToService() throws Exception {
    CopyEventsService copyEventsService = mock(CopyEventsService.class);
    Path path = Paths.get("some", "path");
    StringWriter err = new StringWriter();
    ReadCliArgsController readCliArgsController =
        new ReadCliArgsController(copyEventsService, singletonList(path.toString()), err);

    readCliArgsController.execute();

    verify(copyEventsService).execute(path);
  }

  @Test
  void givenArgsWithPathToLogFile_whenExecuting_thenReturns0() throws Exception {
    CopyEventsService copyEventsService = mock(CopyEventsService.class);
    Path path = Paths.get("some", "path");
    StringWriter err = new StringWriter();
    ReadCliArgsController readCliArgsController =
        new ReadCliArgsController(copyEventsService, singletonList(path.toString()), err);

    ExitCode exitCode = readCliArgsController.execute();

    assertThat(exitCode).isEqualTo(ExitCode.SUCCESS);
  }

  @Test
  void givenArgsWithPathToLogFile_whenExecuting_thenPrintsNothingToErr() throws Exception {
    CopyEventsService copyEventsService = mock(CopyEventsService.class);
    Path path = Paths.get("some", "path");
    StringWriter err = new StringWriter();
    ReadCliArgsController readCliArgsController =
        new ReadCliArgsController(copyEventsService, singletonList(path.toString()), err);

    readCliArgsController.execute();

    assertThat(err.toString().trim()).isEmpty();
  }

  @Test
  void givenEmptyArgs_whenExecuting_thenReturnsUsageErrorExitCode() {
    CopyEventsService copyEventsService = mock(CopyEventsService.class);
    StringWriter err = new StringWriter();
    ReadCliArgsController readCliArgsController =
        new ReadCliArgsController(copyEventsService, emptyList(), err);

    ExitCode exitCode = readCliArgsController.execute();

    assertThat(exitCode).isEqualTo(ExitCode.USAGE_ERROR);
  }

  @Test
  void givenEmptyArgs_whenExecuting_thenPrintsUsageErrorMessage() {
    CopyEventsService copyEventsService = mock(CopyEventsService.class);
    StringWriter err = new StringWriter();
    ReadCliArgsController readCliArgsController =
        new ReadCliArgsController(copyEventsService, emptyList(), err);

    readCliArgsController.execute();

    assertThat(err.toString()).isEqualTo("At least one argument was expected.\n");
  }

  @Test
  void givenIsExecuting_whenServiceThrowsAppException_thenReturnsUsageErrorExitCode()
      throws Exception {
    CopyEventsService copyEventsService = mock(CopyEventsService.class);
    doThrow(AppException.usageError("some-usage-error")).when(copyEventsService).execute(any());
    StringWriter err = new StringWriter();
    ReadCliArgsController readCliArgsController =
        new ReadCliArgsController(
            copyEventsService, singletonList(Paths.get("some", "path").toString()), err);

    ExitCode exitCode = readCliArgsController.execute();

    assertThat(exitCode).isEqualTo(ExitCode.USAGE_ERROR);
  }

  @Test
  void givenIsExecuting_whenServiceThrowsUsageErrorAppException_thenPrintsExceptionMessage()
      throws Exception {
    CopyEventsService copyEventsService = mock(CopyEventsService.class);
    doThrow(AppException.usageError("some-usage-error")).when(copyEventsService).execute(any());
    StringWriter err = new StringWriter();
    ReadCliArgsController readCliArgsController =
        new ReadCliArgsController(
            copyEventsService, singletonList(Paths.get("some", "path").toString()), err);

    readCliArgsController.execute();

    assertThat(err.toString()).isEqualTo("some-usage-error\n");
  }

  @Test
  void givenIsExecuting_whenServiceThrowsInternalErrorAppException_thenPrintsExceptionStackTrace()
      throws Exception {
    CopyEventsService copyEventsService = mock(CopyEventsService.class);
    AppException appException =
        AppException.internalError(
            "some-internal-error", new IOException("some-internal-error-cause"));
    doThrow(appException).when(copyEventsService).execute(any());
    StringWriter err = new StringWriter();
    ReadCliArgsController readCliArgsController =
        new ReadCliArgsController(
            copyEventsService, singletonList(Paths.get("some", "path").toString()), err);

    readCliArgsController.execute();

    assertThat(err.toString()).isEqualTo(Throwables.getStackTraceAsString(appException));
  }
}
