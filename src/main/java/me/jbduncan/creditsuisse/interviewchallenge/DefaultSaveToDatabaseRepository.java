package me.jbduncan.creditsuisse.interviewchallenge;

import static java.util.Objects.requireNonNull;

import com.google.common.flogger.FluentLogger;
import org.jdbi.v3.core.Jdbi;

public final class DefaultSaveToDatabaseRepository implements SaveToDatabaseRepository {

  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private final Jdbi jdbi;

  public DefaultSaveToDatabaseRepository(Jdbi jdbi) {
    this.jdbi = requireNonNull(jdbi, "jdbi must not be null");
  }

  @Override
  public void save(Event event) throws AppException {
    requireNonNull(event, "event must not be null");
    try {
      jdbi.useTransaction(
          handle -> {
            handle
                .createUpdate(
                    "create table if not exists events ("
                        + "  id varchar(255) not null,"
                        + "  type varchar(255),"
                        + "  host varchar(255),"
                        + "  duration bigint not null,"
                        + "  alert boolean not null"
                        + ")")
                .execute();
            handle
                .createUpdate(
                    "insert into events (id, type, host, duration, alert) "
                        + "values (:id, :type, :host, :duration, :alert)")
                .bind("id", event.id())
                .bind("type", event.type())
                .bind("host", event.host())
                .bind("duration", event.duration().toMillis())
                .bind("alert", event.alert())
                .execute();
          });
      logger.atFine().log(
          "Saved event: id = %s, type = %s, host = %s, duration = %s, alert = %s",
          event.id(), event.type(), event.host(), event.duration(), event.alert());
    } catch (Exception e) {
      throw AppException.internalError(String.format("Cannot save event: %s", event), e);
    }
  }
}
