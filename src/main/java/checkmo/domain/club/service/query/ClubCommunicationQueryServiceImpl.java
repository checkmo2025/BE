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
import checkmo.domain.member.facade.MemberQueryFacade;
import checkmo.global.dto.MemberSharedDTO;
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

    private final MemberQueryFacade memberQueryFacade;

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

            case "투표" -> {
                Vote vote = voteRepository.findById(itemId)
                        .orElseThrow(() -> new GeneralException(ErrorStatus.VOTE_NOT_FOUND));

                List<String> voteItems = vote.getItems();
                int itemCount = voteItems.size();

                // 전체 투표 결과
                List<MemberVote> memberVotes = memberVoteRepository.findAllByVoteId(vote.getId());

                // 항목별 투표자 정보 리스트 초기화
                List<List<MemberSharedDTO.BasicInfoDTO>> votedMembersByItem = new ArrayList<>();
                for (int i = 0; i < itemCount; i++) {
                    votedMembersByItem.add(new ArrayList<>());
                }

                // 각 MemberVote에 대해 항목별 투표 여부 확인 후 추가
                for (MemberVote mv : memberVotes) {
                    String voterId = mv.getMemberId();
                    MemberSharedDTO.BasicInfoDTO memberInfo = memberQueryFacade.getMemberBasicInfoForShare(voterId);

                    if (mv.isItem1()) votedMembersByItem.get(0).add(memberInfo);
                    if (itemCount >= 2 && mv.isItem2()) votedMembersByItem.get(1).add(memberInfo);
                    if (itemCount >= 3 && mv.isItem3()) votedMembersByItem.get(2).add(memberInfo);
                    if (itemCount >= 4 && mv.isItem4()) votedMembersByItem.get(3).add(memberInfo);
                    if (itemCount >= 5 && mv.isItem5()) votedMembersByItem.get(4).add(memberInfo);
                }

                // 본인 투표 정보
                MemberVote myVote = memberVoteRepository.findByVoteIdAndMemberId(vote.getId(), memberId).orElse(null);

                List<ClubResponseDTO.EachItemDTO> itemDTOs = new ArrayList<>();
                for (int i = 0; i < itemCount; i++) {
                    boolean isSelected = false;
                    if (myVote != null) {
                        isSelected = switch (i) {
                            case 0 -> myVote.isItem1();
                            case 1 -> myVote.isItem2();
                            case 2 -> myVote.isItem3();
                            case 3 -> myVote.isItem4();
                            case 4 -> myVote.isItem5();
                            default -> false;
                        };
                    }

                    itemDTOs.add(
                            ClubConverter.toEachItemDTO(
                                    voteItems.get(i),
                                    isSelected,
                                    votedMembersByItem.get(i)
                            )
                    );
                }

                ClubResponseDTO.VoteDTO voteDTO = ClubConverter.toVoteDTO(vote, itemDTOs);

                yield ClubResponseDTO.ClubNoticeDetailDTO.builder()
                        .noticeItem(voteDTO)
                        .build();
            }

            default -> throw new GeneralException(ErrorStatus.CLUB_INVALID_TAG_TYPE);
        };

    }
}