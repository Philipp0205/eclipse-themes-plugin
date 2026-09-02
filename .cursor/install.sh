#!/usr/bin/env bash
# Idempotent bootstrap for the Eclipse Themes (Maven/Tycho) plugin.
# Installs the required Maven version, then warms the local ~/.m2 cache and the
# Eclipse target platform by running the same build the CI uses.
set -euo pipefail

MAVEN_VERSION="3.9.9"
MAVEN_HOME="/opt/apache-maven-${MAVEN_VERSION}"

# JDK 21 ships in the base image; fail early if it is somehow missing.
if ! command -v java >/dev/null 2>&1; then
  echo "ERROR: java not found on PATH; JDK 21 is required." >&2
  exit 1
fi

# Install Maven only if the pinned version is not already present.
if [ ! -x "${MAVEN_HOME}/bin/mvn" ]; then
  echo "Installing Apache Maven ${MAVEN_VERSION}..."
  tmp_archive="$(mktemp --suffix=.tar.gz)"
  curl -fsSL -o "${tmp_archive}" \
    "https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz"
  sudo tar -xzf "${tmp_archive}" -C /opt
  rm -f "${tmp_archive}"
else
  echo "Apache Maven ${MAVEN_VERSION} already installed."
fi

# Expose mvn on the default PATH without mutating shell profiles.
sudo ln -sfn "${MAVEN_HOME}" /opt/maven
sudo ln -sfn /opt/maven/bin/mvn /usr/local/bin/mvn

mvn -version

# Warm the dependency cache and Eclipse target platform, and validate the build.
# This is the same command documented in CONTRIBUTING.md and run in CI.
mvn -B clean verify

echo "Environment setup complete."
