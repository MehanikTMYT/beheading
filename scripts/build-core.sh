#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p build/classes build/test-classes
find src/main/java -name '*.java' -print | sort > build/main-sources.txt
find src/test/java -name '*.java' -print | sort > build/test-sources.txt
javac --release 25 -encoding UTF-8 -d build/classes @build/main-sources.txt
javac --release 25 -encoding UTF-8 -cp build/classes -d build/test-classes @build/test-sources.txt
java -cp build/classes:build/test-classes ru.mehtm.beheading.core.BeheadingCalculatorTest
jar --create --file build/beheading-core.jar -C build/classes .
