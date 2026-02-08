import logging
import re
import random  # 🔥 랜덤 수식어 선택을 위해 추가
import os      # .env 환경변수 로드를 위해 추가
from openai import OpenAI
from geopy.geocoders import Nominatim
from typing import TypedDict
from langgraph.graph import StateGraph, END

# 로그 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class NewsState(TypedDict):
    raw_data: dict
    location_str: str
    prompt_text: str
    article: str
    headline: str

class NewsService:
    def __init__(self):
        logger.info("📰 [NewsService] 로컬 Ollama 및 워크플로우 초기화 중...")
        
        # ✅ [수정] .env 환경변수 사용 (보안 유지)
        base_url = os.getenv("OLLAMA_BASE_URL", "http://localhost:11434/v1")
        api_key = os.getenv("OLLAMA_API_KEY", "ollama")

        self.client = OpenAI(
            base_url=base_url,
            api_key=api_key
        )
        self.model_name = "exaone3.5"
        
        self.geolocator = Nominatim(user_agent="pnt_ai_news_service")
        
        self.app = self._build_workflow()
        logger.info(f"✅ [NewsService] 준비 완료! (Model: {self.model_name})")

    def _get_address(self, lat, lng):
        try:
            if lat is None or lng is None:
                return "작전 지역"
            # 명확한 한국어 주소를 위해 language='ko' 설정
            location = self.geolocator.reverse(f"{lat}, {lng}", language='ko', timeout=3)
            
            if location and 'address' in location.raw:
                addr = location.raw['address']
                
                # 1. 시/도/군 추출
                city = addr.get('city') or addr.get('town') or addr.get('village') or addr.get('county') or ""
                
                # 2. 동/읍/면 추출
                dong = addr.get('suburb') or addr.get('neighbourhood') or addr.get('borough') or ""
                
                if city and dong:
                    return f"{city} {dong}"
                elif city or dong:
                    return f"{city}{dong}".strip()
                
                return location.address
            return f"좌표 ({lat:.2f}, {lng:.2f}) 인근"
        except Exception as e:
            logger.warning(f"⚠️ 주소 변환 실패: {e}")
            return "도시 외곽 지역"

    def _clean_text(self, text: str) -> str:
        if not text:
            return ""
        text = text.replace("**", "")
        text = text.replace("[보도]", "").replace("[보도 종료]", "").replace("[속보]", "")
        text = text.replace("\n\n", " ").replace("\n", " ")
        return text.strip()

    def _build_workflow(self):
        workflow = StateGraph(NewsState)
        workflow.add_node("preprocess", self.preprocess_node)
        workflow.add_node("generate_article", self.generate_article_node)
        workflow.add_node("generate_headline", self.generate_headline_node)
        
        workflow.set_entry_point("preprocess")
        workflow.add_edge("preprocess", "generate_article")
        workflow.add_edge("generate_article", "generate_headline")
        workflow.add_edge("generate_headline", END)
        return workflow.compile()

    def preprocess_node(self, state: NewsState):
        d = state['raw_data']
        location_str = self._get_address(d.get('latitude'), d.get('longitude'))
        
        win_team = d.get('winningTeam')
        thief_cnt = d.get('thiefCount', 0)
        police_cnt = d.get('policeCount', 0)
        
        # 닉네임 가져오기 (없으면 일반 명사 처리)
        winner_top_name = d.get('winnerTopMember', '소속 대원')
        loser_top_name = d.get('loserTopMember', '신원 미상')
        mvp_name = d.get('mvp', '선정 안됨')

        # 🔥 [수정] 상황별 '센스 있는' 수식어 랜덤 배정 로직
        if win_team == "경찰":
            summary = f"경찰팀이 도둑 {thief_cnt}명을 상대로 완벽한 포위망을 구축, 전원 검거에 성공했습니다."
            
            # 경찰 승리 시 수식어
            winner_modifiers = ["발빠른 경찰", "파워풀한 검거 전문가", "철벽 수비를 보여준", "빈틈없는"]
            mvp_modifiers = ["모두에게 귀감이 된", "작전의 일등공신", "현장을 완벽히 지휘한", "최고의 활약을 펼친"]
            loser_modifiers = ["필사적으로 도주한 도둑", "마지막까지 저항한", "아쉽게 검거된"]
            
        else: # 도둑 승리
            summary = f"도둑 일당이 {police_cnt}명의 경찰 추격을 따돌리고 유유히 현장을 빠져나갔습니다."
            
            # 도둑 승리 시 수식어
            winner_modifiers = ["바람보다 빠른 도둑", "신출귀몰한", "수사망을 농락한", "그림자 같은"]
            mvp_modifiers = ["도둑들을 이끄는 대도", "전설적인 도주 실력의", "대담한 작전 설계자", "현장을 지배한"]
            loser_modifiers = ["끝까지 추격한 경찰", "고군분투한", "아쉽게 범인을 놓친", "빗속을 뚫고 쫓은"]

        # 랜덤 선택 적용
        winner_desc = f"{random.choice(winner_modifiers)} '{winner_top_name}'"
        mvp_desc = f"{random.choice(mvp_modifiers)} MVP '{mvp_name}'"
        loser_desc = f"{random.choice(loser_modifiers)} '{loser_top_name}'"

        # 프롬프트 구성
        system_prompt = f"""당신은 공중파 메인 뉴스 앵커입니다.
**불필요한 서론 없이 핵심 사건만 1문단(약 2~3문장)으로 임팩트 있게 보도하십시오.**

[절대 금지 사항]
1. 마크다운(**) 및 메타 태그([보도], [속보]) 절대 사용 금지.
2. 닉네임과 제공된 수식어는 변경 없이 그대로 사용할 것.

[내용 구성]
- 첫 문장: {location_str}에서 발생한 사건 결과 ({win_team} 승리).
- 다음 문장: 활약상 묘사. 승리팀의 주역인 {winner_desc}와(과), {mvp_desc}의 활약을 강조하고, 패배팀 {loser_desc}의 상황을 짧게 덧붙일 것.
- 말투: "~했습니다." 체의 정중하고 박진감 넘치는 앵커 말투. 총 150자 이내."""

        user_message = f"""[속보 데이터]
- 장소: {location_str}
- 상황: {summary}
- 승리팀 주역(2인자): {winner_desc}
- MVP: {mvp_desc}
- 패배팀 에이스: {loser_desc}

위 정보를 바탕으로 실제 뉴스 앵커가 읽는 대본을 작성해."""

        return {
            "location_str": location_str,
            "prompt_text": f"System: {system_prompt}\nUser: {user_message}"
        }

    def generate_article_node(self, state: NewsState):
        logger.info("📝 뉴스 기사 생성 중...")
        try:
            response = self.client.chat.completions.create(
                model=self.model_name,
                messages=[
                    {"role": "system", "content": state['prompt_text'].split('\nUser:')[0]},
                    {"role": "user", "content": state['prompt_text'].split('\nUser:')[1]}
                ],
                temperature=0.5, # 조금 더 창의적인 표현을 위해 약간 올림
                extra_body={"options": {"num_ctx": 4096}}
            )
            raw_article = response.choices[0].message.content.strip()
            cleaned_article = self._clean_text(raw_article)
            return {"article": cleaned_article}
        except Exception as e:
            logger.error(f"❌ 기사 생성 실패: {e}")
            return {"article": "통신 장애로 인해 현장 연결이 지연되고 있습니다."}

    def generate_headline_node(self, state: NewsState):
        logger.info("🎬 헤드라인 생성 중...")
        try:
            prompt = f"""다음 기사를 한 줄 헤드라인(20자 이내)으로 요약해.
            - 조건: 특수기호 제거, 문장형으로 끝낼 것.
            - 기사: {state['article']}"""
            
            response = self.client.chat.completions.create(
                model=self.model_name,
                messages=[{"role": "user", "content": prompt}],
                temperature=0.3
            )
            headline = response.choices[0].message.content.strip()
            headline = self._clean_text(headline)
            
            final_headline = f"{state['raw_data'].get('winningTeam')} 승리, {headline}"
            return {"headline": final_headline}
        except:
            return {"headline": f"{state['raw_data'].get('winningTeam')} 승리, {state['location_str']} 작전 종료"}

    def generate(self, data: dict) -> dict:
        initial_state = {"raw_data": data}
        result = self.app.invoke(initial_state)
        return {
            "headline": result.get("headline"),
            "article": result.get("article"),
            "location": result.get("location_str")
        }