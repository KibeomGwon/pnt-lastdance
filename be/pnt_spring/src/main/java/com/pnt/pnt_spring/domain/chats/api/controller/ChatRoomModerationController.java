package com.pnt.pnt_spring.domain.chats.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pnt.pnt_spring.domain.chats.api.req.ChatRoomKickRequest;
import com.pnt.pnt_spring.domain.chats.api.req.ChatRoomOwnerDelegateRequest;
import com.pnt.pnt_spring.domain.chats.api.resp.ChatRoomOwnerDelegateResponse;
import com.pnt.pnt_spring.domain.chats.application.ChatRoomModerationService;
import com.pnt.pnt_spring.global.api.response.CommonResponse;
import com.pnt.pnt_spring.global.utils.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chats")
@Tag(name = "Chat Room Moderation", description = "채팅방 운영(강퇴/제재) API")
public class ChatRoomModerationController {

	private final ChatRoomModerationService moderationService;

	// 채팅방 강퇴
	@Operation(summary = "채팅방 강퇴")
	@PostMapping("/{chatRoomId}/kick")
	public CommonResponse<Void> kick(
			@PathVariable Long chatRoomId,
			@Valid @RequestBody ChatRoomKickRequest req) {
		Long actorMemberId = SecurityUtils.currentMemberId();

		moderationService.kickAndBan3Days(
				actorMemberId,
				chatRoomId,
				req.getTargetMemberId(),
				req.getReason());

		return new CommonResponse<>(null, "강퇴 처리 성공", HttpStatus.OK);
	}

	@Operation(summary = "채팅방 방장 위임")
	@PostMapping("/{chatRoomId}/owner")
	public CommonResponse<ChatRoomOwnerDelegateResponse> delegateOwner(
			@PathVariable Long chatRoomId,
			@Valid @RequestBody ChatRoomOwnerDelegateRequest req
	) {
		Long actorMemberId = SecurityUtils.currentMemberId();

		ChatRoomOwnerDelegateResponse chatRoomOwnerDelegateResponse = moderationService.delegateOwner(
				chatRoomId,
				actorMemberId,
				req.getTargetMemberId()
		);

		return new CommonResponse<>(chatRoomOwnerDelegateResponse, "방장 위임 성공", HttpStatus.OK);
	}

}
