plugins {
    kotlin("jvm") version "2.4.0"
    kotlin("plugin.serialization") version "2.4.0"
    id("io.ktor.plugin") version "3.5.2"
    application
}

ktor {
    openApi {
        enabled = true
        codeInferenceEnabled = true
        onlyCommented = false
    }
}

group = "com.ojsolutions"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("ch.qos.logback:logback-classic:1.5.37")

    implementation("io.ktor:ktor-server-openapi:3.5.2")
    implementation("io.ktor:ktor-server-swagger:3.5.2")
    implementation("io.ktor:ktor-server-routing-openapi:3.5.2")

    implementation("io.ktor:ktor-server-core:3.5.2")
    implementation("io.ktor:ktor-server-netty:3.5.2")
    implementation("io.ktor:ktor-server-status-pages:3.5.2")
    implementation("io.ktor:ktor-server-content-negotiation:3.5.2")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.5.2")

    implementation("io.insert-koin:koin-core:4.2.2")
    implementation("io.insert-koin:koin-ktor:4.2.2")
    implementation("io.insert-koin:koin-logger-slf4j:4.2.2")

    implementation("com.tigerbeetle:tigerbeetle-java:0.17.8")

    implementation("org.jetbrains.exposed:exposed-core:1.3.0")
    implementation("org.jetbrains.exposed:exposed-dao:1.3.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:1.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")

    implementation("org.postgresql:postgresql:42.7.12")
    implementation("com.zaxxer:HikariCP:7.1.0")

    implementation("org.jetbrains.exposed:exposed-java-time:1.3.1")

    implementation("dev.restate:sdk-kotlin-http:2.9.4")

    // ======================== TESTING \/

    testImplementation(kotlin("test"))

    testImplementation("org.junit.jupiter:junit-jupiter:6.1.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.1.3")

    testImplementation("io.mockk:mockk:1.14.11")

    testImplementation("io.ktor:ktor-server-test-host:3.5.2")

    // ======================== TESTING /\
}

kotlin {
    jvmToolchain(25)
}

application {
    mainClass.set("com.ojsolutions.ApplicationKt")

    applicationDefaultJvmArgs = listOf(
        "--enable-native-access=ALL-UNNAMED"
    )
}

tasks.test {
    useJUnitPlatform()
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events(
            "passed",
            "failed",
            "skipped"
        )

        showStandardStreams = true
    }
    reports {
        html.outputLocation.set(
            file("C:/test-reports")
        )
    }
}