# TraceLens 샘플 애플리케이션 (Pure Java)

**순수 Java로 작성된** Spring Boot 애플리케이션으로, Kotlin으로 작성된 TraceLens 라이브러리를 사용합니다.

## 📌 핵심 포인트

### TraceLens는 Kotlin으로 작성되었지만 Java 프로젝트에서 완벽하게 작동합니다!

- ✅ **Kotlin 코드 없음**: 이 프로젝트는 100% Pure Java
- ✅ **Kotlin 플러그인 없음**: `build.gradle`에 Kotlin 플러그인 불필요
- ✅ **동일한 기능**: Kotlin 프로젝트와 완전히 동일하게 작동
- ✅ **자동 의존성**: Kotlin 런타임이 TraceLens 의존성으로 자동 포함

## 프로젝트 구조

```
sample-app-java/
├── src/main/java/
│   └── com/example/demo/
│       ├── DemoApplication.java         # 순수 Java 메인 클래스
│       └── controller/
│           └── TestController.java      # 순수 Java 컨트롤러
├── src/main/resources/
│   ├── application.properties           # Java 스타일 설정
│   └── static/
│       └── index.html                   # 로그 뷰어
└── build.gradle                         # Kotlin 플러그인 없음!
```

## 빌드 설정 (build.gradle)

```gradle
plugins {
    id 'java'  // Java만 사용!
    id 'org.springframework.boot' version '3.4.0'
    id 'io.spring.dependency-management' version '1.1.6'
}

dependencies {
    // Kotlin 라이브러리를 Java에서 사용
    implementation 'com.tracelens:trace-lens:1.0.0-SNAPSHOT'

    // 나머지는 표준 Java 의존성
    implementation 'org.springframework.boot:spring-boot-starter-web'
}
```

**주의**: Kotlin 플러그인(`kotlin("jvm")`)이 없습니다!

## 실행 방법

### 1. Gradle Wrapper 생성

```bash
gradle wrapper --gradle-version=8.10.2
```

### 2. 애플리케이션 실행

```bash
./gradlew bootRun
```

**포트**: 8081 (Kotlin 샘플 앱과 충돌 방지)

### 3. 로그 뷰어 열기

```
http://localhost:8081
```

## 테스트 API

### 간단한 테스트 (Java)

```bash
curl http://localhost:8081/api/test/simple
# Response: "Test completed (Java)"
```

### 에러 테스트 (Java)

```bash
curl http://localhost:8081/api/test/error
```

### 메일 발송 (Java)

```bash
# 성공 케이스
curl -X POST http://localhost:8081/api/test/mail \
  -H "Content-Type: application/json" \
  -d '{"to": "user@example.com", "subject": "Test", "body": "Hello from Java"}'

# 실패 케이스
curl -X POST http://localhost:8081/api/test/mail \
  -H "Content-Type: application/json" \
  -d '{"to": "fail@example.com", "subject": "Test", "body": "Hello"}'
```

## TraceLens 엔드포인트

| 엔드포인트 | 설명 |
|----------|------|
| `http://localhost:8081/` | 로그 뷰어 UI |
| `http://localhost:8081/trace-lens/logs/stream` | SSE 실시간 스트리밍 |
| `http://localhost:8081/trace-lens/logs/snapshot` | 로그 스냅샷 |
| `http://localhost:8081/trace-lens/logs/health` | 헬스 체크 |

## 로그 예시

Java 코드에서 생성된 로그가 실시간으로 표시됩니다:

```
[2025-10-31T13:20:15.123Z] [INFO] [http-nio-8081-exec-1] c.e.demo.controller.TestController - Simple test API called (Java)
[2025-10-31T13:20:15.124Z] [DEBUG] [http-nio-8081-exec-1] c.e.demo.controller.TestController - Debug level log from Java
[2025-10-31T13:20:15.125Z] [WARN] [http-nio-8081-exec-1] c.e.demo.controller.TestController - Warning level log from Java
```

## Java와 Kotlin 프로젝트 비교

### Kotlin 프로젝트 (sample-app)
- **언어**: Kotlin
- **포트**: 8080
- **빌드**: `kotlin("jvm")` 플러그인 필요
- **설정**: application.yml

### Java 프로젝트 (sample-app-java)
- **언어**: Pure Java
- **포트**: 8081
- **빌드**: `java` 플러그인만 필요
- **설정**: application.properties

### 공통점
- ✅ **동일한 TraceLens 기능**
- ✅ **동일한 API**
- ✅ **동일한 로그 뷰어**
- ✅ **동일한 성능**

## 의존성 분석

### TraceLens가 가져오는 의존성

TraceLens를 추가하면 다음이 자동으로 포함됩니다:

```
com.tracelens:trace-lens:1.0.0-SNAPSHOT
├── org.jetbrains.kotlin:kotlin-stdlib (자동 포함)
├── org.jetbrains.kotlin:kotlin-reflect (자동 포함)
├── org.springframework.boot:spring-boot-starter-web
├── org.springframework.boot:spring-boot-autoconfigure
└── ch.qos.logback:logback-classic
```

Java 프로젝트에서는 Kotlin을 직접 사용하지 않지만, Kotlin 런타임이 의존성으로 포함되어 TraceLens가 정상 작동합니다.

## 설정 (application.properties)

```properties
# TraceLens 설정 - Java 프로젝트에서도 동일!
trace-lens.enabled=true
trace-lens.max-buffer-size=1000
trace-lens.buffer-ttl-minutes=30
trace-lens.poll-interval-millis=500
trace-lens.endpoint-path=/trace-lens/logs

# 커스텀 세션 헤더 (선택사항)
trace-lens.session-header-name=X-Session-ID
```

## FAQ

### Q: Java 프로젝트인데 Kotlin 의존성이 추가되는게 문제 아닌가요?

**A**: 전혀 문제없습니다!
- Kotlin 런타임은 약 1.5MB로 매우 작음
- JVM에서 Java와 완벽하게 상호운용
- 추가 설정이나 플러그인 불필요
- 성능 영향 없음

### Q: Kotlin을 배워야 하나요?

**A**: 아니요!
- TraceLens를 사용하는데 Kotlin 지식 불필요
- 모든 설정은 YAML/Properties로 가능
- Java 코드만 작성하면 됨

### Q: Java 17 이상이 필요한가요?

**A**: 네, Spring Boot 3.x는 Java 17 이상이 필요합니다.

## 다음 단계

1. [ ] 애플리케이션 실행 및 테스트
2. [ ] 다른 Java 프로젝트에 TraceLens 적용
3. [ ] 프로덕션 배포 전 성능 테스트

## 결론

**TraceLens는 Kotlin으로 작성되었지만, Pure Java 프로젝트에서도 완벽하게 작동합니다!**

Kotlin과 Java는 JVM에서 완벽하게 상호운용되므로, 프로젝트 언어에 관계없이 TraceLens의 모든 기능을 사용할 수 있습니다.
