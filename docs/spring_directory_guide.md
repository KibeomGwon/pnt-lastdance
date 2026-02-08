# 📁 Spring 디렉토리 구조 가이드

본 문서는 `도메인(예: order)` 단위로 패키지를 구성하는 **레이어드
아키텍처 기반 구조**를 설명합니다.\
컨트롤러 - 서비스 - 엔티티 - 레포지토리의 책임을 명확히 분리하여
유지보수성과 확장성을 높이는 것을 목표로 합니다.

------------------------------------------------------------------------

## 📂 전체 구조 예시

    src/main/java/com/example/project
    └── order
        ├── api
        │   ├── OrderController.java
        │   ├── req
        │   │   └── OrderCreateRequest.java
        │   └── resp
        │       └── OrderResponse.java
        │
        ├── application
        │   └── OrderService.java
        │
        ├── entity
        │   └── Order.java
        │
        └── repository
            └── OrderRepository.java

------------------------------------------------------------------------

## 📦 패키지별 역할

### 1. api (Presentation Layer)

외부 요청(Request)과 응답(Response)을 처리하는 계층입니다.

-   HTTP 요청을 받고 Service에 위임\
-   비즈니스 로직을 직접 처리하지 않음\
-   DTO를 통해 데이터 전달

구성 요소: - OrderController.java : API 엔드포인트 정의\
- req/: Request DTO 모음\
- resp/: Response DTO 모음

------------------------------------------------------------------------

### 2. application (Service Layer)

비즈니스 로직을 처리하는 계층입니다.

-   트랜잭션 처리\
-   도메인 객체(Entity) 조작\
-   Repository와 협력\
-   Controller와 Domain 사이의 흐름 제어 담당

------------------------------------------------------------------------

### 3. entity (Domain Layer)

핵심 도메인 모델을 담당합니다.

-   테이블과 매핑되는 객체\
-   비즈니스 규칙 일부 포함 가능\
-   JPA Entity 포함

------------------------------------------------------------------------

### 4. repository (Persistence Layer)

데이터베이스 접근 계층입니다.

-   DB와 직접 통신\
-   JPA Repository 인터페이스 정의

------------------------------------------------------------------------



## 📌 새로운 도메인 추가 예시

예: member 도메인 추가 시

    com/example/project
    ├── order
    └── member
        ├── api
        ├── application
        ├── entity
        └── repository

→ 구조 일관성 유지 

------------------------------------------------------------------------

## ✍️ 권장 규칙

-   Controller는 최대한 얇게 유지\
-   Service에서 핵심 흐름 제어\
-   Entity는 단순 데이터 객체가 아니라 도메인 의미를 갖도록 설계\
-   DTO(req/resp)는 api 계층 내부에서만 사용
