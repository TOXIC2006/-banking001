#!/bin/sh
# Lightweight Maven wrapper for this repository.
set -eu
MAVEN_VERSION=3.9.9
BASE_DIR="${MAVEN_USER_HOME:-$HOME/.m2}/wrapper/dists/apache-maven-$MAVEN_VERSION"
MAVEN_HOME="$BASE_DIR/apache-maven-$MAVEN_VERSION"

if [ ! -x "$MAVEN_HOME/bin/mvn" ]; then
  mkdir -p "$BASE_DIR"
  ARCHIVE="$BASE_DIR/apache-maven-$MAVEN_VERSION-bin.tar.gz"
  URL="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$MAVEN_VERSION/apache-maven-$MAVEN_VERSION-bin.tar.gz"
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "$URL" -o "$ARCHIVE"
  elif command -v wget >/dev/null 2>&1; then
    wget -q "$URL" -O "$ARCHIVE"
  else
    echo "curl or wget is required to download Maven" >&2
    exit 1
  fi
  tar -xzf "$ARCHIVE" -C "$BASE_DIR"
  rm -f "$ARCHIVE"
fi
exec "$MAVEN_HOME/bin/mvn" "$@"
