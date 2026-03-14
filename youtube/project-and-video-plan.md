# Certificate Service: Project Update And Video Plan

## What We Changed In The Project

The codebase was updated with a focused maintenance pass aimed at build compatibility, async correctness, runtime cleanup, and a few quality-of-life fixes.

### Build And Runtime

- upgraded the Gradle wrapper to `9.4.0`
- upgraded Spring Boot to `3.5.7`
- aligned the project with Java `25`
- updated Heroku runtime expectations via `system.properties`

### Async And Analytics

- removed the mixed async approach that used both Spring `@Async` and `CompletableFuture.runAsync(...)`
- introduced a dedicated Spring-managed analytics executor
- extracted request metadata into an immutable analytics request context before dispatching async work
- updated analytics and metadata services to use Spring-managed async execution consistently

### Metrics And Behavior Fixes

- corrected daily, weekly, and monthly metrics aggregation so it uses bounded time windows instead of all-time data
- updated the repository queries and test expectations accordingly
- made Docker-based analytics integration tests skip cleanly when Docker is unavailable

### Runtime And Config Cleanup

- disabled `spring.jpa.open-in-view`
- cleaned up PDF generator runtime logging to use SLF4J instead of `System.out` and `System.err`
- cleaned Spring bean wiring for signing and keystore usage
- updated the Heroku deploy script to stop forcing a local default certificate password into Heroku config

### Dependency Cleanup

- identified that the `commons-logging` warning came from PDFBox, not Spring
- excluded `commons-logging` from the PDFBox dependency
- added `jcl-over-slf4j` so Commons Logging calls are bridged into the normal Spring Boot logging stack

### Gag-Level Over-Engineering (Branch: `gag-features`)

- implemented a **Local Immutable Blockchain** for certificate "Proof of Existence"
- added **Biometric Stylometry Analysis** (image metadata) for signature verification
- integrated **SHA-3 512-bit Quantum-Resistant Hashing**
- built a **Nuclear-Grade Deep Verification Engine** with real-time audit logging
- added automated tests for all "gag" services and verified them with `./gradlew test`

## Verification Results

- local test suite passed on Java `25.0.2`
- Heroku auto-deploy from GitHub `main` was confirmed to still work
- Heroku logs showed the upgraded app running successfully on:
  - Spring Boot `3.5.7`
  - Java `25.0.2`

## Git History From This Work

Relevant commits:

- `de1353f` `Upgrade build to Java 25 and refactor analytics async flow`
- `8a2dc07` `Bridge PDFBox commons-logging to SLF4J`

## YouTube Folder Setup

A local `youtube/` folder was created for video-planning assets.

It is intentionally ignored by Git so that video notes, scripts, thumbnail ideas, and other media planning files do not affect the code repository.

## Video Direction

### Core Angle

This should be a story-first video for Java developers about solving an absurd problem with completely serious engineering.

The strongest framing is:

"I built a Java app to sign an ebook."

The joke gets people in. The payoff is that the implementation touches real topics:

- Spring Boot
- PDF generation
- digital signatures
- key stores
- QR code verification
- deployment

### Core Joke

The central line worth building around is:

"I created a self-signed certificate signed with my own self-signed certificate."

That is the best one-sentence summary of both the humor and the technical premise.

## Assets Created For The Video

The following local files were created:

- `youtube/outline.md`
- `youtube/script.md`
- `youtube/titles-and-thumbnails.md`

### What Each File Contains

`outline.md`

- recommended 8 to 10 minute structure
- story beats
- technical lesson beats
- suggested opening and closing direction

`script.md`

- a full first-pass draft script
- story-first tone with Java-specific lessons
- emphasis on the absurdity of the premise and the reality of the APIs

`titles-and-thumbnails.md`

- 10 title options
- 5 thumbnail concepts
- a recommended shortlist

## Current Recommended Positioning

### Recommended Primary Title

`I Built a Java App to Sign an Ebook. Yes, Really.`

### Recommended Alternate Title

`The Dumbest Useful Java App I've Ever Built`

### Recommended Thumbnail Text

`I SIGNED AN EBOOK`

## Video Goals

The video should be:

- entertaining first
- technically credible second
- informative without becoming a full tutorial

The target balance is roughly:

- 60% amusing engineering story
- 30% demo
- 10% Java lessons learned

## Recommended Video Structure

1. Open with the absurd request: "Can you sign my ebook?"
2. Show the finished certificate immediately
3. Explain why the idea is silly but irresistible
4. Demo the app end-to-end
5. Walk through the interesting Java pieces
6. Share lessons learned from PDF, crypto, and verification work
7. Close on the idea that Java is excellent at taking nonsense seriously

## Themes To Emphasize

- serious APIs used for a ridiculous purpose
- the difference between visible signatures and digital signatures
- how quickly a joke project turns into infrastructure
- why silly side projects are a good way to learn unfamiliar libraries

## Things To Avoid In The Video

- do not make it a step-by-step coding tutorial
- do not over-explain certificate theory
- do not bury the joke under implementation detail
- do not spend too much time justifying why the project exists

## Planned Next Steps For The Video

1. Review the current outline, script, and title directions
2. Choose the preferred angle:
   - more story-driven
   - more Java-driven
   - more absurdity-driven
3. Tighten the current script into a more natural spoken version
4. Build a shot list for:
   - app demo
   - certificate PDF
   - verification page
   - selected code snippets
5. Pick a final title and thumbnail concept
6. Record the video

## Working Summary

The app is now in a cleaner technical state and is deployed successfully.

The video concept is strong because it combines:

- a ridiculous premise
- a clean live demo
- real Java engineering
- a memorable punchline

The next content task is not ideation anymore. It is refinement.
