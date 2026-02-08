package com.pnt.pnt_spring.domain.members.stat.api.resp;

import com.pnt.pnt_spring.domain.members.stat.entity.MemberStatThief;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberThiefResponse {

	private Long memberId;
	private ThiefStatResponse thiefStat; // 분리된 클래스 사용

	public static MemberThiefResponse of(Long memberId, MemberStatThief stat) {
		return MemberThiefResponse.builder()
			.memberId(memberId)
			.thiefStat(ThiefStatResponse.from(stat))
			.build();
	}
}