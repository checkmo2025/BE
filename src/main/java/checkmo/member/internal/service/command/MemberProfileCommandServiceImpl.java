package checkmo.member.internal.service.command;

import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
import checkmo.member.MemberEvent;
import checkmo.member.internal.converter.MemberConverter;
import checkmo.member.internal.entity.Member;
import checkmo.member.internal.repository.MemberRepository;
import checkmo.member.web.dto.MemberRequestDTO;
import checkmo.member.web.dto.MemberResponseDTO;
import java.util.HashSet;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberProfileCommandServiceImpl implements MemberProfileCommandService {

    private final MemberRepository memberRepository;

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public MemberResponseDTO.MemberProfileWithCategory updateMemberProfile(
            String memberId,
            MemberRequestDTO.MemberProfileUpdate request
    ) {
        // 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(
                        ErrorStatus.MEMBER_NOT_FOUND));

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

    @Override
    public void updatePassword(String memberId, MemberRequestDTO.PasswordUpdate request) {
        throw new UnsupportedOperationException("추후 구현 예정");
    }

    @Override
    public void deactivateMember(String memberId) {
        throw new UnsupportedOperationException("추후 구현 예정");
    }

    @Override
    public void deleteMember(String memberId) {
        throw new UnsupportedOperationException("추후 구현 예정");
    }
}