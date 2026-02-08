package com.pnt.pnt_spring.domain.members.stat.entity;

import com.pnt.pnt_spring.domain.games.game.enums.Position;
import com.pnt.pnt_spring.domain.members.member.entity.Member;
import com.pnt.pnt_spring.domain.utils.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member_stat")
@Builder
@AllArgsConstructor
public class MemberStat extends BaseEntity {

	@Id
	private Long id;

	@MapsId
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	private Integer totalGames;
	private Integer wins;
	private Integer loses;
	private Integer policeGame;
	private Integer thiefGame;

	// 초기 생성 메소드
	public static MemberStat createInitial(Member member) {
		return MemberStat.builder()
			.member(member)
			.totalGames(0)
			.wins(0)
			.loses(0)
			.policeGame(0)
			.thiefGame(0)
			.build();
	}

	// 게임 결과 업데이트 로직
	public void updateGameStats(boolean isWin, Position position) {
		// 1. 전체 게임 수 및 승패 증가
		this.totalGames = (this.totalGames == null ? 0 : this.totalGames) + 1;

		if (isWin) {
			this.wins = (this.wins == null ? 0 : this.wins) + 1;
		} else {
			this.loses = (this.loses == null ? 0 : this.loses) + 1;
		}

		// 2. 포지션별 게임 수 증가
		if (position == Position.POLICE) {
			this.policeGame = (this.policeGame == null ? 0 : this.policeGame) + 1;
		} else if (position == Position.THIEF) {
			this.thiefGame = (this.thiefGame == null ? 0 : this.thiefGame) + 1;
		}
	}
}