package com.pnt.pnt_spring.domain.chats.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pnt.pnt_spring.domain.chats.api.req.ChatRoomCreateRequest;
import com.pnt.pnt_spring.domain.chats.api.req.ChatRoomUpdateRequest;
import com.pnt.pnt_spring.domain.chats.api.resp.ChatRoomListResponse;
import com.pnt.pnt_spring.domain.chats.api.resp.ChatRoomResponse;
import com.pnt.pnt_spring.domain.chats.application.ChatRoomService;
import com.pnt.pnt_spring.global.api.response.CommonResponse;
import com.pnt.pnt_spring.global.utils.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Chat Room", description = "채팅방 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/chats")
public class ChatRoomController {

	private final ChatRoomService chatRoomService;

	// 채팅방 생성
	@Operation(summary = "채팅방 생성")
	@PostMapping
	public CommonResponse<ChatRoomResponse> create(
			@RequestBody @Valid ChatRoomCreateRequest req) {
		Long memberId = SecurityUtils.currentMemberId();
		ChatRoomResponse data = chatRoomService.create(memberId, req);
		return new CommonResponse<>(data, "채팅방 생성 성공", HttpStatus.CREATED);
	}

	// 채팅방 단건 조회
	@Operation(summary = "채팅방 단건 조회")
	@GetMapping("/{id}")
	public CommonResponse<ChatRoomResponse> get(@PathVariable Long id) {
		Long memberId = SecurityUtils.currentMemberId();
		ChatRoomResponse data = chatRoomService.get(memberId, id);
		return new CommonResponse<>(data, "채팅방 조회 성공", HttpStatus.OK);
	}

	// 채팅방 목록 조회
	@Operation(summary = "채팅방 목록 조회")
	@GetMapping
	public CommonResponse<ChatRoomListResponse> list(
			@RequestParam(required = false) Integer regionCode,
			@RequestParam(required = false) String title) {
		Long memberId = SecurityUtils.currentMemberId();
		List<ChatRoomResponse> rooms = chatRoomService.list(memberId, regionCode, title);
		ChatRoomListResponse data = ChatRoomListResponse.from(rooms);
		return new CommonResponse<>(data, "채팅방 목록 조회 성공", HttpStatus.OK);
	}

	// 채팅방 수정
	@Operation(summary = "채팅방 수정")
	@PatchMapping("/{id}")
	public CommonResponse<ChatRoomResponse> update(
			@PathVariable Long id,
			@RequestBody @Valid ChatRoomUpdateRequest req) {
		Long memberId = SecurityUtils.currentMemberId();
		ChatRoomResponse data = chatRoomService.update(memberId, id, req);
		return new CommonResponse<>(data, "채팅방 수정 성공", HttpStatus.OK);
	}

	// 채팅방 삭제
	@Operation(summary = "채팅방 삭제")
	@DeleteMapping("/{id}")
	public CommonResponse<Void> delete(@PathVariable Long id) {
		Long memberId = SecurityUtils.currentMemberId();
		chatRoomService.delete(memberId, id);
		return new CommonResponse<>(null, "채팅방 삭제 성공", HttpStatus.NO_CONTENT);
	}
}