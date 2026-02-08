package com.pnt.pnt_spring.domain.auth.application.impl;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pnt.pnt_spring.domain.auth.api.req.LoginRequest;
import com.pnt.pnt_spring.domain.auth.api.req.SignupRequest;
import com.pnt.pnt_spring.domain.auth.api.req.SocialLoginRequest;
import com.pnt.pnt_spring.domain.auth.api.req.TokenDto;
import com.pnt.pnt_spring.domain.auth.api.resp.LoginResponse;
import com.pnt.pnt_spring.domain.auth.api.resp.SignupResponse;
import com.pnt.pnt_spring.domain.auth.application.AuthService;
import com.pnt.pnt_spring.domain.auth.application.SocialTokenValidator;
import com.pnt.pnt_spring.domain.auth.jwt.JwtTokenProvider;
import com.pnt.pnt_spring.domain.games.game.repository.GradePoliceRepository;
import com.pnt.pnt_spring.domain.games.game.repository.GradeThiefRepository;
import com.pnt.pnt_spring.domain.members.member.entity.Member;
import com.pnt.pnt_spring.domain.members.member.entity.MemberAuthProvider;
import com.pnt.pnt_spring.domain.members.member.entity.MemberProfile;
import com.pnt.pnt_spring.domain.members.member.entity.MemberRole;
import com.pnt.pnt_spring.domain.members.member.entity.document.MemberDoc;
import com.pnt.pnt_spring.domain.members.member.repository.MemberAuthProviderRepository;
import com.pnt.pnt_spring.domain.members.member.repository.MemberProfileRepository;
import com.pnt.pnt_spring.domain.members.member.repository.jpa.MemberRepository;
import com.pnt.pnt_spring.domain.members.member.repository.mongo.MemberMongoRepository;
import com.pnt.pnt_spring.domain.members.stat.entity.GradePolice;
import com.pnt.pnt_spring.domain.members.stat.entity.GradeThief;
import com.pnt.pnt_spring.domain.members.stat.entity.MemberStatPolice;
import com.pnt.pnt_spring.domain.members.stat.entity.MemberStatThief;
import com.pnt.pnt_spring.domain.members.stat.repository.MemberStatPoliceRepository;
import com.pnt.pnt_spring.domain.members.stat.repository.MemberStatThiefRepository;
import com.pnt.pnt_spring.global.api.code.ErrorCode;
import com.pnt.pnt_spring.global.exception.BusinessException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

	private final MemberRepository memberRepository;
	private final MemberProfileRepository memberProfileRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final StringRedisTemplate redisTemplate;
	private final MemberMongoRepository memberMongoRepository;
	private final SocialTokenValidator socialTokenValidator;
	private final MemberAuthProviderRepository memberAuthProviderRepository;

	private final GradeThiefRepository gradeThiefRepository;
	private final GradePoliceRepository gradePoliceRepository;
	private final MemberStatThiefRepository memberStatThiefRepository;
	private final MemberStatPoliceRepository memberStatPoliceRepository;

	@Transactional
	public SignupResponse signup(SignupRequest request) {

		// 닉네임 중복 체크
		if (checkNicknameDuplicate(request.getNickname())) {
			throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME, "이미 사용 중인 닉네임입니다.");
		}

		// 비밀번호 일치 확인
		if (!request.getPassword().equals(request.getPasswordConfirm())) {
			throw new BusinessException(ErrorCode.VALIDATION_ERROR, "비밀번호가 일치하지 않습니다.");
		}

		// id 중복체크
		if (checkIdDuplicate(request.getId())) {
			throw new BusinessException(ErrorCode.DUPLICATE_USER_ID, "이미 사용 중인 아이디입니다.");
		}

		// 이메일 체크
		if (StringUtils.isNotBlank(request.getEmail()) && memberRepository.existsByEmail(request.getEmail())) {
			throw new BusinessException(ErrorCode.VALIDATION_ERROR, "이미 사용 중인 이메일입니다.");
		}

		// Member 엔터티 생성 및 저장
		Member member = Member.builder()
				.loginId(request.getId())
				.password(passwordEncoder.encode(request.getPassword()))
				.email(StringUtils.isBlank(request.getEmail()) ? null : request.getEmail())
				.birth(request.getBirth())
				.role(MemberRole.USER) // 일반 회원가입 시 유저 권한 부여
				.build();

		memberRepository.save(member);

		// MemberProfile 엔터티 생성 및 저장
		MemberProfile memberProfile = MemberProfile.builder()
				.member(member)
				.nickname(request.getNickname())
				.avatarUrl(request.getAvatarUrl())
				.build();

		memberProfileRepository.save(memberProfile);

		// mongodb에 member 정보 저장.
		MemberDoc memberDoc = MemberDoc.builder()
				.memberId(member.getId())
				.nickname(memberProfile.getNickname())
				.avatarUrl(memberProfile.getAvatarUrl())
				.build();

		memberMongoRepository.save(memberDoc);

		// 초기 등급 및 스탯 부여

		initMemberStats(member);

		return SignupResponse.from(member, memberProfile);
	}

	// 아이디 중복 체크
	public boolean checkIdDuplicate(String loginId) {
		return memberRepository.existsByLoginId(loginId);
	}

	// 닉네임 중복 체크
	public boolean checkNicknameDuplicate(String nickname) {
		return memberRepository.existsByMemberProfile_Nickname(nickname);
	}

	public LoginResponse login(LoginRequest request) {

		// 유저 조회
		Member member = memberRepository.findByLoginId(request.getId())
			.orElseThrow(() -> new BusinessException(ErrorCode.VALIDATION_ERROR, "존재하지 않는 아이디입니다."));

		// 패스워드 검사
		if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
			throw new BusinessException(ErrorCode.VALIDATION_ERROR, "비밀번호가 일치하지 않습니다.");
		}

		// 프로필 조회
		MemberProfile memberProfile = memberProfileRepository.findByMember(member)
			.orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND, "프로필을 찾을 수 없습니다"));

		// 인증 객체 생성 (DB에 저장된 Role 사용)
		Authentication authentication = new UsernamePasswordAuthenticationToken(
				member.getLoginId(),
				null,
				List.of(new SimpleGrantedAuthority(member.getRole().getKey())));

		// JWT 발급
		TokenDto tokenDto = jwtTokenProvider.generateToken(authentication, member.getId());

		// Redis
		// RT(Key - String) : loginId(Value - String)
		ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
		valueOperations.set(
				"RT:" + member.getLoginId(),
				tokenDto.getRefreshToken(),
				tokenDto.getRefreshTokenExpiresIn(),
				TimeUnit.MILLISECONDS);

		// 응답 반환
		return LoginResponse.of(tokenDto, member, memberProfile);
	}

	@Transactional
	public void logout(String accessToken) {
		// Access Token 검증
		if (!jwtTokenProvider.validateToken(accessToken)) {
			throw new BusinessException(ErrorCode.INVALID_TOKEN, "유효하지 않는 토큰입니다");
		}

		// Access Token 에서 유저 정보
		Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
		String loginId = authentication.getName();

		// redis 에서 해당 유저 refresh token 삭제
		if (redisTemplate.opsForValue().get("RT:" + loginId) != null) {
			redisTemplate.delete("RT:" + loginId);
		}

		// 해당 Access Token 로그아웃(블랙리스트 처리)
		Long expiration = jwtTokenProvider.getExpiration(accessToken);
		if (expiration > 0) {
			redisTemplate.opsForValue().set("BL:" + accessToken, "logout", expiration, TimeUnit.SECONDS);
		}
	}

	@Override
	@Transactional
	public LoginResponse socialLogin(SocialLoginRequest request) {

		String providerId = socialTokenValidator.validateAndGetId(request.getProvider(), request.getToken());

		String provider = request.getProvider().toUpperCase();

		// 기존 가입 여부 확인
		MemberAuthProvider authProvider = memberAuthProviderRepository
				.findByProviderAndProviderUserKey(provider, providerId)
				.orElse(null);

		Member member;

		if (authProvider == null) {
			// 신규 회원가입 (자동 가입)
			String socialLoginId = provider + "_" + providerId; // 예: KAKAO_12345

			// Member 생성 (빌더 패턴 활용)
			member = Member.builder()
					.loginId(socialLoginId)
					.password(UUID.randomUUID().toString()) // 비밀번호는 랜덤 처리
					.email(socialLoginId + "@social.user") // 이메일 없을 경우 임시 처리
					.role(MemberRole.USER)
					.build();
			memberRepository.save(member);

			// MemberProfile 생성
			MemberProfile memberProfile = MemberProfile.builder()
					.member(member)
					.nickname("User_" + providerId.substring(0, 5))
					.avatarUrl("default")
					.build();
			memberProfileRepository.save(memberProfile);

			authProvider = MemberAuthProvider.builder()
					.member(member)
					.provider(provider)
					.providerUserKey(providerId)
					.build();
			memberAuthProviderRepository.save(authProvider);

			MemberDoc memberDoc = MemberDoc.builder()
					.memberId(member.getId())
					.nickname(memberProfile.getNickname())
					.avatarUrl(memberProfile.getAvatarUrl())
					.build();
			memberMongoRepository.save(memberDoc);

			// 초기 등급 및 스탯 부여
			initMemberStats(member);

		} else {
			// 기존 회원이면 정보 로드
			member = authProvider.getMember();
		}

		// JWT 발급 및 로그인
		Authentication authentication = new UsernamePasswordAuthenticationToken(
				member.getLoginId(),
				null,
				List.of(new SimpleGrantedAuthority(member.getRole().getKey())));

		TokenDto tokenDto = jwtTokenProvider.generateToken(authentication, member.getId());

		// Redis에 Refresh Token 저장
		redisTemplate.opsForValue().set(
				"RT:" + member.getLoginId(),
				tokenDto.getRefreshToken(),
				tokenDto.getRefreshTokenExpiresIn(),
				TimeUnit.MILLISECONDS);

		// 프로필 조회 (Lazy Loading 이슈 방지용 조회)
		MemberProfile memberProfile = memberProfileRepository.findByMember(member)
				.orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));

		return LoginResponse.of(tokenDto, member, memberProfile);
	}

	@Override
	@Transactional
	public TokenDto reissue(TokenDto tokenDto) {
		// Refresh Token 검증 (만료 여부 및 서명 확인)
		if (!jwtTokenProvider.validateToken(tokenDto.getRefreshToken())) {
			throw new BusinessException(ErrorCode.INVALID_TOKEN, "유효하지 않은 리프레시 토큰입니다.");
		}

		// Refresh Token에서 Member LoginId 가져오기
		String refreshToken = tokenDto.getRefreshToken();
		String loginId = jwtTokenProvider.getMemberLoginId(refreshToken);

		// Redis에서 저장된 Refresh Token 가져오기
		String redisRefreshToken = redisTemplate.opsForValue().get("RT:" + loginId);

		// Redis에 저장된 토큰이 없거나(로그아웃 등), 요청된 토큰과 일치하지 않는 경우
		if (StringUtils.isEmpty(redisRefreshToken) || !redisRefreshToken.equals(refreshToken)) {
			throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
		}

		// 새로운 토큰 생성을 위해 Member 정보 조회
		// (토큰의 정보보다 DB의 최신 정보를 기준으로 권한 등을 다시 담는 것이 안전함)
		Member member = memberRepository.findByLoginId(loginId)
				.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

		Authentication authentication = new UsernamePasswordAuthenticationToken(
				member.getLoginId(),
				null,
				List.of(new SimpleGrantedAuthority(member.getRole().getKey())));

		// 새로운 토큰 발급
		TokenDto newTokenDto = jwtTokenProvider.generateToken(authentication, member.getId());

		// Redis에 새로운 Refresh Token 저장 (RTR: Refresh Token Rotation 적용 시)
		redisTemplate.opsForValue().set(
				"RT:" + loginId,
				newTokenDto.getRefreshToken(),
				newTokenDto.getRefreshTokenExpiresIn(),
				TimeUnit.MILLISECONDS);

		return newTokenDto;
	}

	private void initMemberStats(Member member) {
		// 1. 도둑 초기 세팅
		GradeThief initialThiefGrade = gradeThiefRepository.findById(1L)
				.orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
						"도둑 초기 등급(ID:1) 데이터가 없습니다. DB 초기화를 확인하세요."));

		MemberStatThief thiefStat = MemberStatThief.createInitial(member, initialThiefGrade);
		memberStatThiefRepository.save(thiefStat);

		// 2. 경찰 초기 세팅
		GradePolice initialPoliceGrade = gradePoliceRepository.findById(1L)
				.orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
						"경찰 초기 등급(ID:1) 데이터가 없습니다. DB 초기화를 확인하세요."));

		MemberStatPolice policeStat = MemberStatPolice.createInitial(member, initialPoliceGrade);
		memberStatPoliceRepository.save(policeStat);
	}

}
