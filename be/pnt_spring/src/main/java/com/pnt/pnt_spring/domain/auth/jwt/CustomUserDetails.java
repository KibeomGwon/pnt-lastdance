package com.pnt.pnt_spring.domain.auth.jwt;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import lombok.Getter;

@Getter
public class CustomUserDetails extends User {

	private final Long memberId;

	public CustomUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities,
		Long memberId) {
		super(username, password, authorities);
		this.memberId = memberId;
	}

}
