package checkmo.member.internal.service.command;

import checkmo.authentication.AuthenticationAPI;
import checkmo.common.validation.NicknamePolicy;
import checkmo.member.MemberEvent;
import checkmo.member.internal.converter.MemberConverter;
import checkmo.member.internal.entity.Member;
import checkmo.member.internal.exception.MemberErrorStatus;
import checkmo.member.internal.exception.MemberException;
import checkmo.member.internal.repository.FollowRepository;
import checkmo.member.internal.repository.MemberBlockRepository;
import checkmo.member.internal.repository.MemberRepository;
import checkmo.member.web.dto.MemberRequestDTO;
import checkmo.member.web.dto.MemberResponseDTO.DetailInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashSet;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
@Transactional
@Service
public class MemberCommandService {

    private final AuthenticationAPI authenticationAPI;

    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
    private final MemberBlockRepository memberBlockRepository;

    private final ApplicationEventPublisher eventPublisher;

    public void createMember(Long memberId, String legacyId, String email) {
        Member member = Member.builder()
                .id(memberId)
                .legacyId(legacyId)
                .email(email)
                .name("")
                .phoneNumber("")
                .description("")
                .imgUrl(null)
                .build();

        memberRepository.save(member);
    }

    /**
     * 회원 추가 정보 입력
     *
     * @param request 추가 정보 DTO (닉네임, 프로필 이미지, 관심 카테고리)
     * @return void -> 어차피 회원 프로필 정보 완료 후에는 메인 화면에 로그인된 상태로 리다이렉트
     */
    public void addAdditionalInfo(Long memberId, MemberRequestDTO.AdditionalInfo request) {
        Member member = findActiveMember(memberId);

        if (!StringUtils.hasText(request.getNickname())) {
            throw new MemberException(MemberErrorStatus.NICKNAME_REQUIRED);
        }

        String nicknameKey = NicknamePolicy.comparisonKey(request.getNickname());
        ensureNicknameAvailable(nicknameKey);

        member.updateAdditionalInfo(
                request.getNickname(),
                request.getName(),
                request.getPhoneNumber(),
                request.getDescription()
        );

        member.updateImageAndInterestCategories(
                request.getImgUrl(),
                new HashSet<>(request.getCategories())
        );

        flushNicknameChange();
        authenticationAPI.updateNickname(memberId, member.getNickName());

        // 프로필 완료 상태로 변경
        authenticationAPI.completeProfile(memberId);

        // 회원 등록 완료 이벤트 발행
        eventPublisher.publishEvent(
                MemberEvent.MemberRegistrationCompleted.builder()
                        .memberId(memberId)
                        .build());
    }

    /**
     * 회원 프로필 정보 수정
     *
     * @param memberId 수정할 회원의 ID
     * @param request  수정할 프로필 정보 DTO - 닉네임, 프로필 이미지, 간단 소개, 관심 카테고리
     *                 (닉네임은 미전송/빈 값이면 변경 없음, 현재 닉네임과 같으면 스킵, 타인이 사용 중이면 NICKNAME_ALREADY_EXISTS)
     * @return 수정된 회원 프로필 정보 엔티티 - 이때는 관심 카테고리 정보 DTO에 포함 X , -> 반드시 CategoryQueryFacade를 통해 조회해야 함
     */
    public DetailInfo updateProfile(
            Long memberId,
            MemberRequestDTO.MemberProfileUpdate request
    ) {
        // 회원 조회
        Member member = findActiveMember(memberId);

        // 닉네임 변경: 값이 있고 현재 닉네임과 다를 때만 처리(중복 검사 후 Member + AuthUser 동기 갱신)
        String requestedNickname = request.getNickname();
        if (StringUtils.hasText(requestedNickname)) {
            String normalizedNickname = NicknamePolicy.normalize(requestedNickname);
            boolean sameIdentity = member.hasSameNicknameIdentity(normalizedNickname);
            if (!sameIdentity) {
                ensureNicknameAvailable(NicknamePolicy.comparisonKey(normalizedNickname));
            }
            if (!normalizedNickname.equals(member.getNickName())) {
                member.updateNickname(normalizedNickname);
                flushNicknameChange();
                authenticationAPI.updateNickname(memberId, member.getNickName());
            }
        }

        String existingImageUrl = member.getImgUrl();
        String newImageUrl = request.getImgUrl();

        // 기존 이미지와 새로운 이미지가 다를 경우 S3에서 기존 이미지 삭제 이벤트 발행
        // 새로운 이미지 url이 null이면 기존 이미지 삭제
        if (existingImageUrl != null && !existingImageUrl.equals(newImageUrl)) {
            eventPublisher.publishEvent(
                    MemberEvent.DeleteProfileImage.builder()
                            .imageUrl(existingImageUrl)
                            .build());
        }

        member.updateProfile(request.getDescription(), newImageUrl, request.getPhoneNumber());

        if (request.getCategories() != null) {
            member.updateInterestCategories(new HashSet<>(request.getCategories()));
        }

        return MemberConverter.toMemberProfileWithCategory(member, isSocialMember(memberId));
    }

    private void ensureNicknameAvailable(String nicknameKey) {
        if (memberRepository.existsByNickNameKeyAndDeactivatedAtIsNull(nicknameKey)) {
            throw new MemberException(MemberErrorStatus.NICKNAME_ALREADY_EXISTS);
        }
    }

    private void flushNicknameChange() {
        try {
            memberRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new MemberException(MemberErrorStatus.NICKNAME_ALREADY_EXISTS);
        }
    }

    /**
     * 회원 비밀번호 변경 (일반 회원만)
     *
     * @param memberId 비밀번호를 변경할 회원의 ID
     * @param request  비밀번호 변경 정보 DTO (현재 비밀번호, 새 비밀번호, 새 비밀번호 확인)
     */
    public void updatePassword(Long memberId, MemberRequestDTO.UpdatePassword request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new MemberException(MemberErrorStatus.PASSWORD_MISMATCH);
        }

        boolean isSuccess = authenticationAPI.updatePassword(memberId, request.getCurrentPassword(), request.getNewPassword());

        if (!isSuccess) {
            throw new MemberException(MemberErrorStatus.CURRENT_PASSWORD_INCORRECT);
        }
    }

    /**
     * 회원 계정 삭제 (soft delete)
     *
     * @param memberId 비활성화할 회원의 ID
     */
    public void deactivateMember(Long memberId, HttpServletRequest request, HttpServletResponse response) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorStatus.MEMBER_NOT_FOUND));

        if (member.isDeactivated()) {
            return;
        }

        member.deactivate();
        authenticationAPI.deactivateMember(memberId, request, response);
    }

    public void reactivateIfDeactivated(Long memberId) {
        memberRepository.findById(memberId)
                .filter(Member::isDeactivated)
                .ifPresent(Member::reactivate);
    }

    /**
     * 회원 계정 완전 삭제 (hard delete)
     *
     * @param memberId 삭제할 회원의 ID
     */
    public void deleteMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElse(null);

        if (member == null) {
            authenticationAPI.deleteAuthData(memberId);
            return;
        }

        memberBlockRepository.deleteAllByMemberId(memberId);
        followRepository.deleteAllByMemberId(memberId);
        memberRepository.delete(member);
        authenticationAPI.deleteAuthData(memberId);
    }

    /**
     * 이메일 변경
     *
     * @param memberId
     * @param request
     */
    public void updateEmail(Long memberId, MemberRequestDTO.UpdateEmail request) {
        // 새 이메일 중복 체크
        if (memberRepository.existsByEmail(request.getNewEmail())) {
            throw new MemberException(MemberErrorStatus.EMAIL_ALREADY_EXISTS);
        }

        // 소셜체크 + 기존이메일체크 + 인증번호체크
        authenticationAPI.updateEmail(memberId, request.getCurrentEmail(), request.getNewEmail(), request.getVerificationCode());

        // 성공 시 Member 이메일 업데이트
        Member member = findActiveMember(memberId);
        member.updateEmail(request.getNewEmail());
    }

    private Member findActiveMember(Long memberId) {
        return memberRepository.findByIdAndDeactivatedAtIsNull(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorStatus.MEMBER_NOT_FOUND));
    }

    private boolean isSocialMember(Long memberId) {
        return !"LOCAL".equals(authenticationAPI.fetchProvider(memberId));
    }
}
