package com.pnt.pnt_spring.domain.chats.application;

import com.pnt.pnt_spring.domain.chats.api.resp.ChatRoomOwnerDelegateResponse;

public interface ChatRoomModerationService {
	void kickAndBan3Days(Long actorMemberId, Long chatRoomId, Long targetMemberId, String reason);

	ChatRoomOwnerDelegateResponse delegateOwner(Long roomId, Long requesterId, Long targetMemberId);

}
