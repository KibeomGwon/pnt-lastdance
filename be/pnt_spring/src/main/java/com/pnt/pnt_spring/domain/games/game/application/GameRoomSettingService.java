package com.pnt.pnt_spring.domain.games.game.application;

import com.pnt.pnt_spring.domain.games.game.api.req.GameRoomSettingUpdateRequest;
import com.pnt.pnt_spring.domain.games.game.api.resp.GameRoomSettingResponse;

public interface GameRoomSettingService {

	GameRoomSettingResponse getSettings(Long actorMemberId, Long roomId);

	GameRoomSettingResponse updateSettings(Long actorMemberId, Long roomId, GameRoomSettingUpdateRequest req);
}
