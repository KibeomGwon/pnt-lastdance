package com.pnt.pnt_spring.domain.games.game.entity;

import java.time.OffsetDateTime;

import com.pnt.pnt_spring.domain.members.member.entity.Member;
import com.pnt.pnt_spring.domain.utils.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(
	name = "game_skill",
	uniqueConstraints = {
		@UniqueConstraint(columnNames = {"game_id", "member_id"})
	}
)
public class GameSkill extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// FK 컬럼명 고정(이미 스키마 있으면 안전)
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "game_id", nullable = false)
	private Game game;

	@Column(name = "is_used", nullable = false)
	private boolean isUsed;

	@Column(name = "used_at")
	private OffsetDateTime usedAt;

    /* =========================
       생성/행위 메서드
       ========================= */

	public static GameSkill create(Game game, Member member) {
		if (game == null)
			throw new IllegalArgumentException("game은 null일 수 없습니다.");
		if (member == null)
			throw new IllegalArgumentException("member는 null일 수 없습니다.");

		GameSkill skill = new GameSkill();
		skill.game = game;
		skill.member = member;
		skill.isUsed = false;
		skill.usedAt = null;
		return skill;
	}

	public void use() {
		if (this.isUsed) {
			throw new IllegalStateException("이미 사용한 스킬입니다.");
		}
		this.isUsed = true;
		this.usedAt = OffsetDateTime.now();
	}

	public boolean isUsable() {
		return !this.isUsed;
	}
}
