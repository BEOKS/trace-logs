# TraceLens

세션 기반 실시간 로그 스트리밍 라이브러리 for Spring Boot

## 개요

TraceLens는 사용자가 브라우저에서 직접 자신의 세션에 해당하는 로그를 실시간으로 확인할 수 있게 해주는 Spring Boot Starter 라이브러리입니다.

### 주요 기능

- ✅ **세션별 로그 격리**: 각 사용자는 자신의 세션에 해당하는 로그만 확인 가능
- ✅ **실시간 스트리밍**: Server-Sent Events (SSE)를 통한 실시간 로그 전송
- ✅ **간편한 통합**: 의존성 추가만으로 자동 구성
- ✅ **커스터마이징 가능**: JSESSIONID 또는 커스텀 헤더를 통한 세션 식별
- ✅ **메모리 효율적**: 버퍼 크기 제한 및 자동 정리 기능

## 사용 사례

메일 발송, 결제 처리 등 복잡한 비즈니스 로직의 처리 결과를 사용자가 확인할 때, 단순히 "실패했습니다"라는 메시지만 보여주는 것이 아니라, 실제 로그를 통해 왜 실패했는지 직접 확인할 수 있게 합니다.

## 설치

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("com.tracelens:trace-lens:1.0.0")
}
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation 'com.tracelens:trace-lens:1.0.0'
}
```

### Maven

```xml
<dependency>
    <groupId>com.tracelens</groupId>
    <artifactId>trace-lens</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 빠른 시작

의존성을 추가하면 자동으로 활성화됩니다. 추가 설정 없이 바로 사용 가능합니다.

### 로그 스트리밍 확인

브라우저에서 다음 URL에 접속:

```
http://localhost:8080/trace-lens/logs/stream
```

Server-Sent Events를 통해 실시간으로 로그가 스트리밍됩니다.

### 로그 스냅샷 확인

현재까지 버퍼된 모든 로그를 한 번에 확인:

```
http://localhost:8080/trace-lens/logs/snapshot
```

### 헬스 체크

```
http://localhost:8080/trace-lens/logs/health
```

## 설정

`application.yml` 또는 `application.properties`에서 설정을 커스터마이징할 수 있습니다.

### application.yml

```yaml
trace-lens:
  # TraceLens 활성화 여부 (기본: true)
  enabled: true

  # 커스텀 세션 헤더 이름 (기본: null, JSESSIONID 사용)
  session-header-name: X-Session-ID

  # 세션당 최대 버퍼 크기 (기본: 1000)
  max-buffer-size: 2000

  # 버퍼 TTL (분 단위, 기본: 30)
  buffer-ttl-minutes: 60

  # 정리 작업 간격 (밀리초, 기본: 60000)
  cleanup-interval: 60000

  # SSE 타임아웃 (밀리초, 기본: 1800000 = 30분)
  sse-timeout-millis: 3600000

  # 로그 폴링 간격 (밀리초, 기본: 500)
  poll-interval-millis: 1000

  # 엔드포인트 경로 (기본: /trace-lens/logs)
  endpoint-path: /trace-lens/logs

  # 필터 순서 (기본: -100)
  filter-order: -100

  # 포함할 URL 패턴 (기본: /**)
  include-patterns:
    - /api/**
    - /app/**

  # 제외할 URL 패턴
  exclude-patterns:
    - /trace-lens/**
    - /actuator/**
    - /static/**
    - /public/**
```

### application.properties

```properties
trace-lens.enabled=true
trace-lens.session-header-name=X-Session-ID
trace-lens.max-buffer-size=2000
trace-lens.buffer-ttl-minutes=60
trace-lens.cleanup-interval=60000
trace-lens.sse-timeout-millis=3600000
trace-lens.poll-interval-millis=1000
trace-lens.endpoint-path=/trace-lens/logs
trace-lens.filter-order=-100
```

## 사용 예제

### JavaScript에서 SSE 연결

```javascript
const eventSource = new EventSource('/trace-lens/logs/stream');

eventSource.addEventListener('connected', (event) => {
  console.log('Connected:', event.data);
});

eventSource.addEventListener('log', (event) => {
  console.log('Log:', event.data);
  // UI에 로그 표시
  document.getElementById('logs').innerHTML += event.data + '<br>';
});

eventSource.addEventListener('heartbeat', (event) => {
  // 연결 유지를 위한 하트비트
});

eventSource.onerror = (error) => {
  console.error('SSE Error:', error);
  eventSource.close();
};
```

### React 컴포넌트 예제

```jsx
import React, { useEffect, useState } from 'react';

function LogViewer() {
  const [logs, setLogs] = useState([]);

  useEffect(() => {
    const eventSource = new EventSource('/trace-lens/logs/stream');

    eventSource.addEventListener('log', (event) => {
      setLogs(prev => [...prev, event.data]);
    });

    return () => {
      eventSource.close();
    };
  }, []);

  return (
    <div>
      <h2>실시간 로그</h2>
      <div style={{
        fontFamily: 'monospace',
        backgroundColor: '#1e1e1e',
        color: '#d4d4d4',
        padding: '10px',
        maxHeight: '500px',
        overflow: 'auto'
      }}>
        {logs.map((log, index) => (
          <div key={index}>{log}</div>
        ))}
      </div>
    </div>
  );
}

export default LogViewer;
```

### 커스텀 세션 헤더 사용

만약 애플리케이션에서 커스텀 헤더를 통해 세션을 관리한다면:

```yaml
trace-lens:
  session-header-name: X-Request-ID
```

클라이언트에서 요청 시 해당 헤더를 포함:

```javascript
fetch('/api/send-email', {
  headers: {
    'X-Request-ID': 'unique-session-id-123'
  }
});
```

## API 엔드포인트

### GET /trace-lens/logs/stream

**설명**: SSE를 통한 실시간 로그 스트리밍

**응답 타입**: `text/event-stream`

**이벤트 타입**:
- `connected`: 연결 성공
- `log`: 새로운 로그 항목
- `heartbeat`: 연결 유지용 하트비트

### GET /trace-lens/logs/snapshot

**설명**: 현재 버퍼된 모든 로그 조회

**응답 예시**:
```json
{
  "sessionId": "ABC123",
  "count": 15,
  "logs": [
    "[2025-10-31T12:34:56.789Z] [INFO] [http-nio-8080-exec-1] com.example.MailService - Sending email to user@example.com",
    "[2025-10-31T12:34:57.123Z] [ERROR] [http-nio-8080-exec-1] com.example.MailService - Failed to send email: Connection timeout"
  ]
}
```

### GET /trace-lens/logs/health

**설명**: TraceLens 상태 확인

**응답 예시**:
```json
{
  "status": "UP",
  "activeSessionBuffers": 5
}
```

## 보안 고려사항

### 주의: 프로덕션 환경에서의 사용

TraceLens는 사용자가 자신의 로그를 볼 수 있게 하는 도구입니다. 프로덕션 환경에서 사용 시 다음 사항을 반드시 고려하세요:

1. **접근 제어**: 로그 엔드포인트에 대한 적절한 인증/인가 구현
2. **민감 정보**: 로그에 비밀번호, API 키 등 민감 정보가 포함되지 않도록 주의
3. **필터링**: 필요에 따라 특정 로그 레벨만 캡처하도록 설정

### Spring Security와 함께 사용

```kotlin
@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/trace-lens/**").authenticated()
                    .anyRequest().permitAll()
            }
        return http.build()
    }
}
```

## 작동 원리

1. **SessionTrackingFilter**: 모든 HTTP 요청을 인터셉트하여 세션 ID를 추출하고 SLF4J MDC에 저장
2. **SessionAwareAppender**: Logback 커스텀 appender가 MDC의 세션 ID를 확인하고 해당 세션의 버퍼에 로그 저장
3. **LogBufferService**: 세션별로 로그를 메모리에 버퍼링하고 자동 정리
4. **LogStreamController**: SSE 엔드포인트를 제공하여 클라이언트에게 실시간으로 로그 전송

## 성능 고려사항

- **메모리 사용**: 각 세션당 최대 `max-buffer-size` 개의 로그 항목 보관
- **자동 정리**: `buffer-ttl-minutes` 이후 비활성 세션 버퍼 자동 제거
- **동시성**: ConcurrentHashMap과 ConcurrentLinkedQueue 사용으로 스레드 안전성 보장

## 트러블슈팅

### 로그가 보이지 않을 때

1. 세션이 올바르게 설정되어 있는지 확인
2. `trace-lens.enabled=true` 설정 확인
3. URL 패턴이 `include-patterns`에 포함되고 `exclude-patterns`에서 제외되는지 확인

### SSE 연결이 자주 끊길 때

`sse-timeout-millis` 값을 증가시키세요:

```yaml
trace-lens:
  sse-timeout-millis: 7200000  # 2시간
```

## 라이선스

MIT License

## 기여

Issues와 Pull Requests를 환영합니다!

## 개발 환경

- Kotlin 2.1.0
- Spring Boot 3.4.0
- Java 17+
- Gradle 8.x

## 빌드

```bash
./gradlew build
```

## 로컬 퍼블리시

```bash
./gradlew publishToMavenLocal
```
