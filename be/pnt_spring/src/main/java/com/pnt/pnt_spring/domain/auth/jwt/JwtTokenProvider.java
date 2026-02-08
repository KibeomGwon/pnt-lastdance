package com.pnt.pnt_spring.domain.auth.jwt;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.pnt.pnt_spring.domain.auth.api.req.TokenDto;
import com.pnt.pnt_spring.global.api.code.ErrorCode;
import com.pnt.pnt_spring.global.exception.BusinessException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtTokenProvider {

	private static final String AUTHORITIES_KEY = "role";
	private static final String MEMBER_ID_KEY = "memberId";
	private static final String BEARER_TYPE = "Bearer";
	private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 4;        // 4시간
	private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7;  // 7일

	private final Key key;
	private final StringRedisTemplate redisTemplate;

	public JwtTokenProvider(@Value("${jwt.secret}") String secretKey, StringRedisTemplate redisTemplate) {
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		this.key = Keys.hmacShaKeyFor(keyBytes);
		this.redisTemplate = redisTemplate;
	}

	public TokenDto generateToken(Authentication authentication, Long memberId) {
		// 권한 가져오기
		String authorities = authentication.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.collect(Collectors.joining(","));

		long now = (new Date()).getTime();

		// Access Token 생성
		Date accessTokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);

		String accessToken = Jwts.builder()
			.setSubject(authentication.getName()) // loginId (인증용)
			.claim(MEMBER_ID_KEY, memberId) // Long memberId (웹소켓 용)
			.claim(AUTHORITIES_KEY, authorities) // Role
			.setExpiration(accessTokenExpiresIn) // 만료 시간
			.signWith(key, SignatureAlgorithm.HS512) // 서명
			.compact();

		// Refresh Token 생성
		String refreshToken = Jwts.builder()
			.setSubject(authentication.getName())
			.setExpiration(new Date(now + REFRESH_TOKEN_EXPIRE_TIME))
			.signWith(key, SignatureAlgorithm.HS512)
			.compact();

		return TokenDto.builder()
			.grantType(BEARER_TYPE)
			.accessToken(accessToken)
			.accessTokenExpiresIn(accessTokenExpiresIn.getTime())
			.refreshToken(refreshToken)
			.refreshTokenExpiresIn(REFRESH_TOKEN_EXPIRE_TIME)
			.build();
	}

	public Authentication getAuthentication(String accessToken) {
		// 토큰 복호화
		Claims claims = parseClaims(accessToken);

		if (claims.get(AUTHORITIES_KEY) == null) {
			throw new RuntimeException("권한 정보가 없는 토큰입니다.");
		}

		// 클레임에서 권한 정보 가져오기
		Collection<? extends GrantedAuthority> authorities =
			Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
				.map(SimpleGrantedAuthority::new)
				.collect(Collectors.toList());

		Long memberId = claims.get(MEMBER_ID_KEY, Long.class);
		// UserDetails 객체를 만들어서 Authentication 리턴
		UserDetails principal = new CustomUserDetails(claims.getSubject(), "", authorities, memberId);

		return new UsernamePasswordAuthenticationToken(principal, "", authorities);
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
			return true;
		} catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
			throw new BusinessException(ErrorCode.INVALID_TOKEN, "잘못된 JWT 서명입니다.");
		} catch (ExpiredJwtException e) {
			throw new BusinessException(ErrorCode.EXPIRED_TOKEN, "만료된 JWT 토큰입니다.");
		} catch (UnsupportedJwtException e) {
			throw new BusinessException(ErrorCode.INVALID_TOKEN, "지원되지 않는 JWT 토큰입니다.");
		} catch (IllegalArgumentException e) {
			throw new BusinessException(ErrorCode.INVALID_TOKEN, "JWT 토큰이 잘못되었습니다.");
		}
	}

	private Claims parseClaims(String accessToken) {
		try {
			return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
		} catch (ExpiredJwtException e) {
			return e.getClaims();
		}
	}

	// 리프레시 토큰에서 LoginId(Subject)만 추출하기 위한 메서드
	public String getMemberLoginId(String token) {
		return parseClaims(token).getSubject();
	}

	// 토큰 남은 유효시간 계산
	public Long getExpiration(String accessToken) {
		Date expiration = parseClaims(accessToken).getExpiration();
		long now = new Date().getTime();
		return expiration.getTime() - now;
	}
}