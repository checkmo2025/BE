package checkmo.member.internal.service.command;

import checkmo.member.internal.entity.Member;
import checkmo.member.internal.entity.MemberBlock;
import checkmo.member.internal.exception.MemberErrorStatus;
import checkmo.member.internal.exception.MemberException;
import checkmo.member.internal.repository.FollowRepository;
import checkmo.member.internal.repository.MemberBlockRepository;
import checkmo.member.internal.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberBlockCommandService {

    private final MemberRepository memberRepository;
    private final MemberBlockRepository memberBlockRepository;
    private final FollowRepository followRepository;

    public void block(String blockerId, String blockedNickname) {
        Member blocker = memberRepository.findByIdAndDeactivatedAtIsNull(blockerId)
                .orElseThrow(() -> new MemberException(MemberErrorStatus.MEMBER_NOT_FOUND));
        Member blocked = memberRepository.findByNickName(blockedNickname)
                .orElseThrow(() -> new MemberException(MemberErrorStatus.MEMBER_NOT_FOUND));

        if (Member.isSameMember(blocker.getId(), blocked.getId())) {
            throw new MemberException(MemberErrorStatus.MEMBER_CANNOT_BLOCK_SELF);
        }

        if (memberBlockRepository.existsByBlocker_IdAndBlocked_Id(blocker.getId(), blocked.getId())) {
            throw new MemberException(MemberErrorStatus.MEMBER_ALREADY_BLOCKED);
        }

        memberBlockRepository.save(MemberBlock.builder()
                .blocker(blocker)
                .blocked(blocked)
                .build());
        followRepository.deleteBetweenMembers(blocker.getId(), blocked.getId());
    }

    public void unblock(String blockerId, String blockedNickname) {
        String blockedId = memberRepository.findIdByNickName(blockedNickname)
                .orElseThrow(() -> new MemberException(MemberErrorStatus.MEMBER_NOT_FOUND));

        MemberBlock memberBlock = memberBlockRepository.findByBlocker_IdAndBlocked_Id(blockerId, blockedId)
                .orElseThrow(() -> new MemberException(MemberErrorStatus.MEMBER_BLOCK_NOT_FOUND));

        memberBlockRepository.delete(memberBlock);
    }
}
