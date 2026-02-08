package com.pnt.pnt_spring.domain.members.report.api.resp;

import java.time.OffsetDateTime;

import com.pnt.pnt_spring.domain.members.report.entity.ReportStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
	private Long reportId;
	private ReportStatus status;
	private OffsetDateTime createdAt;
}