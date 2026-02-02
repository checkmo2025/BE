package checkmo.clubMeeting.internal.service;

import checkmo.book.BookAPI;
import checkmo.book.BookExternalDTO;
import checkmo.book.BookExternalDTO.DetailInfo;
import checkmo.clubManagement.ClubManagementAPI;
import checkmo.clubManagement.ClubManagementExternalDTO.MembershipInfo;
import checkmo.clubMeeting.internal.converter.ClubMeetingConverter;
import checkmo.clubMeeting.internal.entity.BookReview;
import checkmo.clubMeeting.internal.entity.ClubMemberTeam;
import checkmo.clubMeeting.internal.entity.Meeting;
import checkmo.clubMeeting.internal.entity.Team;
import checkmo.clubMeeting.internal.entity.Topic;
import checkmo.clubMeeting.internal.exception.ClubMeetingErrorStatus;
import checkmo.clubMeeting.internal.exception.ClubMeetingException;
import checkmo.clubMeeting.internal.service.query.ClubBookReviewQueryService;
import checkmo.clubMeeting.internal.service.query.ClubMeetingQueryService;
import checkmo.clubMeeting.internal.service.query.ClubMeetingTeamQueryService;
import checkmo.clubMeeting.internal.service.query.ClubTopicQueryService;
import checkmo.clubMeeting.web.dto.bookshelf.BookShelfResponseDTO;
import checkmo.clubMeeting.web.dto.bookshelf.BookShelfResponseDTO.BookShelfDetail;
import checkmo.clubMeeting.web.dto.meeting.MeetingResponseDTO;
import checkmo.clubMeeting.web.dto.meeting.MeetingResponseDTO.MeetingInfo;
import checkmo.common.template.CursorPagingHelper;
import checkmo.common.template.CursorResult;
import checkmo.common.template.ExtractHelper;
import checkmo.member.MemberAPI;
import checkmo.member.MemberExternalDTO;
import java.time.LocalDateTime;
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
public class ClubMeetingQueryFacade {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final BookAPI bookAPI;
    private final MemberAPI memberAPI;
    private final ClubManagementAPI clubManagementAPI;

    private final ClubMeetingQueryService clubMeetingQueryService;
    private final ClubTopicQueryService clubTopicQueryService;
    private final ClubBookReviewQueryService clubBookReviewQueryService;
    private final ClubMeetingTeamQueryService clubMeetingTeamQueryService;

    // ========== 책장 관련 조회 메서드 ==========
    public BookShelfResponseDTO.BookShelfList retrieveBookShelfList(
            Long clubId,
            String memberId,
            Long cursorId
    ) {
        clubManagementAPI.validateClub(clubId);
        clubManagementAPI.fetchMembershipInfo(clubId, memberId);

        CursorResult<Meeting> meetingCursorResult = CursorPagingHelper.getPage(
                pageSize -> clubMeetingQueryService.retrieveMeetings(clubId, cursorId, pageSize),
                Meeting::getId,
                DEFAULT_PAGE_SIZE
        );
        List<Meeting> meetings = meetingCursorResult.content();

        // 미팅의 책 정보 배치 조회
        List<String> bookIds = ExtractHelper.extractDistinctList(meetings, Meeting::getBookId);
        Map<String, BookExternalDTO.BasicInfo> bookInfoMap = bookAPI.fetchBookBasicInfoByBookIds(bookIds);

        return BookShelfResponseDTO.BookShelfList.builder()
                .bookShelfInfoList(mapMeetingsToBookshelfInfo(meetings, bookInfoMap))
                .hasNext(meetingCursorResult.hasNext())
                .nextCursor(meetingCursorResult.nextCursor())
                .build();
    }

    public BookShelfResponseDTO.BookShelfDetail retrieveBookShelfDetail(Long clubId, Long meetingId, String memberId) {
        clubManagementAPI.validateClub(clubId);
        clubManagementAPI.fetchMembershipInfo(clubId, memberId);
        Meeting meeting = clubMeetingQueryService.validateMeeting(clubId, meetingId);

        DetailInfo bookInfo = bookAPI.fetchBookDetailInfo(meeting.getBookId());

        return BookShelfDetail.builder()
                .meetingInfo(ClubMeetingConverter.toMeetingInfoDTOForBookshelves(meeting))
                .bookDetailInfo(bookInfo)
                .build();
    }

    public BookShelfResponseDTO.TopicList retrieveTopicList(
            Long clubId,
            Long meetingId,
            String memberId,
            Long cursorId
    ) {
        clubManagementAPI.validateClub(clubId);
        clubManagementAPI.fetchMembershipInfo(clubId, memberId);
        clubMeetingQueryService.validateMeeting(clubId, meetingId);

        CursorResult<Topic> topicCursorResult = CursorPagingHelper.getPage(
                size -> clubTopicQueryService.retrieveTopics(meetingId, cursorId, size),
                Topic::getId,
                DEFAULT_PAGE_SIZE
        );
        List<Topic> topics = topicCursorResult.content();

        // 발제의 작성자 정보 배치 조회
        List<String> authorIds = ExtractHelper.extractDistinctList(topics, Topic::getMemberId);
        Map<String, MemberExternalDTO.BasicInfo> authorInfoMap = memberAPI.fetchMemberBasicInfoByMemberIds(authorIds);

        return BookShelfResponseDTO.TopicList.builder()
                .topicDetailList(mapTopicsToTopicDetail(topics, authorInfoMap, memberId))
                .hasNext(topicCursorResult.hasNext())
                .nextCursor(topicCursorResult.nextCursor())
                .build();
    }

    public BookShelfResponseDTO.BookReviewList retrieveBookReviewList(
            Long clubId,
            Long meetingId,
            String memberId,
            Long cursorId
    ) {
        clubManagementAPI.validateClub(clubId);
        MembershipInfo membershipInfo = clubManagementAPI.fetchMembershipInfo(clubId, memberId);
        clubMeetingQueryService.validateMeeting(clubId, meetingId);

        CursorResult<BookReview> bookReviewCursorResult = CursorPagingHelper.getPage(
                size -> clubBookReviewQueryService.retrieveBookReviews(meetingId, cursorId, size),
                BookReview::getId,
                DEFAULT_PAGE_SIZE
        );
        List<BookReview> bookReviews = bookReviewCursorResult.content();

        // 한줄평 작성자 정보 배치 조회
        List<String> authorIds = ExtractHelper.extractDistinctList(bookReviews, BookReview::getMemberId);
        Map<String, MemberExternalDTO.BasicInfo> authorInfoMap = memberAPI.fetchMemberBasicInfoByMemberIds(authorIds);

        return BookShelfResponseDTO.BookReviewList.builder()
                .bookReviewDetailList(
                        mapReviewsToReviewDetail(bookReviews, membershipInfo.getClubMemberId(), authorInfoMap))
                .hasNext(bookReviewCursorResult.hasNext())
                .nextCursor(bookReviewCursorResult.nextCursor())
                .build();
    }

    // ========== 미팅 관련 조회 메서드 ==========
    public MeetingResponseDTO.NextMeetingRedirect retrieveNextMeeting(Long clubId, String memberId) {
        clubManagementAPI.validateClub(clubId);
        clubManagementAPI.fetchMembershipInfo(clubId, memberId);

        Meeting nextMeeting = clubMeetingQueryService.retrieveNextFutureMeeting(clubId, LocalDateTime.now());
        return MeetingResponseDTO.NextMeetingRedirect.builder()
                .meetingId(nextMeeting.getId())
                .redirectUrl("/clubs/" + clubId + "/meetings/" + nextMeeting.getId())
                .build();
    }

    public MeetingResponseDTO.MeetingInfo retrieveMeetingInfo(
            Long clubId,
            Long meetingId,
            String memberId
    ) {
        clubManagementAPI.validateClub(clubId);
        clubManagementAPI.fetchMembershipInfo(clubId, memberId);
        Meeting meeting = clubMeetingQueryService.validateMeeting(clubId, meetingId);
        List<Integer> existingTeamNumbers = clubMeetingTeamQueryService.retrieveExistingTeamNumbers(meetingId);

        return MeetingInfo.builder()
                .meetingId(meeting.getId())
                .title(meeting.getTitle())
                .meetingTime(meeting.getMeetingTime())
                .location(meeting.getLocation())
                .existingTeamNumbers(existingTeamNumbers)
                .build();
    }

    public MeetingResponseDTO.MeetingMemberList retrieveMeetingMemberList(
            Long clubId,
            Long meetingId,
            String memberId,
            Long cursorId
    ) {
        clubManagementAPI.validateClub(clubId);
        MembershipInfo clubMembership = clubManagementAPI.fetchMembershipInfo(clubId, memberId);
        clubMeetingQueryService.validateMeeting(clubId, meetingId);
        if (!clubMembership.isStaff()) {
            throw new ClubMeetingException(ClubMeetingErrorStatus.CLUB_STAFF_ONLY);
        }

        CursorResult<MembershipInfo> membershipCursorResult = CursorPagingHelper.getPage(
                size -> clubManagementAPI.fetchActiveMembershipInfo(clubId, cursorId, size),
                MembershipInfo::getClubMemberId,
                DEFAULT_PAGE_SIZE
        );
        List<MembershipInfo> clubMembershipList = membershipCursorResult.content();

        List<String> memberIds = ExtractHelper.extractDistinctList(clubMembershipList, MembershipInfo::getMemberId);
        Map<String, MemberExternalDTO.BasicInfo> memberInfoMap = memberAPI.fetchMemberBasicInfoByMemberIds(memberIds);

        // 미팅에 존재하는 모든 팀 조회
        List<Team> teams = clubMeetingTeamQueryService.retrieveTeams(meetingId);
        List<Long> teamIds = ExtractHelper.extractDistinctList(teams, Team::getId);
        Map<Long, Integer> teamIdToTeamNumberMap = mapTeamIdToTeamNumber(teams);

        // Map<clubMemberId, teamId> 형태로 모든 팀의 팀원 조회
        Map<Long, Long> memberIdToTeamIdMap = clubMeetingTeamQueryService.retrieveTeamIdByClubMemberId(teamIds);

        // memberId -> teamNumber 맵 구성
        Map<String, Integer> memberIdToTeamNumberMap
                = mapMemberIdToTeamNumber(clubMembershipList, memberIdToTeamIdMap, teamIdToTeamNumberMap);

        return MeetingResponseDTO.MeetingMemberList.builder()
                .members(
                        clubMembershipList.stream()
                                .map(membership
                                        -> toMeetingMemberDTO(membership, memberInfoMap, memberIdToTeamNumberMap))
                                .toList()
                )
                .existingTeamNumbers(ExtractHelper.extractDistinctList(teams, Team::getTeamNumber))
                .hasNext(membershipCursorResult.hasNext())
                .nextCursor(membershipCursorResult.nextCursor())
                .build();
    }

    public MeetingResponseDTO.TeamMember retrieveTeamMember(
            Long clubId,
            Long meetingId,
            Integer teamNumber,
            String memberId,
            Long cursorId
    ) {
        clubManagementAPI.validateClub(clubId);
        clubManagementAPI.fetchMembershipInfo(clubId, memberId);
        clubMeetingQueryService.validateMeeting(clubId, meetingId);
        Team team = clubMeetingTeamQueryService.validateTeam(meetingId, teamNumber);

        CursorResult<ClubMemberTeam> clubMemberTeamCursorResult = CursorPagingHelper.getPage(
                size -> clubMeetingTeamQueryService.retrieveClubMemberTeams(
                        team.getId(),
                        cursorId,
                        size
                ),
                ClubMemberTeam::getId,
                DEFAULT_PAGE_SIZE
        );
        List<ClubMemberTeam> clubMemberTeams = clubMemberTeamCursorResult.content();

        // 클럽 멤버의 기본 정보 배치 조회
        Set<Long> clubMemberIds = ExtractHelper.extractSet(clubMemberTeams, ClubMemberTeam::getClubMemberId);
        Map<Long, MembershipInfo> clubMemberIdToClubMembership = clubManagementAPI.fetchMembershipInfoByClubMemberIds(
                clubMemberIds);
        List<String> memberIds = extractMemberIds(clubMemberIdToClubMembership);
        Map<String, MemberExternalDTO.BasicInfo> memberBasicInfoMap
                = memberAPI.fetchMemberBasicInfoByMemberIds(memberIds);

        return MeetingResponseDTO.TeamMember.builder()
                .teamNumber(teamNumber)
                .members(clubMemberTeams.stream()
                        .map(clubMemberTeam -> {
                            Long clubMemberId = clubMemberTeam.getClubMemberId();
                            MembershipInfo membershipInfo = clubMemberIdToClubMembership.get(clubMemberId);
                            MemberExternalDTO.BasicInfo memberInfo = memberBasicInfoMap.get(
                                    membershipInfo.getMemberId());
                            return MeetingResponseDTO.MeetingMember.builder()
                                    .clubMemberId(clubMemberId)
                                    .memberInfo(memberInfo)
                                    .teamNumber(null)
                                    .build();
                        })
                        .filter(Objects::nonNull)
                        .toList()
                )
                .hasNext(clubMemberTeamCursorResult.hasNext())
                .nextCursor(clubMemberTeamCursorResult.nextCursor())
                .build();
    }

    public MeetingResponseDTO.TeamTopic retrieveSelectableTopics(
            Long clubId, Long meetingId, Integer teamNumber, String memberId, Long cursorId) {
        clubManagementAPI.validateClub(clubId);
        clubManagementAPI.fetchMembershipInfo(clubId, memberId);
        clubMeetingQueryService.validateMeeting(clubId, meetingId);
        Team team = clubMeetingTeamQueryService.validateTeam(meetingId, teamNumber);

        CursorResult<Topic> topicCursorResult = CursorPagingHelper.getPage(
                size -> clubTopicQueryService.retrieveTopics(meetingId, cursorId, size),
                Topic::getId,
                DEFAULT_PAGE_SIZE
        );
        List<Topic> topics = topicCursorResult.content();

        List<Long> topicIds = ExtractHelper.extractDistinctList(topics, Topic::getId);
        Set<Long> selectedTopicIds = clubMeetingTeamQueryService.retrieveSelectedTopicIds(team.getId(), topicIds);

        // 토픽 작성자 정보 배치 조회
        List<String> authorIds = ExtractHelper.extractDistinctList(topics, Topic::getMemberId);
        Map<String, MemberExternalDTO.BasicInfo> authorInfoMap = memberAPI.fetchMemberBasicInfoByMemberIds(authorIds);

        return MeetingResponseDTO.TeamTopic.builder()
                .teamNumber(teamNumber)
                .topics(topics.stream()
                        .map(t -> ClubMeetingConverter.toTopicDTO(
                                        t,
                                        authorInfoMap.get(t.getMemberId()),
                                        selectedTopicIds.contains(t.getId())
                                )
                        )
                        .toList())
                .hasNext(topicCursorResult.hasNext())
                .nextCursor(topicCursorResult.nextCursor())
                .build();
    }

    private List<BookShelfResponseDTO.TopicDetail> mapTopicsToTopicDetail(
            List<Topic> topics,
            Map<String, MemberExternalDTO.BasicInfo> authorInfoMap,
            String memberId //요청한 회원 ID -> 작성자 판별용
    ) {
        return topics.stream()
                .map(topic ->
                        ClubMeetingConverter.toTopicDetailDTO(topic, authorInfoMap.get(topic.getMemberId()), memberId))
                .toList();
    }

    private List<BookShelfResponseDTO.BookShelfInfo> mapMeetingsToBookshelfInfo(
            List<Meeting> meetings,
            Map<String, BookExternalDTO.BasicInfo> bookInfoMap
    ) {
        return meetings.stream()
                .map(meeting -> ClubMeetingConverter.toBookshelfInfoDTO(meeting, bookInfoMap.get(meeting.getBookId())))
                .toList();
    }

    private List<BookShelfResponseDTO.BookReviewDetail> mapReviewsToReviewDetail(
            List<BookReview> bookReviews,
            Long actorId,
            Map<String, MemberExternalDTO.BasicInfo> authorInfoMap
    ) {
        return bookReviews.stream()
                .map(review -> ClubMeetingConverter.toBookReviewDetailDTO(
                        review,
                        authorInfoMap.get(review.getMemberId()),
                        review.isOwnedBy(actorId)
                ))
                .toList();
    }

    private Map<String, Integer> mapMemberIdToTeamNumber(
            List<MembershipInfo> membershipInfos,
            Map<Long, Long> clubMemberIdToTeamIdMap,
            Map<Long, Integer> teamIdToTeamNumberMap
    ) {
        Map<String, Integer> result = new HashMap<>();

        for (MembershipInfo membershipInfo : membershipInfos) {
            Long clubMemberId = membershipInfo.getClubMemberId();
            String memberId = membershipInfo.getMemberId();

            Long teamId = clubMemberIdToTeamIdMap.get(clubMemberId);
            if (teamId == null) {
                // 팀이 없는 멤버는 teamNumber = null
                result.put(memberId, null);
                continue;
            }

            Integer teamNumber = teamIdToTeamNumberMap.get(teamId);
            result.put(memberId, teamNumber);
        }

        return result;
    }

    private Map<Long, Integer> mapTeamIdToTeamNumber(List<Team> teams) {
        if (teams == null) {
            return Map.of();
        }
        return teams.stream()
                .collect(Collectors.toMap(
                        Team::getId, // key: 팀 ID
                        Team::getTeamNumber // value: 팀 번호
                ));
    }

    private MeetingResponseDTO.MeetingMember toMeetingMemberDTO(
            MembershipInfo membershipInfo,
            Map<String, MemberExternalDTO.BasicInfo> memberBasicInfoMap,
            Map<String, Integer> memberIdToTeamNumberMap
    ) {
        String memberId = membershipInfo.getMemberId();
        MemberExternalDTO.BasicInfo memberInfo = memberBasicInfoMap.get(memberId);
        Integer teamNumber = memberIdToTeamNumberMap.get(memberId);
        return MeetingResponseDTO.MeetingMember.builder()
                .clubMemberId(membershipInfo.getClubMemberId())
                .memberInfo(memberInfo)
                .teamNumber(teamNumber)
                .build();
    }

    // ========== 추출 메서드 ==========
    private List<String> extractMemberIds(Map<Long, MembershipInfo> clubMembershipMap) {
        if (clubMembershipMap == null) {
            return List.of();
        }
        return clubMembershipMap.values().stream()
                .map(MembershipInfo::getMemberId)
                .distinct()
                .toList();
    }

}
