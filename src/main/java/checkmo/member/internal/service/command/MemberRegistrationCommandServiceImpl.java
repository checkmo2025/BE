package checkmo.member.internal.service.command;

import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
import checkmo.member.MemberEvent;
import checkmo.member.internal.converter.MemberConverter;
import checkmo.member.internal.entity.Member;
import checkmo.member.internal.repository.MemberRepository;
import checkmo.member.internal.service.query.MemberQueryService;
import checkmo.member.web.dto.MemberRequestDTO;
import java.util.HashSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class MemberRegistrationCommandServiceImpl implements MemberRegistrationCommandService {

    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void createMember(String memberId, String email) {

        Member member = MemberConverter.toMember(memberId, email);

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
        eventPublisher.publishEvent(new MemberEvent.MemberProfileCompleted(memberId));
    }
}
