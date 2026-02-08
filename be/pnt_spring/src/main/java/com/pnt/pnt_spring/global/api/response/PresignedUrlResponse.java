package com.pnt.pnt_spring.global.api.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PresignedUrlResponse {
	private String presignedUrl; // 업로드용 (PUT)
	private String downloadUrl;  // 다운로드/조회용 (GET)
	private String imageKey;     // S3 파일 키
}