package checkmo.clubMeeting.internal.service;

import checkmo.book.BookAPI;
import checkmo.book.BookExternalDTO;
import checkmo.book.BookExternalDTO.BasicInfo;
import checkmo.book.BookExternalDTO.DetailInfo;
import checkmo.clubManagement.ClubManagementAPI;
import checkmo.clubManagement.ClubManagementExternalDTO.MembershipInfo;
import checkmo.clubMeeting.internal.converter.ClubMeetingConverter;
import checkmo.clubMeeting.internal.entity.BookReview;
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
import checkmo.common.template.CursorPagingHelper;
import checkmo.common.template.CursorResult;
import checkmo.common.template.ExtractHelper;
import checkmo.member.MemberAPI;
import checkmo.member.MemberExternalDTO;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
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
        MembershipInfo clubMembership = validateClubAndClubMembership(clubId, memberId);

        CursorResult<Meeting> meetingCursorResult = CursorPagingHelper.getPage(
                pageSize -> clubMeetingQueryService.retrieveMeetings(clubId, cursorId, pageSize),
                Meeting::getId,
                DEFAULT_PAGE_SIZE
        );
        List<Meeting> meetings = meetingCursorResult.content();

        Map<String, BasicInfo> bookInfoMap
                = bookAPI.fetchBookBasicInfoByBookIds(ExtractHelper.extractDistinctList(meetings, Meeting::getBookId));

        return BookShelfResponseDTO.BookShelfList.builder()
                .bookShelfInfoList(mapMeetingsToBookshelfInfo(meetings, bookInfoMap))
                .hasNext(meetingCursorResult.hasNext())
                .nextCursor(meetingCursorResult.nextCursor())
                .staff(clubMembership.isStaff())
                .build();
    }

    public BookShelfResponseDTO.BookShelfDetail retrieveBookShelfDetail(Long clubId, Long meetingId, String memberId) {
        validateClubAndClubMembership(clubId, memberId);
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
        validateClubAndClubMembership(clubId, memberId);
        clubMeetingQueryService.validateMeeting(clubId, meetingId);

        CursorResult<Topic> topicCursorResult = CursorPagingHelper.getPage(
                size -> clubTopicQueryService.retrieveTopics(meetingId, cursorId, size),
                Topic::getId,
                DEFAULT_PAGE_SIZE
        );
        List<Topic> topics = topicCursorResult.content();

        Map<String, MemberExternalDTO.BasicInfo> authorInfoMap
                = memberAPI.fetchMemberBasicInfoByMemberIds(
                ExtractHelper.extractDistinctList(topics, Topic::getMemberId));

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
        MembershipInfo membershipInfo = validateClubAndClubMembership(clubId, memberId);
        clubMeetingQueryService.validateMeeting(clubId, meetingId);

        CursorResult<BookReview> bookReviewCursorResult = CursorPagingHelper.getPage(
                size -> clubBookReviewQueryService.retrieveBookReviews(meetingId, cursorId, size),
                BookReview::getId,
                DEFAULT_PAGE_SIZE
        );
        List<BookReview> bookReviews = bookReviewCursorResult.content();

        Map<String, MemberExternalDTO.BasicInfo> authorInfoMap =
                memberAPI.fetchMemberBasicInfoByMemberIds(
                        ExtractHelper.extractDistinctList(bookReviews, BookReview::getMemberId));

        return BookShelfResponseDTO.BookReviewList.builder()
                .bookReviewDetailList(
                        mapReviewsToReviewDetail(bookReviews, membershipInfo.getClubMemberId(), authorInfoMap))
                .hasNext(bookReviewCursorResult.hasNext())
                .nextCursor(bookReviewCursorResult.nextCursor())
                .build();
    }

    // ========== 미팅 관련 조회 메서드 ==========
    public MeetingResponseDTO.NextMeetingRedirect retrieveNextMeeting(Long clubId, String memberId) {
        validateClubAndClubMembership(clubId, memberId);

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
        validateClubAndClubMembership(clubId, memberId);
        Meeting meeting = clubMeetingQueryService.validateMeeting(clubId, meetingId);

        List<Team> teams = clubMeetingTeamQueryService.retrieveTeams(meetingId);
        if (teams == null || teams.isEmpty()) {
            return ClubMeetingConverter.toMeetingInfoWithTeams(meeting, List.of(), Map.of());
        }

        List<Long> teamIds = ExtractHelper.extractDistinctList(teams, Team::getId);
        Map<Long, Integer> teamIdToTeamNumberMap = mapTeamIdToTeamNumber(teams);

        // 미팅의 모든 팀원 조회
        Map<Long, Long> clubMemberIdToTeamIdMap = clubMeetingTeamQueryService.retrieveTeamIdByClubMemberId(teamIds);
        if (clubMemberIdToTeamIdMap == null || clubMemberIdToTeamIdMap.isEmpty()) {
            return ClubMeetingConverter.toMeetingInfoWithTeams(meeting, teams, Map.of());
        }

        // 클럽 멤버의 멤버십 정보 배치 조회
        Map<Long, MembershipInfo> clubMemberIdToMembershipInfoMap
                = clubManagementAPI.fetchMembershipInfoByClubMemberIds(clubMemberIdToTeamIdMap.keySet());

        // 멤버십에 해당하는 멤버 정보 배치 조회
        Map<String, MemberExternalDTO.BasicInfo> memberBasicInfoMap
                = memberAPI.fetchMemberBasicInfoByMemberIds(extractMemberIds(clubMemberIdToMembershipInfoMap));

        // teamNumber -> members 리스트 맵 구성
        Map<Integer, List<MeetingResponseDTO.MeetingMember>> teamNumberToMembersMap = mapTeamNumberToMeetingMembers(
                clubMemberIdToTeamIdMap,
                teamIdToTeamNumberMap,
                clubMemberIdToMembershipInfoMap,
                memberBasicInfoMap
        );

        return ClubMeetingConverter.toMeetingInfoWithTeams(meeting, teams, teamNumberToMembersMap);
    }

    public MeetingResponseDTO.MeetingMemberList retrieveMeetingMemberList(
            Long clubId,
            Long meetingId,
            String memberId,
            Long cursorId
    ) {
        MembershipInfo clubMembership = validateClubAndClubMembership(clubId, memberId);
        if (!clubMembership.isStaff()) {
            throw new ClubMeetingException(ClubMeetingErrorStatus.CLUB_STAFF_ONLY);
        }
        clubMeetingQueryService.validateMeeting(clubId, meetingId);

        CursorResult<MembershipInfo> membershipCursorResult = CursorPagingHelper.getPage(
                size -> clubManagementAPI.fetchActiveMembershipInfo(clubId, cursorId, size),
                MembershipInfo::getClubMemberId,
                DEFAULT_PAGE_SIZE
        );
        List<MembershipInfo> clubMembershipList = membershipCursorResult.content();

        Map<String, MemberExternalDTO.BasicInfo> memberInfoMap
                = memberAPI.fetchMemberBasicInfoByMemberIds(
                ExtractHelper.extractDistinctList(clubMembershipList, MembershipInfo::getMemberId));

        // 미팅에 존재하는 모든 팀 조회
        List<Team> teams = clubMeetingTeamQueryService.retrieveTeams(meetingId);
        Map<Long, Integer> teamIdToTeamNumberMap = mapTeamIdToTeamNumber(teams);
        List<Long> teamIds = ExtractHelper.extractDistinctList(teams, Team::getId);

        // Map<clubMemberId, teamId> 형태로 모든 팀의 팀원 조회
        Map<Long, Long> memberIdToTeamIdMap = clubMeetingTeamQueryService.retrieveTeamIdByClubMemberId(teamIds);

        // memberId -> teamNumber 맵 구성
        Map<Long, Integer> memberIdToTeamNumberMap
                = mapClubMemberIdToTeamNumber(clubMembershipList, memberIdToTeamIdMap, teamIdToTeamNumberMap);

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

    public MeetingResponseDTO.TeamTopic retrieveSelectableTopics(
            Long clubId, Long meetingId, Integer teamNumber, String memberId, Long cursorId) {
        validateClubAndClubMembership(clubId, memberId);
        clubMeetingQueryService.validateMeeting(clubId, meetingId);
        Team team = clubMeetingTeamQueryService.validateTeam(meetingId, teamNumber);

        List<Integer> existingTeamNumbers = clubMeetingTeamQueryService.retrieveExistingTeamNumbers(meetingId);

        CursorResult<Topic> topicCursorResult = CursorPagingHelper.getPage(
                size -> clubTopicQueryService.retrieveTopics(meetingId, cursorId, size),
                Topic::getId,
                DEFAULT_PAGE_SIZE
        );
        List<Topic> topics = topicCursorResult.content();

        List<Long> topicIds = ExtractHelper.extractDistinctList(topics, Topic::getId);
        Set<Long> selectedTopicIds = clubMeetingTeamQueryService.retrieveSelectedTopicIds(team.getId(), topicIds);

        // 토픽 작성자 정보 배치 조회
        Map<String, MemberExternalDTO.BasicInfo> authorInfoMap
                = memberAPI.fetchMemberBasicInfoByMemberIds(
                ExtractHelper.extractDistinctList(topics, Topic::getMemberId));

        return MeetingResponseDTO.TeamTopic.builder()
                .existingTeamNumbers(existingTeamNumbers)
                .requestedTeamNumber(teamNumber)
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

    private MembershipInfo validateClubAndClubMembership(Long clubId, String memberId) {
        clubManagementAPI.validateClub(clubId);
        return clubManagementAPI.fetchMembershipInfo(clubId, memberId);
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

    private Map<Long, Integer> mapClubMemberIdToTeamNumber(
            List<MembershipInfo> membershipInfos,
            Map<Long, Long> clubMemberIdToTeamIdMap,
            Map<Long, Integer> teamIdToTeamNumberMap
    ) {
        if (membershipInfos == null || membershipInfos.isEmpty()) {
            return Map.of();
        }

        Map<Long, Integer> result = new HashMap<>();
        for (MembershipInfo membershipInfo : membershipInfos) {
            Long clubMemberId = membershipInfo.getClubMemberId();
            Long teamId = clubMemberIdToTeamIdMap.get(clubMemberId);
            if (teamId == null) {
                // 팀이 없는 멤버는 teamNumber = null
                result.put(clubMemberId, null);
                continue;
            }

            Integer teamNumber = teamIdToTeamNumberMap.get(teamId);
            result.put(clubMemberId, teamNumber);
        }

        return result;
    }

    private Map<Integer, List<MeetingResponseDTO.MeetingMember>> mapTeamNumberToMeetingMembers(
            Map<Long, Long> clubMemberIdToTeamIdMap,
            Map<Long, Integer> teamIdToTeamNumberMap,
            Map<Long, MembershipInfo> clubMemberIdToMembershipMap,
            Map<String, MemberExternalDTO.BasicInfo> memberInfoMap
    ) {
        Map<Integer, List<MeetingResponseDTO.MeetingMember>> result = new HashMap<>();

        for (Map.Entry<Long, Long> e : clubMemberIdToTeamIdMap.entrySet()) {
            Long clubMemberId = e.getKey();
            Long teamId = e.getValue();

            Integer teamNumber = teamIdToTeamNumberMap.get(teamId);
            if (teamNumber == null) {
                continue;
            }

            MembershipInfo membership = clubMemberIdToMembershipMap.get(clubMemberId);
            if (membership == null) {
                continue;
            }

            MemberExternalDTO.BasicInfo info = memberInfoMap.get(membership.getMemberId());
            if (info == null) {
                continue;
            }

            MeetingResponseDTO.MeetingMember dto = MeetingResponseDTO.MeetingMember.builder()
                    .clubMemberId(clubMemberId)
                    .memberInfo(info)
                    .teamNumber(null)
                    .build();

            result.computeIfAbsent(teamNumber, k -> new java.util.ArrayList<>()).add(dto);
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
            Map<Long, Integer> clubMemberIdToTeamNumberMap
    ) {
        MemberExternalDTO.BasicInfo memberInfo = memberBasicInfoMap.get(membershipInfo.getMemberId());
        Long clubMemberId = membershipInfo.getClubMemberId();
        Integer teamNumber = clubMemberIdToTeamNumberMap.get(clubMemberId);
        return MeetingResponseDTO.MeetingMember.builder()
                .clubMemberId(clubMemberId)
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
