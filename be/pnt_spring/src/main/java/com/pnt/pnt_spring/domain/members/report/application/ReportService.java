package com.pnt.pnt_spring.domain.members.report.application;

import com.pnt.pnt_spring.domain.members.report.api.req.ReportRequest;
import com.pnt.pnt_spring.domain.members.report.api.resp.ReportResponse;

public interface ReportService {

	ReportResponse createReport(Long reporterId, ReportRequest request);

}
