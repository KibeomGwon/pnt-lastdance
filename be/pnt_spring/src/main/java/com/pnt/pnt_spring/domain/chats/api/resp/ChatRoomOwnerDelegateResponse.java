package com.pnt.pnt_spring.domain.chats.api.resp;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatRoomOwnerDelegateResponse {
    private Long roomId;
    private Long oldOwnerId;
    private Long newOwnerId;

    public static ChatRoomOwnerDelegateResponse of(Long roomId, Long oldOwnerId, Long newOwnerId) {
        return ChatRoomOwnerDelegateResponse.builder()
                .roomId(roomId)
                .oldOwnerId(oldOwnerId)
                .newOwnerId(newOwnerId)
                .build();
    }
}
