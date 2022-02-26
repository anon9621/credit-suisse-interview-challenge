package me.jbduncan.creditsuisse.interviewchallenge;

import com.google.common.collect.ImmutableList;
import com.squareup.moshi.Moshi;
import java.io.PrintWriter;
import java.io.Writer;
import org.jdbi.v3.core.Jdbi;

public final class App {
  public static void main(String[] args) {
    ExitCode exitCode = execute(ImmutableList.copyOf(args), new PrintWriter(System.err, true));
    System.exit(exitCode.value());
  }

  static ExitCode execute(ImmutableList<String> args, Writer err) {
    System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
    String eventsDbFile = "eventsdb";
    SaveToDatabaseRepository saveToDatabaseRepository =
        new DefaultSaveToDatabaseRepository(
            Jdbi.create("jdbc:hsqldb:file:" + eventsDbFile, "sa", ""));
    CopyEventsService copyEventsService =
        new DefaultCopyEventsService(
            saveToDatabaseRepository, eventsDbFile, new Moshi.Builder().build());
    return new ReadCliArgsController(copyEventsService, args, err).execute();
  }

  private App() {}
}
