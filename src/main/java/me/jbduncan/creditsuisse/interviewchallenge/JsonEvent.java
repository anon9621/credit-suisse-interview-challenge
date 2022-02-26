package me.jbduncan.creditsuisse.interviewchallenge;

import org.jetbrains.annotations.Nullable;

public final class JsonEvent {
  public String id;
  public State state;
  public @Nullable String type;
  public @Nullable String host;
  public long timestamp;
}
