package checkmo.member.service.command;

import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
import checkmo.member.converter.MemberConverter;
import checkmo.member.entity.Member;
import checkmo.member.repository.FollowRepository;
import checkmo.member.repository.MemberRepository;
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

    // 자신의 Repository
    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;

    // 이벤트 발행을 위한 ApplicationEventPublisher
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void followingMember(String memberId, String followingNickname) {

        // 닉네임으로 팔로잉 대상 조회
        Member following = memberRepository.findByNickName(followingNickname)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 자기 자신을 팔로우할 수 없음
        if (memberId.equals(following.getId())) {
            throw new GeneralException(ErrorStatus.MEMBER_CANNOT_FOLLOW_SELF);
        }
        
        // 이미 팔로잉 중인지 확인
        if (followRepository.existsByFollowerIdAndFollowingId(memberId, following.getId())) {
            throw new GeneralException(ErrorStatus.MEMBER_ALREADY_FOLLOWING);
        }

        // 회원 조회
        Member follower = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 팔로잉 관계 생성
        followRepository.save(MemberConverter.toFollow(follower, following));
        
        // 팔로잉 이벤트 발행
        eventPublisher.publishEvent(new FollowEvent(memberId, following.getId()));
    }

    @Override
    public void unfollowingMember(String memberId, String followingNickname) {

        // 닉네임으로 팔로잉 대상의 Id 조회
        String followingId = memberRepository.findIdByNickName(followingNickname)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 팔로잉 하고 있는지 여부 확인
        if (!followRepository.existsByFollowerIdAndFollowingId(memberId, followingId)) {
            throw new GeneralException(ErrorStatus.MEMBER_NOT_FOLLOWING);
        }

        // 팔로잉 관계 삭제
        followRepository.deleteByFollowerIdAndFollowingId(memberId, followingId);
    }

    @Override
    public void deleteFollower(String memberId, String followerNickname) {

        // 닉네임으로 팔로워의 Id 조회
        String followerId = memberRepository.findIdByNickName(followerNickname)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 팔로워가 존재하는지 확인
        if (!followRepository.existsByFollowerIdAndFollowingId(followerId, memberId)) {
            throw new GeneralException(ErrorStatus.MEMBER_NOT_FOLLOWER);
        }

        // 팔로워 관계 삭제
        followRepository.deleteByFollowerIdAndFollowingId(followerId, memberId);
    }
}
