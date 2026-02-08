from contextlib import asynccontextmanager
from fastapi import FastAPI
from services.vision_service import VisionService
from routers import news, vision

@asynccontextmanager
async def lifespan(app: FastAPI):
    print("🚀 [System] FastAPI Starting...")
    
    # ✅ YOLO만 로딩 (가벼움)
    app.state.vision_service = VisionService()
    
    # ❌ NewsService(Llama)는 여기서 로딩 금지! 
    # (worker.py가 따로 가져가서 씁니다)
    
    yield
    print("🛑 [System] Shutting down...")

app = FastAPI(lifespan=lifespan)
app.include_router(news.router)
app.include_router(vision.router)

@app.get("/")
def health():
    return {"status": "ok", "mode": "Hybrid (YOLO=API, Llama=Worker)"}