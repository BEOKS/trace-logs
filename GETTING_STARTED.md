# TraceLens 빠른 시작 가이드

## 프로젝트 개요

TraceLens는 사용자가 브라우저에서 직접 자신의 세션 로그를 실시간으로 확인할 수 있는 Spring Boot Starter 라이브러리입니다.

## 빌드된 산출물

- **JAR 파일**: `build/libs/trace-lens-1.0.0-SNAPSHOT.jar`
- **크기**: ~26KB

## 빠른 시작

### 1. 라이브러리 빌드

```bash
./gradlew build
```

### 2. 로컬 Maven 저장소에 퍼블리시

```bash
./gradlew publishToMavenLocal
```

### 3. 다른 프로젝트에서 사용

#### build.gradle.kts
```kotlin
dependencies {
    implementation("com.tracelens:trace-lens:1.0.0-SNAPSHOT")
}
```

#### application.yml (선택사항)
```yaml
trace-lens:
  enabled: true
  session-header-name: X-Session-ID  # 선택사항
```

### 4. 애플리케이션 실행

Spring Boot 애플리케이션을 실행하면 TraceLens가 자동으로 활성화됩니다.

### 5. 로그 확인

#### 브라우저에서 실시간 스트리밍

1. 애플리케이션에 로그인하여 세션 생성
2. `http://localhost:8080/trace-lens/logs/stream` 접속
3. 다른 탭에서 API 요청 수행
4. 로그 스트리밍 탭에서 실시간 로그 확인

#### HTML 뷰어 사용

`examples/example-log-viewer.html` 파일을 브라우저에서 열고 "연결 시작" 버튼 클릭

## 주요 엔드포인트

| 엔드포인트 | 설명 |
|----------|------|
| `GET /trace-lens/logs/stream` | SSE 실시간 로그 스트리밍 |
| `GET /trace-lens/logs/snapshot` | 현재 버퍼된 로그 조회 |
| `GET /trace-lens/logs/health` | 헬스 체크 |

## 프로젝트 구조

```
trace-lens/
├── src/main/kotlin/com/tracelens/
│   ├── model/
│   │   └── LogEntry.kt                    # 로그 엔트리 데이터 클래스
│   ├── service/
│   │   └── LogBufferService.kt            # 세션별 로그 버퍼 관리
│   ├── appender/
│   │   └── SessionAwareAppender.kt        # Logback 커스텀 appender
│   ├── filter/
│   │   └── SessionTrackingFilter.kt       # 세션 추적 필터
│   ├── controller/
│   │   └── LogStreamController.kt         # SSE 컨트롤러
│   └── config/
│       ├── TraceLensProperties.kt         # 설정 프로퍼티
│       └── TraceLensAutoConfiguration.kt  # 자동 구성
├── src/main/resources/META-INF/spring/
│   └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
├── examples/
│   ├── example-log-viewer.html            # HTML 로그 뷰어 예제
│   └── application.yml                    # 설정 예제
├── build.gradle.kts
├── README.md
└── GETTING_STARTED.md (이 파일)
```

## 작동 방식

```
1. 사용자 요청 → SessionTrackingFilter
   ↓
2. 세션 ID 추출 (JSESSIONID 또는 커스텀 헤더)
   ↓
3. MDC에 세션 ID 저장
   ↓
4. 비즈니스 로직 실행 중 로깅
   ↓
5. SessionAwareAppender가 MDC의 세션 ID 확인
   ↓
6. LogBufferService의 세션별 버퍼에 로그 저장
   ↓
7. LogStreamController가 SSE로 클라이언트에 전송
```

## 사용 시나리오

### 시나리오 1: 메일 발송 실패 원인 확인

1. 사용자가 메일 발송 버튼 클릭
2. 별도 창에서 `/trace-lens/logs/stream` 열기
3. 메일 발송 실패 시 로그에서 정확한 원인 확인
   - SMTP 연결 실패
   - 잘못된 이메일 주소
   - 첨부파일 크기 초과 등

### 시나리오 2: 결제 처리 과정 추적

1. 결제 페이지에 로그 뷰어 임베딩
2. 결제 프로세스 진행 상황 실시간 확인
3. 각 단계별 로그 확인
   - 결제 정보 검증
   - PG사 API 호출
   - 결제 승인/거절
   - DB 저장

## 테스트 방법

### 로컬 테스트용 샘플 프로젝트

```kotlin
@SpringBootApplication
class TestApplication

fun main(args: Array<String>) {
    runApplication<TestApplication>(*args)
}

@RestController
class TestController {
    private val logger = LoggerFactory.getLogger(TestController::class.java)

    @GetMapping("/api/test")
    fun test(): String {
        logger.info("테스트 API 호출됨")
        logger.debug("디버그 로그")
        logger.warn("경고 로그")

        try {
            // 의도적으로 예외 발생
            throw IllegalStateException("테스트 예외")
        } catch (e: Exception) {
            logger.error("에러 발생", e)
        }

        return "테스트 완료"
    }
}
```

### 테스트 순서

1. 테스트 애플리케이션 실행
2. 브라우저 탭 1: `http://localhost:8080/trace-lens/logs/stream` 열기
3. 브라우저 탭 2: `http://localhost:8080/api/test` 호출
4. 탭 1에서 실시간 로그 확인

## 설정 팁

### 개발 환경
```yaml
trace-lens:
  enabled: true
  max-buffer-size: 2000
  buffer-ttl-minutes: 60
  poll-interval-millis: 500
```

### 프로덕션 환경
```yaml
trace-lens:
  enabled: true  # 또는 false
  max-buffer-size: 500
  buffer-ttl-minutes: 15
  include-patterns:
    - /api/internal/**  # 내부 API만
  exclude-patterns:
    - /api/public/**
```

### Spring Security 통합
```kotlin
@Configuration
@EnableWebSecurity
class SecurityConfig {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http.authorizeHttpRequests { auth ->
            auth
                .requestMatchers("/trace-lens/**")
                .hasRole("ADMIN")  // 관리자만 접근
                .anyRequest().permitAll()
        }
        return http.build()
    }
}
```

## 문제 해결

### 로그가 보이지 않을 때

1. 세션이 생성되었는지 확인
   ```bash
   curl -v http://localhost:8080/api/test
   ```
   응답 헤더에 `Set-Cookie: JSESSIONID=...` 확인

2. TraceLens 활성화 확인
   ```bash
   curl http://localhost:8080/trace-lens/logs/health
   ```

3. 로그 레벨 확인
   ```yaml
   logging:
     level:
       com.tracelens: DEBUG
   ```

### SSE 연결이 끊길 때

`sse-timeout-millis` 증가:
```yaml
trace-lens:
  sse-timeout-millis: 3600000  # 1시간
```

## 다음 단계

- [ ] Spring Security 통합 테스트
- [ ] 커스텀 헤더 방식 테스트
- [ ] 프로덕션 배포 전 성능 테스트
- [ ] 로그 필터링 기능 추가 (레벨별, 키워드별)
- [ ] 로그 다운로드 기능 추가

## 라이선스

MIT License

## 지원

이슈나 질문이 있으시면 GitHub Issues를 이용해주세요.
