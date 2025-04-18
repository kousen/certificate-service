plugins {
    java
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories { mavenCentral() }

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.xhtmlrenderer:flying-saucer-pdf:9.12.0")
    implementation("com.github.librepdf:openpdf:2.0.3")
    implementation("org.apache.pdfbox:pdfbox:3.0.4")
    implementation("org.bouncycastle:bcprov-jdk18on:1.80")
    implementation("org.bouncycastle:bcpkix-jdk18on:1.80")
    implementation("org.bouncycastle:bcutil-jdk18on:1.80")
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.google.zxing:javase:3.5.3")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.apache.pdfbox:pdfbox:3.0.4")
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.bootJar { 
    archiveFileName.set("app.jar") 
}

// Configure test task
tasks.test {
    useJUnitPlatform()

    // Disable class data sharing
    jvmArgs("-Xshare:off")
    
    // Increase heap size for tests
    minHeapSize = "256m"
    maxHeapSize = "1g"
    
    // Show standard out/err from tests
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}