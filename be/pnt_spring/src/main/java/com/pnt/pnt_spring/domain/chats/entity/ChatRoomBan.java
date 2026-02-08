package com.pnt.pnt_spring.domain.chats.entity;

import java.time.OffsetDateTime;

import com.pnt.pnt_spring.domain.members.member.entity.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
	name = "chat_room_ban",
	uniqueConstraints = {
		@UniqueConstraint(name = "uq_chat_room_ban_room_member", columnNames = {"chat_room_id", "member_id"})
	},
	indexes = {
		@Index(name = "idx_chat_room_ban_lookup", columnList = "chat_room_id, member_id, banned_until")
	}
)
public class ChatRoomBan {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "chat_room_id", nullable = false)
	private ChatRoom chatRoom;

	// 밴 대상
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	// 밴 실행자
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "banned_by", nullable = false)
	private Member bannedBy;

	@Column(name = "banned_at", nullable = false)
	private OffsetDateTime bannedAt;

	@Column(name = "banned_until", nullable = false)
	private OffsetDateTime bannedUntil;

	@Column(name = "reason", length = 255)
	private String reason;

	public boolean isActive(OffsetDateTime now) {
		return now.isBefore(this.bannedUntil);
	}

	public void extendOrOverwrite(OffsetDateTime now, OffsetDateTime newUntil, String reason, Member actor) {
		// 정책: 재강퇴 시 지금부터 3일로 덮어쓰기(또는 max로 연장도 가능)
		this.bannedAt = now;
		this.bannedUntil = newUntil;
		this.reason = reason;
		this.bannedBy = actor;
	}
}
