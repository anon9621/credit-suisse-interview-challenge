Credit Suisse Interview Challenge
===

Setup
---

Install Java 8. Later versions might work, but have not been tested. Zulu's OpenJDK was used during
development, but any distribution of Java 8 should work. Java 8 can be installed with a tool like
[SDKMAN!](https://sdkman.io/), [asdf](https://asdf-vm.com/) or
[jabba](https://github.com/shyiko/jabba/).

This repository has a `.tool-versions` file that works with asdf out of the box.

Build
---

Open this project in a terminal and run `./gradlew shadowJar`.

Test
---

Open this project in a terminal and run `./gradlew check`.

Run
---

1. Open a terminal.
2. Ensure that your terminal is using Java 8.
3. Build the project.
4. Run:

```shell
java -jar build/libs/credit-suisse-interview-challenge-0.1.0-SNAPSHOT-all.jar <log-file>
```

There are some example log files in `src/test/resources/`. So an example command would be:

```shell
java -jar build/libs/credit-suisse-interview-challenge-0.1.0-SNAPSHOT-all.jar src/test/resources/logFile.txt
```

If I had more time...
---

...I would:

- Use structured logging to allow the logs to be searched more easily in whatever log search tool
that the logs would be redirected to.
- Use a tool like [Uber's NullAway](https://github.com/uber/NullAway) or the
[Checker Framework](https://checkerframework.org/) to catch `NullPointerException`s at compile-time
rather than runtime (as this project does by using `Objects::requireNonNull` liberally.)
- Profile this program with random, gigabytes-large log files to see if my strategy of using a map
in `DefaultCopyEventsService` (to track STARTED and FINISHED events until they can be matched)
doesn't use too much memory. If it does use too much memory, then I'd see if using an optimized map
implementation from [fastutil](https://fastutil.di.unimi.it/) (to reduce overall memory
consumption), another data structure like an array, and/or reading the log file's lines randomly (to
avoid pathological cases where many STARTED events are stored in memory until a matching FINISHED
event is read) would help. If not, then a new strategy would be needed...
- Benchmark this program to see if any of the following multithreading strategies would speed it up:
    - A parallel `Files::lines` stream (might be limited by how fast the disk can be read, or the
stream implementation itself if it can't be parallelized).
    - Using a thread pool to read sections of the log file in parallel (again, might be limited by
how fast the disk can read the file).
    - Either of these strategies would need synchronization to put the log events into the
`DefaultCopyEventsService`'s map in a thread-safe way, which may slow things down.
    - Is there a way of splitting the log file across multiple machines, MapReduce style, to
parallelize things? Perhaps by splitting the file into multiple smaller files (with
each file having matching STARTED and FINISHED events), processing one file per machine, and writing
the events into a shared database.
- Fix the TODOs in `DefaultCopyEventsService` to test the `continue` statements, which I forgot to
test-drive!
- Fix the TODOs in `DefaultCopyEventsServiceTests` to find an alternative to mocking the
`Files::lines` static method, which I did to be able to test-drive what happens when `Files::lines`
throws an `IOException` or `UncheckedIOException`. A potential solution might be to inject a mock
`FileSystem` (or the log file `Path` object that points to said file system) into
`DefaultCopyEventsService`, to allow the exact behaviour of the file system to be controlled.