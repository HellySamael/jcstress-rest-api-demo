#!/bin/bash

echo "Starting Java client ConcurrentVoteClient..."

# Get the directory of the script, then resolve the project root if the script is in a subdirectory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$SCRIPT_DIR"

# Construct the classpath with absolute paths
# Using ':' for bash classpath separator
CLASSPATH="$PROJECT_ROOT/build/classes/java/main"
# Add all JARs from build/libs
for jar in "$PROJECT_ROOT/build/libs"/*.jar; do
    CLASSPATH="$CLASSPATH:$jar"
done
# Add all JARs from app/build/libs
for jar in "$PROJECT_ROOT/app/build/libs"/*.jar; do
    CLASSPATH="$CLASSPATH:$jar"
done

# Execute the Java client
java -cp "$CLASSPATH" com.example.demo.client.ConcurrentVoteClient "$@"

echo "Java client finished."
