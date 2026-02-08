package com.pnt.pnt_spring.domain.members.report.application.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pnt.pnt_spring.domain.members.report.api.req.ReportRequest;
import com.pnt.pnt_spring.domain.members.report.api.resp.ReportResponse;
import com.pnt.pnt_spring.domain.members.report.application.ReportService;
import com.pnt.pnt_spring.domain.members.report.entity.Report;
import com.pnt.pnt_spring.domain.members.report.repository.ReportRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
	private final ReportRepository reportRepository;

	@Transactional
	public ReportResponse createReport(Long reporterId, ReportRequest request) {
		Report report = Report.builder()
			.reporterId(reporterId)
			.reportedNickname(request.getReportedNickname())
			.reason(request.getReason())
			.detail(request.getDetail())
			.build();

		Report savedReport = reportRepository.save(report);

		return ReportResponse.builder()
			.reportId(savedReport.getId())
			.status(savedReport.getStatus())
			.createdAt(savedReport.getCreatedAt())
			.build();
	}
}