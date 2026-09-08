# miniauth

Spring Boot 기반 **미니 인증 플랫폼**입니다.  
회원가입 · JWT 로그인 · 역할 기반 인가 · 로그인 실패 잠금 · 관리자 API를 학습용으로 구현한 프로젝트입니다.

## 기술 스택

| 구분 | 내용 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 4.1 |
| Security | Spring Security (STATELESS) + JWT (jjwt HS256) |
| Persistence | Spring Data JPA + MySQL |
| 기타 | Validation, Lombok |

## 주요 기능

- 이메일 회원가입 (비밀번호 BCrypt 해시)
- JWT Access Token 로그인 (`Authorization: Bearer ...`)
- 현재 사용자 조회 (`/api/auth/me`)
- 역할 기반 인가 (`ROLE_USER`, `ROLE_ADMIN`)
- 로그인 실패 5회 → 15분 임시 잠금 + `login_attempts` 기록
- 관리자: 사용자 목록/조회, 잠금 해제, enable/disable, 역할 부여·회수
- 브라우저 테스트용 HTML 클라이언트 (`/test-client.html`)

## 인증 흐름

```text
[로그인]
Client → POST /api/auth/login
      → AuthenticationManager (이메일/비밀번호 + 계정 상태)
      → LoginAttempt 기록
      → JwtTokenProvider.createToken
      ← { accessToken, tokenType, user }

[API 호출]
Client → Authorization: Bearer <token>
      → JwtAuthenticationFilter
      → SecurityContext에 CustomUserDetails 설정
      → Controller
```

- Access Token 만료: **15분** (`app.jwt.expiration-ms`)
- Refresh Token / 서버측 토큰 블랙리스트는 **미구현**
- 로그아웃: 클라이언트가 토큰 삭제 (서버는 204만 응답)

## 사전 준비

1. **JDK 21+**
2. **MySQL** 데이터베이스 `mini_auth` 생성
3. `ddl-auto: validate` 이므로 테이블을 미리 만들어야 합니다.

예시 스키마:

```sql
CREATE DATABASE mini_auth CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE mini_auth;

CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    email VARCHAR(320) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    failed_login_count INT NOT NULL,
    locked_until TIMESTAMP NULL,
    last_login_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE login_attempts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(320) NOT NULL,
    user_id BIGINT NULL,
    success BOOLEAN NOT NULL,
    ip_address VARCHAR(45),
    user_agent VARCHAR(512),
    failure_reason VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

INSERT INTO roles (code, name, description, created_at) VALUES
('ROLE_USER', '일반 사용자', '기본 사용자 권한', UTC_TIMESTAMP()),
('ROLE_ADMIN', '관리자', '관리자 권한', UTC_TIMESTAMP());
```

관리자 권한 부여 예:

```sql
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'your@email.com' AND r.code = 'ROLE_ADMIN';
```

역할을 바꾼 뒤에는 **다시 로그인**해야 JWT claim에 반영됩니다.

## 설정

[`src/main/resources/application.yaml`](src/main/resources/application.yaml)

| 키 | 설명 |
|----|------|
| `spring.datasource.*` | MySQL 접속 정보 |
| `app.jwt.secret` | HS256 시크릿 (`JWT_SECRET` 환경변수 권장, 최소 32바이트) |
| `app.jwt.expiration-ms` | Access Token 만료(ms). 기본 `900000` (15분) |

로컬에서 시크릿을 바꿀 때:

```bash
# Windows PowerShell 예
$env:JWT_SECRET="your-long-random-secret-at-least-32-bytes"
```

> DB 비밀번호 등 민감 정보는 저장소에 직접 두지 말고 환경변수로 분리하는 것을 권장합니다.

## 실행

```bash
./gradlew bootRun
# Windows
.\gradlew.bat bootRun
```

- API 기본 포트: `http://localhost:8080`
- 테스트 클라이언트: [http://localhost:8080/test-client.html](http://localhost:8080/test-client.html)

## API

### 인증 (`/api/auth`)

| Method | Path | Auth | 설명 |
|--------|------|------|------|
| `POST` | `/api/auth/signup` | 없음 | 회원가입 |
| `POST` | `/api/auth/login` | 없음 | 로그인 → JWT 발급 |
| `GET` | `/api/auth/me` | Bearer | 현재 사용자 |
| `POST` | `/api/auth/logout` | 없음 | 204 (클라이언트 토큰 삭제용) |

**회원가입 요청**

```json
{
  "email": "user@example.com",
  "password": "password123",
  "displayName": "테스터"
}
```

- password: 8~64자
- displayName: 2~100자

**로그인 응답**

```json
{
  "accessToken": "<jwt>",
  "tokenType": "Bearer",
  "user": {
    "publicId": "...",
    "email": "user@example.com",
    "displayName": "테스터",
    "roles": ["ROLE_USER"]
  }
}
```

이후 요청 헤더:

```http
Authorization: Bearer <accessToken>
```

### 관리자 (`/api/admin/users`) — `ROLE_ADMIN` 필요

| Method | Path | 설명 |
|--------|------|------|
| `GET` | `/api/admin/users` | 사용자 목록 (페이징) |
| `GET` | `/api/admin/users/{publicId}` | 단건 조회 |
| `POST` | `/api/admin/users/{publicId}/unlock` | 잠금 해제 |
| `POST` | `/api/admin/users/{publicId}/enable` | 활성화 |
| `POST` | `/api/admin/users/{publicId}/disable` | 비활성화 |
| `POST` | `/api/admin/users/{publicId}/roles` | 역할 부여 `{"roleCode":"ROLE_ADMIN"}` |
| `DELETE` | `/api/admin/users/{publicId}/roles/{roleCode}` | 역할 회수 |

## 패키지 구조

```text
com.example.miniauth
├── config          # SecurityConfig, JwtProperties
├── controller      # Auth / Admin
├── domain          # User, Role, LoginAttempt
├── dto
├── exception       # GlobalExceptionHandler
├── repository
├── security
│   └── jwt         # JwtTokenProvider, JwtAuthenticationFilter
└── service
```

## 학습용으로 보면 좋은 파일

1. `JwtTokenProvider` — 토큰 발급·검증
2. `JwtAuthenticationFilter` — Bearer → SecurityContext
3. `SecurityConfig` — STATELESS + 인가 규칙
4. `AuthService` — 로그인 + LoginAttempt
5. `CustomUserDetails` — DB 로드 / JWT claim 재구성

## 현재 한계 / 다음 후보

- Refresh Token 없음 (Access 만료 시 재로그인)
- 서버측 토큰 폐기(블랙리스트) 없음
- 비밀번호 변경 API 없음
- 이메일 인증(`PENDING`) 미사용
- 통합 테스트 거의 없음
- CSRF는 Bearer API 기준으로 비활성화

다음에 넣기 좋은 순서: **Refresh Token → 비밀번호 변경 → MockMvc 통합 테스트**.

## 라이선스

학습/개인 프로젝트용입니다.
