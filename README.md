# Buddi Live development

Use JDK 21 or newer and Apache Ant. Set `JAVA_HOME` to your JDK installation.
On a Homebrew machine with `openjdk` installed:

```sh
export JAVA_HOME="$(brew --prefix openjdk)/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
ant run
```

The local server uses `WebContent/WEB-INF/classes/config.properties` and listens at
http://localhost:8686/buddilive/. Configure the local database there before running.
See `doc/README PostgreSQL.txt` for PostgreSQL setup or `doc/README Standalone.txt`
for Derby. Local configuration files are ignored by Git.

In Eclipse, select JDK 21 or newer, refresh the project after pulling library
changes, then use **Project → Clean** before launching `BuddiLiveStandalone`.
The embedded server now uses Jetty 12 with Jakarta Servlet 6.
The launcher uses parent-first class loading because Eclipse and Ant already
provide the application and Restlet libraries. Keep `WebContent/WEB-INF/classes`
on the launch classpath for translation resources; `.classpath` and `ant run`
include it. This prevents separate Restlet copies from causing a class-cast error.
Keep the `ca.digitalcave.moss.restlet` checkout beside this project and open both
in Eclipse. The project reference is retained so source edits and breakpoints in
Moss work while running Buddi Live. Both projects use matching Restlet 2.6.1 and
Jakarta Servlet 6 libraries. Ant builds Moss from source before compiling Buddi
Live, and packages that newly built Moss JAR in each WAR. No bundled Moss Restlet
JAR is used. Set `-Dmoss.project=/path/to/ca.digitalcave.moss.restlet` for Ant if the
checkout is elsewhere. Refresh and clean both projects after upgrading. If an
existing launch configuration overrides the classpath, restore its defaults.

`ant jar` compiles the Java application. `ant war-all` also builds the React UI
and produces both deployment WARs; it requires Node.js 22.12+ and installed UI packages
(`npm ci` in `WebContent/react-ui`). Deploy the resulting WARs on Tomcat 10.1 or
newer using Java 21 or newer. The WAR migration step still converts legacy mail
libraries to Jakarta APIs.

## Java dependencies

JARs remain checked in so normal builds and Eclipse do not require Maven.
`tools/dependencies.xml` records the public dependency versions, verified against
Maven Central on November 18, 2026. With Maven 3.9+ installed, run:

```sh
ant update-dependencies
```

This resolves both application and embedded-server libraries, including transitive
dependencies, before replacing existing JARs. In particular, Liquibase 5.0.4 now
includes SnakeYAML and its other required runtime dependencies. The application
and embedded-server libraries remain separate so Jetty and servlet APIs are not
packaged in deployment WARs. If library filenames change, update `.classpath`
for Eclipse too.

Restlet is pinned to 2.6.1, the latest stable servlet-capable line. Restlet 2.7
removed servlet deployment. Commons Email uses its latest stable
1.x release. FileUpload uses the current Jakarta Servlet 6 milestone, 2.0.0-M5,
to avoid the deprecated request API in 1.x. Jackson uses the current 2.x line.
TOTP remains at its latest release, 1.7.1. Moss Common and Crypto remain private
JARs. Moss Restlet is compiled from the sibling source project on every Ant build.
The Tomcat migration tool remains at its current release, 1.0.13.

## Startup smoke test

After `ant jar`, run `python3 tools/smoke-test.py`. It starts Jetty against a
temporary web root and Derby database, checks the rendered home page and JSON
error handling, then restarts to check that migrations are not reapplied.
It does not use your configured database. Set `JAVA_HOME` as above.
The local test keeps application classes outside the web root to reproduce
Eclipse's launch layout and verifies that Moss classes load from the sibling
source build. The WAR test puts only the launcher and container
libraries on the parent classpath, so application classes load from the WAR.

After building WARs, test the converted deployment libraries as well:

```sh
python3 tools/smoke-test.py --war build/dist/buddilive.war
```

The frontend dependencies and npm lockfile were also refreshed, including React
19.3.1 and Vite 8.3.8. Vite uses its supported Rolldown configuration and automatic
module grouping. Forced size-based vendor splitting caused circular initialization
imports and a blank screen after login, so it has been removed. The large-bundle
warning is retained as a performance issue to address with deliberate lazy loading.

After building the frontend, `python3 tools/frontend-smoke-test.py` loads the actual
production bundles in isolated headless Chrome profiles and checks desktop and
mobile account-screen rendering for uncaught JavaScript errors. It uses an empty
account API fixture and does not access your account or database. Chrome is
discovered at its standard macOS path or on `PATH`; use `--chrome` or `CHROME_BIN`
to specify another Chrome/Chromium executable.

Known runtime warnings: Restlet 2.6 deprecates the servlet bridge as a class,
although it remains the final servlet-capable release. c3p0 0.15 also warns about
its default class-instantiation whitelist policy; no whitelist policy is changed
by this upgrade. These warnings do not prevent startup. Java compilation enables
deprecation diagnostics and currently reports no deprecated method calls.
