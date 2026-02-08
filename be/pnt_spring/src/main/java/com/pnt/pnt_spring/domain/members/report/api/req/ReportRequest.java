package com.pnt.pnt_spring.domain.members.report.api.req;

import com.pnt.pnt_spring.domain.members.report.entity.ReportReason;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReportRequest {
	private String reportedNickname;
	private ReportReason reason;
	private String detail;
}