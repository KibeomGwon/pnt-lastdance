package com.pnt.pnt_spring.domain.games.game.application;

import com.pnt.pnt_spring.domain.games.game.api.req.GameResultRequest;
import com.pnt.pnt_spring.domain.games.game.api.resp.GameResultResponse;

public interface GameResultService {

	// 게임 결과 저장
	void saveGameResult(GameResultRequest request);

	// 게임 결과 조회
	GameResultResponse getGameResult(Long gameId, Long memberId);

}
