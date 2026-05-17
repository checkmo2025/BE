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

    public List<MemberBlock> retrieveBlocks(String blockerId, Long cursorId, int pageSize) {
        if (blockerId == null) {
            return List.of();
        }

        return memberBlockRepository.findBlocks(blockerId, cursorId, pageSize);
    }

    public List<String> retrieveBlockedMemberIds(String blockerId) {
        if (blockerId == null) {
            return List.of();
        }

        return memberBlockRepository.findBlockedMemberIds(blockerId);
    }

    public List<String> retrieveBlockRelatedMemberIds(String memberId) {
        if (memberId == null) {
            return List.of();
        }

        return memberBlockRepository.findBlockRelatedMemberIds(memberId);
    }

    public boolean hasBlockBetween(String memberId1, String memberId2) {
        if (memberId1 == null || memberId2 == null) {
            return false;
        }

        return memberBlockRepository.existsBetween(memberId1, memberId2);
    }

    public boolean hasBlocked(String blockerId, String blockedId) {
        if (blockerId == null || blockedId == null) {
            return false;
        }

        return memberBlockRepository.existsByBlocker_IdAndBlocked_Id(blockerId, blockedId);
    }

    public void validateProfileAccessible(String viewerId, String targetMemberId) {
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
