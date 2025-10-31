# TraceLens 샘플 애플리케이션

TraceLens 라이브러리의 기능을 테스트할 수 있는 샘플 Spring Boot 애플리케이션입니다.

## 실행 방법

### 1. 애플리케이션 빌드 및 실행

```bash
cd sample-app
./gradlew bootRun
```

또는 Gradle wrapper가 없는 경우:

```bash
gradle wrapper --gradle-version=8.10.2
./gradlew bootRun
```

### 2. 웹 브라우저에서 확인

애플리케이션이 시작되면:

1. **로그 뷰어 열기**: http://localhost:8080
2. "연결 시작" 버튼 클릭
3. 새 탭에서 API 테스트 수행
4. 로그 뷰어 탭에서 실시간 로그 확인

## 테스트 API 엔드포인트

### 1. 메일 발송 시뮬레이션

```bash
# 성공 케이스
curl -X POST http://localhost:8080/api/mail/send \
  -H "Content-Type: application/json" \
  -d '{
    "to": "user@example.com",
    "subject": "테스트 메일",
    "body": "안녕하세요"
  }'

# 실패 케이스 1: 잘못된 이메일
curl -X POST http://localhost:8080/api/mail/send \
  -H "Content-Type: application/json" \
  -d '{
    "to": "invalid-email",
    "subject": "테스트 메일",
    "body": "안녕하세요"
  }'

# 실패 케이스 2: SMTP 연결 실패
curl -X POST http://localhost:8080/api/mail/send \
  -H "Content-Type: application/json" \
  -d '{
    "to": "fail@example.com",
    "subject": "테스트 메일",
    "body": "안녕하세요"
  }'

# 실패 케이스 3: 발송 한도 초과
curl -X POST http://localhost:8080/api/mail/send \
  -H "Content-Type: application/json" \
  -d '{
    "to": "user@example.com",
    "subject": "error test",
    "body": "안녕하세요"
  }'
```

### 2. 결제 처리 시뮬레이션

```bash
# 성공 케이스
curl -X POST http://localhost:8080/api/payment/process \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 10000,
    "method": "card",
    "cardNumber": "1234-5678-9012-3456"
  }'

# 실패 케이스 1: 잔액 부족
curl -X POST http://localhost:8080/api/payment/process \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 10000,
    "method": "card",
    "cardNumber": "0000-0000-0000-0000"
  }'

# 실패 케이스 2: 잘못된 금액
curl -X POST http://localhost:8080/api/payment/process \
  -H "Content-Type: application/json" \
  -d '{
    "amount": -1000,
    "method": "card"
  }'

# 실패 케이스 3: 지원하지 않는 결제 수단
curl -X POST http://localhost:8080/api/payment/process \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 10000,
    "method": "crypto"
  }'
```

### 3. 일반 테스트 API

```bash
# 간단한 테스트
curl http://localhost:8080/api/test/simple

# 에러 테스트
curl http://localhost:8080/api/test/error

# 느린 API 테스트
curl http://localhost:8080/api/test/slow

# 다중 로그 테스트
curl http://localhost:8080/api/test/multi-log
```

## TraceLens 엔드포인트

### 1. SSE 스트리밍

```
GET http://localhost:8080/trace-lens/logs/stream
```

### 2. 로그 스냅샷

```bash
curl http://localhost:8080/trace-lens/logs/snapshot
```

### 3. 헬스 체크

```bash
curl http://localhost:8080/trace-lens/logs/health
```

## 사용 시나리오

### 시나리오 1: 메일 발송 실패 원인 확인

1. 브라우저 탭 1: http://localhost:8080 열기
2. "연결 시작" 버튼 클릭
3. 브라우저 탭 2 또는 터미널에서:
   ```bash
   curl -X POST http://localhost:8080/api/mail/send \
     -H "Content-Type: application/json" \
     -d '{"to": "fail@example.com", "subject": "테스트", "body": "내용"}'
   ```
4. 탭 1에서 실시간으로 로그 확인:
   - 이메일 검증 과정
   - SMTP 연결 시도
   - 연결 실패 오류 메시지

### 시나리오 2: 결제 처리 과정 추적

1. 로그 뷰어 열기 및 연결
2. 결제 API 호출:
   ```bash
   curl -X POST http://localhost:8080/api/payment/process \
     -H "Content-Type: application/json" \
     -d '{"amount": 10000, "method": "card", "cardNumber": "1234-5678-9012-3456"}'
   ```
3. 로그에서 확인 가능한 정보:
   - 결제 정보 검증
   - 결제 수단 확인
   - PG사 API 호출
   - 승인번호 발급
   - DB 저장
   - 영수증 발행

### 시나리오 3: 다중 세션 테스트

여러 브라우저를 사용하여 서로 다른 세션의 로그가 격리되는지 확인:

1. Chrome에서 로그 뷰어 열기 (세션 A)
2. Firefox에서 로그 뷰어 열기 (세션 B)
3. 각 브라우저에서 API 호출
4. 각 브라우저가 자신의 로그만 표시하는지 확인

## 프로젝트 구조

```
sample-app/
├── src/main/kotlin/com/example/demo/
│   ├── DemoApplication.kt              # 메인 애플리케이션
│   └── controller/
│       ├── MailController.kt           # 메일 발송 시뮬레이션
│       ├── PaymentController.kt        # 결제 처리 시뮬레이션
│       └── TestController.kt           # 일반 테스트
├── src/main/resources/
│   ├── application.yml                 # 애플리케이션 설정
│   └── static/
│       └── index.html                  # 로그 뷰어 UI
└── build.gradle.kts
```

## 개발 팁

### 로그 레벨 변경

`application.yml`에서 로그 레벨 조정:

```yaml
logging:
  level:
    root: DEBUG
    com.example.demo: TRACE
```

### TraceLens 설정 변경

```yaml
trace-lens:
  enabled: true
  max-buffer-size: 2000
  buffer-ttl-minutes: 60
  poll-interval-millis: 300
```

### 커스텀 세션 헤더 사용

```yaml
trace-lens:
  session-header-name: X-Session-ID
```

API 호출 시 헤더 포함:
```bash
curl -H "X-Session-ID: my-custom-session-123" \
  http://localhost:8080/api/test/simple
```

## 문제 해결

### 로그가 보이지 않을 때

1. 애플리케이션 로그 확인:
   ```
   TraceLens is enabled
   TraceLens endpoint: /trace-lens/logs/stream
   ```

2. 세션 확인:
   ```bash
   curl -v http://localhost:8080/api/test/simple
   ```
   응답 헤더에 `Set-Cookie: JSESSIONID=...` 확인

3. TraceLens 헬스 체크:
   ```bash
   curl http://localhost:8080/trace-lens/logs/health
   ```

### 빌드 오류 발생 시

TraceLens가 로컬 Maven 저장소에 설치되어 있는지 확인:

```bash
cd ..
./gradlew publishToMavenLocal
cd sample-app
./gradlew clean build
```

## 다음 단계

- [ ] 실제 프로젝트에 TraceLens 통합
- [ ] Spring Security 추가하여 보안 테스트
- [ ] 커스텀 로그 필터 구현
- [ ] 프로덕션 환경 설정 최적화
