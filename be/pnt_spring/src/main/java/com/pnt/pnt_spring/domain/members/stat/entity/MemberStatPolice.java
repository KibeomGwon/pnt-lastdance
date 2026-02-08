package com.pnt.pnt_spring.domain.members.stat.entity;

import com.pnt.pnt_spring.domain.members.member.entity.Member;
import com.pnt.pnt_spring.domain.utils.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "member_stat_police")
@Builder
@AllArgsConstructor
public class MemberStatPolice extends BaseEntity {

	@Id
	private Long id;

	@MapsId
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "grade_police_id")
	private GradePolice gradePolice;

	private Integer totalArrestCount;
	private Integer mostArrestsInGame;

	public static MemberStatPolice createInitial(Member member, GradePolice initialGrade) {
		return MemberStatPolice.builder()
			.member(member)
			.gradePolice(initialGrade)// 바늘도둑(ID:1) 객체를 주입받아야 함
			.totalArrestCount(0)
			.mostArrestsInGame(0)
			.build();
	}

	public void updateAfterGame(boolean isWin, Integer gameArrestCount) {
		int arrests = (gameArrestCount == null) ? 0 : gameArrestCount;

		// 1. 누적 스탯 업데이트
		this.totalArrestCount = (this.totalArrestCount == null ? 0 : this.totalArrestCount) + arrests;

		// 2. 최고 기록 갱신 (한 판 최대 체포 수)
		if (this.mostArrestsInGame == null || arrests > this.mostArrestsInGame) {
			this.mostArrestsInGame = arrests;
		}

		// 3. 등급(계급) 변경은 Service에서 계산된 다음 등급 객체를 받아 setter로 변경하거나,
		//    여기서 로직을 처리할 수도 있지만, Grade Repository 조회가 필요하므로
		//    Service에서 다음 등급을 찾아서 넘겨주는 방식(changeGrade)을 유지합니다.
	}

	public void changeGrade(GradePolice newGrade) {
		this.gradePolice = newGrade;
	}

}