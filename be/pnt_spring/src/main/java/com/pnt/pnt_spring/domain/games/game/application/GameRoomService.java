package com.pnt.pnt_spring.domain.games.game.application;

import com.pnt.pnt_spring.domain.games.game.api.req.GameRoomCreateRequest;
import com.pnt.pnt_spring.domain.games.game.api.resp.GameRoomCreateResponse;
import com.pnt.pnt_spring.domain.games.game.api.resp.GameRoomStartableResponse;
import com.pnt.pnt_spring.domain.games.game.api.resp.GameStartResponse;

public interface GameRoomService {
	GameRoomCreateResponse createRoom(Long hostMemberId, GameRoomCreateRequest req);

	GameStartResponse start(Long actorMemberId, Long roomId);

	GameRoomStartableResponse getStartable(Long actorMemberId, Long roomId);

	void resetGame(Long gameId);

}
