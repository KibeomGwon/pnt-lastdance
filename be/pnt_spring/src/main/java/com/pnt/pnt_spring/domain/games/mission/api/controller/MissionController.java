package com.pnt.pnt_spring.domain.games.mission.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pnt.pnt_spring.domain.games.mission.api.resp.GameMissionResponse;
import com.pnt.pnt_spring.domain.games.mission.api.resp.MissionResponse;
import com.pnt.pnt_spring.domain.games.mission.application.MissionService;
import com.pnt.pnt_spring.global.api.response.CommonResponse;
import com.pnt.pnt_spring.global.api.response.PresignedUrlResponse;
import com.pnt.pnt_spring.global.utils.S3Service;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Game Mission", description = "게임 미션 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/games")
public class MissionController {

	private final MissionService missionService;
	private final S3Service s3Service;

	@Operation(summary = "전체 미션 목록 조회 (원본)", description = "게임 할당 여부와 관계없이 시스템에 등록된 모든 원본 미션 목록을 조회합니다.")
	@GetMapping("/missions")
	public CommonResponse<List<MissionResponse>> getAllMissions() {
		List<MissionResponse> response = missionService.getAllMissions();
		return new CommonResponse<>(response, "전체 미션 목록 조회 성공", HttpStatus.OK);
	}

	@Operation(summary = "미션 상세 조회", description = "미션 상세 정보를 조회합니다.")
	@GetMapping("/missions/{missionId}")
	public CommonResponse<MissionResponse> getMission(
		@PathVariable Long missionId) {

		MissionResponse response = missionService.getMission(missionId);

		return new CommonResponse<>(response, "미션 상세 조회 성공", HttpStatus.OK);
	}

	@Operation(summary = "인게임 미션 목록 조회", description = "게임에 할당된 미션 목록을 조회합니다. (진행 상태 포함)")
	@GetMapping("/{gameId}/missions")
	public CommonResponse<List<GameMissionResponse>> getMissions(@PathVariable Long gameId) {
		List<GameMissionResponse> response = missionService.getGameAllMissions(gameId); // 타입 변경
		return new CommonResponse<>(response, "미션 목록 조회 성공", HttpStatus.OK);
	}

	@Operation(summary = "인게임 미션 상세 조회", description = "게임에 할당된 미션 상세 정보를 조회합니다. (진행 상태 포함)")
	@GetMapping("/{gameId}/missions/{missionId}")
	public CommonResponse<GameMissionResponse> getMissionDetail(
		@PathVariable Long gameId,
		@PathVariable Long missionId) {

		GameMissionResponse response = missionService.getGameMission(gameId, missionId); // 타입 변경
		return new CommonResponse<>(response, "미션 상세 조회 성공", HttpStatus.OK);
	}

	@Operation(summary = "미션 사진 업로드 URL 발급", description = "미션 수행 사진을 S3에 업로드하기 위한 Presigned URL을 발급합니다.")
	@GetMapping("/missions/upload-url")
	public CommonResponse<PresignedUrlResponse> getMissionUploadUrl(@RequestParam String fileName) {

		// 1. 업로드용(PUT) URL 발급 (기존 로직)
		PresignedUrlResponse putResponse = s3Service.getPresignedPutUrl("missions", fileName);

		// 2. 조회용(GET) URL 별도 생성
		// putResponse에 들어있는 imageKey를 꺼내서 GET URL을 만듭니다.
		String downloadUrl = s3Service.getPresignedGetUrl(putResponse.getImageKey());

		// 3. 두 URL을 모두 포함하여 새로운 응답 객체 생성
		PresignedUrlResponse finalResponse = PresignedUrlResponse.builder()
			.presignedUrl(putResponse.getPresignedUrl()) // PUT URL
			.imageKey(putResponse.getImageKey())         // Key
			.downloadUrl(downloadUrl)                    // GET URL (여기가 채워져야 함!)
			.build();

		return new CommonResponse<>(finalResponse, "업로드 URL 발급 완료", HttpStatus.OK);
	}

}