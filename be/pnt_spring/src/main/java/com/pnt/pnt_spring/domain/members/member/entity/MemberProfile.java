package com.pnt.pnt_spring.domain.members.member.entity;

import com.pnt.pnt_spring.domain.utils.BaseEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "member_profile")
public class MemberProfile extends BaseEntity {

	@Id
	private Long id;

	@MapsId
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	@Column(length = 20)
	private String nickname;

	@Builder.Default
	@Column(length = 2048)
	private String avatarUrl = "default.png";

	public void updateProfile(String nickname, String avatarUrl) {
		if (nickname != null) {
			this.nickname = nickname;
		}
		if (avatarUrl != null) {
			this.avatarUrl = avatarUrl;
		}
	}
}