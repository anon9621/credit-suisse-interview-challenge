package me.jbduncan.creditsuisse.interviewchallenge;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Paths;
import me.jbduncan.creditsuisse.interviewchallenge.AppException.UsageError;

public final class ReadCliArgsController {

  private final CopyEventsService copyEventsService;
  private final ImmutableList<String> args;
  private final PrintWriter err;

  public ReadCliArgsController(
      CopyEventsService copyEventsService, Iterable<String> args, Writer err) {
    this.copyEventsService =
        requireNonNull(copyEventsService, "copyEventsService must not be null");
    this.args = ImmutableList.copyOf(requireNonNull(args, "args must not be null"));
    this.err = new PrintWriter(requireNonNull(err, "err must not be null"), true);
  }

  ExitCode execute() {
    if (args.isEmpty()) {
      err.println("At least one argument was expected.");
      return ExitCode.USAGE_ERROR;
    }

    try {
      copyEventsService.execute(Paths.get(args.get(0)));
    } catch (AppException e) {
      if (e instanceof UsageError) {
        err.println(e.getMessage());
      } else {
        e.printStackTrace(err);
      }
      return e.exitCode();
    }

    return ExitCode.SUCCESS;
  }
}
