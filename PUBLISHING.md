# Maven Central 배포 가이드

이 문서는 TraceLens 라이브러리를 Maven Central에 배포하는 방법을 설명합니다.

## 사전 준비

### 1. Central Portal 계정 생성

1. [Central Portal](https://central.sonatype.com/)에 접속합니다
2. "Sign In" 버튼을 클릭합니다
3. 다음 중 하나의 방법으로 가입합니다:
   - **Google 계정** (권장)
   - **GitHub 계정** (권장) - GitHub 기반 네임스페이스 자동 검증
   - 이메일/패스워드 직접 생성

**중요**: 유효한 이메일 주소를 입력해야 합니다. 계정 확인 및 지원 관련 소통에 사용됩니다.

### 2. 네임스페이스 등록 및 검증

네임스페이스는 Maven Central에서 게시자를 식별하는 고유 식별자입니다.

#### 옵션 A: GitHub 기반 네임스페이스 (가장 간단)

GitHub 계정으로 가입한 경우:
1. `io.github.<사용자명>` 형태의 네임스페이스가 자동으로 검증됩니다
2. 예: GitHub 사용자명이 `beoks`라면 `io.github.beoks` 사용 가능

**이 프로젝트의 경우**: `io.github.beoks` 네임스페이스를 권장합니다.

수동 검증이 필요한 경우:
1. Portal의 "View Namespaces"에서 네임스페이스 추가
2. 시스템이 제공하는 검증 키를 복사
3. 공개 GitHub 저장소 생성 (예: `github.com/beoks/<검증키>`)
4. 저장소명이 검증 키와 정확히 일치해야 함
5. 검증 완료 후 저장소 삭제 가능

#### 옵션 B: 도메인 기반 네임스페이스

자체 도메인이 있는 경우:
1. Java 패키지 명명 규칙에 따라 역순 도메인명 사용
   - 예: `tracelens.com` 소유 시 `com.tracelens` 사용 가능
2. Portal에서 네임스페이스 추가 시 검증 키를 받음
3. DNS 설정에서 TXT 레코드 추가:
   ```
   Type: TXT
   Host: @ (또는 example.com)
   Value: <시스템이 제공한 검증 키>
   ```
4. 몇 분 내에 자동 검증 완료

### 3. GPG 키 생성 및 배포

#### GPG 키 생성
```bash
# GPG 설치 확인
gpg --version

# 새 키 생성
gpg --gen-key
# 또는 더 많은 옵션을 원하면
gpg --full-gen-key
```

키 생성 시:
- Real name: 개발자 이름
- Email: GitHub/Portal에 등록된 이메일
- Passphrase: 안전한 비밀번호 설정

#### GPG 키 ID 확인
```bash
gpg --list-secret-keys --keyid-format=long

# 출력 예시:
# sec   rsa3072/ABCD1234EFGH5678 2024-01-01 [SC]
# uid                 [ultimate] Your Name <your.email@example.com>
# ssb   rsa3072/XXXXXXXXXXXX 2024-01-01 [E]
```

위 예시에서 `ABCD1234EFGH5678`의 마지막 8자리 `EFGH5678`가 Key ID입니다.

#### 공개 키를 키 서버에 배포
```bash
gpg --keyserver keyserver.ubuntu.com --send-keys EFGH5678
gpg --keyserver keys.openpgp.org --send-keys EFGH5678
```

#### 비밀 키 내보내기 (Gradle에서 사용)
```bash
# 레거시 형식으로 내보내기
gpg --export-secret-keys -o ~/.gnupg/secring.gpg
```

### 4. User Token 생성

1. [Central Portal](https://central.sonatype.com/)에 로그인
2. 우측 상단 사용자 메뉴에서 "View Account" 클릭
3. "Generate User Token" 버튼 클릭
4. Username과 Password를 안전하게 보관

### 5. gradle.properties 파일 설정

프로젝트 루트에 `gradle.properties` 파일을 생성합니다:

```properties
# Central Portal User Token (Account 페이지에서 생성)
centralPortalUsername=your-token-username
centralPortalPassword=your-token-password

# GPG 서명 설정
signing.keyId=EFGH5678
signing.password=your-gpg-passphrase
signing.secretKeyRingFile=/Users/yourusername/.gnupg/secring.gpg
```

또는 환경 변수로 설정:
```bash
export CENTRAL_PORTAL_USERNAME=your-token-username
export CENTRAL_PORTAL_PASSWORD=your-token-password
export ORG_GRADLE_PROJECT_signing.keyId=EFGH5678
export ORG_GRADLE_PROJECT_signing.password=your-gpg-passphrase
export ORG_GRADLE_PROJECT_signing.secretKeyRingFile=/Users/yourusername/.gnupg/secring.gpg
```

### 6. Group ID 설정

`build.gradle.kts`에서 검증된 네임스페이스와 일치하는 group ID를 설정합니다:

#### GitHub 기반 네임스페이스 사용 (권장)
```kotlin
group = "io.github.beoks"
version = "1.0.0"
```

#### 도메인 기반 네임스페이스 사용
```kotlin
group = "com.tracelens"  // tracelens.com 도메인 소유 필요
version = "1.0.0"
```

## 배포 프로세스

### 1. 버전 업데이트

릴리스 버전으로 배포하기 전에 `build.gradle.kts`에서 버전을 업데이트합니다:

```kotlin
version = "1.0.0"  // SNAPSHOT 제거
```

### 2. 빌드 및 검증

```bash
# 프로젝트 빌드
./gradlew clean build

# 배포 아티팩트 생성 (서명 포함)
./gradlew publishToMavenLocal

# 생성된 아티팩트 확인 (GitHub 네임스페이스 기준)
ls -la ~/.m2/repository/io/github/beoks/trace-lens/1.0.0/
```

다음 파일들이 생성되어야 합니다:
- `trace-lens-1.0.0.jar` (메인 JAR)
- `trace-lens-1.0.0.jar.asc` (서명)
- `trace-lens-1.0.0-sources.jar` (소스 코드)
- `trace-lens-1.0.0-sources.jar.asc` (서명)
- `trace-lens-1.0.0-javadoc.jar` (문서)
- `trace-lens-1.0.0-javadoc.jar.asc` (서명)
- `trace-lens-1.0.0.pom` (POM)
- `trace-lens-1.0.0.pom.asc` (서명)

### 3. Maven Central에 배포

#### 방법 A: Portal API를 통한 배포 (권장)

1. **번들 생성**
```bash
# 모든 아티팩트를 하나의 ZIP으로 묶기
cd ~/.m2/repository/io/github/beoks/trace-lens/1.0.0/
zip -r trace-lens-1.0.0-bundle.zip *
```

2. **Central Portal에 업로드**
```bash
# User Token을 Base64로 인코딩
TOKEN=$(echo -n "your-username:your-password" | base64)

# 번들 업로드 (자동 게시)
curl -X POST \
  https://central.sonatype.com/api/v1/publisher/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "bundle=@trace-lens-1.0.0-bundle.zip" \
  -F "publishingType=AUTOMATIC"

# 또는 수동 게시
curl -X POST \
  https://central.sonatype.com/api/v1/publisher/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "bundle=@trace-lens-1.0.0-bundle.zip"
```

3. **배포 상태 확인**
```bash
# 응답에서 받은 deployment ID 사용
curl -X POST \
  "https://central.sonatype.com/api/v1/publisher/status?id=<deployment-id>" \
  -H "Authorization: Bearer $TOKEN"
```

배포 상태:
- `PENDING`: 대기 중
- `VALIDATING`: 검증 진행 중
- `VALIDATED`: 검증 완료 (수동 게시 필요)
- `PUBLISHING`: 게시 진행 중
- `PUBLISHED`: Maven Central에 업로드 완료
- `FAILED`: 오류 발생

4. **수동 게시 (publishingType=USER_MANAGED인 경우)**
```bash
curl -X POST \
  "https://central.sonatype.com/api/v1/publisher/deployment/<deployment-id>" \
  -H "Authorization: Bearer $TOKEN"
```

#### 방법 B: Gradle publish 태스크 사용 (레거시)

현재 `build.gradle.kts`는 레거시 OSSRH 방식을 사용합니다:

```bash
# Staging 저장소에 배포
./gradlew publish
```

**참고**: Central Portal로 완전히 마이그레이션하려면 위의 Portal API 방식을 사용하는 것이 좋습니다.

### 4. 배포 확인

1. 배포 완료 후 2-4시간 내에 Maven Central에서 사용 가능
2. 24시간 후 [Maven Central Search](https://search.maven.org/)에서 검색 가능
3. 자신의 라이브러리 확인:
   - `https://central.sonatype.com/artifact/io.github.beoks/trace-lens`
   - `https://search.maven.org/artifact/io.github.beoks/trace-lens`

### 5. SNAPSHOT 배포 (선택사항)

개발 중인 버전을 SNAPSHOT으로 배포:

```kotlin
version = "1.0.1-SNAPSHOT"
```

```bash
./gradlew publish
```

SNAPSHOT 버전은:
- 자동으로 승인됨
- `https://s01.oss.sonatype.org/content/repositories/snapshots/`에서 즉시 사용 가능
- Maven Central Search에는 표시되지 않음

## 사용자를 위한 의존성 추가 가이드

Maven Central에 배포된 후, 사용자들은 다음과 같이 의존성을 추가할 수 있습니다:

### Gradle (Kotlin DSL)
```kotlin
dependencies {
    implementation("io.github.beoks:trace-lens:1.0.0")
}
```

### Gradle (Groovy)
```groovy
dependencies {
    implementation 'io.github.beoks:trace-lens:1.0.0'
}
```

### Maven
```xml
<dependency>
    <groupId>io.github.beoks</groupId>
    <artifactId>trace-lens</artifactId>
    <version>1.0.0</version>
</dependency>
```

### SNAPSHOT 사용 (개발 버전)

#### Gradle
```kotlin
repositories {
    mavenCentral()
    maven {
        url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
}

dependencies {
    implementation("io.github.beoks:trace-lens:1.0.1-SNAPSHOT")
}
```

#### Maven
```xml
<repositories>
    <repository>
        <id>ossrh-snapshots</id>
        <url>https://s01.oss.sonatype.org/content/repositories/snapshots/</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>

<dependency>
    <groupId>io.github.beoks</groupId>
    <artifactId>trace-lens</artifactId>
    <version>1.0.1-SNAPSHOT</version>
</dependency>
```

## 자동화 (CI/CD)

GitHub Actions를 사용한 자동 배포 예시:

```yaml
name: Publish to Maven Central

on:
  release:
    types: [created]

jobs:
  publish:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Import GPG key
        run: |
          echo "${{ secrets.GPG_PRIVATE_KEY }}" | base64 --decode | gpg --import
          gpg --export-secret-keys -o $HOME/.gnupg/secring.gpg

      - name: Build and Publish
        env:
          CENTRAL_PORTAL_USERNAME: ${{ secrets.CENTRAL_PORTAL_USERNAME }}
          CENTRAL_PORTAL_PASSWORD: ${{ secrets.CENTRAL_PORTAL_PASSWORD }}
          ORG_GRADLE_PROJECT_signing.keyId: ${{ secrets.GPG_KEY_ID }}
          ORG_GRADLE_PROJECT_signing.password: ${{ secrets.GPG_PASSPHRASE }}
          ORG_GRADLE_PROJECT_signing.secretKeyRingFile: ${{ env.HOME }}/.gnupg/secring.gpg
        run: |
          ./gradlew build publishToMavenLocal

          # 번들 생성
          cd $HOME/.m2/repository/io/github/beoks/trace-lens/${{ github.ref_name }}/
          zip -r bundle.zip *

          # Portal API로 업로드
          TOKEN=$(echo -n "$CENTRAL_PORTAL_USERNAME:$CENTRAL_PORTAL_PASSWORD" | base64)
          curl -X POST \
            https://central.sonatype.com/api/v1/publisher/upload \
            -H "Authorization: Bearer $TOKEN" \
            -F "bundle=@bundle.zip" \
            -F "publishingType=AUTOMATIC"
```

GitHub Secrets에 추가할 내용:
- `CENTRAL_PORTAL_USERNAME`: Central Portal User Token의 Username
- `CENTRAL_PORTAL_PASSWORD`: Central Portal User Token의 Password
- `GPG_PRIVATE_KEY`: `gpg --armor --export-secret-keys YOUR_KEY_ID | base64`
- `GPG_KEY_ID`: GPG Key ID (8자리)
- `GPG_PASSPHRASE`: GPG 키 비밀번호

## 커뮤니티 플러그인 (선택사항)

공식 Gradle 플러그인은 아직 없지만, 커뮤니티 플러그인들을 사용할 수 있습니다:

### 1. vanniktech/gradle-maven-publish-plugin
```kotlin
plugins {
    id("com.vanniktech.maven.publish") version "0.28.0"
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
}
```

### 2. GradleUp/nmcp
```kotlin
plugins {
    id("com.gradleup.nmcp") version "0.0.4"
}

nmcp {
    publishAllProjectsProbablyBreakingProjectIsolation {
        username = project.findProperty("centralPortalUsername") as String?
        password = project.findProperty("centralPortalPassword") as String?
        publicationType = "AUTOMATIC"
    }
}
```

**주의**: 커뮤니티 플러그인은 Sonatype의 공식 지원을 받지 않습니다.

## 문제 해결

### 서명 실패
- GPG 키가 올바르게 설정되었는지 확인
- `signing.secretKeyRingFile` 경로가 올바른지 확인
- GPG 버전 확인 (2.1 이상에서는 추가 설정 필요할 수 있음)

### 인증 실패
- Central Portal User Token이 올바른지 확인
- Token 생성 후 Username/Password를 정확히 복사했는지 확인

### 네임스페이스 검증 실패
- GitHub 저장소명이 검증 키와 정확히 일치하는지 확인
- DNS TXT 레코드가 올바르게 설정되었는지 확인
- 네임스페이스와 group ID가 일치하는지 확인

### 배포 실패
- 모든 필수 아티팩트(JAR, sources, javadoc, POM) 및 서명 파일이 있는지 확인
- POM 메타데이터(name, description, url, license, developers, scm)가 모두 있는지 확인
- group ID가 검증된 네임스페이스로 시작하는지 확인

## 참고 자료

- [Central Portal 문서](https://central.sonatype.org/)
- [Central Portal 등록](https://central.sonatype.org/register/central-portal/)
- [네임스페이스 검증](https://central.sonatype.org/register/namespace/)
- [Portal API 문서](https://central.sonatype.org/publish/publish-portal-api/)
- [Gradle Signing Plugin](https://docs.gradle.org/current/userguide/signing_plugin.html)
- [Gradle Maven Publish Plugin](https://docs.gradle.org/current/userguide/publishing_maven.html)
