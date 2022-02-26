package me.jbduncan.creditsuisse.interviewchallenge;

import com.google.auto.value.AutoValue;
import java.time.Duration;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

@AutoValue
abstract class Event {

  abstract String id();

  abstract Optional<String> type();

  abstract Optional<String> host();

  abstract Duration duration();

  abstract boolean alert();

  static Builder builder() {
    return new AutoValue_Event.Builder();
  }

  @AutoValue.Builder
  abstract static class Builder {
    abstract Builder id(String value);

    abstract Builder type(@Nullable String value);

    abstract Builder host(@Nullable String value);

    abstract Builder duration(Duration value);

    abstract Builder alert(boolean value);

    abstract Event build();
  }
}
