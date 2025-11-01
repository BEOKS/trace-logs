import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.spring") version "2.1.0"
    id("org.springframework.boot") version "3.4.0" apply false
    id("io.spring.dependency-management") version "1.1.6"
    id("com.vanniktech.maven.publish") version "0.30.0"
}

// Group ID must match your verified namespace in Central Portal
// Option 1 (Recommended): GitHub-based namespace - io.github.beoks (auto-verified)
// Option 2: Domain-based namespace - com.tracelens (requires tracelens.com domain verification)
group = "io.github.beoks"
version = "1.0.1"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starter (without embedded server)
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Logging
    implementation("ch.qos.logback:logback-classic")
    implementation("org.slf4j:slf4j-api")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.4.0")
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

kotlin {
    jvmToolchain(17)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Enable jar task for library publishing
tasks.named<Jar>("jar") {
    enabled = true
    archiveClassifier.set("")
}

// Configure Maven publishing with vanniktech plugin
mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    signAllPublications()

    coordinates("io.github.beoks", "trace-lens", version.toString())

    pom {
        name.set("TraceLens")
        description.set("Session-based log streaming library for Spring Boot applications")
        url.set("https://github.com/beoks/trace-lens")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        developers {
            developer {
                id.set("BEOKS")
                name.set("Jaeseong LEE")
                email.set("lee01042000@gmail.com")
            }
        }

        scm {
            connection.set("scm:git:git://github.com/beoks/trace-lens.git")
            developerConnection.set("scm:git:ssh://github.com/beoks/trace-lens.git")
            url.set("https://github.com/beoks/trace-lens")
        }
    }
}
