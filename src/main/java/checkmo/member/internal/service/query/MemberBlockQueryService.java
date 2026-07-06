package checkmo.member.internal.service.query;

import checkmo.member.internal.entity.MemberBlock;
import checkmo.member.internal.exception.MemberErrorStatus;
import checkmo.member.internal.exception.MemberException;
import checkmo.member.internal.repository.MemberBlockRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberBlockQueryService {

    private final MemberBlockRepository memberBlockRepository;

    public List<MemberBlock> retrieveBlocks(Long blockerId, Long cursorId, int pageSize) {
        if (blockerId == null) {
            return List.of();
        }

        return memberBlockRepository.findBlocks(blockerId, cursorId, pageSize);
    }

    public List<Long> retrieveBlockedMemberIds(Long blockerId) {
        if (blockerId == null) {
            return List.of();
        }

        return memberBlockRepository.findBlockedMemberIds(blockerId);
    }

    public List<Long> retrieveBlockRelatedMemberIds(Long memberId) {
        if (memberId == null) {
            return List.of();
        }

        return memberBlockRepository.findBlockRelatedMemberIds(memberId);
    }

    public boolean hasBlockBetween(Long memberId1, Long memberId2) {
        if (memberId1 == null || memberId2 == null) {
            return false;
        }

        return memberBlockRepository.existsBetween(memberId1, memberId2);
    }

    public boolean hasBlocked(Long blockerId, Long blockedId) {
        if (blockerId == null || blockedId == null) {
            return false;
        }

        return memberBlockRepository.existsByBlocker_IdAndBlocked_Id(blockerId, blockedId);
    }

    public void validateProfileAccessible(Long viewerId, Long targetMemberId) {
        if (viewerId == null || targetMemberId == null || viewerId.equals(targetMemberId)) {
            return;
        }

        memberBlockRepository.findBetween(viewerId, targetMemberId)
                .ifPresent(memberBlock -> {
                    if (viewerId.equals(memberBlock.getBlocker().getId())) {
                        throw new MemberException(MemberErrorStatus.MEMBER_BLOCKED_BY_ME);
                    }

                    throw new MemberException(MemberErrorStatus.MEMBER_BLOCKED_ME);
                });
    }
}
