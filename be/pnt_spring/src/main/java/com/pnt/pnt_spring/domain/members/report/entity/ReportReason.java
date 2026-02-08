package com.pnt.pnt_spring.domain.members.report.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportReason {
	ABUSE("REASON_ABUSE", "과격한 플레이"),
	CHEAT("REASON_CHEAT", "정당하지 않은 플레이"),
	HATE("REASON_HATE", "분쟁"),
	OTHER("REASON_OTHER", "이외");

	private final String key;
	private final String value;
}

