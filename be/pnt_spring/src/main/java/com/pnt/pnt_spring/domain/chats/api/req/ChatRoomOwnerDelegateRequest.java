package com.pnt.pnt_spring.domain.chats.api.req;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatRoomOwnerDelegateRequest {

    @NotNull
    private Long targetMemberId;
}
