package com.pnt.pnt_spring.domain.games.game.entity;

import com.pnt.pnt_spring.domain.games.game.enums.Position;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "game_member_stat",
	uniqueConstraints = @UniqueConstraint(columnNames = "game_member_id"))
public class GameMemberStat extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "game_member_id", nullable = false)
	private GameMember gameMember;

	@Enumerated(EnumType.STRING)
	private Position position;

	private Integer walk = 0;

	@Column(name = "arrest_count")
	private Integer arrestCount = 0;

	@Column(name = "longest_survived")
	private Integer longestSurvived = 0;

	@Column(name = "mission_count")
	private Integer missionCount = 0;

	// Service에서 사용할 생성자 추가
	public GameMemberStat(GameMember gameMember) {
		this.gameMember = gameMember;
		this.position = gameMember.getGivenPosition();
		this.walk = 0;
		this.arrestCount = 0;
		this.longestSurvived = 0;
	}

	public static GameMemberStat create(GameMember gameMember) {
		GameMemberStat stat = new GameMemberStat();
		stat.gameMember = gameMember;
		stat.position = gameMember.getGivenPosition().toStatPosition();
		stat.walk = 0;
		stat.arrestCount = 0;
		stat.longestSurvived = 0;
		return stat;
	}

	// 생존 시간 업데이트 메서드 추가
	public void updateLongestSurvived(int survivalSec) {
		this.longestSurvived = survivalSec;
	}

	@Builder
	public GameMemberStat(GameMember gameMember, Position position, Integer walk, Integer arrestCount,
		Integer longestSurvived) {
		this.gameMember = gameMember;
		this.position = position;
		this.walk = walk;
		this.arrestCount = arrestCount;
		this.longestSurvived = longestSurvived;
	}

	// 스탯 초기화 설정
	public static GameMemberStat createInitialStat(GameMember gameMember) {
		return GameMemberStat.builder()
			.gameMember(gameMember)
			.position(gameMember.getGivenPosition()) // 게임 멤버의 포지션을 가져옴
			.walk(0)
			.arrestCount(0)
			.longestSurvived(0)
			.build();
	}

	// 게임 시작 시 확정된 포지션으로 강제 업데이트하는 메서드
	public void syncPosition(Position position) {
		this.position = position;
		// 필요하다면 점수들도 여기서 확실하게 0으로 리셋
		this.walk = 0;
		this.arrestCount = 0;
		this.longestSurvived = 0;
	}

	// 결과 업데이트
	public void updateResultStats(Integer walk, Integer longestSurvived, Integer missionCount) {
		this.walk = walk;
		this.longestSurvived = longestSurvived;
		this.missionCount = (missionCount == null) ? 0 : missionCount;
	}
}