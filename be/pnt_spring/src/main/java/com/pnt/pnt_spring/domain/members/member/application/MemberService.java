package com.pnt.pnt_spring.domain.members.member.application;

import com.pnt.pnt_spring.domain.members.member.api.req.FcmTokenRegisterRequest;
import com.pnt.pnt_spring.domain.members.member.api.req.FcmTokenUpdateRequest;
import com.pnt.pnt_spring.domain.members.member.api.req.MemberProfileUpdateRequest;
import com.pnt.pnt_spring.domain.members.member.api.resp.FcmTokenRegisterResponse;
import com.pnt.pnt_spring.domain.members.member.api.resp.FcmTokenUpdateResponse;
import com.pnt.pnt_spring.domain.members.member.api.resp.MemberProfileResponse;
import com.pnt.pnt_spring.domain.members.member.api.resp.MemberProfileUpdateResponse;
import com.pnt.pnt_spring.domain.members.stat.api.resp.MemberPoliceResponse;
import com.pnt.pnt_spring.domain.members.stat.api.resp.MemberThiefResponse;

public interface MemberService {

	// 조회
	MemberProfileResponse getMemberProfile(Long memberId);

	// 경찰 스탯 조회
	MemberPoliceResponse getPoliceProfile(Long memberId);

	// 도둑 스탯 조회
	MemberThiefResponse getThiefProfile(Long memberId);

	// 프로필 수정
	MemberProfileUpdateResponse updateProfile(Long memberId, MemberProfileUpdateRequest request);

	// fcm 토큰 등록
	FcmTokenRegisterResponse registerFcmToken(Long memberId, FcmTokenRegisterRequest request);

	// fcm 토큰 업데이트
	FcmTokenUpdateResponse updateFcmToken(Long memberId, FcmTokenUpdateRequest isActive);

}
