package com.pnt.pnt_spring.domain.chats.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pnt.pnt_spring.domain.chats.api.resp.ChatRoomListResponse;
import com.pnt.pnt_spring.domain.chats.api.resp.ChatRoomMemberInfoResponse;
import com.pnt.pnt_spring.domain.chats.api.resp.ChatRoomResponse;
import com.pnt.pnt_spring.domain.chats.application.ChatRoomMemberQueryService;
import com.pnt.pnt_spring.global.api.response.CommonResponse;
import com.pnt.pnt_spring.global.utils.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chats")
@Tag(name = "Chat Room Member Query", description = "채팅방 멤버/참여 목록 조회 API")
public class ChatRoomMemberQueryController {

	private final ChatRoomMemberQueryService queryService;

	// 채팅방 참여 멤버 목록 조회
	@Operation(summary = "채팅방 멤버 목록 조회")
	@GetMapping("/{chatRoomId}/members")
	public CommonResponse<List<ChatRoomMemberInfoResponse>> list(@PathVariable Long chatRoomId) {
		Long requesterId = SecurityUtils.currentMemberId();
		List<ChatRoomMemberInfoResponse> data = queryService.listMembers(requesterId, chatRoomId);

		return new CommonResponse<>(data, "채팅방 멤버 목록 조회 성공", HttpStatus.OK);
	}

	// 내가 참여한 채팅방 목록 조회
	@Operation(summary = "내 참여 채팅방 목록 조회")
	@GetMapping("/me/rooms")
	public CommonResponse<ChatRoomListResponse> myRooms() {
		Long memberId = SecurityUtils.currentMemberId();

		List<ChatRoomResponse> rooms = queryService.myJoinedRooms(memberId);

		ChatRoomListResponse data = ChatRoomListResponse.from(rooms);
		return new CommonResponse<>(data, "내 참여 채팅방 목록 조회 성공", HttpStatus.OK);
	}
}
