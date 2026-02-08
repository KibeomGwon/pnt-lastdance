package com.pnt.pnt_spring.domain.chats.application.impl;

import java.time.OffsetDateTime;

import org.springframework.stereotype.Service;

import com.pnt.pnt_spring.domain.chats.api.resp.ChatRoomMemberResponse;
import com.pnt.pnt_spring.domain.chats.application.ChatRoomMemberService;
import com.pnt.pnt_spring.domain.chats.entity.ChatRoom;
import com.pnt.pnt_spring.domain.chats.entity.MemberChatRoom;
import com.pnt.pnt_spring.domain.chats.repository.ChatRoomBanRepository;
import com.pnt.pnt_spring.domain.chats.repository.ChatRoomRepository;
import com.pnt.pnt_spring.domain.chats.repository.MemberChatRoomRepository;
import com.pnt.pnt_spring.global.api.code.ErrorCode;
import com.pnt.pnt_spring.global.exception.BusinessException;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomMemberServiceImpl implements ChatRoomMemberService {

	private final ChatRoomRepository chatRoomRepository;
	private final MemberChatRoomRepository memberChatRoomRepository;
	private final ChatRoomBanRepository chatRoomBanRepository;

	@Override
	@Transactional
	public ChatRoomMemberResponse join(Long memberId, Long chatRoomId) {
		// 0) 밴(재입장 금지) 체크: 락 잡기 전에 먼저
		OffsetDateTime now = OffsetDateTime.now();
		if (chatRoomBanRepository.existsActiveBan(chatRoomId, memberId, now)) {
			throw new BusinessException(ErrorCode.CHAT_ROOM_BANNED);
			// 또는 IllegalArgumentException/커스텀 예외로
		}

		// 1) 채팅방 row 락 조회 (정원/현재인원 안전)
		ChatRoom room = chatRoomRepository.findByIdForUpdate(chatRoomId)
			.orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));

		// 2) 멤버십 조회 (있으면 복구 / 없으면 생성)
		MemberChatRoom mcr = memberChatRoomRepository.findByChatRoomIdAndMemberId(chatRoomId, memberId)
			.orElse(null);

		if (mcr == null) {
			// 신규 입장: 정원 체크 후 currentMembers 증가
			room.increaseMembers();

			mcr = MemberChatRoom.builder()
				.memberId(memberId)
				.chatRoomId(chatRoomId)
				.isConnected(true)
				.isDeleted(false)
				.build();

			memberChatRoomRepository.save(mcr);
			return ChatRoomMemberResponse.from(mcr);
		}

		// 기존 row가 있는 경우
		if (!mcr.isDeleted()) {
			// 이미 참여중이면 인원 증가 금지. "connect 처리"는 멱등적으로 OK
			mcr.connect();
			return ChatRoomMemberResponse.from(mcr);
		}

		// soft delete 상태였으면 "재입장": 정원 체크 + currentMembers 증가 + 복구
		room.increaseMembers();
		mcr.rejoin(); // isDeleted=false, isConnected=true

		return ChatRoomMemberResponse.from(mcr);
	}

	@Override
	@Transactional
	public void leave(Long memberId, Long chatRoomId) {
		// 1) 채팅방 락 조회 (currentMembers-- 안전)
		ChatRoom room = chatRoomRepository.findByIdForUpdate(chatRoomId)
			.orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));

		// 2) 멤버십 조회
		MemberChatRoom mcr = memberChatRoomRepository.findByChatRoomIdAndMemberId(chatRoomId, memberId)
			.orElseThrow(() -> new IllegalArgumentException("채팅방에 참여중이 아닙니다."));

		if (mcr.isDeleted()) {
			// 이미 나간 상태면 멱등 처리
			return;
		}

		// 방장 위임 로직 (나가기 직전에 수행)
		if (room.getOwnerId().equals(memberId)) {
			memberChatRoomRepository.findFirstByChatRoomIdAndMemberIdNotAndIsDeletedFalseOrderByCreatedAtAsc(chatRoomId, memberId)
				.ifPresent(nextOwner -> {
					room.delegateOwner(nextOwner.getMemberId());
				});
		}

		// 3) soft delete + 접속 종료
		mcr.leave();

		// 4) 현재 인원 감소
		room.decreaseMembers();
	}

	@Override
	@Transactional
	public void connect(Long memberId, Long chatRoomId) {
		MemberChatRoom mcr = memberChatRoomRepository.findByChatRoomIdAndMemberId(chatRoomId, memberId)
			.orElseThrow(() -> new IllegalArgumentException("채팅방에 참여중이 아닙니다."));

		if (mcr.isDeleted()) {
			throw new IllegalArgumentException("채팅방에 참여중이 아닙니다.");
		}

		mcr.connect();
	}

	@Override
	@Transactional
	public void disconnect(Long memberId, Long chatRoomId) {
		MemberChatRoom mcr = memberChatRoomRepository
			.findByChatRoomIdAndMemberId(chatRoomId, memberId)
			.orElseThrow(() -> new IllegalArgumentException("채팅방에 참여중이 아닙니다."));

		if (mcr.isDeleted()) {
			return; // 멱등 처리
		}

		mcr.disconnect();
	}
}
