# 🏃‍♂️ 경찰과 도둑 : 라스트댄스 (Police & Thief : Last Dance)

> **현실 기반 실시간 위치 추적 서바이벌 액션 게임**
> **Real-world GPS Survival Action Game powered by Pixel Art & AI**

## 📖 프로젝트 소개 (Project Overview)

**'경찰과 도둑 : 라스트댄스'**는 사용자의 실시간 위치 데이터를 활용하여 도심 속에서 추격전을 벌이는 **위치 기반 액션 게임**입니다.
단순한 술래잡기를 넘어, **객체 인식 AI(Vision)**와 **생성형 AI(LLM)** 기술을 접목하여 몰입감을 극대화했습니다. 최대 30명의 플레이어가 현실 공간을 뛰어다니며 실시간으로 소통하고 경쟁하는 새로운 차원의 경험을 제공합니다.

---

## ✨ 핵심 기능 및 차별점 (Key Features)

### 🛰️ 실시간 GPS & 커스텀 맵 (Real-time GPS & Custom Map)

* **커스텀 게임 구역:** 방장(Host)이 지도 위에 직접 핀을 찍어 다각형(Polygon) 형태의 게임 구역과 감옥 위치를 자유롭게 설정합니다.
* **초정밀 동기화:** **WebSocket**을 활용하여 1초 주기로 모든 플레이어의 좌표를 동기화하고, 맵 경계 이탈 여부를 실시간으로 판정합니다.

### 🤖 AI 기반 미션 및 리포트 (AI Integration)

* **Vision AI (YOLOv5):** 도둑은 맵 곳곳의 미션 장소(벤치, 편의점 간판 등)를 카메라로 촬영합니다. 서버의 Vision AI가 이를 실시간으로 분석하여 미션 성공 여부를 자동 판정합니다.
* **LLM (Llama-3.1):** 게임 종료 후, 발생한 로그(최초 체포, 최장 생존, 역전승 등)를 바탕으로 생성형 AI가 "도둑 A, 경찰의 포위망을 뚫고 극적 탈출!"과 같은 **맞춤형 뉴스 기사**를 생성해줍니다.

### 👮 경찰 (Police) - 추적과 검거

* **검거 시스템:** QR 코드 스캔을 통해 도둑을 체포하고 감옥으로 이송하는 단계별 프로세스를 구현했습니다.
* **Push To Talk 무전기:** **WebRTC(LiveKit)** 기술을 활용하여, 별도의 통화 앱 없이도 경찰 팀원끼리 지연 없는 실시간 음성 작전 회의가 가능합니다.
* **특수 스킬:** CCTV(위치 노출), 헬리콥터(광역 스캔) 등을 활용하여 도둑을 압박합니다.

### 🕵️ 도둑 (Thief) - 생존과 교란

* **심박수 근접 알림:** 경찰과의 거리에 따라 휴대폰 진동 패턴과 비프음(Beep) 소리가 달라져 시각/청각적 긴장감을 제공합니다.
* **미션 수행:** 사물 인식 미션을 수행하여 CCTV 추적을 무력화하거나 점수를 획득할 수 있습니다.

---

## 🛠 기술 스택 (Tech Stack)

### Android (Client)

| Category | Technology |
| --- | --- |
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose (Material3) |
| **Architecture** | MVVM, Clean Architecture |
| **DI** | Hilt |
| **Network** | Retrofit2, OkHttp, **Socket.IO** (Real-time) |
| **Local DB** | Room, DataStore |
| **Map & Location** | Google Maps SDK, GMS Location |
| **Media & AI** | **LiveKit** (Voice Chat), CameraX |

### Backend & AI (Infrastructure)

| Category | Technology |
| --- | --- |
| **Framework** | Spring Boot 3.2.x, Node.js 20 (LTS), FastAPI (Python 3.12) |
| **Database** | PostgreSQL 17 (PostGIS 3.6), Redis 7, MongoDB 7 |
| **AI Models** | **YOLOv5** (Object Detection), **Llama-3.1-8b** (LLM) |
| **Infra** | Ubuntu 22.04 LTS, Docker Compose, Nginx |

---

## 📱 스크린샷 (Screenshots)

| 메인 화면 | 게임 대기방 | 플레이 화면 (경찰) | 플레이 화면 (도둑) |
| --- | --- | --- | --- |
| <img src="" width="200" height="400"/> | <img src="" width="200" height="400"/> | <img src="" width="200" height="400"/> | <img src="" width="200" height="400"/> |

| 무전기 기능 | AI 미션 수행 | 근접 알림 | AI 뉴스 결과 |
| --- | --- | --- | --- |
| <img src="" width="200" height="400"/> | <img src="" width="200" height="400"/> | <img src="" width="200" height="400"/> | <img src="" width="200" height="400"/> |

*(스크린샷 이미지를 `assets` 폴더 등에 업로드 후 경로를 입력해주세요)*
