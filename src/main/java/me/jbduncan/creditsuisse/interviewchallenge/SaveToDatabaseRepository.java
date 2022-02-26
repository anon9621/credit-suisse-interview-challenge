package me.jbduncan.creditsuisse.interviewchallenge;

public interface SaveToDatabaseRepository {
  void save(Event event) throws AppException;
}
