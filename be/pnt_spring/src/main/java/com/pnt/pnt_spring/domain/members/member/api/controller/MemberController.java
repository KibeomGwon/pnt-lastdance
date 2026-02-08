package com.pnt.pnt_spring.domain.members.member.api.controller;

import org.checkerframework.checker.units.qual.C;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pnt.pnt_spring.domain.members.member.api.req.FcmTokenRegisterRequest;
import com.pnt.pnt_spring.domain.members.member.api.req.FcmTokenUpdateRequest;
import com.pnt.pnt_spring.domain.members.member.api.req.MemberProfileUpdateRequest;
import com.pnt.pnt_spring.domain.members.member.api.resp.FcmTokenRegisterResponse;
import com.pnt.pnt_spring.domain.members.member.api.resp.FcmTokenUpdateResponse;
import com.pnt.pnt_spring.domain.members.member.api.resp.MemberProfileResponse;
import com.pnt.pnt_spring.domain.members.member.api.resp.MemberProfileUpdateResponse;
import com.pnt.pnt_spring.domain.members.member.application.MemberService;
import com.pnt.pnt_spring.domain.members.stat.api.resp.MemberPoliceResponse;
import com.pnt.pnt_spring.domain.members.stat.api.resp.MemberThiefResponse;
import com.pnt.pnt_spring.global.api.response.CommonResponse;
import com.pnt.pnt_spring.global.api.response.PresignedUrlResponse;
import com.pnt.pnt_spring.global.utils.S3Service;
import com.pnt.pnt_spring.global.utils.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Member", description = "멤버 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

	private final MemberService memberService;
	private final S3Service s3Service;

	@Operation(summary = "멤버 프로필 조회", description = "멤버 ID를 받아 멤버 프로필을 조회합니다.")
	@GetMapping("/{id}")
	public CommonResponse<MemberProfileResponse> getMemberProfile(@PathVariable("id") Long memberId) {

		MemberProfileResponse response = memberService.getMemberProfile(memberId);
		return new CommonResponse<>(response, "멤버 프로필 조회 완료", HttpStatus.OK);
	}

	@Operation(summary = "멤버 경찰 프로필 조회", description = "멤버 ID를 받아 멤버의 경찰 프로필을 조회합니다.")
	@GetMapping("/{id}/police")
	public CommonResponse<MemberPoliceResponse> getMemberPoliceProfile(@PathVariable("id") Long memberId) {
		MemberPoliceResponse response = memberService.getPoliceProfile(memberId);
		return new CommonResponse<>(response, "경찰 프로필 조회 완료", HttpStatus.OK);
	}

	@Operation(summary = "멤버 도둑 프로필 조회", description = "멤버 ID를 받아 멤버의 도둑 프로필을 조회합니다.")
	@GetMapping("/{id}/thief")
	public CommonResponse<MemberThiefResponse> getMemberThiefProfile(@PathVariable("id") Long memberId) {
		MemberThiefResponse response = memberService.getThiefProfile(memberId);
		return new CommonResponse<>(response, "도둑 프로필 조회 완료", HttpStatus.OK);
	}

	@Operation(summary = "멤버 업데이트", description = "멤버 닉네임, 프로필 사진을 업데이트 합니다.")
	@PatchMapping("/{id}")
	public CommonResponse<MemberProfileUpdateResponse> updateProfile(
		@PathVariable("id") Long memberId,
		@RequestBody MemberProfileUpdateRequest request) {
		MemberProfileUpdateResponse response = memberService.updateProfile(memberId, request);
		return new CommonResponse<>(response, "프로필 수정 완료", HttpStatus.OK);
	}

	@Operation(summary = "S3 업로드 URL 발급", description = "이미지 업로드를 위한 Presigned URL과 저장될 Key를 반환합니다. downloadUrl은 Mission 위해 있는거니 상관X 해주시오")
	@GetMapping("/{id}/presigned-url")
	public CommonResponse<PresignedUrlResponse> getPresignedUrl(@RequestParam String fileName) {
		Long currentMemberId = SecurityUtils.currentMemberId();

		String pathPrefix = "profiles/" + currentMemberId;
		PresignedUrlResponse response = s3Service.getPresignedPutUrl(pathPrefix, fileName);

		return new CommonResponse<>(response, "업로드 URL 발급 완료", HttpStatus.OK);
	}

	@Operation(summary = "fcm 토큰 등록", description = "fcm token 등록을 위한 API, value에 토큰값을 담고, isActive에 활성화 여부 주세요. isActive 기본값은 false 입니다.")
	@PostMapping("/fcm-token")
	public CommonResponse<FcmTokenRegisterResponse> registerFcmToken(@RequestBody FcmTokenRegisterRequest request) {
		Long currentMemberId = SecurityUtils.currentMemberId();

		FcmTokenRegisterResponse response = memberService.registerFcmToken(currentMemberId, request);

		return new CommonResponse<>(response, "fcm 토큰 등록 완료", HttpStatus.CREATED);
	}

	@Operation(summary = "fcm 토큰 활성/비활성", description = "푸시 알림 활성/비활성 전환을 위한 API")
	@PatchMapping("/fcm-token")
	public CommonResponse<FcmTokenUpdateResponse> updateFcmToken(@RequestBody FcmTokenUpdateRequest request) {
		Long currentMemberId = SecurityUtils.currentMemberId();

		FcmTokenUpdateResponse response = memberService.updateFcmToken(currentMemberId, request);

		return new CommonResponse<>(response, "토큰 활성화/비활성화 완료", HttpStatus.OK);
	}
}
