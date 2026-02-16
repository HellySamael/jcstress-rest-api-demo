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

## Running the Concurrent Client

The `ConcurrentLikeClient` simulates concurrent requests to a like-counting service. It now accepts optional command-line arguments to customize the number of concurrent requests and the total number of requests.

**Usage:**

```bash
# Run with default values (100 concurrent requests, 10000 total requests)
# On Linux/macOS (using Git Bash, WSL, or any Unix-like shell)
./run-concurrent-client.sh

# On Windows (using Command Prompt or PowerShell)
.run-concurrent-client.bat

# Run with custom values (e.g., 10 concurrent requests, 100 total requests)
# On Linux/macOS
./run-concurrent-client.sh 10 100

# On Windows
.run-concurrent-client.bat 10 100
```

-   The first argument (optional) specifies the **number of concurrent requests**. Default is `100`.
-   The second argument (optional) specifies the **total number of POST requests**. Default is `10000`.

For example, to test with a low number of concurrent requests where issues might be less apparent, you could run:
`./run-concurrent-client.sh 5 50`

The client will execute the specified number of concurrent requests and then report the final like count.

## Resetting the Like Count

You can reset the current like count to zero using a `DELETE` request to the `/likes` endpoint. This is useful for running multiple tests without restarting the server.

```bash
# Example using curl
curl -X DELETE http://localhost:7070/likes
```

After this, a subsequent `GET http://localhost:7070/likes` should return `{"likes":0}`.

## Running Unit Tests

To execute the standard unit tests for the project:

```bash
# On Linux/macOS
./gradlew test

# On Windows
.\gradlew.bat test
```

Test reports will be generated in `app/build/reports/tests/test/`.

## Running JCStress Tests

JCStress (Java Concurrency Stress) tests are specifically designed to find bugs in concurrent code.

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
