package com.pnt.pnt_spring.global.utils;

import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.pnt.pnt_spring.global.api.response.PresignedUrlResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

	private final S3Presigner s3Presigner;
	private final S3Client s3Client;

	@Value("${cloud.aws.s3.bucket}")
	private String bucketName;

	/**
	 * Presigned URL 생성 메서드 (범용)
	 * prefix 경로 아래에 UUID 파일명으로 저장
	 * 예) prefix="profiles/1" -> "profiles/1/uuid.jpg"
	 * 예) prefix="missions"   -> "missions/uuid.jpg"
	 */
	public PresignedUrlResponse getPresignedPutUrl(String prefix, String fileName) {
		if (fileName == null || fileName.isBlank()) {
			return null;
		}

		// 1. 확장자 추출
		String extension = "";
		int dotIndex = fileName.lastIndexOf(".");
		if (dotIndex > 0) {
			extension = fileName.substring(dotIndex).toLowerCase();
		} else {
			extension = ".jpg";
		}

		// 2. Content-Type 결정
		String contentType = "image/jpeg";
		if (extension.equals(".png")) {
			contentType = "image/png";
		}

		// 3. Key 생성 (prefix + "/" + UUID + ext)
		String key = prefix + "/" + UUID.randomUUID() + extension;

		PutObjectRequest objectRequest = PutObjectRequest.builder()
			.bucket(bucketName)
			.key(key)
			.contentType(contentType)
			.build();

		PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
			.signatureDuration(Duration.ofMinutes(5))
			.putObjectRequest(objectRequest)
			.build();

		String presignedUrl = s3Presigner.presignPutObject(presignRequest).url().toString();

		return PresignedUrlResponse.builder()
			.presignedUrl(presignedUrl)
			.imageKey(key)
			.build();
	}

	// 2. 조회용 URL (GET) - Private 이미지를 볼 수 있게 함
	public String getPresignedGetUrl(String path) {
		if (path == null || path.isBlank()) return null;

		GetObjectRequest objectRequest = GetObjectRequest.builder()
			.bucket(bucketName)
			.key(path)
			.build();

		GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
			.signatureDuration(Duration.ofHours(3)) // 3시간 유효
			.getObjectRequest(objectRequest)
			.build();

		return s3Presigner.presignGetObject(presignRequest).url().toString();
	}

	/**
	 * S3 파일 삭제 메서드
	 * @param path (Key) 삭제할 파일의 경로 (예: profiles/1/uuid_image.jpg)
	 */
	public void deleteFile(String path) {
		if (path == null || path.isBlank()) {
			return;
		}

		try {
			DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
				.bucket(bucketName)
				.key(path)
				.build();

			s3Client.deleteObject(deleteRequest);
		} catch (Exception e) {
			log.error("S3 파일 삭제 중 오류 발생: {}", e.getMessage());
		}
	}
}