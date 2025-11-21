package checkmo.clubMeeting.internal.service;

import checkmo.book.BookAPI;
import checkmo.book.BookExternalDTO;
import checkmo.book.BookExternalDTO.DetailInfo;
import checkmo.clubManagement.ClubManagementAPI;
import checkmo.clubManagement.ClubManagementExternalDTO.Membership;
import checkmo.clubMeeting.internal.converter.ClubMeetingConverter;
import checkmo.clubMeeting.internal.entity.BookReview;
import checkmo.clubMeeting.internal.entity.ClubMemberTeam;
import checkmo.clubMeeting.internal.entity.Meeting;
import checkmo.clubMeeting.internal.entity.Team;
import checkmo.clubMeeting.internal.entity.TeamTopic;
import checkmo.clubMeeting.internal.entity.Topic;
import checkmo.clubMeeting.internal.service.query.ClubBookReviewQueryService;
import checkmo.clubMeeting.internal.service.query.ClubMeetingQueryService;
import checkmo.clubMeeting.internal.service.query.ClubMeetingTeamQueryService;
import checkmo.clubMeeting.internal.service.query.ClubTopicQueryService;
import checkmo.clubMeeting.web.dto.bookshelf.BookShelfResponseDTO;
import checkmo.clubMeeting.web.dto.bookshelf.BookShelfResponseDTO.BookShelfDetail;
import checkmo.clubMeeting.web.dto.meeting.MeetingResponseDTO;
import checkmo.clubMeeting.web.dto.meeting.MeetingResponseDTO.MeetingInfo;
import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
import checkmo.common.template.CursorPagingHelper;
import checkmo.common.template.CursorResult;
import checkmo.member.MemberAPI;
import checkmo.member.MemberExternalDTO;
import checkmo.member.MemberExternalDTO.BasicInfo;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClubMeetingQueryFacade {

    // 페이징 기본 크기 상수
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int TOPIC_PREVIEW_SIZE_FOR_BOOKSHELF = 3;
    private static final int TOPIC_PREVIEW_SIZE_FOR_MEETING = 4;

    // Domain level 2
    private final MemberAPI memberAPI;

    // Domain level 1
    private final BookAPI bookAPI;

    private final ClubManagementAPI clubManagementAPI;

    private final ClubMeetingQueryService clubMeetingQueryService;
    private final ClubTopicQueryService clubTopicQueryService;
    private final ClubBookReviewQueryService clubBookReviewQueryService;
    private final ClubMeetingTeamQueryService clubMeetingTeamQueryService;

    public BookShelfResponseDTO.BookShelfList getBookShelfList(
            Long clubId,
            Long cursorId,
            Integer generation,
            String memberId
    ) {
        clubManagementAPI.validateClub(clubId);
        Membership clubMembershipInfo = clubManagementAPI.getClubMembershipInfo(clubId, memberId);

        CursorResult<Meeting> meetingCursorResult = CursorPagingHelper.getPage(
                pageSize -> clubMeetingQueryService.getBookShelfList(clubId, generation, cursorId, pageSize),
                Meeting::getId,
                DEFAULT_PAGE_SIZE
        );
        List<Meeting> meetings = meetingCursorResult.content();

        // 미팅의 책 정보 배치 조회
        List<String> bookIds = extractBookIdsFromMeetings(meetings);
        Map<String, BookExternalDTO.BasicInfo> bookInfoMap = bookAPI.getBookBasicInfoMapForShare(bookIds);

        return BookShelfResponseDTO.BookShelfList.builder()
                .bookShelfInfoList(mapMeetingsToBookshelfInfo(meetings, bookInfoMap))
                .hasNext(meetingCursorResult.hasNext())
                .nextCursor(meetingCursorResult.nextCursor())
                .membership(clubMembershipInfo)
                .build();
    }

    // TODO: getBookShelftDetail, findTopicsByMeeting 간 중복 제거
    public BookShelfResponseDTO.BookShelfDetail getBookShelfDetail(Long meetingId, String memberId) {
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        Membership clubMembershipInfo = clubManagementAPI.getClubMembershipInfo(meeting.getClubId(), memberId);

        // [발제 미리보기] 발제 리스트 조회
        CursorResult<Topic> topicCursorResult = CursorPagingHelper.getPage(
                size -> clubTopicQueryService.findTopicsByMeeting(meetingId, null, size),
                Topic::getId,
                TOPIC_PREVIEW_SIZE_FOR_BOOKSHELF
        );
        List<Topic> topics = topicCursorResult.content();

        // 발제의 작성자 정보 배치 조회
        List<String> authorIds = extractMemberIdsFromTopics(topics);
        Map<String, MemberExternalDTO.BasicInfo> authorInfoMap = memberAPI.getMemberBasicInfoMapForShare(authorIds);

        // 미팅의 책 정보 조회
        DetailInfo bookInfo = bookAPI.getBookDetailInfoForShare(meeting.getBookId());

        List<BookShelfResponseDTO.TopicDetail> topicDetailList
                = mapTopicsToTopicDetail(topics, authorInfoMap, memberId);
        BookShelfResponseDTO.TopicList topicListDTO = BookShelfResponseDTO.TopicList.builder()
                .topicDetailList(topicDetailList)
                .hasNext(topicCursorResult.hasNext())
                .nextCursor(topicCursorResult.nextCursor())
                .membership(null)
                .build();
        return BookShelfDetail.builder()
                .meetingInfo(ClubMeetingConverter.toMeetingInfoDTO(meeting))
                .bookDetailInfo(bookInfo)
                .topicList(topicListDTO)
                .membership(clubMembershipInfo)
                .build();
    }

    public BookShelfResponseDTO.TopicList findTopicsByMeeting(Long meetingId, Long cursorId, String memberId) {
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        Membership clubMembershipInfo = clubManagementAPI.getClubMembershipInfo(meeting.getClubId(), memberId);

        CursorResult<Topic> topicCursorResult = CursorPagingHelper.getPage(
                size -> clubTopicQueryService.findTopicsByMeeting(meetingId, cursorId, size),
                Topic::getId,
                TOPIC_PREVIEW_SIZE_FOR_BOOKSHELF
        );
        List<Topic> topics = topicCursorResult.content();

        // 발제의 작성자 정보 배치 조회
        List<String> authorIds = extractMemberIdsFromTopics(topics);
        Map<String, MemberExternalDTO.BasicInfo> authorInfoMap = memberAPI.getMemberBasicInfoMapForShare(authorIds);

        List<BookShelfResponseDTO.TopicDetail> topicDetailList
                = mapTopicsToTopicDetail(topics, authorInfoMap, memberId);
        return BookShelfResponseDTO.TopicList.builder()
                .topicDetailList(topicDetailList)
                .hasNext(topicCursorResult.hasNext())
                .nextCursor(topicCursorResult.nextCursor())
                .membership(clubMembershipInfo)
                .build();
    }

    public BookShelfResponseDTO.BookReviewList getBookReviewList(Long meetingId, Long lastReviewId, String memberId) {
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        Membership clubMembershipInfo = clubManagementAPI.getClubMembershipInfo(meeting.getClubId(), memberId);

        CursorResult<BookReview> bookReviewCursorResult = CursorPagingHelper.getPage(
                size -> clubBookReviewQueryService.findBookReviewsByMeeting(meetingId, lastReviewId, size),
                BookReview::getId,
                DEFAULT_PAGE_SIZE
        );
        List<BookReview> bookReviews = bookReviewCursorResult.content();

        // 한줄평 작성자 정보 배치 조회
        List<String> authorIds = extractMemberIdsFromBookReviews(bookReviews);
        Map<String, MemberExternalDTO.BasicInfo> authorInfoMap = memberAPI.getMemberBasicInfoMapForShare(authorIds);

        List<BookShelfResponseDTO.BookReviewDetail> bookReviewDetailList
                = mapReviewsToReviewDetail(bookReviews, authorInfoMap);
        return BookShelfResponseDTO.BookReviewList.builder()
                .bookReviewDetailList(bookReviewDetailList)
                .hasNext(bookReviewCursorResult.hasNext())
                .nextCursor(bookReviewCursorResult.nextCursor())
                .membership(clubMembershipInfo)
                .build();
    }

    public MeetingResponseDTO.MeetingList getMeetingsByClub(
            Long clubId,
            Long cursorId,
            String memberId
    ) {
        clubManagementAPI.validateClub(clubId);
        Membership clubMembershipInfo = clubManagementAPI.getClubMembershipInfo(clubId, memberId);

        CursorResult<Meeting> meetingCursorResult = CursorPagingHelper.getPage(
                size -> clubMeetingQueryService.findMeetingsByClubAndCursor(clubId, cursorId, size),
                Meeting::getId,
                DEFAULT_PAGE_SIZE
        );
        List<Meeting> meetings = meetingCursorResult.content();

        // 미팅의 모든 도서 배치 조회
        List<String> bookIds = extractBookIdsFromMeetings(meetings);
        Map<String, BookExternalDTO.BasicInfo> bookInfoMap = bookAPI.getBookBasicInfoMapForShare(bookIds);

        List<MeetingResponseDTO.MeetingInfo> meetingInfoList = mapMeetingsToMeetingInfo(meetings, bookInfoMap);
        return MeetingResponseDTO.MeetingList.builder()
                .meetingInfoList(meetingInfoList)
                .hasNext(meetingCursorResult.hasNext())
                .nextCursor(meetingCursorResult.nextCursor())
                .membership(clubMembershipInfo)
                .build();
    }

    public MeetingResponseDTO.MeetingDetail findMeetingDetailById(Long meetingId, String memberId) {
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        Membership clubMembershipInfo = clubManagementAPI.getClubMembershipInfo(meeting.getClubId(), memberId);

        // [발제 전체보기 - 미리보기] 발제 최신순 상위 4개 토픽 리스트 조회
        List<Topic> topics
                = clubTopicQueryService.findTopicsByMeeting(meetingId, null, TOPIC_PREVIEW_SIZE_FOR_MEETING);

        // [발제 전체보기 - 미리보기] TeamTopic(+Team) 배치 조회
        List<Long> topicIds = extractTopicIds(topics);
        Map<Long, List<Integer>> topicIdToSelectTeamNumbers
                = clubMeetingTeamQueryService.findTeamTopicsWithTeamByTopicIds(topicIds);

        // [토론 x조 - 미리보기] 해당하는 미팅의 존재하는 모든 팀 조회
        List<Team> teams = clubMeetingTeamQueryService.findTeamsByMeeting(meetingId);

        // [토론 x조 - 미리보기] 모든 팀의 발제 등록순 상위 4개 토픽 조회
        Map<Integer, List<TeamTopic>> teamNumberToTeamTopics = teams.stream()
                .collect(Collectors.toMap(
                        Team::getTeamNumber, // key: 팀 번호
                        team -> clubMeetingTeamQueryService.findTeamTopicsWithTopicByTeamId(team.getId(),
                                TOPIC_PREVIEW_SIZE_FOR_MEETING) //value : 해당 팀의 발제 최신순 상위 4개 팀 토픽 리스트
                ));

        // 조회한 모든 발제(topics와 teamTopics)의 작성자 id를 중복 없이 리스트 조회
        List<String> authorIds1 = extractMemberIdsFromTopics(topics);
        List<String> authorIds2 = extractMemberIdsFromTeamTopics(teamNumberToTeamTopics);
        List<String> authorIds = Stream.concat(authorIds1.stream(), authorIds2.stream())
                .distinct()
                .toList();

        // 발제의 작성자 정보 배치 조회
        Map<String, MemberExternalDTO.BasicInfo> authorInfoMap = memberAPI.getMemberBasicInfoMapForShare(authorIds);

        // 미팅의 책 정보 조회
        BookExternalDTO.BasicInfo bookSharedDTO = bookAPI.getBookBasicInfoForShare(meeting.getBookId());

        // DTO 변환
        MeetingResponseDTO.MeetingInfo meetingInfo = ClubMeetingConverter.toMeetingInfoDTO(meeting, bookSharedDTO);
        List<MeetingResponseDTO.Topic> topicList = mapTopicsToTopicDetail(topics, authorInfoMap,
                topicIdToSelectTeamNumbers);
        List<MeetingResponseDTO.TeamTopic> teamTopicList = assemble(teams, teamNumberToTeamTopics, authorInfoMap);
        return MeetingResponseDTO.MeetingDetail.builder()
                .meetingInfo(meetingInfo)
                .topics(topicList)
                .teams(teamTopicList)
                .membership(clubMembershipInfo)
                .build();
    }

    public MeetingResponseDTO.TopicDTO findMeetingTopicsWithTeam(Long meetingId, String memberId) {
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        Membership clubMembershipInfo = clubManagementAPI.getClubMembershipInfo(meeting.getClubId(), memberId);

        // 2. 토픽 리스트 조회
        List<Topic> topics = clubTopicQueryService.findTopicsByMeeting(meetingId, null, null);

        // Topic -> TeamTopic과 Team FETCH JOIN 조회
        List<Long> topicIds = extractTopicIds(topics);
        Map<Long, List<Integer>> topicIdToSelectTeamNumbers
                = clubMeetingTeamQueryService.findTeamTopicsWithTeamByTopicIds(topicIds);

        // 토픽 작성자 정보 배치 조회
        List<String> authorIds = extractMemberIdsFromTopics(topics);
        Map<String, MemberExternalDTO.BasicInfo> authorInfoMap = memberAPI.getMemberBasicInfoMapForShare(authorIds);

        List<MeetingResponseDTO.Topic> topicList
                = mapTopicsToTopicDetail(topics, authorInfoMap, topicIdToSelectTeamNumbers);
        return MeetingResponseDTO.TopicDTO.builder()
                .topics(topicList)
                .membership(clubMembershipInfo)
                .build();
    }

    public MeetingResponseDTO.TeamTopic findMeetingTopicsByTeam(Long meetingId, Integer teamNumber, String memberId) {
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        Membership clubMembershipInfo = clubManagementAPI.getClubMembershipInfo(meeting.getClubId(), memberId);
        Team team = clubMeetingTeamQueryService.validateTeam(meetingId, teamNumber);

        // 팀 토픽 > 토픽 > 클럽 멤버 정보 전체 조회
        List<TeamTopic> teamTopics = clubMeetingTeamQueryService.findTeamTopicsWithTopicByTeamId(team.getId(), null);

        // 토픽 작성자 정보 배치 조회
        List<String> authorIds = extractMemberIdsFromTeamTopics(teamTopics);
        Map<String, MemberExternalDTO.BasicInfo> authorInfoMap = memberAPI.getMemberBasicInfoMapForShare(authorIds);

        List<MeetingResponseDTO.Topic> topicList = mapTopicsToTopicDetail(
                extractTopicFromTeamTopics(teamTopics),
                authorInfoMap,
                Map.of() // 팀 토픽은 팀 번호가 필요없으니까 빈 Map 전달
        );
        return MeetingResponseDTO.TeamTopic.builder()
                .teamNumber(teamNumber)
                .topics(topicList)
                .membership(clubMembershipInfo)
                .build();
    }

    public MeetingResponseDTO.CalendarMeeting getClubMeetingCalendar(
            Long clubId,
            int year,
            int month,
            String memberId
    ) {
        clubManagementAPI.validateClub(clubId);
        Membership clubMembershipInfo = clubManagementAPI.getClubMembershipInfo(clubId, memberId);

        List<Meeting> meetings = clubMeetingQueryService.getClubMeetingByYearAndMonth(clubId, year, month, memberId);

        List<MeetingInfo> meetingInfoDTOList = toMeetingInfoDTOList(meetings);
        return MeetingResponseDTO.CalendarMeeting.builder()
                .meetingInfoList(meetingInfoDTOList)
                .membership(clubMembershipInfo)
                .build();
    }

    public MeetingResponseDTO.MeetingMemberList findMeetingMembersByMeeting(
            Long meetingId,
            Long cursorId,
            String memberId
    ) {
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        Membership clubMembershipInfo = clubManagementAPI.getClubMembershipInfo(meeting.getClubId(), memberId);
        if (!clubMembershipInfo.isStaff()) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }

        // 2. 클럽의 회원 조회 및 페이징 처리 (이때 PENDING이나 BLOCKED 상태는 제외하고 STAFF나 MEMBER만 조회)
        CursorResult<Membership> membershipCursorResult = CursorPagingHelper.getPage(
                size -> clubManagementAPI.getClubMembersByStatus(meeting.getClubId(), cursorId, size),
                Membership::getClubMemberId,
                DEFAULT_PAGE_SIZE
        );
        List<Membership> clubMembership = membershipCursorResult.content();

        // 3. 클럽 멤버에 대한 정보 배치 조회 (ClubMember의 memberId로 MemberExternalDTO.BasicInfoDTO 조회)
        List<String> memberIds = extractMemberIdsFromClubMembers(clubMembership);
        Map<String, MemberExternalDTO.BasicInfo> memberInfoMap
                = memberAPI.getMemberBasicInfoMapForShare(memberIds);

        // 4. 미팅에 존재하는 모든 팀 조회
        List<Team> teams = clubMeetingTeamQueryService.findTeamsByMeeting(meetingId);
        List<Long> teamIds = extractTeamIds(teams);
        Map<Long, Integer> teamIdToTeamNumberMap = mapTeamIdToTeamNumber(teams);

        // 5. Map<clubMemberId, teamId> 형태로 모든 팀의 팀원 조회
        Map<Long, Long> memberIdToTeamIdMap = clubMeetingTeamQueryService.getClubMemberIdToTeamIdMap(teamIds);

        // 6. teamId -> teamNumber 맵 구성
        Map<String, Integer> memberIdToTeamNumberMap
                = mapMemberIdToTeamNumber(clubMembership, memberIdToTeamIdMap, teamIdToTeamNumberMap);
        List<MeetingResponseDTO.MeetingMember> meetingMemberList = clubMembership.stream()
                .map(membership -> toMeetingMemberDTO(membership, memberInfoMap, memberIdToTeamNumberMap))
                .toList();

        return MeetingResponseDTO.MeetingMemberList.builder()
                .members(meetingMemberList)
                .hasNext(membershipCursorResult.hasNext())
                .nextCursor(membershipCursorResult.nextCursor())
                .membership(clubMembershipInfo)
                .build();
    }

    public MeetingResponseDTO.TeamMember findTeamMembersByMeeting(
            Long meetingId,
            Integer teamNumber,
            String memberId
    ) {
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        Membership clubMembershipInfo = clubManagementAPI.getClubMembershipInfo(meeting.getClubId(), memberId);
        Team team = clubMeetingTeamQueryService.validateTeam(meetingId, teamNumber);

        // 2. 팀 멤버 조회
        List<ClubMemberTeam> clubMemberTeams = clubMeetingTeamQueryService.getMemberTeamsByTeam(team.getId());

        // 3. 클럽 멤버의 기본 정보 배치 조회
        Set<Long> clubMemberIds = extractClubMemberIdsFromMemberTeams(clubMemberTeams);
        Map<Long, Membership> clubMembership = clubManagementAPI.getClubMembershipInfos(clubMemberIds);
        List<String> memberIds = extractMemberIds(clubMembership);
        Map<String, MemberExternalDTO.BasicInfo> memberBasicInfoMap
                = memberAPI.getMemberBasicInfoMapForShare(memberIds);
        List<BasicInfo> memberInfo = memberBasicInfoMap.values().stream().toList();

        return MeetingResponseDTO.TeamMember.builder()
                .teamNumber(teamNumber)
                .members(memberInfo)
                .membership(clubMembershipInfo)
                .build();
    }

    private List<MeetingResponseDTO.MeetingInfo> toMeetingInfoDTOList(List<Meeting> meetings) {
        return meetings.stream()
                .map(meeting -> ClubMeetingConverter.toMeetingInfoDTO(meeting, null))
                .toList();
    }

    private List<MeetingResponseDTO.TeamTopic> assemble(
            List<Team> teams,
            Map<Integer, List<TeamTopic>> teamNumberToTeamTopics,
            Map<String, BasicInfo> authorInfoMap
    ) {
        return teams.stream()
                .sorted(Comparator.comparing(Team::getTeamNumber)) // 팀 번호 기준 정렬
                .map(team -> {
                    List<TeamTopic> teamTopics = teamNumberToTeamTopics.get(team.getTeamNumber()); // 팀 번호로 팀 토픽 조회
                    List<MeetingResponseDTO.Topic> teamTopicDTOs = teamTopics.stream()
                            .map(tt -> ClubMeetingConverter.toTopicDTO(
                                    tt.getTopic(),
                                    authorInfoMap.get(tt.getTopic().getMemberId()),
                                    null // TeamTopicDTO-TopicDTO에서는 teamNumbers 필드가 NULL이어야 함
                            ))
                            .toList();
                    return MeetingResponseDTO.TeamTopic.builder()
                            .teamNumber(team.getTeamNumber())
                            .topics(teamTopicDTOs)
                            .membership(null)
                            .build();
                })
                .toList();
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

    private List<MeetingResponseDTO.Topic> mapTopicsToTopicDetail(
            List<Topic> topics,
            Map<String, MemberExternalDTO.BasicInfo> authorInfoMap,
            Map<Long, List<Integer>> topicIdToSelectTeamNumbers
    ) {
        return topics.stream()
                .map(topic -> ClubMeetingConverter.toTopicDTO(
                        topic,
                        authorInfoMap.get(topic.getMemberId()),
                        topicIdToSelectTeamNumbers.getOrDefault(topic.getId(), List.of())
                ))
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
            Map<String, MemberExternalDTO.BasicInfo> authorInfoMap
    ) {
        return bookReviews.stream()
                .map(review -> ClubMeetingConverter.toBookReviewDetailDTO(
                        review,
                        authorInfoMap.get(review.getMemberId())
                ))
                .toList();
    }

    private List<MeetingResponseDTO.MeetingInfo> mapMeetingsToMeetingInfo(
            List<Meeting> meetings,
            Map<String, BookExternalDTO.BasicInfo> bookBasicInfoMap
    ) {
        return meetings.stream()
                .map(meeting -> ClubMeetingConverter.toMeetingInfoDTO(
                        meeting,
                        bookBasicInfoMap.get(meeting.getBookId())
                ))
                .toList();
    }

    private Map<String, Integer> mapMemberIdToTeamNumber(
            List<Membership> memberships,
            Map<Long, Long> clubMemberIdToTeamIdMap,
            Map<Long, Integer> teamIdToTeamNumberMap
    ) {
        Map<String, Integer> result = new HashMap<>();

        for (Membership membership : memberships) {
            Long clubMemberId = membership.getClubMemberId();
            String memberId = membership.getMemberId();

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
            Membership membership,
            Map<String, MemberExternalDTO.BasicInfo> memberBasicInfoMap,
            Map<String, Integer> memberIdToTeamNumberMap
    ) {
        String memberId = membership.getMemberId();
        MemberExternalDTO.BasicInfo memberInfo = memberBasicInfoMap.get(memberId);
        Integer teamNumber = memberIdToTeamNumberMap.get(memberId);
        return MeetingResponseDTO.MeetingMember.builder()
                .memberInfo(memberInfo)
                .teamNumber(teamNumber)
                .build();
    }

    // ========== 추출 메서드 ==========

    private List<String> extractMemberIds(Map<Long, Membership> clubMembershipMap) {
        if (clubMembershipMap == null) {
            return List.of();
        }
        return clubMembershipMap.values().stream()
                .map(Membership::getMemberId)
                .distinct()
                .toList();
    }

    private List<String> extractMemberIdsFromBookReviews(List<BookReview> bookReviews) {
        return bookReviews.stream()
                .map(checkmo.clubMeeting.internal.entity.BookReview::getMemberId)
                .distinct()
                .toList();
    }

    private List<String> extractMemberIdsFromTopics(List<Topic> topics) {
        if (topics == null) {
            return List.of();
        }
        return topics.stream()
                .map(checkmo.clubMeeting.internal.entity.Topic::getMemberId)
                .distinct()
                .toList();
    }

    private List<String> extractMemberIdsFromTeamTopics(Map<Integer, List<TeamTopic>> teamNumberToTeamTopics) {
        if (teamNumberToTeamTopics == null) {
            return List.of();
        }
        return teamNumberToTeamTopics.values().stream()
                .flatMap(List::stream)
                .map(tt -> tt.getTopic().getMemberId())
                .distinct()
                .toList();
    }

    private List<String> extractMemberIdsFromTeamTopics(List<TeamTopic> teamTopics) {
        if (teamTopics == null) {
            return List.of();
        }
        return teamTopics.stream()
                .map(tt -> tt.getTopic().getMemberId())
                .distinct()
                .toList();
    }

    private List<String> extractMemberIdsFromClubMembers(List<Membership> clubMembership) {
        if (clubMembership == null) {
            return List.of();
        }
        return clubMembership.stream()
                .map(Membership::getMemberId)
                .distinct()
                .toList();
    }

    private Set<Long> extractClubMemberIdsFromMemberTeams(List<ClubMemberTeam> clubMemberTeams) {
        if (clubMemberTeams == null) {
            return Set.of();
        }
        return clubMemberTeams.stream()
                .map(ClubMemberTeam::getClubMemberId)
                .collect(Collectors.toSet());
    }

    private List<String> extractBookIdsFromMeetings(List<Meeting> meetings) {
        if (meetings == null) {
            return List.of();
        }
        return meetings.stream()
                .map(Meeting::getBookId)
                .distinct()
                .toList();
    }

    private List<Topic> extractTopicFromTeamTopics(List<TeamTopic> teamTopics) {
        if (teamTopics == null) {
            return List.of();
        }
        return teamTopics.stream()
                .map(checkmo.clubMeeting.internal.entity.TeamTopic::getTopic)
                .distinct()
                .toList();
    }

    private List<Long> extractTopicIds(List<Topic> topics) {
        if (topics == null) {
            return List.of();
        }
        return topics.stream()
                .map(checkmo.clubMeeting.internal.entity.Topic::getId)
                .distinct()
                .toList();
    }

    private List<Long> extractTeamIds(List<Team> teams) {
        if (teams == null) {
            return List.of();
        }
        return teams.stream()
                .map(Team::getId)
                .distinct()
                .toList();
    }

}
