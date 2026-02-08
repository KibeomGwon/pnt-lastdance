package com.pnt.pnt_spring.domain.games.game.entity;

import com.pnt.pnt_spring.domain.games.game.enums.Position;
import com.pnt.pnt_spring.domain.games.game.enums.PreferPosition;
import com.pnt.pnt_spring.domain.members.member.entity.Member;
import com.pnt.pnt_spring.domain.utils.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "game_member", uniqueConstraints = @UniqueConstraint(columnNames = { "game_id", "member_id" }))
public class GameMember extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "game_id", nullable = false)
	private Game game;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@Column(name = "serial_code")
	private String serialCode;

	// === 선호 포지션 (픽) ===
	@Enumerated(EnumType.STRING)
	@Column(name = "prefer_position", length = 10, nullable = false)
	private PreferPosition preferPosition;

	// === 배정 포지션 (게임 시작 시 확정) ===
	@Enumerated(EnumType.STRING)
	@Column(name = "given_position", length = 20)
	private Position givenPosition;

	@Column(nullable = false)
	private Boolean ready;

	/*
	 * =========================
	 * 생성/상태 변경 메서드
	 * =========================
	 */

	public static GameMember join(Game game, Member member) {
		if (game == null)
			throw new IllegalArgumentException("game은 null일 수 없습니다.");
		if (member == null)
			throw new IllegalArgumentException("member는 null일 수 없습니다.");

		GameMember gm = new GameMember();
		gm.game = game;
		gm.member = member;

		gm.isDeleted = false;
		gm.ready = false;
		gm.preferPosition = PreferPosition.ANY;
		gm.givenPosition = null;

		return gm;
	}

	/** 재입장: 소프트삭제 복구 + 상태 초기화(정책) */
	public void rejoin() {
		this.isDeleted = false;

		this.ready = false;
		this.givenPosition = null;

	}

	/** 나가기: 소프트삭제 */
	public void leave() {
		this.isDeleted = true;

		this.ready = false;
		this.givenPosition = null;
	}

	/** 강퇴: 소프트삭제 */
	public void kick() {
		this.isDeleted = true;

		this.ready = false;
		this.givenPosition = null;
	}

	/** A안: ready 값을 명시적으로 세팅 */
	public void setReady(boolean ready) {
		this.ready = ready;
	}

	public void pickPreferPosition(PreferPosition preferPosition) {
		if (preferPosition == null) {
			throw new IllegalArgumentException("preferPosition은 null일 수 없습니다.");
		}
		this.preferPosition = preferPosition;
	}

	// 배정은 게임 시작 단계에서
	public void assignPosition(Position position) {
		if (position == null) {
			throw new IllegalArgumentException("givenPosition은 null일 수 없습니다.");
		}
		this.givenPosition = position;
	}


	public void resetForNextGame() {
		// 이미 나간 사람은 건드리지 않음
		if (this.isDeleted()) {
			return;
		}

		// 역할(Position)은 로비에서 다시 정해야 하므로 초기화
		this.preferPosition = PreferPosition.ANY;
		this.givenPosition = null;
		this.ready = false;

	}
}
