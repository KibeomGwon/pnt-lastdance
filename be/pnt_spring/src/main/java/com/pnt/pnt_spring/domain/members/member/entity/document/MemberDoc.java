package com.pnt.pnt_spring.domain.members.member.entity.document;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.persistence.Id;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Document(collection = "members")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class MemberDoc {
	@Id
	private String id;
	@Indexed(unique = true, name = "memberId_1")
	@Field(name = "memberId")
	private Long memberId;
	@Size(max = 20)
	private String nickname;
	@Builder.Default
	private String avatarUrl = "default.png";
	@Builder.Default
	private boolean isDeleted = false;
	@CreatedDate
	private LocalDateTime createdAt; // 생성 일시
	@LastModifiedDate
	private LocalDateTime updatedAt; // 수정 일시

	public void update(String nickname, String avatarUrl) {
		if (nickname != null) {
			this.nickname = nickname;
		}
		if (avatarUrl != null) {
			this.avatarUrl = avatarUrl;
		}
	}
}
