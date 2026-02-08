package com.pnt.pnt_spring.domain.chats.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pnt.pnt_spring.domain.chats.entity.ChatRoom;
import com.pnt.pnt_spring.domain.chats.entity.MemberChatRoom;

public interface MemberChatRoomRepository extends JpaRepository<MemberChatRoom, Long> {

	/**
	 * (기본) 멤버십 조회
	 * - join에서 "있으면 복구(isDeleted=false) / 없으면 생성" 할 때 사용
	 */
	Optional<MemberChatRoom> findByChatRoomIdAndMemberId(Long chatRoomId, Long memberId);

	/**
	 * (기본) 현재 참여중인지 체크 (is_deleted=false)
	 * - currentMembers++ 중복 방지
	 */
	boolean existsByChatRoomIdAndMemberIdAndIsDeletedFalse(Long chatRoomId, Long memberId);

	/**
	 * (목록) 특정 유저가 참여중인 방 목록 뽑을 때 유용
	 */
	List<MemberChatRoom> findAllByMemberIdAndIsDeletedFalse(Long memberId);

	/**
	 * (목록) 특정 방의 참여자 목록 뽑을 때 유용
	 */
	List<MemberChatRoom> findAllByChatRoomIdAndIsDeletedFalse(Long chatRoomId);

	/**
	 * (옵션) current_members를 DB값 대신 count로 맞추고 싶을 때(동기화/검증용)
	 */
	//    long countByChatRoomIdAndIsDeletedFalse(Long chatRoomId);
	@Query("""
		    select cr
		    from ChatRoom cr, MemberChatRoom mcr
		    where mcr.memberId = :memberId
		      and mcr.isDeleted = false
		      and cr.isDeleted = false
		      and mcr.chatRoomId = cr.id
		    order by cr.updatedAt desc
		""")
	List<ChatRoom> findMyJoinedChatRooms(@Param("memberId") Long memberId);

	// 다음 방장 후보 조회: "내"가 아니고(Not), "나가지 않은"(DeletedAtIsNull) 멤버 중, "가장 먼저 온"(OrderByCreatedAtAsc) 사람
	Optional<MemberChatRoom> findFirstByChatRoomIdAndMemberIdNotAndIsDeletedFalseOrderByCreatedAtAsc(Long chatRoomId, Long memberId);
}
