package com.pnt.pnt_spring.domain.members.report.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportStatus {
	RECEIVED("REPORT_RECEIVED", "접수됨"),
	COMPLETED("REPORT_COMPLETED", "처리완료");

	private final String key;
	private final String value;
}
