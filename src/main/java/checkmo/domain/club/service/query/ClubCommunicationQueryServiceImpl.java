package checkmo.domain.club.service.query;

import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.book.facade.BookQueryFacade;
import checkmo.domain.club.converter.ClubConverter;
import checkmo.domain.club.entity.ClubMember;
import checkmo.domain.club.entity.announcement.MemberVote;
import checkmo.domain.club.entity.announcement.Notice;
import checkmo.domain.club.entity.announcement.Vote;
import checkmo.domain.club.repository.announcement.MemberVoteRepository;
import checkmo.domain.club.repository.announcement.NoticeRepository;
import checkmo.domain.club.repository.announcement.VoteRepository;
import checkmo.domain.club.web.dto.club.ClubResponseDTO;
import checkmo.domain.member.facade.MemberQueryFacade;
import checkmo.global.dto.BookSharedDTO;
import checkmo.global.dto.MemberSharedDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ClubCommunicationQueryServiceImpl implements ClubCommunicationQueryService {

    private final NoticeRepository noticeRepository;
    private final VoteRepository voteRepository;
    private final MemberVoteRepository memberVoteRepository;

    private final ClubQueryService clubQueryService;
    private final ClubMemberQueryService clubMemberQueryService;

    private final MemberQueryFacade memberQueryFacade;
    private final BookQueryFacade bookQueryFacade;

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
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        return switch (tag) {
            case "공지" -> {
                Notice notice = noticeRepository.findById(itemId)
                        .orElseThrow(() -> new GeneralException(ErrorStatus.NOTICE_NOT_FOUND));

                yield ClubResponseDTO.ClubNoticeDetailDTO.builder()
                        .isStaff(clubMember.isStaff())
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

                    MemberSharedDTO.BasicInfoDTO memberInfo = null;

                    // 익명 투표 여부 확인
                    if (vote.isAnonymity()) {
                        String voterName = "익명";
                        String profileImageUrl = "https://avatars.githubusercontent.com/u/217887881?s=200&v=4";
                        memberInfo = new MemberSharedDTO.BasicInfoDTO(voterName, profileImageUrl);
                    } else {
                        String voterId = mv.getMemberId();
                        memberInfo = memberQueryFacade.getMemberBasicInfoForShare(voterId);
                    }

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
                        .isStaff(clubMember.isStaff())
                        .noticeItem(voteDTO)
                        .build();
            }

            default -> throw new GeneralException(ErrorStatus.CLUB_INVALID_TAG_TYPE);
        };

    }

    /**
     * 클럽의 모든 공지와 투표를 조회합니다.
     *
     * @param clubId 클럽 ID
     * @param onlyImportant 중요 공지/투표만 조회할지 여부
     * @param cursorId 커서 ID (페이징을 위한 커서, 처음에는 null 또는 0)
     * @param pageSize 페이지당 조회할 개수
     * @return 공지와 투표 목록 DTO
     */
    @Override
    public List<ClubResponseDTO.NoticeItem> getAllNoticesAndVotes(Long clubId, boolean onlyImportant, Long cursorId, Pageable pageable) {

        int pageSize = pageable.getPageSize();

        // 1. 공지사항 리스트 조회
        List<Notice> notices = noticeRepository.findByClubIdAndCursorPaging(clubId, onlyImportant, cursorId, Pageable.ofSize(pageSize + 1));

        // 2. 투표 리스트 조회
        List<Vote> votes = voteRepository.findByClubIdAndCursorPaging(clubId, onlyImportant, cursorId, Pageable.ofSize(pageSize + 1));

        List<ClubResponseDTO.NoticeItem> resultList = new ArrayList<>();
        int n = notices.size();  // 공지사항 개수
        int m = votes.size();    // 투표 개수

        // 공지사항과 투표를 생성일시 기준으로 병합하여 pageSize 만큼 결과 채움
        int i = 0, j = 0;
        while (resultList.size() < pageSize + 1 && (i < n || j < m)) {

            LocalDateTime noticeTime = i < n ? notices.get(i).getCreatedAt() : LocalDateTime.MIN;
            LocalDateTime voteTime = j < m ? votes.get(j).getCreatedAt() : LocalDateTime.MIN;

            // 공지사항 우선순위가 높거나 투표가 없을 경우 공지사항 처리
            if (i < n && (j >= m || noticeTime.isAfter(voteTime))) {

                Notice notice = notices.get(i++);
                ClubResponseDTO.NoticeItem dto;

                // 공지사항이 미팅 관련이면 책 정보도 조회하여 DTO 변환
                if (notice.getMeeting() != null) {
                    BookSharedDTO.BasicInfoDTO bookInfo = bookQueryFacade.getBookBasicInfoForShare(notice.getMeeting().getBookId());
                    dto = ClubConverter.toMeetingNoticeDTO(notice, bookInfo);
                } else {
                    // 순수 공지사항 DTO 변환
                    dto = ClubConverter.toPureNoticeDTO(notice);
                }

                resultList.add(dto);

            } else if (j < m) {  // 투표 조회
                Vote vote = votes.get(j++);
                List<ClubResponseDTO.EachItemDTO> itemDTOs = ClubConverter.toEachItemDTOListFromItems(vote.getItems());
                ClubResponseDTO.VoteDTO voteDTO = ClubConverter.toVoteDTO(vote, itemDTOs);
                resultList.add(voteDTO);
            }
        }

        return resultList;
    }

}