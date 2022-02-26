package me.jbduncan.creditsuisse.interviewchallenge;

public enum ExitCode {
  SUCCESS(0),
  INTERNAL_ERROR(1),
  USAGE_ERROR(2);

  private final int value;

  ExitCode(int value) {
    this.value = value;
  }

  public int value() {
    return value;
  }
}
