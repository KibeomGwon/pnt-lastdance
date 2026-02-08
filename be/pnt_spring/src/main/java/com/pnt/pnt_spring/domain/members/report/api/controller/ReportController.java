package com.pnt.pnt_spring.domain.members.report.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pnt.pnt_spring.domain.members.report.api.req.ReportRequest;
import com.pnt.pnt_spring.domain.members.report.api.resp.ReportResponse;
import com.pnt.pnt_spring.domain.members.report.application.ReportService;
import com.pnt.pnt_spring.global.api.response.CommonResponse;
import com.pnt.pnt_spring.global.utils.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Report", description = "멤버 신고 관련 API")
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {
	private final ReportService reportService;

	@Operation(summary = "멤버 신고", description = "멤버 신고 기능을 지원합니다.")
	@PostMapping
	public CommonResponse<ReportResponse> report(@RequestBody ReportRequest request) {
		Long memberId = SecurityUtils.currentMemberId();
		ReportResponse response = reportService.createReport(memberId, request);
		return new CommonResponse<>(response, "신고 접수 완료", HttpStatus.CREATED);
	}
}