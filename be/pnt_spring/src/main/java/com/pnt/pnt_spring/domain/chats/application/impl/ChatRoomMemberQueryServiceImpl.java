package com.pnt.pnt_spring.domain.chats.application.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pnt.pnt_spring.domain.chats.api.resp.ChatRoomMemberInfoResponse;
import com.pnt.pnt_spring.domain.chats.api.resp.ChatRoomResponse;
import com.pnt.pnt_spring.domain.chats.application.ChatRoomMemberQueryService;
import com.pnt.pnt_spring.domain.chats.entity.ChatRoom;
import com.pnt.pnt_spring.domain.chats.entity.MemberChatRoom;
import com.pnt.pnt_spring.domain.chats.repository.ChatRoomRepository;
import com.pnt.pnt_spring.domain.chats.repository.MemberChatRoomRepository;
import com.pnt.pnt_spring.domain.members.member.entity.Member;
import com.pnt.pnt_spring.domain.members.member.repository.jpa.MemberRepository;
import com.pnt.pnt_spring.global.api.code.ErrorCode;
import com.pnt.pnt_spring.global.exception.BusinessException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomMemberQueryServiceImpl implements ChatRoomMemberQueryService {

	private final ChatRoomRepository chatRoomRepository;
	private final MemberChatRoomRepository memberChatRoomRepository;
	private final MemberRepository memberRepository;

	@Override
	@Transactional(readOnly = true)
	public List<ChatRoomMemberInfoResponse> listMembers(Long requesterId, Long chatRoomId) {

		// 1) 채팅방 존재 확인
		ChatRoom room = chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));

		// 2) (권장) 요청자가 방 참여자인지 체크 (아니면 멤버 목록 못 보게)
		boolean isParticipant = memberChatRoomRepository
			.existsByChatRoomIdAndMemberIdAndIsDeletedFalse(chatRoomId, requesterId);

		if (!isParticipant) {
			throw new BusinessException(ErrorCode.CHAT_ROOM_FORBIDDEN, "채팅방 참여자만 목록을 조회할 수 있습니다.");
		}

		// 3) 방의 현재 참여자 링크 조회
		List<MemberChatRoom> links = memberChatRoomRepository
			.findAllByChatRoomIdAndIsDeletedFalse(chatRoomId);

		if (links.isEmpty()) {
			return List.of();
		}

		// 4) memberId 추출
		List<Long> memberIds = links.stream()
			.map(MemberChatRoom::getMemberId)
			.distinct()
			.toList();

		// 5) Member + MemberProfile fetch join으로 한번에 조회 (N+1 방지)
		List<Member> members = memberRepository.findAllWithProfileByIdIn(memberIds);

		Map<Long, Member> memberMap = members.stream()
			.collect(Collectors.toMap(Member::getId, Function.identity()));

		// 6) 연결상태(isConnected)는 MemberChatRoom에 있으니 링크 기반으로 응답 조립
		Long ownerId = room.getOwnerId();

		return links.stream()
			.map(link -> {
				Member m = memberMap.get(link.getMemberId());
				if (m == null) {
					// 데이터 정합성 문제: 링크는 있는데 member가 없는 경우
					// 운영상 그냥 스킵하거나 예외 처리 선택
					return null;
				}
				return ChatRoomMemberInfoResponse.of(m, link.isConnected(), ownerId);
			})
			.filter(Objects::nonNull)
			.toList();
	}

	@Override
	public List<ChatRoomResponse> myJoinedRooms(Long memberId) {
		List<ChatRoom> rooms = memberChatRoomRepository.findMyJoinedChatRooms(memberId);
		return rooms.stream().map(ChatRoomResponse::from).toList();
	}
}
