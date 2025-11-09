package checkmo.member.internal.service.command;

import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
import checkmo.member.internal.converter.MemberConverter;
import checkmo.member.internal.entity.Member;
import checkmo.member.internal.repository.MemberRepository;
import checkmo.member.internal.service.common.EmailSender;
import checkmo.member.internal.service.query.MemberQueryService;
import checkmo.member.web.dto.MemberRequestDTO;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberRegistrationCommandServiceImpl implements MemberRegistrationCommandService {

    // 이메일 인증 관련 상수
    private static final String EMAIL_VERIFICATION_PREFIX = "verification:";
    private static final Duration EMAIL_VERIFICATION_TTL = Duration.ofMinutes(10); // 10분
    // 랜덤 인증번호 생성용 정적 필드
    private static final SecureRandom secureRandom = new SecureRandom();
    // 자신의 CommandService
    private final MemberCategoryCommandService memberCategoryCommandService;
    // 자신의 QueryService
    private final MemberQueryService memberQueryService;
    // 자신의 Repository
    private final MemberRepository memberRepository;
    // 인증 관련 서비스
    private final PasswordEncoder passwordEncoder;
    // 외부 서비스
    private final RedisTemplate<String, Object> redisTemplate;
    private final EmailSender emailSender;

    @Override
    public void sendEmailVerification(String email) {

        // 이미 인증번호가 Redis에 존재하면 예외 처리
        String redisKey = EMAIL_VERIFICATION_PREFIX + email;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
            throw new GeneralException(ErrorStatus.EMAIL_VERIFICATION_CODE_ALREADY_SENT);
        }

        // 이미 회원가입이 완료된 이메일인지 확인하는 로직
        if (memberRepository.existsByEmail(email)) {
            throw new GeneralException(ErrorStatus.MEMBER_ALREADY_EXISTS);
        }

        // 6자리 랜덤 인증번호 생성
        String verificationCode = String.format("%06d", secureRandom.nextInt(1000000));

        // Redis에 인증번호 저장
        Map<String, Object> verificationData = new HashMap<>();
        verificationData.put("code", verificationCode);
        verificationData.put("verified", false);

        redisTemplate.opsForHash().putAll(redisKey, verificationData);
        redisTemplate.expire(redisKey, EMAIL_VERIFICATION_TTL);

        // 이메일 발송 메서드 호출
        emailSender.sendEmail(email, verificationCode);

        log.info("Verification code sent to email: {}", email);
    }

    @Override
    public boolean verifyEmailCode(MemberRequestDTO.EmailVerificationRequestDTO request) {

        String redisKey = EMAIL_VERIFICATION_PREFIX + request.getEmail();

        // redis에서 인증 정보 조회
        String storedCode = (String) redisTemplate.opsForHash().get(redisKey, "code");
        Boolean isVerified = (Boolean) redisTemplate.opsForHash().get(redisKey, "verified");

        // 인증번호가 만료된 경우
        if (storedCode == null) {
            throw new GeneralException(ErrorStatus.EMAIL_VERIFICATION_CODE_EXPIRED);
        }

        // 인증번호가 일치하지 않는 경우
        if (!request.getVerificationCode().equals(storedCode)) {
            throw new GeneralException(ErrorStatus.EMAIL_VERIFICATION_CODE_INVALID);
        }

        // 이미 인증된 경우
        if (Boolean.TRUE.equals(isVerified)) {
            throw new GeneralException(ErrorStatus.EMAIL_VERIFICATION_CODE_ALREADY_VERIFIED);
        }

        // 인증 성공 시 verified 상태 업데이트
        redisTemplate.opsForHash().put(redisKey, "verified", true);

        return true;
    }

    @Override
    @Transactional
    public Member signUp(MemberRequestDTO.SignUpRequestDTO request) {

        // 이메일 중복 확인
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new GeneralException(ErrorStatus.MEMBER_ALREADY_EXISTS);
        }

        // 이메일 인증 여부 확인
        String redisKey = EMAIL_VERIFICATION_PREFIX + request.getEmail();
        Boolean isVerified = (Boolean) redisTemplate.opsForHash().get(redisKey, "verified");
        if (!Boolean.TRUE.equals(isVerified)) {
            throw new GeneralException(ErrorStatus.EMAIL_NOT_VERIFIED);
        }

        // 회원 정보 저장
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        Member newMember = MemberConverter.fromSignUpRequestDTO(request, encodedPassword);

        memberRepository.save(newMember);
        redisTemplate.delete(redisKey); // 회원가입 후 인증 정보 삭제

        return newMember;
    }

    @Override
    @Transactional
    public void addAdditionalInfo(String memberId, MemberRequestDTO.AdditionalInfoDTO request) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 이미 프로필이 완성된 경우 예외
        if (member.isProfileCompleted()) {
            throw new GeneralException(ErrorStatus.MEMBER_PROFILE_ALREADY_COMPLETED);
        }

        // --추가 정보 업데이트 하기--

        // 일단 닉네임 중복 체크
        if (memberQueryService.isNicknameDuplicated(request.getNickname())) {
            throw new GeneralException(ErrorStatus.NICKNAME_ALREADY_EXISTS);
        }

        // 멤버 엔티티 업데이트 (일단 카테고리 빼고)
        member.updateAdditionalInfo(
                request.getNickname(),
                request.getDescription(),
                request.getImgUrl()
        );

        // 관심 카테고리 저장
        memberCategoryCommandService.modifyMemberCategories(memberId, request.getCategoryIds());

        // 프로필 완료 상태로 변경
        member.completeProfile();
    }
}
