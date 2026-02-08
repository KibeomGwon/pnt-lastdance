package com.pnt.pnt_spring.domain.games.game.api.req;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GameRoomHostDelegateRequest {

    @NotNull
    private Long targetMemberId;
}
