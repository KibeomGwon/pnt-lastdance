import pika
import json
import requests
import sys
import os
import time
import io
import datetime
from PIL import Image
from dotenv import load_dotenv # 패키지 추가

# .env 파일 로드 (가장 먼저 실행)
load_dotenv()

# ======================================================
# ✅ [로그 설정] 즉시 출력 및 타임스탬프 함수
# ======================================================
sys.stdout = io.TextIOWrapper(sys.stdout.detach(), encoding='utf-8')
sys.stderr = io.TextIOWrapper(sys.stderr.detach(), encoding='utf-8')

def log(msg, level="INFO"):
    """PM2 로그 가독성 및 즉시 출력(flush=True) 처리"""
    now = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    if level == "IMPORTANT":
        print(f"\n{'='*60}", flush=True)
        print(f"[{now}] 🚀 {msg}", flush=True)
        print(f"{'='*60}\n", flush=True)
    elif level == "ERROR":
        print(f"[{now}] ❌ [ERROR] {msg}", flush=True)
    elif level == "SUCCESS":
        print(f"[{now}] ✅ {msg}", flush=True)
    else:
        print(f"[{now}] ℹ️  {msg}", flush=True)

# 모듈 경로 설정
sys.path.append(os.path.dirname(os.path.abspath(__file__)))
from services.news_service import NewsService
from services.vision_service import VisionService

# ======================================================
# ✅ [설정] 환경변수(.env)에서 로드
# ======================================================
MQ_HOST = os.getenv("MQ_HOST")
MQ_PORT = int(os.getenv("MQ_PORT", 5672)) # 숫자로 변환 필요
MQ_USER = os.getenv("MQ_USER")
MQ_PASS = os.getenv("MQ_PASS")

SPRING_NEWS_URL = os.getenv("SPRING_NEWS_URL")
NODE_NEWS_URL = os.getenv("NODE_NEWS_URL")
NODE_MISSION_URL = os.getenv("NODE_MISSION_URL")

def connect_mq():
    """RabbitMQ 연결 재시도 로직"""
    credentials = pika.PlainCredentials(MQ_USER, MQ_PASS)
    while True:
        try:
            log(f"RabbitMQ({MQ_HOST}:{MQ_PORT}) 접속 시도 중...", "INFO")
            connection = pika.BlockingConnection(pika.ConnectionParameters(
                host=MQ_HOST, port=MQ_PORT, credentials=credentials, heartbeat=600
            ))
            log(f"RabbitMQ 연결 성공!", "SUCCESS")
            return connection
        except Exception as e:
            log(f"RabbitMQ 접속 실패 ({e}), 5초 후 재시도...", "ERROR")
            time.sleep(5)

def download_image(url):
    try:
        res = requests.get(url, timeout=5)
        if res.status_code == 200:
            return Image.open(io.BytesIO(res.content)).convert("RGB")
        else:
            log(f"이미지 다운로드 실패: Status={res.status_code}", "ERROR")
    except Exception as e:
        log(f"이미지 다운로드 에러: {e}", "ERROR")
    return None

def main():
    log("통합 AI 워커 프로세스 시작 (Env 로드 완료)", "IMPORTANT")
    
    # 환경변수 로드 확인
    if not MQ_HOST or not MQ_PASS:
        log(".env 파일 로드 실패 또는 변수 누락! 설정을 확인하세요.", "ERROR")
        return

    # 모델 초기화
    try:
        log("NewsService / VisionService 모델 로딩 시작...")
        news_service = NewsService()
        
        # 경로 확인
        model_path = os.path.join(os.path.dirname(__file__), "models/best.pt")
        vision_service = VisionService(weights_path=model_path)
        log("모든 AI 모델 로딩 완료 (News + Vision)", "SUCCESS")
    except Exception as e:
        log(f"모델 초기화 치명적 오류: {e}", "ERROR")
        return

    connection = connect_mq()
    channel = connection.channel()
    
    channel.queue_declare(queue='NEWS', durable=True)
    channel.queue_declare(queue='MISSION', durable=True)
    
    log("NEWS, MISSION 큐 대기 시작...", "IMPORTANT")

    # -----------------------------------------------------
    # [1] 뉴스 큐 핸들러
    # -----------------------------------------------------
    def news_callback(ch, method, properties, body):
        try:
            payload = json.loads(body.decode('utf-8'))
            game_id = payload.get('gameId')
            log(f"[NEWS] 처리 요청 수신 (GameId: {game_id})", "IMPORTANT")
            
            ai_result = news_service.generate(payload)
            log(f"[NEWS] AI 기사 생성 완료 (제목: {ai_result['headline']})")
            
            # Spring 전송
            res = requests.post(SPRING_NEWS_URL, json={
                "gameId": game_id,
                "title": ai_result['headline'],
                "content": ai_result['article']
            }, timeout=10)

            if res.status_code == 200:
                log(f"[NEWS] Spring 전송 성공 (200 OK)", "SUCCESS")
                
                spring_response = res.json()
                saved_news_id = spring_response.get("newsId", 0)

                # Node 전송
                node_payload = {
                    "gameId": game_id,
                    "newsId": saved_news_id,
                    "success": True
                }
                res2 = requests.post(NODE_NEWS_URL, json=node_payload, timeout=10)
                
                if res2.status_code == 200:
                    log(f"[NEWS] Node.js 알림 전송 성공", "SUCCESS")
                else:
                    log(f"[NEWS] Node.js 전송 실패: {res2.status_code}", "ERROR")
            else:
                log(f"[NEWS] Spring 전송 실패: {res.status_code}", "ERROR")

            ch.basic_ack(delivery_tag=method.delivery_tag)
            
        except Exception as e:
            log(f"[NEWS] 에러: {e}", "ERROR")
            ch.basic_ack(delivery_tag=method.delivery_tag)

    # -----------------------------------------------------
    # [2] 미션 큐 핸들러
    # -----------------------------------------------------
    def mission_callback(ch, method, properties, body):
        try:
            payload = json.loads(body.decode('utf-8'))
            game_id = payload.get('gameId')
            game_mission_id = payload.get('gameMissionId')
            keyword = payload.get('keyword')
            image_url = payload.get('image')

            log(f"[MISSION] 분석 요청 (Game: {game_id}, Mission: {game_mission_id}, Key: {keyword})", "IMPORTANT")

            pil_image = download_image(image_url)
            is_success = False
            
            if pil_image and keyword:
                result_str, similarity = vision_service.check_object(pil_image, keyword)
                if result_str == "O":
                    is_success = True
                    log(f"   ㄴ 성공! (정확도: {similarity:.2f})", "SUCCESS")
                else:
                    log(f"   ㄴ 실패 (감지 안됨)", "INFO")
            else:
                log(f"   ㄴ 분석 불가 (이미지/키워드 오류)", "ERROR")

            node_payload = {
                "gameId": game_id,
                "missionId": game_mission_id,
                "memberId": payload.get('memberId'),
                "success": is_success
            }

            res = requests.post(NODE_MISSION_URL, json=node_payload, timeout=5)
            
            if res.status_code == 200:
                log(f"[MISSION] Node.js 전송 완료", "SUCCESS")
            else:
                log(f"[MISSION] Node.js 전송 실패: {res.status_code}", "ERROR")

            ch.basic_ack(delivery_tag=method.delivery_tag)

        except Exception as e:
            log(f"[MISSION] 에러: {e}", "ERROR")
            ch.basic_ack(delivery_tag=method.delivery_tag)

    channel.basic_qos(prefetch_count=1)
    channel.basic_consume(queue='NEWS', on_message_callback=news_callback)
    channel.basic_consume(queue='MISSION', on_message_callback=mission_callback)
    
    log("Worker 루프 실행 중...", "INFO")
    channel.start_consuming()

if __name__ == "__main__":
    main()