package checkmo.clubNotice.internal.service;

import checkmo.clubManagement.ClubManagementAPI;
import checkmo.clubManagement.ClubManagementExternalDTO.MembershipInfo;
import checkmo.clubMeeting.ClubMeetingAPI;
import checkmo.clubMeeting.ClubMeetingExternalDTO.DetailInfo;
import checkmo.clubNotice.internal.converter.ClubNoticeConverter;
import checkmo.clubNotice.internal.entity.ClubMemberVote;
import checkmo.clubNotice.internal.entity.Notice;
import checkmo.clubNotice.internal.entity.Vote;
import checkmo.clubNotice.internal.exception.ClubNoticeErrorStatus;
import checkmo.clubNotice.internal.exception.ClubNoticeException;
import checkmo.clubNotice.internal.service.query.ClubNoticeQueryService;
import checkmo.clubNotice.web.dto.ClubNoticeResponseDTO;
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

    private final ClubNoticeQueryService clubNoticeQueryService;

    public ClubNoticeResponseDTO.ClubNoticeList retrieveClubNoticeList(
            Long clubId,
            String memberId,
            Long cursorId,
            boolean onlyImportant
    ) {
        clubManagementAPI.validateClub(clubId);
        MembershipInfo clubMembershipInfoInfo = clubManagementAPI.fetchMembershipInfo(clubId, memberId);

        // 공지사항과 투표 각각 조회
        List<Notice> notices = clubNoticeQueryService.retrieveNotices(clubId, onlyImportant, cursorId,
                DEFAULT_PAGE_SIZE);
        List<Vote> votes = clubNoticeQueryService.retrieveVotes(clubId, onlyImportant, cursorId, DEFAULT_PAGE_SIZE);

        // 공지사항에 모임 정보 미리 조회
        Set<Long> meetingIds = extractMeetingIdsFromNotices(notices);
        Map<Long, DetailInfo> meetingInfos = clubMeetingAPI.fetchMeetingDetailInfoByMeetingIds(meetingIds);

        // 생성시간 순으로 병합 및 DTO 변환
        List<ClubNoticeResponseDTO.NoticeItem> noticeItems
                = mergeNoticesAndVotes(notices, meetingInfos, votes, DEFAULT_PAGE_SIZE);

        boolean hasNext = noticeItems.size() > DEFAULT_PAGE_SIZE;
        if (hasNext) {
            noticeItems = noticeItems.subList(0, DEFAULT_PAGE_SIZE);
        }
        Long nextCursor = hasNext && noticeItems.size() >= DEFAULT_PAGE_SIZE ?
                noticeItems.getLast().getId() : null;

        return ClubNoticeResponseDTO.ClubNoticeList.builder()
                .noticeList(noticeItems)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .pageSize(noticeItems.size())
                .isStaff(clubMembershipInfoInfo.isStaff())
                .build();
    }

    public ClubNoticeResponseDTO.ClubNoticeDetail retrieveClubNoticeDetail(
            Long clubId,
            Long noticeId,
            String tag,
            String memberId
    ) {
        clubManagementAPI.validateClub(clubId);
        MembershipInfo clubMembershipInfoInfo = clubManagementAPI.fetchMembershipInfo(clubId, memberId);

        return switch (tag) {
            case TAG_NOTICE -> getPureNoticeDetail(clubId, noticeId, clubMembershipInfoInfo);
            case TAG_MEETING -> getMeetingNoticeDetail(clubId, noticeId, clubMembershipInfoInfo);
            case TAG_VOTE -> getVoteDetail(clubId, noticeId, clubMembershipInfoInfo);
            default -> throw new ClubNoticeException(ClubNoticeErrorStatus.NOTICE_INVALID_TAG_TYPE);
        };
    }

    /**
     * 공지사항과 투표를 생성 시간 순서대로 병합하는 로직
     */
    private List<ClubNoticeResponseDTO.NoticeItem> mergeNoticesAndVotes(
            List<Notice> notices,
            Map<Long, DetailInfo> meetingInfos,
            List<Vote> votes,
            int pageSize
    ) {
        List<ClubNoticeResponseDTO.NoticeItem> resultList = new ArrayList<>();
        int n = notices.size();
        int m = votes.size();

        int i = 0, j = 0;
        while (resultList.size() < pageSize + 1 && (i < n || j < m)) {
            if (i < n && (j >= m || notices.get(i).isCreatedAfter(votes.get(j).getCreatedAt()))) {
                Notice notice = notices.get(i++);
                ClubNoticeResponseDTO.NoticeItem dto;

                if (notice.getMeetingId() != null) {
                    dto = ClubNoticeConverter.toMeetingNoticeDTO(notice, meetingInfos.get(notice.getMeetingId()));
                } else {
                    dto = ClubNoticeConverter.toPureNoticeDTO(notice);
                }

                resultList.add(dto);
            } else if (j < m) {
                Vote vote = votes.get(j++);
                List<ClubNoticeResponseDTO.EachItem> itemDTOs = ClubNoticeConverter.toEachItemDTOList(
                        vote.getItems());
                ClubNoticeResponseDTO.VoteNotice voteNoticeDTO = ClubNoticeConverter.toVoteNoticeDTO(vote, itemDTOs);
                resultList.add(voteNoticeDTO);
            }
        }

        return resultList;
    }

    private Set<Long> extractMeetingIdsFromNotices(List<Notice> notices) {
        if (notices == null) {
            return Set.of();
        }
        return notices.stream()
                .map(Notice::getMeetingId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private ClubNoticeResponseDTO.ClubNoticeDetail getPureNoticeDetail(
            Long clubId,
            Long itemId,
            MembershipInfo clubMembershipInfoInfo
    ) {
        Notice notice = clubNoticeQueryService.retrieveNotice(clubId, itemId);
        if (TAG_MEETING.equals(notice.getTag())) {
            throw new ClubNoticeException(ClubNoticeErrorStatus.NOTICE_NOT_FOUND);
        }

        return ClubNoticeResponseDTO.ClubNoticeDetail.builder()
                .isStaff(clubMembershipInfoInfo.isStaff())
                .noticeItem(ClubNoticeConverter.toPureNoticeDTO(notice))
                .build();
    }

    private ClubNoticeResponseDTO.ClubNoticeDetail getMeetingNoticeDetail(
            Long clubId,
            Long itemId,
            MembershipInfo clubMembershipInfoInfo
    ) {
        Notice notice = clubNoticeQueryService.retrieveNotice(clubId, itemId);
        if (TAG_NOTICE.equals(notice.getTag())) {
            throw new ClubNoticeException(ClubNoticeErrorStatus.NOTICE_NOT_FOUND);
        }

        DetailInfo detailInfo = clubMeetingAPI.fetchMeetingDetailInfo(notice.getMeetingId());

        return ClubNoticeResponseDTO.ClubNoticeDetail.builder()
                .isStaff(clubMembershipInfoInfo.isStaff())
                .noticeItem(ClubNoticeConverter.toMeetingNoticeDTO(notice, detailInfo))
                .build();
    }

    private ClubNoticeResponseDTO.ClubNoticeDetail getVoteDetail(
            Long clubId,
            Long itemId,
            MembershipInfo clubMembershipInfoInfo
    ) {
        Vote vote = clubNoticeQueryService.retrieveVote(clubId, itemId);
        List<String> voteItems = vote.getItems();
        int itemCount = voteItems.size();

        // 전체 투표 결과
        List<ClubMemberVote> clubMemberVotes = clubNoticeQueryService.retrieveClubMemberVotes(vote.getId());

        List<List<MemberExternalDTO.BasicInfo>> votedMembersByItem
                = collectVotedMembersByItem(vote, clubMemberVotes, itemCount);

        // 본인 투표 정보
        ClubMemberVote myVote = clubNoticeQueryService.retrieveClubMemberVote(vote.getId(),
                clubMembershipInfoInfo.getClubMemberId());

        // 투표 항목 DTO 생성
        List<ClubNoticeResponseDTO.EachItem> itemDTOs
                = createVoteItemDTOs(voteItems, myVote, votedMembersByItem, itemCount);

        ClubNoticeResponseDTO.VoteNotice voteNoticeDTO = ClubNoticeConverter.toVoteNoticeDTO(vote, itemDTOs);

        return ClubNoticeResponseDTO.ClubNoticeDetail.builder()
                .isStaff(clubMembershipInfoInfo.isStaff())
                .noticeItem(voteNoticeDTO)
                .build();
    }

    /**
     * 투표 항목별 투표자 정보 수집
     */
    private List<List<MemberExternalDTO.BasicInfo>> collectVotedMembersByItem(
            Vote vote,
            List<ClubMemberVote> clubMemberVotes,
            int itemCount
    ) {
        // 항목별 투표자 정보 리스트 초기화
        List<List<BasicInfo>> votedMembersByItem = initVotedMembersByItem(itemCount);

        // 익명 투표인 경우
        if (vote.isAnonymity()) {
            MemberExternalDTO.BasicInfo anonymousMemberInfo = createAnonymousMemberInfo();
            for (ClubMemberVote mv : clubMemberVotes) {
                addVoterToItems(votedMembersByItem, mv, itemCount, anonymousMemberInfo);
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
    private Map<Long, MemberExternalDTO.BasicInfo> getVoterInfoByClubMemberIds(List<ClubMemberVote> clubMemberVotes) {
        Set<Long> clubMemberIds = extractClubMemberIds(clubMemberVotes);
        if (clubMemberIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // ClubManagementAPI에서 멤버십 정보 배치 조회
        Map<Long, MembershipInfo> membershipMap = clubManagementAPI.fetchMembershipInfoByClubMemberIds(clubMemberIds);
        if (membershipMap.isEmpty()) {
            return Collections.emptyMap();
        }

        // MemberAPI에서 기본 정보 배치 조회
        List<String> memberIds = extractMemberIds(membershipMap);
        if (memberIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, MemberExternalDTO.BasicInfo> memberInfoMap = memberAPI.fetchMemberBasicInfoByMemberIds(memberIds);
        if (memberInfoMap.isEmpty()) {
            return Collections.emptyMap();
        }

        // clubMemberId → BasicInfo 맵으로 변환
        return membershipMap.values().stream()
                .collect(Collectors.toMap(
                        MembershipInfo::getClubMemberId,
                        membership -> memberInfoMap.get(membership.getMemberId()),
                        (existing, ignored) -> existing // key 충돌 시 첫 번째 값 사용
                ));
    }

    private Set<Long> extractClubMemberIds(List<ClubMemberVote> clubMemberVotes) {
        return clubMemberVotes.stream()
                .map(ClubMemberVote::getClubMemberId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private List<String> extractMemberIds(Map<Long, MembershipInfo> membershipMap) {
        return membershipMap.values().stream()
                .map(MembershipInfo::getMemberId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    /**
     * 투표 항목 DTO 리스트 생성
     */
    private List<ClubNoticeResponseDTO.EachItem> createVoteItemDTOs(
            List<String> voteItems,
            ClubMemberVote myVote,
            List<List<MemberExternalDTO.BasicInfo>> votedMembersByItem,
            int itemCount
    ) {
        List<ClubNoticeResponseDTO.EachItem> itemDTOs = new ArrayList<>();
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
