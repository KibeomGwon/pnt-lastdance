# 🤖 PNT 통합 AI Worker Server

이 프로젝트는 RabbitMQ를 통해 **뉴스 생성(News)** 및 **미션 이미지 분석(Mission)** 요청을 받아 처리하는 AI 워커 서버입니다.
Python 기반으로 동작하며, PM2를 사용하여 프로세스를 관리합니다.

---

## 🛠 1. 사전 요구 사항 (Prerequisites)

서버 환경에 다음 소프트웨어가 설치되어 있어야 합니다.

* **OS**: Windows / Linux (Ubuntu 권장)
* **Python**: 3.12 이상
* **Node.js & npm**: (PM2 설치용)
* **Ollama**: 로컬 LLM 구동용 (News 생성)
* **CUDA (Optional)**: GPU 가속을 사용할 경우 (NVIDIA Driver 설치 필요)

---

## 📦 2. 설치 및 환경 설정 (Installation)

### 2-1. 프로젝트 클론 및 이동
```bash
git clone <레포지토리_주소>
cd ai

지금까지 진행했던 설치 과정, 트러블슈팅, 실행 방법(PM2)을 모두 정리하여 **서버 배포용 README.md**를 작성해 드립니다.

이 파일을 프로젝트 루트 디렉토리(ai/ 폴더 안)에 저장해두시면 됩니다.

📄 README.md (복사해서 사용하세요)
Markdown
# 🤖 PNT 통합 AI Worker Server

이 프로젝트는 RabbitMQ를 통해 **뉴스 생성(News)** 및 **미션 이미지 분석(Mission)** 요청을 받아 처리하는 AI 워커 서버입니다.
Python 기반으로 동작하며, PM2를 사용하여 프로세스를 관리합니다.

---

## 🛠 1. 사전 요구 사항 (Prerequisites)

서버 환경에 다음 소프트웨어가 설치되어 있어야 합니다.

* **OS**: Windows / Linux (Ubuntu 권장)
* **Python**: 3.12 이상
* **Node.js & npm**: (PM2 설치용)
* **Ollama**: 로컬 LLM 구동용 (News 생성)
* **CUDA (Optional)**: GPU 가속을 사용할 경우 (NVIDIA Driver 설치 필요)

---

## 📦 2. 설치 및 환경 설정 (Installation)

### 2-1. 프로젝트 클론 및 이동
```bash
git clone <레포지토리_주소>
cd ai
2-2. 가상환경 생성 및 활성화
독립적인 패키지 관리를 위해 가상환경을 사용합니다.

Windows (PowerShell)

PowerShell
python -m venv venv
.\venv\Scripts\Activate.ps1
Linux / Mac

Bash
python3 -m venv venv
source venv/bin/activate
2-3. 의존성 패키지 설치 (중요)
torch의 경우 CUDA(GPU) 버전을 설치하기 위해 반드시 아래 명령어를 사용해야 합니다.

Bash
# 1. 기본 패키지 및 PyTorch (CUDA 12.1 버전) 설치
pip install -r requirements.txt --extra-index-url [https://download.pytorch.org/whl/cu121](https://download.pytorch.org/whl/cu121)

# 2. (혹시 에러 발생 시) Pillow 수동 설치
pip install Pillow
⚙️ 3. 환경 변수 설정 (.env)
ai/ 폴더 내에 .env 파일을 생성하고 작성하세요.

🧠 4. AI 모델 준비
4-1. Ollama 모델 다운로드 (NewsService용)
뉴스 기사 생성을 위해 exaone3.5 모델을 로컬 Ollama에 받아야 합니다. (Ollama가 실행 중이어야 합니다.)

Bash
ollama pull exaone3.5
4-2. YOLO 모델 확인 (VisionService용)
ai/models/ 경로에 학습된 가중치 파일(best.pt)이 있는지 확인합니다.

ai/
 └─ models/
     └─ best.pt  <-- 필수 확인
🚀 5. 서버 실행 (PM2)
실제 운영 환경에서는 PM2를 사용하여 프로세스를 관리하고 유지합니다.

5-1. PM2 설치 (최초 1회)
Bash
npm install -g pm2@latest
5-2. Worker 실행
Cluster Mode(-i)를 사용하여 CPU 코어 수에 맞춰 병렬 처리가 가능합니다. (권장: 2개 이상)

Bash
# 기존 프로세스 삭제 (초기화)
pm2 delete worker

# Worker 2개 실행 (자동 재시작 방지 옵션 포함)
pm2 start worker.py --name "ai-worker" --interpreter python --no-autorestart -i 2
5-3. 상태 모니터링
Bash
# 프로세스 상태 목록 확인
pm2 list

# 실시간 로그 확인
pm2 log ai-worker

# 실시간 대시보드 (CPU/Memory)
pm2 monit
❓ 트러블슈팅 (Troubleshooting)
Q1. ModuleNotFoundError: No module named 'PIL' 에러가 나요.

pip install PIL은 작동하지 않습니다. pip install Pillow를 실행하세요.

Q2. torch 설치가 안 되거나 GPU 인식이 안 돼요.

requirements.txt에 버전이 고정되어 있어 충돌이 날 수 있습니다.

다음 명령어로 강제 재설치 해보세요:

Bash
pip uninstall torch torchvision
pip install torch==2.5.1+cu121 torchvision==0.20.1+cu121 --index-url [https://download.pytorch.org/whl/cu121](https://download.pytorch.org/whl/cu121)
Q3. PM2에서 Script already launched 에러가 나요.

이미 등록된 프로세스입니다. pm2 delete worker 후 다시 start 하세요.

Q4. 로그에 한글이 깨져서 나와요.

worker.py 내부적으로 utf-8 인코딩 처리가 되어 있으나, 윈도우 Powershell의 경우 chcp 65001 명령어를 입력해 인코딩을 변경해 보세요.

📂 디렉토리 구조
ai/
├── models/             # YOLO 학습 가중치 (best.pt)
├── services/           # AI 로직 (News, Vision)
├── logs/               # (Optional) 로그 파일
├── .env                # 환경변수 (Git 제외)
├── worker.py           # 메인 실행 파일 (RabbitMQ Consumer)
├── requirements.txt    # 의존성 목록
└── README.md           # 설치 가이드