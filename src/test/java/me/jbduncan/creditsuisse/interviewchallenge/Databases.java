package me.jbduncan.creditsuisse.interviewchallenge;

import com.google.common.io.MoreFiles;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import me.jbduncan.creditsuisse.interviewchallenge.Event.Builder;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;

final class Databases {

  static void removeEventsDb() throws IOException {
    removeEventsTable();
    if (Files.exists(Paths.get("eventsdb"))) {
      MoreFiles.deleteRecursively(Paths.get("eventsdb"));
    }
    if (Files.exists(Paths.get("eventsdb.tmp"))) {
      MoreFiles.deleteRecursively(Paths.get("eventsdb.tmp"));
    }
    Files.deleteIfExists(Paths.get("eventsdb.log"));
    Files.deleteIfExists(Paths.get("eventsdb.properties"));
    Files.deleteIfExists(Paths.get("eventsdb.script"));
    Files.deleteIfExists(Paths.get("eventsdb.lck"));
  }

  static void removeEventsTable() {
    jdbi().useHandle(h -> h.execute("drop table events if exists"));
  }

  static void createEventsTable() {
    Jdbi jdbi = jdbi();
    jdbi.useHandle(
        h -> {
          h.execute(
              "create table events ("
                  + "  id varchar(255) not null,"
                  + "  type varchar(255),"
                  + "  host varchar(255),"
                  + "  duration bigint not null,"
                  + "  alert boolean not null"
                  + ")");
        });
  }

  static Jdbi jdbi() {
    return Jdbi.create("jdbc:hsqldb:file:eventsdb", "sa", "");
  }

  static RowMapper<Event> eventMapper() {
    return (rs, ctx) -> {
      Builder builder =
          Event.builder()
              .id(rs.getString("id"))
              .duration(Duration.ofMillis(rs.getLong("duration")))
              .alert(rs.getBoolean("alert"));
      String type = rs.getString("type");
      if (type != null) {
        builder.type(type);
      }
      String host = rs.getString("host");
      if (host != null) {
        builder.host(host);
      }
      return builder.build();
    };
  }

  private Databases() {}
}
