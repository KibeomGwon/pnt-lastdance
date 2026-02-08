import sys
import os
import time

# 모듈 경로 설정
current_dir = os.path.dirname(os.path.abspath(__file__))
sys.path.append(current_dir)

from services.news_service import NewsService

def main():
    print("🚀 [TEST] 뉴스 생성 모델(exaone 3.5) 성능 테스트\n")

    # 1. 서비스 초기화
    try:
        news_service = NewsService()
    except Exception as e:
        print(f"🔥 초기화 실패: {e}")
        return

    # 2. 테스트 데이터 (상황: 도둑팀 승리)
    mock_data = {
        "gameId": "speed_test_001",
        "latitude": 35.85, 
        "longitude": 128.50, # 대구 성서
        "winningTeam": "도둑",
        "thiefCount": 4,
        "policeCount": 2,
        "winnerTopMember": "홍길동",
        "mvp": "홍길동"
    }

    print("=" * 60)
    
    # ---------------------------------------------------------
    # 1️⃣ 첫 번째 호출 (Cold Start)
    # ---------------------------------------------------------
    print("1️⃣ 첫 번째 호출 (모델 로딩 & 예열)... 잠시만 기다려주세요.")
    start_time = time.time()
    
    _ = news_service.generate(mock_data) # 결과는 무시
    
    end_time = time.time()
    print(f"   ⏱️ 소요 시간: {end_time - start_time:.2f}초 (초기 로딩 포함)")
    print("-" * 60)

    # ---------------------------------------------------------
    # 2️⃣ 두 번째 호출 (Hot Run)
    # ---------------------------------------------------------
    print("2️⃣ 두 번째 호출 (실제 성능 측정)...")
    start_time = time.time()
    
    result = news_service.generate(mock_data)
    
    end_time = time.time()
    print(f"   🚀 소요 시간: {end_time - start_time:.2f}초 (캐시 적용)")
    
    print("=" * 60)
    print(f"📢 [헤드라인]: {result['headline']}")
    print("-" * 60)
    print(f"📄 [본문 내용]:\n{result['article']}")
    print("=" * 60)

if __name__ == "__main__":
    main()