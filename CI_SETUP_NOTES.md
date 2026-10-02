# CI + SonarCloud Setup Notes

> Working notes for an upcoming session. Goal: bring this repo's CI and code-quality
> setup in line with the MockHub repository. Delete this file once CI is in place.

## Current state (as of June 2026)

- **No GitHub Actions** in this repo — never had any (verified across all branches and history).
- **No SonarCloud** — no plugin, no properties file, no check runs from its GitHub App.
- The only automated check on PRs is the **GitGuardian** secret-scanning app.
- The build enforces an **80% line coverage gate** (`jacocoTestCoverageVerification` wired
  into `check`), but it only runs when someone builds locally.
- Tests were last verified under **JDK 21** (the cloud sandbox had no JDK 25); the repo
  toolchain targets 25. The first CI run will be the first true JDK 25 verification.

## Plan

### 1. Review MockHub's arrangement (needs MockHub in session scope)

Look at, and mirror where sensible:

- `.github/workflows/*.yml` — triggers (push/PR/branches), JDK distribution and version,
  Gradle caching approach, build command, any extra jobs (e.g. dependency submission)
- Sonar wiring — `org.sonarqube` Gradle plugin config vs `sonar-project.properties`
  vs SonarCloud Automatic Analysis; project key / organization naming convention
- Quality gate behavior — does the build fail on gate failure? PR decoration enabled?

### 2. Add the workflow here

Baseline (adjust to match MockHub):

- Trigger on push to `main` and on pull requests
- Temurin JDK 25 + `gradle/actions/setup-gradle` caching
- `./gradlew build` — runs all tests **and** the coverage gate
- For Sonar: `./gradlew sonar` step with `SONAR_TOKEN`; checkout with `fetch-depth: 0`
  (Sonar needs full history for blame/new-code detection)

### 3. SonarCloud specifics for this repo

- JaCoCo XML report is **already enabled** at
  `build/reports/jacoco/test/jacocoTestReport.xml` — point
  `sonar.coverage.jacoco.xmlReportPaths` at it
- Coverage exclusions used by the gate (Application, `config/**`, `model/**`) should be
  mirrored in `sonar.coverage.exclusions` so the two numbers roughly agree
- **Ken's TODO**: create the project on sonarcloud.io and add a `SONAR_TOKEN` secret
  to this repo (Settings → Secrets and variables → Actions)

## Gotchas discovered this session — will save time

1. **Testcontainers tests will run in CI for the first time.** `AnalyticsIntegrationTest`
   (3 tests) is `@Testcontainers(disabledWithoutDocker = true)` and has been silently
   skipped in environments without Docker. GitHub-hosted runners *have* Docker, so these
   will execute — if they fail, it's pre-existing behavior surfacing, not a CI problem.
2. **Coverage gate failures read as build failures.** A CI failure mentioning
   "Rule violated for bundle" means coverage dropped below 80% (or a service class below
   70%) — the fix is tests, not build config. See CLAUDE.md "Coverage Gate".
3. **`PdfBoxGenerator` is excluded** from the per-class 70% rule (defensive fallback code);
   don't "fix" that exclusion without reading the comment in `build.gradle.kts`.
4. **Keystore is not needed for CI.** Tests create their own throwaway keystores
   (`application-test.yaml` and `@TempDir` paths); no `CERTIFICATE_KEYSTORE_B64` or other
   secrets are required for the build to pass.

## Nice-to-haves once CI is green

- Branch protection on `main` requiring the build check (and Sonar gate, if desired)
- A coverage/quality badge in the README
- Dependabot or similar for dependency updates ("Dependency Updates" is already listed
  in TODOs.md)
