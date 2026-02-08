package com.pnt.pnt_spring.domain.chats.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pnt.pnt_spring.domain.chats.api.resp.ChatRoomMemberResponse;
import com.pnt.pnt_spring.domain.chats.application.ChatRoomMemberService;
import com.pnt.pnt_spring.global.api.response.CommonResponse;
import com.pnt.pnt_spring.global.utils.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chats")
@Tag(name = "Chat Room Member", description = "채팅방 참여 및 접속 상태 관리 API")
public class ChatRoomMemberController {

	private final ChatRoomMemberService chatRoomMemberService;

	// 채팅방 참여
	@Operation(summary = "채팅방 참여")
	@PostMapping("/{id}/join")
	public CommonResponse<ChatRoomMemberResponse> join(
			@PathVariable("id") Long chatRoomId) {
		Long memberId = SecurityUtils.currentMemberId();
		ChatRoomMemberResponse data = chatRoomMemberService.join(memberId, chatRoomId);
		return new CommonResponse<>(data, "채팅방 참여 성공", HttpStatus.CREATED);
	}

	// 채팅방 나가기
	@Operation(summary = "채팅방 나가기")
	@PostMapping("/{id}/leave")
	public CommonResponse<Void> leave(
			@PathVariable("id") Long chatRoomId) {
		Long memberId = SecurityUtils.currentMemberId();
		chatRoomMemberService.leave(memberId, chatRoomId);
		return new CommonResponse<>(null, "채팅방 나가기 성공", HttpStatus.OK);
	}

	// 채팅방 연결 (접속 상태 업데이트)
	@Operation(summary = "채팅방 연결")
	@PostMapping("/{id}/connect")
	public CommonResponse<Void> connect(
			@PathVariable("id") Long chatRoomId) {
		Long memberId = SecurityUtils.currentMemberId();
		chatRoomMemberService.connect(memberId, chatRoomId);
		return new CommonResponse<>(null, "채팅방 연결 성공", HttpStatus.OK);
	}

	// 채팅방 연결 해제 (접속 상태 해제)
	@Operation(summary = "채팅방 연결 해제")
	@PostMapping("/{id}/disconnect")
	public CommonResponse<Void> disconnect(
			@PathVariable("id") Long chatRoomId) {
		Long memberId = SecurityUtils.currentMemberId();
		chatRoomMemberService.disconnect(memberId, chatRoomId);
		return new CommonResponse<>(null, "채팅방 연결 해제 성공", HttpStatus.OK);
	}
}
