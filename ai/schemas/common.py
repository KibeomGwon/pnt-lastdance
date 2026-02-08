from pydantic import BaseModel

# ==========================================
# [1] 뉴스 생성 서비스용 DTO (Spring: AiNewsRequest)
# ==========================================
class NewsRequest(BaseModel):
    gameId: int              # Spring: Long gameId
    startTime: str           # Spring: String startTime (yyyy-MM-dd HH:mm)
    winningTeam: str         # Spring: String winningTeam ("경찰" 또는 "도둑")
    playTime: int            # Spring: int playTime (초 단위)
    
    latitude: float          # Spring: Double latitude
    longitude: float         # Spring: Double longitude
    
    thiefCount: int          # Spring: int thiefCount
    policeCount: int         # Spring: int policeCount
    mvp: str                 # Spring: String mvp
    winnerTopMember: str     # Spring: String winnerTopMember
    loserTopMember: str      # Spring: String loserTopMember

# ==========================================
# [2] 객체 인식 서비스용 DTO
# ==========================================
class CheckResponse(BaseModel):
    result: str              # "O" or "X"
    similarity: float