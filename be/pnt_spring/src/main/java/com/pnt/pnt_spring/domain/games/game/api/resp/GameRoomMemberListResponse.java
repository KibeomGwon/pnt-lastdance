package com.pnt.pnt_spring.domain.games.game.api.resp;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GameRoomMemberListResponse {
	private Long roomId;
	private List<GameRoomMemberItem> items;
}
