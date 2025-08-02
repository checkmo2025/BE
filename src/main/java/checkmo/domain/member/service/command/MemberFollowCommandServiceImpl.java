package checkmo.domain.member.service.command;

import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.member.converter.MemberConverter;
import checkmo.domain.member.repository.FollowRepository;
import checkmo.domain.member.repository.MemberRepository;
import checkmo.event.FollowEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MemberFollowCommandServiceImpl implements MemberFollowCommandService {

    private final ApplicationEventPublisher eventPublisher;

    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;

    @Override
    public void followingMember(String memberId, String followingNickname) {

        // 닉네임으로 팔로잉 대상의 Id 조회
        String followingId = memberRepository.findIdByNickName(followingNickname)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        
        // 이미 팔로우 중인지 확인
        if (followRepository.existsByFollowerIdAndFollowingId(memberId, followingId)) {
            throw new GeneralException(ErrorStatus.MEMBER_ALREADY_FOLLOWING);
        }

        // 팔로잉 관계 생성
        followRepository.save(MemberConverter.toFollow(memberId, followingId));
        
        // 팔로우 이벤트 발행
        eventPublisher.publishEvent(new FollowEvent(memberId, followingId));
    }

    @Override
    public void unfollowMember(String memberId, String followingNickname) {

    }

    @Override
    public void unfollowingMember(String memberId, String followingNickname) {

    }
}
