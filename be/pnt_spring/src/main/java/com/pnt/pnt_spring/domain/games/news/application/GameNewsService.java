package com.pnt.pnt_spring.domain.games.news.application;

import com.pnt.pnt_spring.domain.games.news.api.resp.AiNewsResponse;
import com.pnt.pnt_spring.domain.games.news.api.resp.GameNewsResponse;

public interface GameNewsService {
	void saveNews(AiNewsResponse response);

	GameNewsResponse getNewsByGameId(Long gameId);
}