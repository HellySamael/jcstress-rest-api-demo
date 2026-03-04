# DEVOXX Concurrent Like Client

This project demonstrates a concurrent Java client designed to interact with a like-counting service, likely simulating multiple users sending "like" requests simultaneously. It also includes Java Concurrency Stress (JCStress) tests to verify the correctness of concurrent operations within the application's domain logic.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Building the Project](#building-the-project)
- [Running the Concurrent Client](#running-the-concurrent-client)
- [Running Unit Tests](#running-unit-tests)
- [Running JCStress Tests](#running-jcstress-tests)

## Prerequisites

-   **Java Development Kit (JDK) 17 or newer**: Ensure you have a compatible JDK installed.
-   **Gradle**: This project uses the Gradle Wrapper, so you don't need to install Gradle separately.

## Building the Project

To build the project, including compiling Java code and packaging JARs, use the Gradle wrapper:

```bash
# On Linux/macOS
./gradlew build

# On Windows (using Command Prompt or PowerShell)
.\gradlew.bat build
```

This will generate the necessary build artifacts in the `build/` and `app/build/` directories.

## Running the Concurrent Clients

There are two concurrent clients available to test the application:

### 1. Like Client (Single Counter)
The `ConcurrentLikeClient` simulates concurrent requests to a single global like counter.

**Usage:**
```bash
# On Windows
.\run-concurrent-client.bat [concurrent] [total]
# On Linux/macOS
./run-concurrent-client.sh [concurrent] [total]
```

### 2. Vote Client (Multiple Items/HashMap)
The `ConcurrentVoteClient` simulates concurrent votes across multiple items ("item1" to "item4"). This tests both the individual counters and the integrity of the underlying `HashMap`.

**Usage:**
```bash
# On Windows
.\run-concurrent-vote-client.bat [concurrent] [total]
# On Linux/macOS
./run-concurrent-vote-client.sh [concurrent] [total]
```

-   The first argument (optional) specifies the **number of concurrent requests**. Default is `100`.
-   The second argument (optional) specifies the **total number of POST requests**. Default is `10000`.

## Resetting Counts

You can reset the counts using `DELETE` requests:

```bash
# Reset likes
curl -X DELETE http://localhost:7070/likes

# Reset votes
curl -X DELETE http://localhost:7070/votes
```

## Running JCStress Tests

JCStress (Java Concurrency Stress) tests are specifically designed to find bugs in concurrent code.

- **LikeCounterStressTest**: Tests race conditions on a simple counter with 2 actors.
- **VoteCounterStressTest**: Tests race conditions and `HashMap` integrity using **4 actors** and multiple items.

To run the JCStress tests:

```bash
# On Linux/macOS
./gradlew jcstress

# On Windows
.\gradlew.bat jcstress
```

After execution, the JCStress reports, including detailed results and findings, can be found in:

-   `build/reports/jcstress/`
-   `build/tmp/jcstress/` (for raw output and intermediate files)

Specifically, look for `index.html` in `build/reports/jcstress/` for a readable report.

---

Feel free to explore the code in `src/main/java/com/example/demo/` and `src/jcstress/java/com/example/demo/` for more details on the client and the concurrency tests.
