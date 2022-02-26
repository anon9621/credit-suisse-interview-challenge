package me.jbduncan.creditsuisse.interviewchallenge;

import java.nio.file.Path;

public interface CopyEventsService {
  void execute(Path logFile) throws AppException;
}
