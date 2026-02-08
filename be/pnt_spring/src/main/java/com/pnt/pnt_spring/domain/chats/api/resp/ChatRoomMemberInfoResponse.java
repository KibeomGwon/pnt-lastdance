package com.pnt.pnt_spring.domain.chats.api.resp;

import com.pnt.pnt_spring.domain.members.member.entity.Member;
import com.pnt.pnt_spring.domain.members.member.entity.MemberProfile;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatRoomMemberInfoResponse {

	private Long memberId;
	private String nickname;
	private String avatarUrl;
	private boolean connected;
	private boolean owner;

	public static ChatRoomMemberInfoResponse of(
		Member member,
		boolean connected,
		Long ownerId
	) {
		MemberProfile profile = member.getMemberProfile();
		return ChatRoomMemberInfoResponse.builder()
			.memberId(member.getId())
			.nickname(profile != null ? profile.getNickname() : null)
			.avatarUrl(profile != null ? profile.getAvatarUrl() : null)
			.connected(connected)
			.owner(ownerId != null && ownerId.equals(member.getId()))
			.build();
	}
}
