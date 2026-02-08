package com.pnt.pnt_spring.domain.chats.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(
	name = "member_chat_room",
	indexes = {
		@Index(name = "idx_mcr_member_id", columnList = "member_id"),
		@Index(name = "idx_mcr_chat_room_id", columnList = "chat_room_id"),
		@Index(name = "idx_mcr_member_room", columnList = "member_id, chat_room_id")
	},
	uniqueConstraints = {
		// 한 유저가 같은 방에 여러 번 row 생성되는 걸 방지 (복구는 is_deleted로 처리)
		@UniqueConstraint(name = "uk_mcr_member_room", columnNames = {"member_id", "chat_room_id"})
	}
)
public class MemberChatRoom {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "member_id", nullable = false)
	private Long memberId;

	@Column(name = "chat_room_id", nullable = false)
	private Long chatRoomId;

	@Column(name = "is_connected", nullable = false)
	private boolean isConnected;

	@Column(name = "is_deleted", nullable = false)
	private boolean isDeleted;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	// ====== 라이프사이클 ======
	@PrePersist
	protected void onCreate() {
		OffsetDateTime now = OffsetDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = OffsetDateTime.now();
	}

	// ====== 도메인 메서드(추천) ======
	public void connect() {
		this.isConnected = true;
		this.isDeleted = false; // 연결되면 기본적으로 활성으로 보는 경우
	}

	public void disconnect() {
		this.isConnected = false;
	}

	/**
	 * 퇴장(soft delete)
	 */
	public void leave() {
		this.isConnected = false;
		this.isDeleted = true;
	}

	/**
	 * 재입장(soft delete 복구)
	 */
	public void rejoin() {
		this.isDeleted = false;
		this.isConnected = true;
	}

	public void kickOut() {
		this.isConnected = false;
		this.isDeleted = true;
		this.updatedAt = java.time.OffsetDateTime.now();
	}

}
