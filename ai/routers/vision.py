import io
from fastapi import APIRouter, Request, UploadFile, File, Form, HTTPException
from PIL import Image
from schemas.common import CheckResponse  # schemas/common.py가 있어야 합니다

router = APIRouter(prefix="/vision", tags=["Vision"])

@router.post("/check", response_model=CheckResponse)
async def check_image(
    request: Request,
    image: UploadFile = File(...),    # 파일 업로드
    target: str = Form(...),          # 찾을 객체 이름 (Form Data)
    conf: float = Form(0.25)          # 신뢰도 임계값 (기본 0.25)
):
    """
    이미지를 업로드 받아 YOLO 모델을 통해 특정 객체(target)가 있는지 확인
    """
    # 1. main.py에서 로딩한 서비스 가져오기
    service = request.app.state.vision_service
    if not service:
        raise HTTPException(status_code=503, detail="Vision Service not initialized")

    try:
        # 2. 이미지 파일 읽기 (Bytes -> PIL Image)
        content = await image.read()
        pil_img = Image.open(io.BytesIO(content)).convert("RGB")
        
        # 3. 서비스 호출 (YOLO 추론)
        # 서비스 코드에서 ("O", 0.85) 같은 형태로 리턴한다고 가정
        result_str, similarity = service.check_object(pil_img, target, conf)
        
        # 4. 결과 반환 (스키마 형식)
        return CheckResponse(
            result=result_str,
            similarity=round(similarity, 4)
        )
        
    except Exception as e:
        print(f"❌ [Vision Router] Error: {e}")
        raise HTTPException(status_code=500, detail=str(e))