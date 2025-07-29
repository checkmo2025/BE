package checkmo.domain.club.service.query;

import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.club.converter.ClubConverter;
import checkmo.domain.club.entity.announcement.MemberVote;
import checkmo.domain.club.entity.announcement.Notice;
import checkmo.domain.club.entity.announcement.Vote;
import checkmo.domain.club.repository.announcement.MemberVoteRepository;
import checkmo.domain.club.repository.announcement.NoticeRepository;
import checkmo.domain.club.repository.announcement.VoteRepository;
import checkmo.domain.club.web.dto.club.ClubResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubCommunicationQueryServiceImpl implements ClubCommunicationQueryService {

    private final NoticeRepository noticeRepository;
    private final VoteRepository voteRepository;
    private final MemberVoteRepository memberVoteRepository;

    private final ClubQueryService clubQueryService;
    private final ClubMemberQueryService clubMemberQueryService;

    /**
     * 공지 or 투표 상세 조회
     *
     * @param clubId 클럽 ID
     * @param itemId 공지 또는 투표 ID
     * @param tag    "공지", "모임", "투표" 중 하나
     * @return 공통된 NoticeItem DTO
     */
    @Override
    public ClubResponseDTO.ClubNoticeDetailDTO getNoticeOrVoteDetail(Long clubId, Long itemId, String tag, String memberId) {

        // 1. 검증
        clubQueryService.validateClub(clubId);
        clubMemberQueryService.validateClubMember(clubId, memberId);

        return switch (tag) {
            case "공지" -> {
                Notice notice = noticeRepository.findById(itemId)
                        .orElseThrow(() -> new GeneralException(ErrorStatus.NOTICE_NOT_FOUND));

                yield ClubResponseDTO.ClubNoticeDetailDTO.builder()
                        .noticeItem(ClubConverter.toPureNoticeDTO(notice))
                        .build();
            }

            default -> throw new GeneralException(ErrorStatus.CLUB_INVALID_TAG_TYPE);
        };

    }
}