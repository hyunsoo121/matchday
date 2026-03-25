# MatchDay

모든 스포츠 경기 일정과 결과를 한 곳에서 확인하는 웹 서비스 (한국 사용자 타겟)

> 오늘 어떤 경기가 있고, 결과가 어떻게 됐는지 빠르게 파악

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| Backend | Java 21, Spring Boot 3.4.x, Gradle (Groovy DSL) |
| Frontend | React 18, Vite |
| Database | PostgreSQL, Redis |
| Infra | AWS (EC2, RDS, ElastiCache, S3, CloudWatch) |
| CI/CD | GitHub Actions → ECR → EC2 |
| 코드 품질 | Spotless (Google Java Format), EditorConfig |

---

## 지원 종목 및 데이터 소스

| 종목 | 데이터 소스 |
|------|------------|
| 축구 (EPL, 라리가, 세리에A, 분데스리가, 리그1, UCL) | ESPN API |
| NBA | ESPN API |
| MLB | MLB Stats API |
| F1 | OpenF1 API |
| LoL (LCK, LPL, Worlds, MSI, First Stand) | Riot eSports API |
| 발로란트 (VCT) | Riot eSports API |
| K리그 1 / K리그 2 | K리그 공식 사이트 API |

---

## 패키지 구조

```
com.heksis.matchday/
├── collector/
│   ├── espn/          # ESPN Collector (축구, NBA)
│   ├── mlb/           # MLB Collector
│   ├── openf1/        # F1 Collector
│   ├── riot/          # Riot Collector (LoL, 발로란트)
│   └── kleague/       # K리그 Collector
├── sport/
├── league/
├── team/
├── match/
├── user/
└── global/            # ApiResponse, Exception, Config
```

---

## DB 설계 원칙

- `SPORT` 테이블이 확장 핵심 — row insert만으로 새 종목 추가
- `MATCH.score_detail`은 JSONB — 종목별 스코어 형태 자유 저장
- `MATCH_TEAM`으로 경기-팀 관계 분리 — F1처럼 홈/어웨이 없는 종목 대응
- `SPORT.category`로 UI 그룹핑 (`sports` / `esports` / `motorsports`)

---

## 로컬 실행

**1. 환경 변수 설정**
```bash
cp .env.example .env
```

**2. DB / Redis 실행**
```bash
docker-compose up -d
```

**3. 서버 실행**
```bash
./gradlew bootRun
```

서버: `http://localhost:8080`

---

## API

### 공개 API

| Method | URL | 설명 |
|--------|-----|------|
| GET | `/api/v1/matches` | 경기 목록 (`date`, `sportId`, `leagueId`, `status` 필터) |
| GET | `/api/v1/matches/today` | 오늘 경기 |
| GET | `/api/v1/matches/live` | 라이브 경기 |
| GET | `/api/v1/matches/{id}` | 경기 단건 조회 |
| GET | `/api/v1/sports` | 종목 목록 |
| GET | `/api/v1/sports/{id}/leagues` | 리그 목록 |
| GET | `/api/v1/teams/search` | 팀 검색 |
| GET | `/api/v1/teams/{id}` | 팀 단건 조회 |

### 인증 API (예정)

| Method | URL | 설명 |
|--------|-----|------|
| POST | `/api/v1/auth/oauth/{provider}` | OAuth 로그인 (kakao, google) |
| POST | `/api/v1/auth/refresh` | 토큰 갱신 |
| GET | `/api/v1/favorites` | 즐겨찾기 목록 |
| POST | `/api/v1/favorites` | 즐겨찾기 추가 |
| DELETE | `/api/v1/favorites/{id}` | 즐겨찾기 삭제 |

### Admin API (수동 sync)

| Method | URL | 설명 |
|--------|-----|------|
| POST | `/api/v1/admin/collector/espn/sync` | ESPN 수동 sync |
| POST | `/api/v1/admin/collector/mlb/sync` | MLB 수동 sync |
| POST | `/api/v1/admin/collector/f1/sync` | F1 수동 sync |
| POST | `/api/v1/admin/collector/riot/sync` | Riot 수동 sync |
| POST | `/api/v1/admin/collector/kleague/sync` | K리그 수동 sync |

> `from`, `to` 파라미터로 날짜 범위 지정 가능 (기본값: 어제 ~ +30일)

---

## Sync 전략

| 주기 | 범위 | 목적 |
|------|------|------|
| 매 5분 | 오늘 | LIVE / FINISHED 상태 업데이트 |
| 매일 새벽 | 어제 ~ +30일 | 근미래 일정 갱신 및 결과 보정 |
| 매주 일요일 | 오늘 ~ 연말 | 새로 공개된 전체 일정 등록 |

> 모든 시간은 **UTC 기준으로 저장**, API 응답은 `Instant` (UTC) 반환

---

## 진행 상황

- [x] 기획, 기술스택, DB 스키마, API 명세
- [x] 개발 환경 설정 (Spotless, pre-commit, Docker Compose, Flyway)
- [x] 도메인 레이어 (Sport, League, Team, Match, User)
- [x] 글로벌 레이어 (ApiResponse, ErrorCode, GlobalExceptionHandler)
- [x] ESPN Collector (축구 7개 리그, NBA)
- [x] MLB Collector
- [x] F1 Collector
- [x] Riot Collector (LoL, 발로란트)
- [x] K리그 Collector (K리그 1, 2)
- [x] 타임존 처리 (UTC 저장, KST 기준 날짜 조회)
- [ ] Auth (OAuth — 카카오, 구글) + JWT
- [ ] User / Favorite Service & Controller
- [ ] Frontend (React + Vite)
- [ ] KBO Collector (크롤링)
