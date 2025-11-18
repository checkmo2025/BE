package checkmo.member.internal.service.command;

import checkmo.authentication.AuthenticationAPI;
import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
import checkmo.member.internal.entity.Member;
import checkmo.member.internal.repository.MemberRepository;
import checkmo.member.web.dto.MemberRequestDTO;
import java.util.HashSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class MemberRegistrationCommandServiceImpl implements MemberRegistrationCommandService {

    private final MemberRepository memberRepository;
    private final AuthenticationAPI authenticationAPI;

    @Override
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

    @Override
    public void addAdditionalInfo(String memberId, MemberRequestDTO.AdditionalInfo request) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 멤버 엔티티 업데이트
        member.updateAdditionalInfo(
                request.getNickname(),
                request.getDescription(),
                request.getImgUrl()
        );

        // 관심 카테고리 저장
        member.updateInterestCategories(new HashSet<>(request.getCategories()));

        // 프로필 완료 상태로 변경 이벤트 발행
        authenticationAPI.completeProfile(memberId);
    }
}
