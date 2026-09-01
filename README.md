# Groupeat Backend

<div align="center">

### 행사 주체자와 음식점 사장님을 위한 단체주문 매칭 서비스 Groupeat

<br />

<img
  width="520"
  alt="Groupeat 서비스 포스터"
  src="https://github.com/user-attachments/assets/7005fcbd-cde1-4a0d-b54f-0090e2200730"
/>
-->

<br />

고객의 메뉴 탐색, 장바구니, 주문, 결제, 픽업 알림과<br />
점주의 가게 운영, 주문 승인, 리뷰 관리를 위한 API를 제공합니다.

<br />

[**서비스 바로가기 ->**](https://www.groupeat.co.kr/login)

</div>

<br />

## 서비스 소개

Groupeat은 모임, 행사, 회의처럼 여러 사람이 함께 먹을 음식을 더 쉽게 준비할 수 있도록 돕는 단체 주문 기반 푸드 픽업 서비스입니다.

사용자는 주변 가게의 메뉴와 픽업 가능 시간을 확인하고 원하는 메뉴를 미리 주문할 수 있습니다. 점주는 단체 주문을 효율적으로 접수하고 승인하며, 픽업 전까지 필요한 안내를 제공해 주문 준비 과정을 편리하게 운영할 수 있습니다.

<br />

## 주요 기능

- **소셜 로그인 및 JWT 인증** - Google, Kakao, Naver OAuth2 로그인과 Access/Refresh Token 재발급, 로그아웃을 처리합니다.
- **회원가입 및 사용자 관리** - 고객/사업자 회원가입, 마이페이지, 알림 설정, 약관 동의, 회원 탈퇴 기능을 제공합니다.
- **사업자 검증** - 국세청 API 기반 사업자 진위 확인과 관리자 승인 프로세스를 지원합니다.
- **가게 및 메뉴 관리** - 점주의 가게 정보, 영업/주문 가능 일정, 메뉴, 옵션 그룹, 옵션을 관리합니다.
- **검색과 추천** - 매장 검색, 평점 기반 추천, 할인율 기반 추천 API를 제공합니다.
- **장바구니와 주문** - 메뉴 옵션을 포함한 장바구니 계산, 주문 생성, 주문 목록/상세 조회, 취소 흐름을 제공합니다.
- **점주 주문 처리** - 신규 주문 조회, 주문 승인/거절, 픽업 완료 처리와 자동 거절 스케줄러를 지원합니다.
- **결제 및 정산 기반** - Toss Payments 결제 승인/취소 흐름과 정산 수수료 계산 기반을 포함합니다.
- **리뷰와 답글** - 고객 리뷰 작성/삭제/조회와 점주 답글, 리뷰 요약 정보를 제공합니다.
- **푸시 알림** - Firebase FCM 토큰 등록, 알림 목록/읽음 처리, RabbitMQ 기반 비동기 알림 발송을 처리합니다.
- **파일 업로드** - AWS S3 Presigned URL 기반 이미지 및 사업자 서류 업로드를 지원합니다.
- **공통 API 응답과 예외 처리** - 도메인별 에러 코드와 일관된 응답 포맷을 제공합니다.
- **운영 모니터링** - Actuator, Prometheus, Grafana, Loki, Grafana Alloy 기반 메트릭/로그 수집 구성을 포함합니다.

<br />

## Backend 기술 스택

<div align="center">

| Category | Technologies |
| --- | --- |
| **Language & Framework** | Java 21, Spring Boot 3.5 |
| **Database** | PostgreSQL, Spring Data JPA, QueryDSL, Flyway |
| **Security** | Spring Security, OAuth2, JWT |
| **Infra & Messaging** | Redis, RabbitMQ, Docker, GitHub Actions |
| **External Integration** | Toss Payments, NTS Business API, AWS S3, Firebase FCM |
| **API Docs** | Springdoc OpenAPI |

</div>

<br />

## 아키텍처

<!-- 아키텍처 이미지는 아래 src에 첨부 이미지 URL을 넣어 사용하세요.
<img
  width="860"
  alt="Groupeat backend architecture"
  src=""
/>
-->

```text
Client
  ├── Customer Web
  └── Owner Web
        │
        ▼
Groupeat Backend API
  ├── Auth / Signup / Terms
  ├── Store / Menu / Search / Recommendation
  ├── Cart / Order / Payment
  ├── Review / Notification
  └── Admin / Business Verification
        │
        ├── PostgreSQL / Redis
        ├── RabbitMQ -> Firebase FCM
        ├── AWS S3
        ├── Toss Payments
        └── NTS Business API

Observability
  ├── Prometheus <- Actuator
  └── Grafana Alloy -> Loki -> Grafana
```

Groupeat Backend는 고객 웹과 점주 웹의 요청을 하나의 API 서버에서 처리하며, 주문/결제/가게/알림 등 기능별 도메인으로 책임을 나눕니다.

주요 데이터는 PostgreSQL에 저장하고, Redis와 RabbitMQ는 캐시·상태 관리·비동기 알림 처리에 사용합니다. 결제, 사업자 검증, 파일 업로드, 푸시 알림은 각각 Toss Payments, NTS Business API, AWS S3, Firebase FCM과 연동합니다.

<br />

## 도메인 구조

```text
com.groupeat
├── domain
│   ├── store / cart / orders / payment
│   ├── member / owner / auth / signup / terms
│   ├── business / admin / settlement
│   ├── search / recommendation / review
│   └── notification / verification
└── global
    ├── apiPayload / exception / security
    ├── config / logging / upload
    └── entity / dto / util
```

비즈니스 기능을 기준으로 패키지를 나누는 **도메인 중심 패키지 구조**를 사용합니다. 주문, 결제, 가게, 리뷰처럼 변경 이유가 같은 코드를 하나의 도메인 안에 모으고, 각 도메인은 필요한 경우 `controller`, `service`, `repository`, `entity`, `dto`, `converter`, `exception` 계층을 내부에 둡니다.

인증 필터, 공통 응답, 예외 처리, 설정, 업로드, 로깅처럼 여러 도메인에서 함께 사용하는 횡단 관심사는 `global` 패키지로 분리합니다.
<br />

## 배포와 운영

- `main` 브랜치 push 시 GitHub Actions CD 워크플로가 실행됩니다.
- Gradle로 `bootJar`를 생성한 뒤 Docker Hub에 `groupeat-server:latest` 이미지를 push합니다.
- EC2 서버에 `docker-compose.yml`과 `monitoring/` 설정을 복사하고 `docker compose up -d`로 서비스를 갱신합니다.
- 운영 프로필에서는 PostgreSQL URL과 인증 정보는 환경 변수로 주입하며, Flyway migration을 활성화합니다.
- Prometheus는 `/actuator/prometheus`를 scrape하고, Grafana Alloy는 Docker 컨테이너 로그를 Loki로 전달합니다.

<br />


## BE 개발 참여자

<div align="center">
<table>
  <tr>
    <td align="center" width="220">
      <a href="https://github.com/boogiewooki02">
        <img src="https://github.com/boogiewooki02.png" width="100" alt="김동욱" />
        <br />
        <strong>김동욱</strong>
      </a>
      <br />
      Backend
    </td>
    <td align="center" width="220">
      <a href="https://github.com/Seungwon326">
        <img src="https://github.com/Seungwon326.png" width="100" alt="최승원" />
        <br />
        <strong>최승원</strong>
      </a>
      <br />
      Backend
    </td>
  </tr>
</table>
</div>

<br />

---

<div align="center">

**함께 먹는 시간을 더 쉽게 준비하는 단체 주문 플랫폼, Groupeat**

[Organization](https://github.com/CEOS-Groupeat) · [Frontend](https://github.com/CEOS-Groupeat/groupeat-frontend) · [Backend](https://github.com/CEOS-Groupeat/groupeat-backend)

</div>
