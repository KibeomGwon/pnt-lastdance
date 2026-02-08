package com.pnt.pnt_spring.domain.games.mission.entity;

import java.time.OffsetDateTime;

import com.pnt.pnt_spring.domain.games.game.entity.Game;
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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "game_mission")
public class GameMission extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "game_id", nullable = false)
	private Game game;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "mission_id", nullable = false)
	private Mission mission;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "completed_by")
	private Member completedBy;

	@Column(name = "completed_at")
	private OffsetDateTime completedAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private MissionStatus status; // Enum: IN_PROGRESS, DONE 등

	public static GameMission create(Game game, Mission mission) {
		GameMission gameMission = new GameMission();
		gameMission.game = game;
		gameMission.mission = mission;
		gameMission.status = MissionStatus.IN_PROGRESS; // 초기 상태 진행 중
		return gameMission;
	}
}