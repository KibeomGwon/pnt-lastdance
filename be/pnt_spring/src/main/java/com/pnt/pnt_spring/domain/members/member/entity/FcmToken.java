package com.pnt.pnt_spring.domain.members.member.entity;

import com.pnt.pnt_spring.domain.utils.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "fcm_token",
	uniqueConstraints = {
		@UniqueConstraint(columnNames = {"member_id"})
	}
)
public class FcmToken extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JoinColumn(name = "member_id", unique = true)
	@OneToOne
	private Member member;

	@Column(name = "value", nullable = false)
	private String value;

	@Column(name = "is_active", nullable = false)
	@Builder.Default
	private boolean isActive = false;

	@Column(name = "is_deleted", nullable = false)
	@Builder.Default
	private boolean isDeleted = false;

	// active 업데이트
	public void updateActive(boolean isActive) {
		this.isActive = isActive;
	}

	public void updateValue(String value) {
		this.value = value;
	}
}
