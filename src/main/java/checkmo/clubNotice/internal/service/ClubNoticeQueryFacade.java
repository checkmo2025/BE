package checkmo.clubNotice.internal.service;

import checkmo.clubManagement.ClubManagementAPI;
import checkmo.clubManagement.ClubManagementExternalDTO.MembershipInfo;
import checkmo.clubMeeting.ClubMeetingAPI;
import checkmo.clubMeeting.ClubMeetingExternalDTO.DetailInfo;
import checkmo.clubNotice.internal.converter.ClubNoticeConverter;
import checkmo.clubNotice.internal.entity.ClubMemberVote;
import checkmo.clubNotice.internal.entity.Notice;
import checkmo.clubNotice.internal.entity.NoticeComment;
import checkmo.clubNotice.internal.entity.Vote;
import checkmo.clubNotice.internal.exception.ClubNoticeErrorStatus;
import checkmo.clubNotice.internal.exception.ClubNoticeException;
import checkmo.clubNotice.internal.service.query.ClubNoticeQueryService;
import checkmo.clubNotice.internal.service.query.NoticeCommentQueryService;
import checkmo.clubNotice.web.dto.ClubNoticeResponseDTO;
import checkmo.clubNotice.web.dto.ClubNoticeResponseDTO.ClubNoticePreviewPage;
import checkmo.clubNotice.web.dto.ClubNoticeResponseDTO.EachItem;
import checkmo.clubNotice.web.dto.ClubNoticeResponseDTO.LatestNoticePreview;
import checkmo.clubNotice.web.dto.ClubNoticeResponseDTO.NoticeCommentList;
import checkmo.common.template.CursorPagingHelper;
import checkmo.common.template.CursorResult;
import checkmo.common.template.ExtractHelper;
import checkmo.common.template.PagePagingHelper;
import checkmo.common.template.PageResult;
import checkmo.member.MemberAPI;
import checkmo.member.MemberExternalDTO;
import checkmo.member.MemberExternalDTO.BasicInfo;
import java.util.ArrayList;
import java.util.HashMap;
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

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PINNED_NOTICES = 5;

    private static final String ANONYMOUS_NAME = "익명";
    private static final String ANONYMOUS_PROFILE_URL = "https://avatars.githubusercontent.com/u/217887881?s=200&v=4";

    private final ClubManagementAPI clubManagementAPI;
    private final ClubMeetingAPI clubMeetingAPI;
    private final MemberAPI memberAPI;

    private final ClubNoticeQueryService clubNoticeQueryService;
    private final NoticeCommentQueryService noticeCommentQueryService;

    public LatestNoticePreview retrieveLatestNotice(Long clubId) {
        clubManagementAPI.validateClub(clubId);
        Notice notice = clubNoticeQueryService.retrieveLatestNotice(clubId)
                .orElseThrow(() -> new ClubNoticeException(ClubNoticeErrorStatus.NOTICE_EMPTY));
        return ClubNoticeResponseDTO.LatestNoticePreview.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .build();
    }

    public ClubNoticePreviewPage retrieveClubNoticeList(
            Long clubId,
            Long memberId,
            int page
    ) {
        clubManagementAPI.validateClub(clubId);
        clubManagementAPI.fetchMembershipInfo(clubId, memberId);

        List<Notice> pinnedNotices = clubNoticeQueryService.retrievePinnedNotices(clubId, MAX_PINNED_NOTICES);

        PageResult<Notice> normalPageResult = PagePagingHelper.getPage(
                pageable -> clubNoticeQueryService.retrieveNormalNotices(clubId, pageable),
                page,
                DEFAULT_PAGE_SIZE
        );

        return ClubNoticePreviewPage.builder()
                .pinnedNotices(
                        pinnedNotices.stream()
                                .map(ClubNoticeConverter::toClubNoticePreview)
                                .toList()
                )
                .normalNotices(
                        ClubNoticeResponseDTO.NormalNoticePreviewPage.builder()
                                .notices(normalPageResult.content().stream()
                                        .map(ClubNoticeConverter::toClubNoticePreview)
                                        .toList())
                                .page(normalPageResult.page())
                                .size(normalPageResult.size())
                                .totalElements(normalPageResult.totalElements())
                                .totalPages(normalPageResult.totalPages())
                                .hasNext(normalPageResult.hasNext())
                                .build()
                )
                .build();
    }

    public ClubNoticeResponseDTO.ClubNoticeDetail retrieveClubNoticeDetail(
            Long clubId,
            Long noticeId,
            Long memberId
    ) {
        clubManagementAPI.validateClub(clubId);
        MembershipInfo clubMembershipInfo = clubManagementAPI.fetchMembershipInfo(clubId, memberId);

        Notice notice = clubNoticeQueryService.retrieveNoticeDetailWithVoteAndClubMemberVotes(clubId, noticeId)
                .orElseThrow(() -> new ClubNoticeException(ClubNoticeErrorStatus.NOTICE_NOT_FOUND));

        DetailInfo meetingDetail = null;
        if (notice.getTag().isMeeting()) {
            meetingDetail = clubMeetingAPI.fetchMeetingDetailInfo(notice.getMeetingId());
        }

        ClubNoticeResponseDTO.VoteDetail voteDetail = null;
        if (notice.getTag().isVote()) {
            voteDetail = buildVoteDetail(notice.getVote(), clubMembershipInfo.getClubMemberId());
        }

        return ClubNoticeConverter.toClubNoticeDetail(notice, meetingDetail, voteDetail);
    }

    public NoticeCommentList retrieveNoticeComments(Long clubId, Long noticeId, Long memberId, Long cursorId) {
        clubManagementAPI.validateClub(clubId);
        clubManagementAPI.fetchMembershipInfo(clubId, memberId);
        clubNoticeQueryService.validateNotice(clubId, noticeId);

        CursorResult<checkmo.clubNotice.internal.entity.NoticeComment> commentCursorResult =
                CursorPagingHelper.getPage(
                        size -> noticeCommentQueryService.retrieveNoticeComments(
                                noticeId,
                                cursorId,
                                size
                        ),
                        checkmo.clubNotice.internal.entity.NoticeComment::getId,
                        DEFAULT_PAGE_SIZE
                );

        Set<Long> clubMemberIds
                = ExtractHelper.extractSet(commentCursorResult.content(), NoticeComment::getClubMemberId);
        Map<Long, BasicInfo> clubMemberIdToMemberInfo = fetchBasicInfoByClubMemberId(clubMemberIds);

        List<ClubNoticeResponseDTO.NoticeComment> commentList = commentCursorResult.content().stream()
                .map(comment -> {
                    MemberExternalDTO.BasicInfo memberInfo =
                            clubMemberIdToMemberInfo.get(comment.getClubMemberId());
                    return ClubNoticeConverter.toNoticeComment(comment, memberInfo);
                })
                .toList();

        return NoticeCommentList.builder()
                .comments(commentList)
                .hasNext(commentCursorResult.hasNext())
                .nextCursor(commentCursorResult.nextCursor())
                .build();
    }

    // ========== retrieveClubNoticeDetail builder ==========

    private ClubNoticeResponseDTO.VoteDetail buildVoteDetail(Vote vote, Long myClubMemberId) {
        if (vote == null) {
            return null;
        }
        List<ClubMemberVote> clubMemberVotes =
                vote.getClubMemberVotes() == null ? List.of() : vote.getClubMemberVotes();
        List<ClubNoticeResponseDTO.EachItem> items = buildEachItems(vote, clubMemberVotes, myClubMemberId);
        return ClubNoticeConverter.toVoteDetail(vote, items);
    }

    private List<EachItem> buildEachItems(Vote vote, List<ClubMemberVote> clubMemberVotes, Long myClubMemberId) {
        if (vote == null) {
            return List.of();
        }

        // 각 투표 항목별 투표한 clubMemberId 수집
        Map<Integer, List<Long>> itemNumberToClubMemberIds = collectItemNumberToClubMemberIds(clubMemberVotes);
        if (itemNumberToClubMemberIds.isEmpty()) {
            return buildEmptyItems(vote);
        }

        // 각 투표 항목별 투표한 멤버 기본 정보 조회 -> clubMemberId와 MemberBasicInfo 매핑
        Map<Long, MemberExternalDTO.BasicInfo> clubMemberIdToMemberInfo = vote.isAnonymity() ?
                buildInfoMapForAnonymousMode(itemNumberToClubMemberIds, myClubMemberId)
                : fetchVoterBasicInfo(itemNumberToClubMemberIds);

        // itemNumber별 EachItem 생성
        List<ClubNoticeResponseDTO.EachItem> result = new ArrayList<>();
        for (Integer itemNumber : vote.getItemNumbers()) {
            String item = vote.getItemByNumber(itemNumber);
            List<Long> voterClubMemberIds = itemNumberToClubMemberIds.getOrDefault(itemNumber, List.of());

            boolean isSelectedByMe = voterClubMemberIds.contains(myClubMemberId);

            List<MemberExternalDTO.BasicInfo> votedMembers = voterClubMemberIds.stream()
                    .map(clubMemberIdToMemberInfo::get)
                    .filter(Objects::nonNull)
                    .toList();

            result.add(ClubNoticeConverter.toEachItem(itemNumber, item, isSelectedByMe, votedMembers));
        }
        return result;
    }

    private Map<Integer, List<Long>> collectItemNumberToClubMemberIds(List<ClubMemberVote> clubMemberVotes) {
        if (clubMemberVotes == null || clubMemberVotes.isEmpty()) {
            return Map.of();
        }
        Map<Integer, List<Long>> map = new HashMap<>();
        for (ClubMemberVote clubMemberVote : clubMemberVotes) {
            if (clubMemberVote == null || clubMemberVote.getClubMemberId() == null) {
                continue;
            }
            addIfSelected(map, 1, clubMemberVote.isItem1(), clubMemberVote.getClubMemberId());
            addIfSelected(map, 2, clubMemberVote.isItem2(), clubMemberVote.getClubMemberId());
            addIfSelected(map, 3, clubMemberVote.isItem3(), clubMemberVote.getClubMemberId());
            addIfSelected(map, 4, clubMemberVote.isItem4(), clubMemberVote.getClubMemberId());
            addIfSelected(map, 5, clubMemberVote.isItem5(), clubMemberVote.getClubMemberId());
            addIfSelected(map, 6, clubMemberVote.isItem6(), clubMemberVote.getClubMemberId());
        }
        return map;
    }

    private void addIfSelected(Map<Integer, List<Long>> map, int itemNumber, boolean selected, Long clubMemberId) {
        if (selected) {
            map.computeIfAbsent(itemNumber, k -> new ArrayList<>())
                    .add(clubMemberId);
        }
    }

    private List<ClubNoticeResponseDTO.EachItem> buildEmptyItems(Vote vote) {
        List<Integer> itemNumbers = vote.getItemNumbers();
        return itemNumbers.stream()
                .map(itemNumber -> ClubNoticeConverter.toEachItem(
                        itemNumber,
                        vote.getItemByNumber(itemNumber),
                        false,
                        List.of()
                ))
                .toList();
    }

    private Map<Long, MemberExternalDTO.BasicInfo> buildInfoMapForAnonymousMode(
            Map<Integer, List<Long>> itemNoToClubMemberIds,
            Long myClubMemberId
    ) {
        Set<Long> allClubMemberIds = itemNoToClubMemberIds.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toSet());

        if (allClubMemberIds.isEmpty()) {
            return Map.of();
        }

        MemberExternalDTO.BasicInfo anonymous = MemberExternalDTO.BasicInfo.builder()
                .nickname(ANONYMOUS_NAME)
                .profileImageUrl(ANONYMOUS_PROFILE_URL)
                .build();

        Map<Long, MemberExternalDTO.BasicInfo> result = allClubMemberIds.stream()
                .collect(Collectors.toMap(
                        clubMemberId -> clubMemberId,
                        clubMemberId -> anonymous
                ));

        if (myClubMemberId != null && allClubMemberIds.contains(myClubMemberId)) {
            Map<Long, MemberExternalDTO.BasicInfo> myInfoMap = fetchBasicInfoByClubMemberId(Set.of(myClubMemberId));
            MemberExternalDTO.BasicInfo myInfo = myInfoMap.get(myClubMemberId);
            result.put(myClubMemberId, myInfo);
        }
        return result;
    }

    private Map<Long, MemberExternalDTO.BasicInfo> fetchVoterBasicInfo(
            Map<Integer, List<Long>> itemNoToClubMemberIds
    ) {
        Set<Long> clubMemberIds = itemNoToClubMemberIds.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toSet());
        if (clubMemberIds.isEmpty()) {
            return Map.of();
        }

        return fetchBasicInfoByClubMemberId(clubMemberIds);
    }

    private Map<Long, MemberExternalDTO.BasicInfo> fetchBasicInfoByClubMemberId(Set<Long> clubMemberIds) {
        if (clubMemberIds == null || clubMemberIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, MembershipInfo> membershipMap =
                clubManagementAPI.fetchMembershipInfoByClubMemberIds(clubMemberIds);
        List<Long> memberIds = ExtractHelper.extractDistinctList(
                membershipMap.values(),
                MembershipInfo::getMemberId
        );
        if (memberIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, MemberExternalDTO.BasicInfo> memberBasicInfoMap =
                memberAPI.fetchMemberBasicInfoByMemberIds(memberIds);
        if (memberBasicInfoMap == null || memberBasicInfoMap.isEmpty()) {
            return Map.of();
        }
        return membershipMap.values().stream()
                .collect(Collectors.toMap(
                        MembershipInfo::getClubMemberId,
                        m -> memberBasicInfoMap.get(m.getMemberId()),
                        (a, b) -> a
                ));
    }
}
