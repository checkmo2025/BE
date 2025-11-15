package checkmo.clubNotice.internal.service;

import checkmo.clubManagement.ClubManagementAPI;
import checkmo.clubManagement.ClubManagementExternalDTO.MembershipDTO;
import checkmo.clubMeeting.ClubMeetingAPI;
import checkmo.clubMeeting.ClubMeetingExternalDTO.MeetingInfo;
import checkmo.clubNotice.internal.converter.ClubNoticeConverter;
import checkmo.clubNotice.internal.entity.ClubMemberVote;
import checkmo.clubNotice.internal.entity.Notice;
import checkmo.clubNotice.internal.entity.Vote;
import checkmo.clubNotice.internal.service.query.ClubNoticeQueryService;
import checkmo.clubNotice.web.dto.ClubNoticeResponseDTO;
import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
import checkmo.member.MemberAPI;
import checkmo.member.MemberExternalDTO;
import checkmo.member.MemberExternalDTO.BasicInfo;
import java.util.ArrayList;
import java.util.Collections;
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

    // 익명 상수 정의
    private static final String ANONYMOUS_NAME = "익명";
    private static final String ANONYMOUS_PROFILE_URL = "https://avatars.githubusercontent.com/u/217887881?s=200&v=4";

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

        return switch (tag) {
            case TAG_NOTICE -> getPureNoticeDetail(clubId, noticeId, clubMembershipInfo);
            case TAG_MEETING -> getMeetingNoticeDetail(clubId, noticeId, clubMembershipInfo);
            case TAG_VOTE -> getVoteDetail(clubId, noticeId, memberId, clubMembershipInfo);
            default -> throw new GeneralException(ErrorStatus.CLUB_INVALID_TAG_TYPE);
        };
    }

    /**
     * 순수 공지사항 상세 조회
     */
    private ClubNoticeResponseDTO.ClubNoticeDetailDTO getPureNoticeDetail(Long clubId, Long itemId,
                                                                          MembershipDTO clubMembershipInfo
    ) {
        Notice notice = clubNoticeQueryService.getNotice(clubId, itemId);

        if (TAG_MEETING.equals(notice.getTag())) {
            throw new GeneralException(ErrorStatus.NOTICE_NOT_FOUND);
        }

        return ClubNoticeResponseDTO.ClubNoticeDetailDTO.builder()
                .isStaff(clubMembershipInfo.isStaff())
                .noticeItem(ClubNoticeConverter.toPureNoticeDTO(notice))
                .build();
    }

    /**
     * 모임 공지사항 상세 조회
     */
    private ClubNoticeResponseDTO.ClubNoticeDetailDTO getMeetingNoticeDetail(Long clubId, Long itemId,
                                                                             MembershipDTO clubMembershipInfo) {
        Notice notice = clubNoticeQueryService.getNotice(clubId, itemId);

        if (TAG_NOTICE.equals(notice.getTag())) {
            throw new GeneralException(ErrorStatus.NOTICE_NOT_FOUND);
        }

        MeetingInfo meetingInfo = clubMeetingAPI.getMeeting(notice.getMeetingId());

        return ClubNoticeResponseDTO.ClubNoticeDetailDTO.builder()
                .isStaff(clubMembershipInfo.isStaff())
                .noticeItem(ClubNoticeConverter.toMeetingNoticeDTO(notice, meetingInfo))
                .build();
    }

    /**
     * 투표 상세 조회
     */
    private ClubNoticeResponseDTO.ClubNoticeDetailDTO getVoteDetail(Long clubId, Long itemId, String memberId,
                                                                    MembershipDTO clubMembershipInfo) {
        Vote vote = clubNoticeQueryService.getVote(clubId, itemId);
        List<String> voteItems = vote.getItems();
        int itemCount = voteItems.size();

        // 전체 투표 결과
        List<ClubMemberVote> clubMemberVotes = clubNoticeQueryService.getMemberVotesByVoteId(vote.getId());

        // 항목별 투표자 정보 수집
        List<List<MemberExternalDTO.BasicInfo>> votedMembersByItem = collectVotedMembersByItem(vote, clubMemberVotes,
                itemCount);

        // 본인 투표 정보
        ClubMemberVote myVote = clubNoticeQueryService.getMyVote(vote.getId(), clubMembershipInfo.getClubMemberId());

        // 투표 항목 DTO 생성
        List<ClubNoticeResponseDTO.EachItemDTO> itemDTOs = createVoteItemDTOs(voteItems, myVote, votedMembersByItem,
                itemCount);

        ClubNoticeResponseDTO.VoteDTO voteDTO = ClubNoticeConverter.toVoteDTO(vote, itemDTOs);

        return ClubNoticeResponseDTO.ClubNoticeDetailDTO.builder()
                .isStaff(clubMembershipInfo.isStaff())
                .noticeItem(voteDTO)
                .build();
    }

    /**
     * 투표 항목별 투표자 정보 수집
     */
    private List<List<MemberExternalDTO.BasicInfo>> collectVotedMembersByItem(
            Vote vote, List<ClubMemberVote> clubMemberVotes, int itemCount
    ) {
        // 항목별 투표자 정보 리스트 초기화
        List<List<BasicInfo>> votedMembersByItem = initVotedMembersByItem(itemCount);

        // 익명 투표인 경우
        if (vote.isAnonymity()) {
            MemberExternalDTO.BasicInfo anoymousInfo = createAnonymousMemberInfo();

            for (ClubMemberVote mv : clubMemberVotes) {
                addVoterToItems(votedMembersByItem, mv, itemCount, anoymousInfo);
            }
            return votedMembersByItem;
        }

        // 익명 투표가 아닌 경우 회원 정보 조회 후 매핑
        Map<Long, MemberExternalDTO.BasicInfo> clubMemberIdToBasicInfo = getVoterInfoByClubMemberIds(clubMemberVotes);
        if (clubMemberIdToBasicInfo.isEmpty()) {
            return votedMembersByItem;
        }

        for (ClubMemberVote votePerMember : clubMemberVotes) {
            MemberExternalDTO.BasicInfo memberInfo = clubMemberIdToBasicInfo.get(votePerMember.getClubMemberId());
            if (memberInfo == null) {
                continue;
            }
            addVoterToItems(votedMembersByItem, votePerMember, itemCount, memberInfo);
        }

        return votedMembersByItem;
    }

    /**
     * 항목별 투표자 정보 리스트 초기화
     */
    private List<List<BasicInfo>> initVotedMembersByItem(int itemCount) {
        List<List<BasicInfo>> votedMembersByItem = new ArrayList<>();
        for (int i = 0; i < itemCount; i++) {
            votedMembersByItem.add(new ArrayList<>());
        }
        return votedMembersByItem;
    }

    /**
     * 익명 투표자 정보 생성
     */
    private MemberExternalDTO.BasicInfo createAnonymousMemberInfo() {
        return MemberExternalDTO.BasicInfo.builder()
                .nickname(ANONYMOUS_NAME)
                .profileImageUrl(ANONYMOUS_PROFILE_URL)
                .build();
    }

    /**
     * 한 명의 투표자를, 체크된 항목들에 맞게 리스트에 추가
     */
    private void addVoterToItems(
            List<List<MemberExternalDTO.BasicInfo>> votedMembersByItem,
            ClubMemberVote votePerMember,
            int itemCount,
            MemberExternalDTO.BasicInfo memberInfo
    ) {
        if (votePerMember.isItem1()) {
            votedMembersByItem.get(0).add(memberInfo);
        }
        if (itemCount >= 2 && votePerMember.isItem2()) {
            votedMembersByItem.get(1).add(memberInfo);
        }
        if (itemCount >= 3 && votePerMember.isItem3()) {
            votedMembersByItem.get(2).add(memberInfo);
        }
        if (itemCount >= 4 && votePerMember.isItem4()) {
            votedMembersByItem.get(3).add(memberInfo);
        }
        if (itemCount >= 5 && votePerMember.isItem5()) {
            votedMembersByItem.get(4).add(memberInfo);
        }
    }

    /**
     * 실명 투표 시, clubMemberId → BasicInfo 배치 조회
     */
    private Map<Long, MemberExternalDTO.BasicInfo> getVoterInfoByClubMemberIds(
            List<ClubMemberVote> clubMemberVotes
    ) {
        Set<Long> clubMemberIds = extractClubMemberIds(clubMemberVotes);
        if (clubMemberIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // ClubManagementAPI에서 멤버십 정보 배치 조회
        Map<Long, MembershipDTO> membershipMap = clubManagementAPI.getClubMembershipInfos(clubMemberIds);
        if (membershipMap.isEmpty()) {
            return Collections.emptyMap();
        }

        // memberId 리스트 추출
        List<String> memberIds = extractMemberIds(membershipMap);
        if (memberIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // MemberAPI에서 기본 정보 배치 조회
        Map<String, MemberExternalDTO.BasicInfo> memberInfoMap = memberAPI.getMemberBasicInfoMapForShare(memberIds);
        if (memberInfoMap.isEmpty()) {
            return Collections.emptyMap();
        }

        // clubMemberId → BasicInfo 맵으로 변환
        return membershipMap.values().stream()
                .collect(Collectors.toMap(
                        MembershipDTO::getClubMemberId,
                        membership -> memberInfoMap.get(membership.getMemberId()),
                        (existing, ignored) -> existing // key 충돌 시 첫 번째 값 사용
                ));
    }

    /**
     * 투표 내역에서 clubMemberId 집합 추출
     */
    private Set<Long> extractClubMemberIds(List<ClubMemberVote> clubMemberVotes) {
        return clubMemberVotes.stream()
                .map(ClubMemberVote::getClubMemberId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * 멤버십 맵에서 memberId 리스트 추출
     */
    private List<String> extractMemberIds(Map<Long, MembershipDTO> membershipMap) {
        return membershipMap.values().stream()
                .map(MembershipDTO::getMemberId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    /**
     * 투표 항목 DTO 리스트 생성
     */
    private List<ClubNoticeResponseDTO.EachItemDTO> createVoteItemDTOs(
            List<String> voteItems, ClubMemberVote myVote,
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
    private boolean isItemSelected(ClubMemberVote myVote, int itemIndex) {
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
