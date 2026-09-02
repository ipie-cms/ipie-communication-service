# ipie-communication-service

Turns domain events published by `ipie-user-service` into outbound email and SMS, and keeps an
append-only record of everything it sent.

Extracted from the `ipie-platform-mca` monorepo. It builds against the platform as **published
artifacts** — there is no `project(':...')` dependency and no `includeBuild` anywhere in this
repository.

## Platform dependency

The version is fixed by one property in `gradle.properties`:

    ipiePlatformVersion=0.1.0-SNAPSHOT

which pins `in.gov.ipie:ipie-parent` (the version BOM), `in.gov.ipie:ipie-common-libs` (shared
libraries, plus its `test-fixtures` variant for the ArchUnit rules and Testcontainers base classes)
and `in.gov.ipie:ipie-build-conventions` (the `ipie.*` convention plugins, which also carry the
Checkstyle and SpotBugs configuration).

Bumping that property is a deliberate act — that is the trade the extraction buys. This service is
no longer dragged by every platform change, but it must choose when to take one. Automate the bump
as a pull request gated on this repository's own CI. **Never** use a dynamic `1.+` or `-SNAPSHOT`
range: builds stop being reproducible, and one bad platform commit would reach every service
unchecked.

## Resolving the platform artifacts

They are published to GitHub Packages under `ipie-cms/ipie-platform-mca`. That registry
authenticates every read, whether or not the repository is public, so
reads need credentials:

- **Locally** — set `ipie.packages.user` and `ipie.packages.token` in `~/.gradle/gradle.properties`
  (never in this repository), or publish the platform to Maven Local, which is checked first.
- **In CI** — a workflow's default `GITHUB_TOKEN` is scoped to *its own* repository and cannot read
  a private package owned by another one. That access must be granted explicitly in the package
  settings, or a PAT supplied.

## Build

    ./gradlew check      # tests, ArchUnit, Checkstyle, SpotBugs
    ./gradlew bootJar
