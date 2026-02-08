package com.pnt.pnt_spring.domain.games.news.application.Impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pnt.pnt_spring.domain.games.game.entity.Game;
import com.pnt.pnt_spring.domain.games.game.repository.GameRepository;
import com.pnt.pnt_spring.domain.games.news.api.resp.AiNewsResponse;
import com.pnt.pnt_spring.domain.games.news.api.resp.GameNewsResponse;
import com.pnt.pnt_spring.domain.games.news.application.GameNewsService;
import com.pnt.pnt_spring.domain.games.news.entity.GameNews;
import com.pnt.pnt_spring.domain.games.news.repository.GameNewsRepository;
import com.pnt.pnt_spring.global.api.code.ErrorCode;
import com.pnt.pnt_spring.global.exception.BusinessException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class GameNewsServiceImpl implements GameNewsService {

	private final GameNewsRepository gameNewsRepository;
	private final GameRepository gameRepository;

	@Override
	public void saveNews(AiNewsResponse response) {

		// 해당 게임이 존재하는지 확인
		Game game = gameRepository.findById(response.getGameId())
			.orElseThrow(() -> new BusinessException(ErrorCode.GAME_NOT_FOUND));

		// 뉴스 엔티티 빌드
		GameNews gameNews = GameNews.builder()
			.game(game)
			.title(response.getTitle())
			.content(response.getContent())
			.build();

		// 저장
		gameNewsRepository.save(gameNews);
	}

	@Override
	@Transactional(readOnly = true)
	public GameNewsResponse getNewsByGameId(Long gameId) {
		GameNews news = gameNewsRepository.findByGameId(gameId)
			.orElseThrow(() -> new BusinessException(ErrorCode.NEWS_NOT_FOUND));

		return GameNewsResponse.from(news);
	}
}