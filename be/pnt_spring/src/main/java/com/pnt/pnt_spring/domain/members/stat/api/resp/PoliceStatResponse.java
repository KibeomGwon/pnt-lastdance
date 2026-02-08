package com.pnt.pnt_spring.domain.members.stat.api.resp;

import com.pnt.pnt_spring.domain.members.stat.entity.MemberStatPolice;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PoliceStatResponse { // 별도 public 클래스로 독립
	private Integer arrestCount;
	private Integer totalArrestCount;
	private Integer mostArrestsInGame;
	private String grade;

	public static PoliceStatResponse from(MemberStatPolice stat) {
		if (stat == null) {
			return PoliceStatResponse.builder()
				.arrestCount(0).totalArrestCount(0)
				.mostArrestsInGame(0).grade("순경")
				.build();
		}

		String gradeName = "순경";
		if (stat.getGradePolice() != null && stat.getGradePolice().getName() != null) {
			gradeName = stat.getGradePolice().getName();
		}
		return PoliceStatResponse.builder()
			.arrestCount(stat.getTotalArrestCount())
			.totalArrestCount(stat.getTotalArrestCount())
			.mostArrestsInGame(stat.getMostArrestsInGame())
			.grade(gradeName)
			.build();
	}
}