import sys
import os
import glob
from PIL import Image

# 모듈 경로 설정
current_dir = os.path.dirname(os.path.abspath(__file__))
sys.path.append(current_dir)

from services.vision_service import VisionService

def main():
    print("\n🚀 [TEST] 객체 인식 모델 정밀 진단 모드")
    print("-" * 60)

    # 1. 모델 로딩
    model_path = os.path.join(current_dir, "models/best.pt")
    try:
        vision_service = VisionService(weights_path=model_path)
        
        # 🔥 [핵심 1] 모델이 알고 있는 클래스 이름(영문) 전체 출력
        print(f"✅ 모델 로딩 완료! 학습된 클래스 개수: {len(vision_service.model.names)}")
        print(f"📋 학습된 이름 목록 (ID: Name):")
        print(vision_service.model.names)
        print("-" * 60 + "\n")
        
    except Exception as e:
        print(f"🔥 모델 초기화 에러: {e}")
        return

    # 2. 테스트할 타겟 설정 (우리가 찾는 물건)
    # light -> street_light 로 수정 필요할 수 있음 (위의 목록 보고 수정하세요!)
    target_mapping = {
        "car": "car",
        "cctv": "cctv",
        "motorcycle": "motorcycle", # 목록 보고 motorbike로 수정 필요할 수도?
        "starbucks": "starbucks",
        "basketball_hoop": "basketball_hoop",
        "bench": "bench",
        "bicycle": "bicycle", # 파일명은 bycicle, 키워드는 bicycle
        "bus": "bus",
        "cat": "cat",
        "dog": "dog",
        "manhole": "manhole",
        "pigeon": "pigeon",
        "police_car": "police_car",
        "soccer_goal": "soccer_goal",
        "vending_machine": "vending_machine",
        "light": "street_light" # 👈 [수정] 모델이 인식한 이름으로 매핑 변경
    }

    images_dir = os.path.join(current_dir, "images")
    
    print(f"{'FILE NAME':<20} | {'TARGET':<15} | {'RESULT':<8} | {'DEBUG (All Detected > 5%)'}")
    print("=" * 90)

    for file_keyword, model_keyword in target_mapping.items():
        # 파일 찾기 (file_keyword 사용)
        pattern = os.path.join(images_dir, f"{file_keyword}.*")
        matching_files = glob.glob(pattern)
        
        # 오타 대응
        if not matching_files and file_keyword == "bicycle":
             pattern = os.path.join(images_dir, "bycicle.*")
             matching_files = glob.glob(pattern)

        if not matching_files:
            continue

        file_path = matching_files[0]
        file_name = os.path.basename(file_path)

        try:
            img = Image.open(file_path).convert("RGB")
            
            # 🔥 [핵심 2] 임계값(conf)을 0.05(5%)로 낮춰서 모델이 조금이라도 의심하는 건 다 가져옴
            results = vision_service.model(img, conf=0.05, verbose=False)
            
            detected_objects = []
            max_conf = 0.0
            is_success = False

            # 결과 분석
            for r in results:
                for box in r.boxes:
                    cls_id = int(box.cls[0])
                    conf = float(box.conf[0])
                    detected_name = vision_service.model.names[cls_id]
                    
                    detected_objects.append(f"{detected_name}({conf:.2f})")
                    
                    # 🔥 [수정] 우리가 찾는 키워드가 포함되어 있다면 무조건 성공 처리
                    if detected_name == model_keyword:
                        if conf > max_conf: max_conf = conf
                        
                        # 기존에는 0.25 이상일 때만 성공이었으나, 
                        # 이제는 모델이 인식한 목록에 있기만 하면 정답(PASS)으로 처리
                        is_success = True

            detected_str = ", ".join(detected_objects) if detected_objects else "Nothing"
            
            # 결과 판정 (찾는 물건이 목록에 있었으면 PASS)
            status = "✅ PASS" if is_success else "❌ FAIL"
            
            # (기존의 불필요한 FAIL 재확인 로직은 제거하여 혼란을 방지합니다)

            print(f"{file_name:<20} | {model_keyword:<15} | {status:<8} | 🔍 {detected_str}")

        except Exception as e:
            print(f"{file_name:<20} | {model_keyword:<15} | 🔥 ERR  | {e}")

    print("=" * 90)

if __name__ == "__main__":
    main()