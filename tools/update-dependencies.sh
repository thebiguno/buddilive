#!/bin/sh
# Resolve both sets before replacing any checked-in libraries.
set -eu
cd "$(dirname "$0")/.."
repo_dir=$(pwd)
staging_dir=$(mktemp -d "${TMPDIR:-/tmp}/buddilive-dependencies.XXXXXX")
trap 'rm -rf "$staging_dir"' EXIT HUP INT TERM
for profile in web standalone; do
    "${MVN:-mvn}" -B -f tools/dependencies.xml -P"$profile" \
        org.apache.maven.plugins:maven-dependency-plugin:3.9.0:copy-dependencies \
        -DoutputDirectory="$staging_dir/$profile"
done
# Moss artifacts are private project libraries, not available from Maven Central.
find WebContent/WEB-INF/lib -maxdepth 1 -name '*.jar' ! -name 'moss-*.jar' -delete
find lib -maxdepth 1 -name '*.jar' -delete
cp "$staging_dir/web/"*.jar "$repo_dir/WebContent/WEB-INF/lib/"
rm "$staging_dir/standalone/"slf4j-api-*.jar
cp "$staging_dir/standalone/"*.jar "$repo_dir/lib/"
printf '%s\n' 'Dependencies refreshed. Refresh the Eclipse project and clean/rebuild it.'
