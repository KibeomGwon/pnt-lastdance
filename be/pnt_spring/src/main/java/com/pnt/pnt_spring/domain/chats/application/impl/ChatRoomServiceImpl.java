package com.pnt.pnt_spring.domain.chats.application.impl;

import java.util.List;

import com.pnt.pnt_spring.global.api.code.ErrorCode;
import com.pnt.pnt_spring.global.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pnt.pnt_spring.domain.chats.api.req.ChatRoomCreateRequest;
import com.pnt.pnt_spring.domain.chats.api.req.ChatRoomUpdateRequest;
import com.pnt.pnt_spring.domain.chats.api.resp.ChatRoomResponse;
import com.pnt.pnt_spring.domain.chats.application.ChatRoomService;
import com.pnt.pnt_spring.domain.chats.entity.ChatRoom;
import com.pnt.pnt_spring.domain.chats.entity.MemberChatRoom;
import com.pnt.pnt_spring.domain.chats.repository.ChatRoomRepository;
import com.pnt.pnt_spring.domain.chats.repository.MemberChatRoomRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService {

	private final ChatRoomRepository chatRoomRepository;
	private final MemberChatRoomRepository memberChatRoomRepository;

	@Override
	@Transactional
	public ChatRoomResponse create(Long memberId, ChatRoomCreateRequest req) {

		if (req.getMaxMembers() < 2) {
			throw new BusinessException(ErrorCode.CHAT_ROOM_CREATE_INVALIDATE, "최대 인원은 2명 이상이어야 합니다.");
		}

		// 1) 채팅방 생성(ownerId 포함)
		ChatRoom room = ChatRoom.create(
			memberId,
			req.getTitle(),
			req.getRegionCode(),
			req.getDescription(),
			req.getMaxMembers()
		);

		// 2) 저장해서 chatRoomId 확보
		ChatRoom saved = chatRoomRepository.save(room);

		// 3) 방장 멤버십 upsert (레포 메서드에 맞춰 chatRoomId, memberId 순서로 호출!)
		MemberChatRoom mcr = memberChatRoomRepository
			.findByChatRoomIdAndMemberId(saved.getId(), memberId)
			.orElseGet(() -> MemberChatRoom.builder()
				.memberId(memberId)
				.chatRoomId(saved.getId())
				.isConnected(false)
				.isDeleted(false)
				.build()
			);

		memberChatRoomRepository.save(mcr);

		// 4) currentMembers 정합성: 방장은 생성 즉시 1명
		saved.increaseMembers();

		return ChatRoomResponse.from(saved);
	}

	@Override
	@Transactional
	public ChatRoomResponse get(Long memberId, Long chatRoomId) {
		// TODO: memberId가 해당 채팅방을 조회할 권한이 있는지(참여자만 조회 등) 정책 정해지면 검증

		ChatRoom room = chatRoomRepository.findByIdAndIsDeletedFalse(chatRoomId)
			.orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));

		return ChatRoomResponse.from(room);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ChatRoomResponse> list(Long memberId, Integer regionCode, String title) {
		// memberId 기반 필터링(차단 유저 제외 등)은 추후 추가

		String keyword = (title == null) ? null : title.trim();
		boolean hasTitle = (keyword != null && !keyword.isBlank());

		List<ChatRoom> rooms;

		if (regionCode == null && !hasTitle) {
			rooms = chatRoomRepository.findAllByIsDeletedFalse();
		} else if (regionCode != null && !hasTitle) {
			rooms = chatRoomRepository.findAllByRegionCodeAndIsDeletedFalse(regionCode);
		} else if (regionCode == null) { // hasTitle == true
			rooms = chatRoomRepository.findAllByIsDeletedFalseAndTitleContainingIgnoreCase(keyword);
		} else { // regionCode != null && hasTitle == true
			rooms = chatRoomRepository.findAllByRegionCodeAndIsDeletedFalseAndTitleContainingIgnoreCase(regionCode,
				keyword);
		}

		return rooms.stream().map(ChatRoomResponse::from).toList();
	}

	@Override
	@Transactional
	public ChatRoomResponse update(Long memberId, Long chatRoomId, ChatRoomUpdateRequest req) {

		// 1) 채팅방 존재 + 삭제 여부 체크
		ChatRoom room = chatRoomRepository.findByIdAndIsDeletedFalse(chatRoomId)
			.orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));

		// 2) 방장 권한 체크 (방장만 수정 가능)
		// ⚠️ 아래 ownerId getter는 프로젝트에 맞게 바꿔주세요.
		if (room.getOwnerId() == null || !room.getOwnerId().equals(memberId)) {
			throw new IllegalArgumentException("채팅방 수정 권한이 없습니다.");
			// 가능하면 Forbidden(403) 계열 커스텀 예외로 바꾸는 게 더 좋음
		}

		// 3) 빈 요청 방지(선택이지만 추천)
		if (req.getTitle() == null
			&& req.getRegionCode() == null
			&& req.getDescription() == null
			&& req.getMaxMembers() == null) {
			throw new IllegalArgumentException("수정할 값이 없습니다.");
		}

		// 4) maxMembers 유효성: 현재 인원보다 낮출 수 없음
		if (req.getMaxMembers() != null && req.getMaxMembers() < room.getCurrentMembers()) {
			throw new IllegalArgumentException("현재 인원보다 최대 인원을 낮출 수 없습니다.");
		}

		// (선택) maxMembers 최소값 체크가 필요하면
		if (req.getMaxMembers() != null && req.getMaxMembers() < 2) {
			throw new IllegalArgumentException("최대 인원은 2명 이상이어야 합니다.");
		}

		// 5) 업데이트 적용 (엔티티의 update가 null 처리 정책을 갖고 있다는 가정)
		room.update(
			req.getTitle(),
			req.getRegionCode(),
			req.getDescription(),
			req.getMaxMembers()
		);

		// 6) 저장은 JPA dirty checking으로 자동 반영
		return ChatRoomResponse.from(room);
	}

	@Override
	@Transactional
	public void delete(Long memberId, Long chatRoomId) {

		ChatRoom room = chatRoomRepository.findByIdAndIsDeletedFalse(chatRoomId)
			.orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));

		// 방장만 삭제 가능
		if (!room.isOwner(memberId)) {
			throw new IllegalArgumentException("채팅방 삭제 권한이 없습니다.");
			// 실무에선 Forbidden(403) 커스텀 예외 추천
		}

		room.delete(); // soft delete
	}

}
