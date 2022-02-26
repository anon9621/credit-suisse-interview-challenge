package me.jbduncan.creditsuisse.interviewchallenge;

public abstract class AppException extends Exception {

  public static AppException usageError(String message) {
    return new UsageError(message);
  }

  public static AppException internalError(String message, Exception cause) {
    return new InternalError(message, cause);
  }

  private AppException(String message) {
    super(message);
  }

  private AppException(String message, Exception cause) {
    super(message, cause);
  }

  public abstract ExitCode exitCode();

  public static final class UsageError extends AppException {

    private UsageError(String message) {
      super(message);
    }

    @Override
    public ExitCode exitCode() {
      return ExitCode.USAGE_ERROR;
    }
  }

  public static final class InternalError extends AppException {

    private InternalError(String message, Exception cause) {
      super(message, cause);
    }

    @Override
    public ExitCode exitCode() {
      return ExitCode.INTERNAL_ERROR;
    }
  }
}
