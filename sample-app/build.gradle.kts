import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.spring") version "2.1.0"
    id("org.springframework.boot") version "3.4.0"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

repositories {
    // 로컬 테스트용 (원격 배포가 완료되면 제거 가능)
    mavenLocal()

    mavenCentral()

    // OSSRH releases repository - 배포 직후 여기서 즉시 사용 가능
    maven {
        url = uri("https://s01.oss.sonatype.org/content/repositories/releases/")
    }
}

dependencies {
    // TraceLens 라이브러리 - Maven Central에서 가져오기
    implementation("io.github.beoks:trace-lens:1.0.1")

    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
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
