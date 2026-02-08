package com.pnt.pnt_spring.domain.auth.api.req;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SocialLoginRequest {

	private String provider; // KAKAO or GOOGLE
	private String token; // 앱에서 받은 Access Token(Kakao) or ID Token(Google)

}
