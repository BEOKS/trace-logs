# TraceLens - 빠른 시작 (5분 안에!)

## 🚀 샘플 애플리케이션으로 즉시 테스트

### 1. 샘플 애플리케이션 실행 (1분)

```bash
cd sample-app
./gradlew bootRun
```

애플리케이션이 시작되면 다음 메시지가 표시됩니다:
```
TraceLens is enabled
TraceLens endpoint: /trace-lens/logs/stream
Tomcat started on port 8080
```

### 2. 웹 브라우저에서 로그 뷰어 열기 (30초)

브라우저에서 다음 URL을 엽니다:
```
http://localhost:8080
```

**"연결 시작"** 버튼을 클릭하면 실시간 로그 스트리밍이 시작됩니다.

### 3. 테스트 API 호출하기 (30초)

**새 터미널을 열고** 다음 명령어를 실행합니다:

#### 간단한 테스트
```bash
curl http://localhost:8080/api/test/simple
```

#### 메일 발송 성공 케이스
```bash
curl -X POST http://localhost:8080/api/mail/send \
  -H "Content-Type: application/json" \
  -d '{"to": "user@example.com", "subject": "Test", "body": "Hello"}'
```

#### 메일 발송 실패 케이스 (SMTP 연결 실패)
```bash
curl -X POST http://localhost:8080/api/mail/send \
  -H "Content-Type: application/json" \
  -d '{"to": "fail@example.com", "subject": "Test", "body": "Hello"}'
```

#### 결제 처리
```bash
curl -X POST http://localhost:8080/api/payment/process \
  -H "Content-Type: application/json" \
  -d '{"amount": 10000, "method": "card", "cardNumber": "1234-5678-9012-3456"}'
```

#### 결제 실패 (잔액 부족)
```bash
curl -X POST http://localhost:8080/api/payment/process \
  -H "Content-Type: application/json" \
  -d '{"amount": 10000, "method": "card", "cardNumber": "0000-0000-0000-0000"}'
```

### 4. 로그 뷰어에서 실시간 로그 확인 (즉시)

로그 뷰어 탭으로 돌아가면 다음과 같은 로그가 실시간으로 표시됩니다:

```
[2025-10-31T13:15:32.123Z] [INFO] [http-nio-8080-exec-1] com.example.demo.controller.MailController - 메일 발송 요청 - 수신자: user@example.com, 제목: Test
[2025-10-31T13:15:32.124Z] [DEBUG] [http-nio-8080-exec-1] com.example.demo.controller.MailController - 이메일 주소 검증 중...
[2025-10-31T13:15:32.125Z] [INFO] [http-nio-8080-exec-1] com.example.demo.controller.MailController - 이메일 주소 검증 완료
[2025-10-31T13:15:32.126Z] [DEBUG] [http-nio-8080-exec-1] com.example.demo.controller.MailController - SMTP 서버 연결 중...
[2025-10-31T13:15:32.227Z] [INFO] [http-nio-8080-exec-1] com.example.demo.controller.MailController - SMTP 서버 연결 성공
[2025-10-31T13:15:32.428Z] [INFO] [http-nio-8080-exec-1] com.example.demo.controller.MailController - 메일 발송 성공!
```

---

## 🎯 왜 이게 유용한가?

### 기존 방식의 문제점
```
사용자: "메일이 안 갔어요!"
개발자: "서버 로그 확인해볼게요..."
개발자: (서버 SSH 접속 → 로그 파일 찾기 → grep으로 검색)
개발자: "SMTP 서버 연결 실패네요"
사용자: "그게 뭐예요?"
```

### TraceLens 사용 시
```
사용자: "메일이 안 갔어요!"
사용자: (로그 뷰어 확인)
사용자: "아, SMTP 서버 연결 실패라고 나오네요"
개발자: "네트워크 문제인 것 같습니다. IT팀에 문의해주세요"
```

---

## 📊 실제 사용 사례

### 사례 1: 메일 발송 실패 디버깅

**Before**: 사용자가 "메일이 안 간다"고 문의 → 개발자가 서버 로그 확인 → 10분 소요

**After**: 사용자가 로그 뷰어에서 직접 확인 → 즉시 원인 파악
- ✅ 잘못된 이메일 주소
- ✅ SMTP 연결 실패
- ✅ 발송 한도 초과

### 사례 2: 결제 처리 과정 추적

로그에서 전체 프로세스를 실시간으로 확인:
1. 결제 정보 검증
2. 결제 수단 확인
3. PG사 API 호출
4. 승인/거절
5. DB 저장
6. 영수증 발행

---

## 🔒 보안 고려사항

**중요**: TraceLens는 각 사용자가 **자신의 세션 로그만** 볼 수 있습니다.

```bash
# 브라우저 A (세션 ID: ABC123)에서 API 호출
curl http://localhost:8080/api/test/simple
# → 브라우저 A의 로그 뷰어에만 표시됨

# 브라우저 B (세션 ID: DEF456)에서 API 호출
curl http://localhost:8080/api/test/simple
# → 브라우저 B의 로그 뷰어에만 표시됨
```

---

## 🛠 다음 단계

### 실제 프로젝트에 적용하기

1. **의존성 추가** (build.gradle.kts)
```kotlin
dependencies {
    implementation("com.tracelens:trace-lens:1.0.0-SNAPSHOT")
}

repositories {
    mavenLocal()  // 로컬 테스트용
    // 또는 실제 Maven 저장소
}
```

2. **설정** (application.yml) - 선택사항
```yaml
trace-lens:
  enabled: true
  max-buffer-size: 1000
  buffer-ttl-minutes: 30
```

3. **끝!** - 자동으로 활성화됩니다

### 커스터마이징

#### 커스텀 세션 헤더 사용
```yaml
trace-lens:
  session-header-name: X-Request-ID
```

#### Spring Security 통합
```kotlin
http.authorizeHttpRequests { auth ->
    auth.requestMatchers("/trace-lens/**").hasRole("ADMIN")
}
```

---

## 📝 주요 엔드포인트

| 엔드포인트 | 설명 | 사용법 |
|----------|------|--------|
| `/` | 로그 뷰어 UI | 브라우저에서 열기 |
| `/trace-lens/logs/stream` | SSE 실시간 스트리밍 | 로그 뷰어가 자동 연결 |
| `/trace-lens/logs/snapshot` | 현재 버퍼된 로그 | `curl http://localhost:8080/trace-lens/logs/snapshot` |
| `/trace-lens/logs/health` | 헬스 체크 | `curl http://localhost:8080/trace-lens/logs/health` |

---

## 🐛 문제 해결

### 로그가 보이지 않을 때

1. **세션 확인**
```bash
curl -v http://localhost:8080/api/test/simple
# 응답 헤더에 Set-Cookie: JSESSIONID=... 있는지 확인
```

2. **TraceLens 상태 확인**
```bash
curl http://localhost:8080/trace-lens/logs/health
# {"status":"UP","activeSessionBuffers":0}
```

3. **애플리케이션 로그 확인**
```
TraceLens is enabled
TraceLens endpoint: /trace-lens/logs/stream
SessionAwareAppender configured and attached to root logger
```

---

## 🎉 축하합니다!

이제 TraceLens를 사용할 준비가 되었습니다!

더 자세한 내용은 다음 문서를 참고하세요:
- [README.md](README.md) - 전체 문서
- [GETTING_STARTED.md](GETTING_STARTED.md) - 상세 가이드
- [sample-app/README.md](sample-app/README.md) - 샘플 앱 문서

---

## 📞 지원

- GitHub Issues: 버그 리포트 및 기능 요청
- 문서: [README.md](README.md)
