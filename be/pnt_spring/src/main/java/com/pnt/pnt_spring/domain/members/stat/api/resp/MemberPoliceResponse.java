package com.pnt.pnt_spring.domain.members.stat.api.resp;

import com.pnt.pnt_spring.domain.members.stat.entity.MemberStatPolice;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberPoliceResponse {

	private Long memberId;
	private PoliceStatResponse policeStat;

	public static MemberPoliceResponse of(Long memberId, MemberStatPolice stat) {
		return MemberPoliceResponse.builder()
			.memberId(memberId)
			.policeStat(PoliceStatResponse.from(stat))
			.build();
	}
}