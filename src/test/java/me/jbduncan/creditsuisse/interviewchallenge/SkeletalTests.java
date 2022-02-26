package me.jbduncan.creditsuisse.interviewchallenge;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;

abstract class SkeletalTests {

  MockitoSession mockito;

  @BeforeEach
  void setup() {
    mockito =
        Mockito.mockitoSession().initMocks(this).strictness(Strictness.STRICT_STUBS).startMocking();
  }

  @AfterEach
  void tearDown() {
    mockito.finishMocking();
  }
}
