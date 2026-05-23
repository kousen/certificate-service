plugins {
    // Lets Gradle auto-provision the Java 25 toolchain on any machine/CI (e.g. Railway)
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "certificate-service"