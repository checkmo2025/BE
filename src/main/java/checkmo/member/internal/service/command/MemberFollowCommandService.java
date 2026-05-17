package checkmo.member.internal.service.command;

import checkmo.member.MemberEvent;
import checkmo.member.internal.entity.Follow;
import checkmo.member.internal.entity.Member;
import checkmo.member.internal.exception.MemberErrorStatus;
import checkmo.member.internal.exception.MemberException;
import checkmo.member.internal.repository.FollowRepository;
import checkmo.member.internal.repository.MemberRepository;
import checkmo.member.internal.service.query.MemberBlockQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MemberFollowCommandService {

    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
    private final MemberBlockQueryService memberBlockQueryService;

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 특정 회원을 팔로잉
     *
     * @param memberId          팔로우할 회원의 ID
     * @param followingNickname 팔로우 대상 회원의 nickname -> 서비스 로직에서 닉네임으로 회원의 ID를 조회하여 팔로잉 처리
     */
    public void following(String memberId, String followingNickname) {
        // 닉네임으로 팔로잉 대상 조회
        Member following = memberRepository.findByNickName(followingNickname)
                .orElseThrow(() -> new MemberException(MemberErrorStatus.MEMBER_NOT_FOUND));

        // 자기 자신을 팔로우할 수 없음
        following.verifyNotSelf(memberId);

        // 회원 조회
        Member follower = memberRepository.findByIdAndDeactivatedAtIsNull(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorStatus.MEMBER_NOT_FOUND));

        lockMemberPair(follower.getId(), following.getId());

        if (memberBlockQueryService.hasBlockBetween(memberId, following.getId())) {
            throw new MemberException(MemberErrorStatus.MEMBER_BLOCKED_RELATION);
        }

        // 이미 팔로잉 중인지 확인
        if (followRepository.existsByFollow(memberId, following.getId())) {
            throw new MemberException(MemberErrorStatus.MEMBER_ALREADY_FOLLOWING);
        }

        // 팔로잉 관계 생성
        Follow follow = Follow.builder()
                .follower(follower)
                .following(following)
                .build();
        followRepository.save(follow);

        // 팔로잉 이벤트 발행
        eventPublisher.publishEvent(new MemberEvent.Follow(follow.getId(), memberId, following.getId()));
    }

    private void lockMemberPair(String memberId1, String memberId2) {
        List<String> memberIds = List.of(memberId1, memberId2).stream()
                .sorted()
                .toList();
        memberRepository.lockActiveMembersByIdIn(memberIds);
    }

    /**
     * 특정 회원의 팔로잉를 취소 (언팔로잉)
     *
     * @param memberId          언팔로잉할 회원의 ID
     * @param followingNickname 팔로우 대상 회원의 nickname -> 서비스 로직에서 닉네임으로 회원의 ID를 조회하여 언팔로잉 처리
     */
    public void unfollowing(String memberId, String followingNickname) {
        // 닉네임으로 팔로잉 대상의 Id 조회
        String followingId = memberRepository.findIdByNickName(followingNickname)
                .orElseThrow(() -> new MemberException(MemberErrorStatus.MEMBER_NOT_FOUND));

        // 팔로잉 하고 있는지 여부 확인
        if (!followRepository.existsByFollow(memberId, followingId)) {
            throw new MemberException(MemberErrorStatus.MEMBER_NOT_FOLLOWING);
        }

        // 팔로잉 관계 삭제
        followRepository.deleteByFollow(memberId, followingId);
    }

    /**
     * 내 팔로워 중 특정 회원 삭제
     *
     * @param memberId         제거할 회원 ID
     * @param followerNickname 팔로워의 nickname -> 서비스 로직에서 닉네임으로 회원의 ID를 조회하여 팔로워 삭제 처리
     */
    public void deleteFollower(String memberId, String followerNickname) {
        // 닉네임으로 팔로워의 Id 조회
        String followerId = memberRepository.findIdByNickName(followerNickname)
                .orElseThrow(() -> new MemberException(MemberErrorStatus.MEMBER_NOT_FOUND));

        // 팔로워가 존재하는지 확인
        if (!followRepository.existsByFollow(followerId, memberId)) {
            throw new MemberException(MemberErrorStatus.MEMBER_NOT_FOLLOWER);
        }

        // 팔로워 관계 삭제
        followRepository.deleteByFollow(followerId, memberId);
    }
}
