package checkmo.clubNotice.internal.service;

import checkmo.clubManagement.ClubManagementAPI;
import checkmo.clubManagement.ClubManagementExternalDTO.MembershipDTO;
import checkmo.clubMeeting.ClubMeetingAPI;
import checkmo.clubMeeting.ClubMeetingExternalDTO.MeetingInfo;
import checkmo.clubNotice.internal.converter.ClubNoticeConverter;
import checkmo.clubNotice.internal.entity.MemberVote;
import checkmo.clubNotice.internal.entity.Notice;
import checkmo.clubNotice.internal.entity.Vote;
import checkmo.clubNotice.internal.service.query.ClubNoticeQueryService;
import checkmo.clubNotice.web.dto.ClubNoticeResponseDTO;
import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
import checkmo.member.MemberAPI;
import checkmo.member.MemberExternalDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClubNoticeQueryFacade {

    // 페이징 기본 크기 상수 정의
    private static final int DEFAULT_PAGE_SIZE = 10;

    // 태그 상수 정의
    private static final String TAG_NOTICE = "공지";
    private static final String TAG_MEETING = "모임";
    private static final String TAG_VOTE = "투표";

    // Domain level 2
    private final MemberAPI memberAPI;

    private final ClubManagementAPI clubManagementAPI;
    private final ClubMeetingAPI clubMeetingAPI;

    // 자신의 Query Service
    private final ClubNoticeQueryService clubNoticeQueryService;

    public ClubNoticeResponseDTO.ClubNoticeListDTO getLatestNotices(Long clubId, String memberId, Long cursorId,
                                                                    boolean onlyImportant, Integer size) {

        // 1. 검증 -> 소식은 클럽에 속한 사람만 조회할 수 있음
        clubManagementAPI.getClubInfo(clubId);
        MembershipDTO clubMembershipInfo = clubManagementAPI.getClubMembershipInfo(clubId, memberId);

        // 2. 커서 초기화
        Long cursor = (cursorId == null || cursorId == 0L) ? Long.MAX_VALUE : cursorId;

        // 3. 페이지 크기 결정 (size가 null 또는 0 이하이면 기본값 사용)
        int pageSize = (size == null || size <= 0) ? DEFAULT_PAGE_SIZE : size;
        Pageable pageable = PageRequest.of(0, pageSize + 1);

        // 4. 공지사항과 투표 각각 조회
        List<Notice> notices = clubNoticeQueryService.getNoticeList(clubId, onlyImportant, cursor, pageable);
        List<Vote> votes = clubNoticeQueryService.getVoteList(clubId, onlyImportant, cursor, pageable);

        // 5. 공지사항에 모임 정보 미리 조회
        Set<Long> meetingIds = extractNoticeMeetingIds(notices);
        Map<Long, MeetingInfo> meetingInfos = clubMeetingAPI.getMeetings(meetingIds);

        // 6. 생성시간 순으로 병합 및 DTO 변환
        List<ClubNoticeResponseDTO.NoticeItem> noticeItems
                = mergeNoticesAndVotes(notices, meetingInfos, votes, pageSize);

        // 7. 페이징
        boolean hasNext = noticeItems.size() > pageSize;
        if (hasNext) {
            noticeItems = noticeItems.subList(0, pageSize);  // pageSize 만큼만 남기기
        }
        Long nextCursor = hasNext && noticeItems.size() >= pageSize
                ? noticeItems.get(pageSize - 1).getId()
                : null;

        return ClubNoticeConverter.toClubNoticeListDTO(noticeItems, hasNext, nextCursor, clubMembershipInfo.isStaff());
    }

    /**
     * 공지사항과 투표를 생성 시간 순서대로 병합하는 로직
     */
    private List<ClubNoticeResponseDTO.NoticeItem> mergeNoticesAndVotes(
            List<Notice> notices, Map<Long, MeetingInfo> meetingInfos, List<Vote> votes, int pageSize
    ) {
        List<ClubNoticeResponseDTO.NoticeItem> resultList = new ArrayList<>();
        int n = notices.size();
        int m = votes.size();

        int i = 0, j = 0;
        while (resultList.size() < pageSize + 1 && (i < n || j < m)) {
            if (i < n && (j >= m || notices.get(i).getCreatedAt().isAfter(votes.get(j).getCreatedAt()))) {
                Notice notice = notices.get(i++);
                ClubNoticeResponseDTO.NoticeItem dto;

                if (notice.getMeetingId() != null) { // 모임 공지사항인 경우
                    dto = ClubNoticeConverter.toMeetingNoticeDTO(notice, meetingInfos.get(notice.getMeetingId()));
                } else {
                    dto = ClubNoticeConverter.toPureNoticeDTO(notice);
                }

                resultList.add(dto);
            } else if (j < m) {
                Vote vote = votes.get(j++);
                List<ClubNoticeResponseDTO.EachItemDTO> itemDTOs = ClubNoticeConverter.toEachItemDTOListFromItems(
                        vote.getItems());
                ClubNoticeResponseDTO.VoteDTO voteDTO = ClubNoticeConverter.toVoteDTO(vote, itemDTOs);
                resultList.add(voteDTO);
            }
        }

        return resultList;
    }

    /**
     * 공지사항 리스트에서 모임 공지사항인 경우 meetingId 추출
     */
    private Set<Long> extractNoticeMeetingIds(List<Notice> notices) {
        if (notices == null) {
            return Set.of();
        }
        return notices.stream()
                .map(Notice::getMeetingId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public ClubNoticeResponseDTO.ClubNoticeDetailDTO getNoticeDetail(Long clubId, Long noticeId, String tag,
                                                                     String memberId) {
        // 1. 검증
        clubManagementAPI.getClubInfo(clubId);
        MembershipDTO clubMembershipInfo = clubManagementAPI.getClubMembershipInfo(clubId, memberId);
        boolean isStaff = clubMembershipInfo.isStaff();

        return switch (tag) {
            case TAG_NOTICE -> getPureNoticeDetail(clubId, noticeId, isStaff);
            case TAG_MEETING -> getMeetingNoticeDetail(clubId, noticeId, isStaff);
            case TAG_VOTE -> getVoteDetail(clubId, noticeId, memberId, isStaff);
            default -> throw new GeneralException(ErrorStatus.CLUB_INVALID_TAG_TYPE);
        };
    }

    /**
     * 순수 공지사항 상세 조회
     */
    private ClubNoticeResponseDTO.ClubNoticeDetailDTO getPureNoticeDetail(Long clubId, Long itemId,
                                                                          boolean isStaff) {
        Notice notice = clubNoticeQueryService.getNotice(clubId, itemId);

        if (TAG_MEETING.equals(notice.getTag())) {
            throw new GeneralException(ErrorStatus.NOTICE_NOT_FOUND);
        }

        return ClubNoticeResponseDTO.ClubNoticeDetailDTO.builder()
                .isStaff(isStaff)
                .noticeItem(ClubNoticeConverter.toPureNoticeDTO(notice))
                .build();
    }

    /**
     * 모임 공지사항 상세 조회
     */
    private ClubNoticeResponseDTO.ClubNoticeDetailDTO getMeetingNoticeDetail(Long clubId, Long itemId,
                                                                             boolean isStaff) {
        Notice notice = clubNoticeQueryService.getNotice(clubId, itemId);

        if (TAG_NOTICE.equals(notice.getTag())) {
            throw new GeneralException(ErrorStatus.NOTICE_NOT_FOUND);
        }

        MeetingInfo meetingInfo = clubMeetingAPI.getMeeting(notice.getMeetingId());

        return ClubNoticeResponseDTO.ClubNoticeDetailDTO.builder()
                .isStaff(isStaff)
                .noticeItem(ClubNoticeConverter.toMeetingNoticeDTO(notice, meetingInfo))
                .build();
    }

    /**
     * 투표 상세 조회
     */
    private ClubNoticeResponseDTO.ClubNoticeDetailDTO getVoteDetail(Long clubId, Long itemId, String memberId,
                                                                    boolean isStaff) {
        Vote vote = clubNoticeQueryService.getVote(clubId, itemId);
        List<String> voteItems = vote.getItems();
        int itemCount = voteItems.size();

        // 전체 투표 결과
        List<MemberVote> memberVotes = clubNoticeQueryService.getMemberVotesByVoteId(vote.getId());

        // 항목별 투표자 정보 수집
        List<List<MemberExternalDTO.BasicInfo>> votedMembersByItem = collectVotedMembersByItem(vote, memberVotes,
                itemCount);

        // 본인 투표 정보
        MemberVote myVote = clubNoticeQueryService.getMyVote(vote.getId(), memberId);

        // 투표 항목 DTO 생성
        List<ClubNoticeResponseDTO.EachItemDTO> itemDTOs = createVoteItemDTOs(voteItems, myVote, votedMembersByItem,
                itemCount);

        ClubNoticeResponseDTO.VoteDTO voteDTO = ClubNoticeConverter.toVoteDTO(vote, itemDTOs);

        return ClubNoticeResponseDTO.ClubNoticeDetailDTO.builder()
                .isStaff(isStaff)
                .noticeItem(voteDTO)
                .build();
    }

    /**
     * 투표 항목별 투표자 정보 수집
     */
    private List<List<MemberExternalDTO.BasicInfo>> collectVotedMembersByItem(
            Vote vote, List<MemberVote> memberVotes, int itemCount
    ) {
        // 항목별 투표자 정보 리스트 초기화
        List<List<MemberExternalDTO.BasicInfo>> votedMembersByItem = new ArrayList<>();
        for (int i = 0; i < itemCount; i++) {
            votedMembersByItem.add(new ArrayList<>());
        }

        // 각 MemberVote에 대해 항목별 투표 여부 확인 후 추가
        for (MemberVote mv : memberVotes) {
            MemberExternalDTO.BasicInfo memberInfo = getMemberInfoForVote(vote, mv);

            if (mv.isItem1()) {
                votedMembersByItem.get(0).add(memberInfo);
            }
            if (itemCount >= 2 && mv.isItem2()) {
                votedMembersByItem.get(1).add(memberInfo);
            }
            if (itemCount >= 3 && mv.isItem3()) {
                votedMembersByItem.get(2).add(memberInfo);
            }
            if (itemCount >= 4 && mv.isItem4()) {
                votedMembersByItem.get(3).add(memberInfo);
            }
            if (itemCount >= 5 && mv.isItem5()) {
                votedMembersByItem.get(4).add(memberInfo);
            }
        }

        return votedMembersByItem;
    }

    /**
     * 투표자의 멤버 정보 조회 (익명 여부에 따라 다르게 처리)
     */
    private MemberExternalDTO.BasicInfo getMemberInfoForVote(Vote vote, MemberVote memberVote) {
        if (vote.isAnonymity()) {
            String voterName = "익명";
            String profileImageUrl = "https://avatars.githubusercontent.com/u/217887881?s=200&v=4";
            return new MemberExternalDTO.BasicInfo(voterName, profileImageUrl);
        } else {
            return memberAPI.getMemberBasicInfoForShare(memberVote.getMemberId());
        }
    }

    /**
     * 투표 항목 DTO 리스트 생성
     */
    private List<ClubNoticeResponseDTO.EachItemDTO> createVoteItemDTOs(
            List<String> voteItems, MemberVote myVote,
            List<List<MemberExternalDTO.BasicInfo>> votedMembersByItem, int itemCount
    ) {
        List<ClubNoticeResponseDTO.EachItemDTO> itemDTOs = new ArrayList<>();
        for (int i = 0; i < itemCount; i++) {
            boolean isSelected = isItemSelected(myVote, i);
            itemDTOs.add(
                    ClubNoticeConverter.toEachItemDTO(
                            voteItems.get(i),
                            isSelected,
                            votedMembersByItem.get(i)
                    )
            );
        }
        return itemDTOs;
    }

    /**
     * 특정 항목이 선택되었는지 확인
     */
    private boolean isItemSelected(MemberVote myVote, int itemIndex) {
        if (myVote == null) {
            return false;
        }
        return switch (itemIndex) {
            case 0 -> myVote.isItem1();
            case 1 -> myVote.isItem2();
            case 2 -> myVote.isItem3();
            case 3 -> myVote.isItem4();
            case 4 -> myVote.isItem5();
            default -> false;
        };
    }
}
