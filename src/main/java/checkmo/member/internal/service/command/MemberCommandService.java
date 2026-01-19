package checkmo.member.internal.service.command;

import checkmo.authentication.AuthenticationAPI;
import checkmo.member.MemberEvent;
import checkmo.member.internal.converter.MemberConverter;
import checkmo.member.internal.entity.Member;
import checkmo.member.internal.exception.MemberErrorStatus;
import checkmo.member.internal.exception.MemberException;
import checkmo.member.internal.repository.MemberRepository;
import checkmo.member.web.dto.MemberRequestDTO;
import checkmo.member.web.dto.MemberResponseDTO.DetailInfo;
import java.util.HashSet;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class MemberCommandService {

    private final AuthenticationAPI authenticationAPI;

    private final MemberRepository memberRepository;

    private final ApplicationEventPublisher eventPublisher;

    public void createMember(String memberId, String email) {
        Member member = Member.builder()
                .id(memberId)
                .email(email)
                .nickName("")
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
    public void addAdditionalInfo(String memberId, MemberRequestDTO.AdditionalInfo request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorStatus.MEMBER_NOT_FOUND));

        member.updateAdditionalInfo(
                request.getNickname(),
                request.getDescription(),
                request.getImgUrl()
        );

        member.updateInterestCategories(new HashSet<>(request.getCategories()));

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
     * @param request  수정할 프로필 정보 DTO - 프로필 이미지, 간단 소개, 관심 카테고리 (닉네임은 변경 불가!!)
     * @return 수정된 회원 프로필 정보 엔티티 - 이때는 관심 카테고리 정보 DTO에 포함 X , -> 반드시 CategoryQueryFacade를 통해 조회해야 함
     */
    public DetailInfo updateProfile(
            String memberId,
            MemberRequestDTO.MemberProfileUpdate request
    ) {
        // 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorStatus.MEMBER_NOT_FOUND));

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

        member.updateProfile(request.getDescription(), newImageUrl);

        if (request.getCategories() != null) {
            member.updateInterestCategories(new HashSet<>(request.getCategories()));
        }

        return MemberConverter.toMemberProfileWithCategory(member);
    }

    /**
     * 회원 비밀번호 변경 (일반 회원만)
     *
     * @param memberId 비밀번호를 변경할 회원의 ID
     * @param request  비밀번호 변경 정보 DTO (현재 비밀번호, 새 비밀번호, 새 비밀번호 확인)
     */
    public void updatePassword(String memberId, MemberRequestDTO.PasswordUpdate request) {
        throw new UnsupportedOperationException("추후 구현 예정");
    }

    /**
     * 회원 계정 삭제 (soft delete)
     *
     * @param memberId 비활성화할 회원의 ID
     */
    public void deactivateMember(String memberId) {
        throw new UnsupportedOperationException("추후 구현 예정");
    }

    /**
     * 회원 계정 완전 삭제 (hard delete)
     *
     * @param memberId 삭제할 회원의 ID
     */
    public void deleteMember(String memberId) {
        throw new UnsupportedOperationException("추후 구현 예정");
    }
}
