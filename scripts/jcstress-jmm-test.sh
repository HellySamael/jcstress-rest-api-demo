#!/usr/bin/env bash

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd -- "${SCRIPT_DIR}/.." && pwd)"

# Clean previous jcstress report if it exists.
rm -rf "${ROOT_DIR}/build/reports/jcstress"

# Run only JMM jcstress tests (fully-qualified names, regex required by jcstress).
"${ROOT_DIR}/gradlew" --project-dir "${ROOT_DIR}" jcstress --no-configuration-cache --tests "domain.HashMapCounterStressTest"
