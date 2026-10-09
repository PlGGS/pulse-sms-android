#!/bin/bash
# Local build helper for this 2020-era project.
#
# Two things need forcing on a modern machine:
#  - Gradle 6.6.1 / AGP 4.1 need JDK 8-11; this repo uses a locally unpacked Temurin 11.
#  - ~/.gradle/gradle.properties pins org.gradle.java.home to JDK 17, and that file wins
#    over JAVA_HOME, so it has to be overridden on the command line.
set -euo pipefail

JDK11="$HOME/Library/Java/JavaVirtualMachines/dl-temurin11/jdk-11.0.32.1+1/Contents/Home"

exec ./gradlew \
  -Dorg.gradle.java.home="$JDK11" \
  "${@:-:app:assembleDebug}"
