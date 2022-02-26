package me.jbduncan.creditsuisse.interviewchallenge;

import static java.util.Objects.requireNonNull;

import com.google.common.flogger.FluentLogger;
import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.Moshi;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public final class DefaultCopyEventsService implements CopyEventsService {

  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private final SaveToDatabaseRepository saveToDatabaseRepository;
  private final String eventsDbFile;
  private final Moshi moshi;

  public DefaultCopyEventsService(
      SaveToDatabaseRepository saveToDatabaseRepository, String eventsDbFile, Moshi moshi) {
    this.saveToDatabaseRepository =
        requireNonNull(saveToDatabaseRepository, "saveToDatabaseRepository must not be null");
    this.eventsDbFile = requireNonNull(eventsDbFile, "eventsDbFile must not be null");
    this.moshi = requireNonNull(moshi, "moshi must not be null");
  }

  @Override
  public void execute(Path logFile) throws AppException {
    requireNonNull(logFile, "logFile must be non null");

    if (Files.notExists(logFile, LinkOption.NOFOLLOW_LINKS)) {
      throw AppException.usageError(
          String.format(
              "Cannot open '%s'. Try to make it readable with 'chmod u+r %s'.", logFile, logFile));
    }

    int numSavedEvents = 0;
    try (Stream<String> lines = Files.lines(logFile)) {
      Map<String, JsonEvent> idToJsonEvent = new HashMap<>();

      int index = -1;
      for (String line : (Iterable<String>) lines::iterator) {
        index++;

        JsonEvent jsonEvent;
        try {
          jsonEvent = moshi.adapter(JsonEvent.class).fromJson(line);
        } catch (IOException | JsonDataException e) {
          logger.atSevere().withCause(e).log("line %d is not a valid event: %s", index, line);
          continue; // TODO: Test-drive this `continue`
        }
        if (jsonEvent == null) {
          logger.atSevere().log(
              "line %d was unexpectedly null when deserializing: %s", index, line);
          continue; // TODO: Test-drive this `continue`
        }
        logger.atFine().log(
            "Read event: id = %s, type = %s, host = %s, state = %s, timestamp = %d",
            jsonEvent.id, jsonEvent.type, jsonEvent.host, jsonEvent.state, jsonEvent.timestamp);

        if (idToJsonEvent.containsKey(jsonEvent.id)) {
          JsonEvent first = idToJsonEvent.remove(jsonEvent.id);

          long durationMs = durationMs(first, jsonEvent);
          Event event =
              Event.builder()
                  .id(first.id)
                  .type(first.type)
                  .host(first.host)
                  .duration(Duration.ofMillis(durationMs))
                  .alert(durationMs > 4)
                  .build();
          logger.atInfo().log(
              "Event took longer than 4ms: id = %s, duration = %s", event.id(), event.duration());

          saveToDatabaseRepository.save(event);
          numSavedEvents++;

          continue;
        }

        idToJsonEvent.put(jsonEvent.id, jsonEvent);
      }
    } catch (IOException e) {
      throw AppException.internalError(String.format("Cannot read '%s'", logFile), e);
    } catch (UncheckedIOException e) {
      throw AppException.internalError(String.format("Cannot read '%s'", logFile), e.getCause());
    } finally {
      logger.atInfo().log("Saved %d events in %s", numSavedEvents, eventsDbFile);
    }
  }

  private long durationMs(JsonEvent first, JsonEvent second) {
    return Math.abs(first.timestamp - second.timestamp);
  }
}
