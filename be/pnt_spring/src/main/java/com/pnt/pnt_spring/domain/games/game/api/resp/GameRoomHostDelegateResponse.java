package com.pnt.pnt_spring.domain.games.game.api.resp;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GameRoomHostDelegateResponse {

    private Long roomId;
    private Long oldHostMemberId;
    private Long newHostMemberId;

    public static GameRoomHostDelegateResponse of(Long roomId, Long oldHostMemberId, Long newHostMemberId) {
        return GameRoomHostDelegateResponse.builder()
                .roomId(roomId)
                .oldHostMemberId(oldHostMemberId)
                .newHostMemberId(newHostMemberId)
                .build();
    }
}
