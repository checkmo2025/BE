package checkmo.member.internal.service.command;

import checkmo.common.nickname.NicknamePolicy;
import checkmo.member.internal.entity.Member;
import checkmo.member.internal.entity.MemberBlock;
import checkmo.member.internal.exception.MemberErrorStatus;
import checkmo.member.internal.exception.MemberException;
import checkmo.member.internal.repository.FollowRepository;
import checkmo.member.internal.repository.MemberBlockRepository;
import checkmo.member.internal.repository.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberBlockCommandService {

    private final MemberRepository memberRepository;
    private final MemberBlockRepository memberBlockRepository;
    private final FollowRepository followRepository;

    public void block(Long blockerId, String blockedNickname) {
        Member blocker = memberRepository.findByIdAndDeactivatedAtIsNull(blockerId)
                .orElseThrow(() -> new MemberException(MemberErrorStatus.MEMBER_NOT_FOUND));
        Member blocked = memberRepository.findByNickNameKey(NicknamePolicy.comparisonKey(blockedNickname))
                .orElseThrow(() -> new MemberException(MemberErrorStatus.MEMBER_NOT_FOUND));

        if (Member.isSameMember(blocker.getId(), blocked.getId())) {
            throw new MemberException(MemberErrorStatus.MEMBER_CANNOT_BLOCK_SELF);
        }

        lockMemberPair(blocker.getId(), blocked.getId());

        if (memberBlockRepository.existsByBlocker_IdAndBlocked_Id(blocker.getId(), blocked.getId())) {
            throw new MemberException(MemberErrorStatus.MEMBER_ALREADY_BLOCKED);
        }

        try {
            memberBlockRepository.saveAndFlush(MemberBlock.builder()
                    .blocker(blocker)
                    .blocked(blocked)
                    .build());
        } catch (DataIntegrityViolationException e) {
            throw new MemberException(MemberErrorStatus.MEMBER_ALREADY_BLOCKED);
        }
        followRepository.deleteBetweenMembers(blocker.getId(), blocked.getId());
    }

    public void unblock(Long blockerId, String blockedNickname) {
        Long blockedId = memberRepository.findIdByNickNameKey(NicknamePolicy.comparisonKey(blockedNickname))
                .orElseThrow(() -> new MemberException(MemberErrorStatus.MEMBER_NOT_FOUND));

        MemberBlock memberBlock = memberBlockRepository.findByBlocker_IdAndBlocked_Id(blockerId, blockedId)
                .orElseThrow(() -> new MemberException(MemberErrorStatus.MEMBER_BLOCK_NOT_FOUND));

        memberBlockRepository.delete(memberBlock);
    }

    private void lockMemberPair(Long memberId1, Long memberId2) {
        List<Long> memberIds = List.of(memberId1, memberId2).stream()
                .sorted()
                .toList();
        memberRepository.lockActiveMembersByIdIn(memberIds);
    }
}
