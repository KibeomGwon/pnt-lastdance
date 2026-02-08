package com.pnt.pnt_spring.domain.chats.entity;

import com.pnt.pnt_spring.domain.utils.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_room")
public class ChatRoom extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 255)
	private String title;

	@Column(name = "region_code", nullable = false)
	private Integer regionCode;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(name = "max_members", nullable = false)
	private int maxMembers;

	@Column(name = "current_members", nullable = false)
	private int currentMembers;

	@Column(name = "owner_id", nullable = false)
	private Long ownerId;

	@Column(name = "is_deleted", nullable = false)
	private boolean isDeleted;

	// ===== 생성 =====
	public static ChatRoom create(Long ownerId, String title, Integer regionCode, String description, int maxMembers) {
		ChatRoom room = new ChatRoom();
		room.ownerId = ownerId;
		room.title = title;
		room.regionCode = regionCode;
		room.description = description;
		room.maxMembers = maxMembers;
		room.currentMembers = 0;
		room.isDeleted = false;
		return room;
	}

	// ===== 수정 =====
	public void update(String title, Integer regionCode, String description, Integer maxMembers) {
		if (title != null)
			this.title = title;
		if (regionCode != null)
			this.regionCode = regionCode;
		if (description != null)
			this.description = description;
		if (maxMembers != null)
			this.maxMembers = maxMembers;
	}

	// ===== 삭제(soft delete) =====
	public void delete() {
		this.isDeleted = true;
	}

	// ===== 현재 인원 증감 =====
	public void increaseMembers() {
		if (this.currentMembers >= this.maxMembers) {
			throw new IllegalStateException("채팅방 정원이 가득 찼습니다.");
		}
		this.currentMembers++;
	}

	public void decreaseMembers() {
		if (this.currentMembers > 0) {
			this.currentMembers--;
		}
	}

	// ===== 권한 체크용 도메인 메서드  =====
	public boolean isOwner(Long memberId) {
		return this.ownerId.equals(memberId);
	}

	public void decreaseCurrentMembersSafely() {
		if (this.currentMembers > 0) {
			this.currentMembers -= 1;
		}
		this.updatedAt = java.time.OffsetDateTime.now();
	}

	// ===== 방장 위임 =====
	public void delegateOwner(Long newOwnerId) {
		if (newOwnerId == null) {
			throw new IllegalArgumentException("newOwnerId는 null일 수 없습니다.");
		}
		this.ownerId = newOwnerId;
	}

	public boolean isSameOwner(Long newOwnerId) {
		return this.ownerId != null && this.ownerId.equals(newOwnerId);
	}


}
