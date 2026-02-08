from fastapi import APIRouter, Request, HTTPException
from pydantic import BaseModel

router = APIRouter(prefix="/news", tags=["News"])

# 스키마는 schemas/common.py 로 빼는게 좋지만 일단 여기에 둡니다.
class NewsRequest(BaseModel):
    location: str
    context: str = ""

@router.post("/generate")
async def generate_news(request: Request, data: NewsRequest):
    # lifespan에서 로딩한 서비스 가져오기
    service = request.app.state.news_service
    
    if not service:
        raise HTTPException(status_code=503, detail="News Service not initialized")

    print(f"🎙️ [News Controller] Request for {data.location}")
    
    try:
        # 서비스 호출 (동기 함수라면 스레드 풀에서 실행됨)
        output = service.generate(data.dict())
        return {
            "headline": output.get("headline", "제목 없음"),
            "content": output.get("article", "내용 없음")
        }
    except Exception as e:
        print(f"Error: {e}")
        raise HTTPException(status_code=500, detail=str(e))