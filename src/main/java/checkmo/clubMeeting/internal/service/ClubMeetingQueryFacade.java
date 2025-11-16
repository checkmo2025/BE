package checkmo.clubMeeting.internal.service;

import checkmo.book.BookAPI;
import checkmo.book.BookExternalDTO;
import checkmo.clubManagement.ClubManagementAPI;
import checkmo.clubManagement.ClubManagementExternalDTO.Membership;
import checkmo.clubMeeting.internal.converter.ClubMeetingConverter;
import checkmo.clubMeeting.internal.entity.*;
import checkmo.clubMeeting.internal.service.query.ClubBookReviewQueryService;
import checkmo.clubMeeting.internal.service.query.ClubMeetingQueryService;
import checkmo.clubMeeting.internal.service.query.ClubMeetingTeamQueryService;
import checkmo.clubMeeting.internal.service.query.ClubTopicQueryService;
import checkmo.clubMeeting.web.dto.bookshelf.BookShelfResponseDTO;
import checkmo.clubMeeting.web.dto.meeting.MeetingResponseDTO;
import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
import checkmo.member.MemberAPI;
import checkmo.member.MemberExternalDTO;
import checkmo.member.MemberExternalDTO.BasicInfo;
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
    private static final int TOPIC_PREVIEW_SIZE_FOR_BOOKSHELF = 3;
    private static final int TOPIC_PREVIEW_SIZE_FOR_MEETING = 4;

    // Domain level 2
    private final MemberAPI memberAPI;

    // Domain level 1
    private final BookAPI bookAPI;

    private final ClubManagementAPI clubManagementAPI;

    // 자신의 QueryService
    private final ClubMeetingQueryService clubMeetingQueryService;
    private final ClubTopicQueryService clubTopicQueryService;
    private final ClubBookReviewQueryService clubBookReviewQueryService;
    private final ClubMeetingTeamQueryService clubMeetingTeamQueryService;

    public BookShelfResponseDTO.BookShelfList getBookShelfList(Long clubId, Long cursorId, Integer size,
                                                               Integer generation, String memberId) {
        // 1. 유효성 검증(club, clubMember)
        clubManagementAPI.getClubInfo(clubId);
        Membership clubMembershipInfo = clubManagementAPI.getClubMembershipInfo(clubId, memberId);

        // 2. [책장] 미팅 리스트 조회
        List<Meeting> meetings = clubMeetingQueryService.getBookShelfList(clubId, generation, cursorId, size + 1);

        // 3. 페이징 처리
        boolean hasNext = meetings.size() > size;
        if (hasNext) {
            meetings = meetings.subList(0, size);
        }
        Long nextCursor = hasNext ? meetings.getLast().getId() : null;

        // 4. DTO 변환
        List<BookShelfResponseDTO.BookShelfInfo> bookShelfInfos = meetings.stream()
                .map(meeting ->
                        ClubMeetingConverter.fromMeetingAndBookSharedDTOToBookShelfInfoDTO(
                                meeting,
                                bookAPI.getBookBasicInfoForShare(meeting.getBookId()) // TODO: 미팅에 사용된 책 배치 조회
                        )
                )
                .toList();
        return ClubMeetingConverter.fromBookShelfInfoDTOListToBookShelfListDTO(bookShelfInfos, hasNext, nextCursor,
                clubMembershipInfo);
    }

    // TODO: getBookShelftDetail, findTopicsByMeeting 간 중복 제거
    public BookShelfResponseDTO.BookShelfDetail getBookShelfDetail(Long meetingId, String memberId) {
        // 1. 유효성 검증(meeting, clubMember)
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        Membership clubMembershipInfo = clubManagementAPI.getClubMembershipInfo(meeting.getClubId(), memberId);

        // 2. [발제 미리보기] 발제 리스트 조회
        List<Topic> topics = clubTopicQueryService.findTopicsByMeeting(meetingId, null,
                TOPIC_PREVIEW_SIZE_FOR_BOOKSHELF + 1);

        // 3. 페이징 처리
        boolean hasNext = topics.size() > TOPIC_PREVIEW_SIZE_FOR_BOOKSHELF;
        if (hasNext) {
            topics = topics.subList(0, TOPIC_PREVIEW_SIZE_FOR_BOOKSHELF);
        }
        Long nextCursor = hasNext ? topics.getLast().getId() : null;

        // 4. 발제의 작성자 정보 배치 조회
        List<String> authorIds = extractMemberIdsFromTopics(topics);
        Map<String, BasicInfo> authorInfoMap =
                memberAPI.getMemberBasicInfoMapForShare(authorIds);

        // 5. DTO 변환
        List<BookShelfResponseDTO.TopicDetail> topicDetailList = ClubMeetingConverter.fromTopicListAndAuthorInfoMapAndMemberIdToTopicDTOList(
                topics,
                authorInfoMap,
                memberId // 요청한 회원 ID -> 작성자 본인 확인용
        );
        return ClubMeetingConverter.fromBookShelfDTOToBookShelfDetailDTO(
                meeting,
                bookAPI.getBookDetailInfoForShare(meeting.getBookId()), // TODO: 책 상세정보 배치 조회로 변경
                ClubMeetingConverter.fromTopicDTOListToTopicListDTOForBookshelf(topicDetailList, hasNext, nextCursor,
                        null),
                clubMembershipInfo
        );
    }

    public BookShelfResponseDTO.TopicList findTopicsByMeeting(Long meetingId, Long cursorId, Integer size,
                                                              String memberId) {
        // 1. 유효성 검증(meeting, clubMember)
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        Membership clubMembershipInfo = clubManagementAPI.getClubMembershipInfo(meeting.getClubId(), memberId);

        // 2. 발제 리스트 조회
        List<Topic> topics = clubTopicQueryService.findTopicsByMeeting(meetingId, cursorId, size + 1);

        // 3. 페이징 처리
        boolean hasNext = topics.size() > size;
        if (hasNext) {
            topics = topics.subList(0, size);
        }
        Long nextCursor = hasNext ? topics.getLast().getId() : null;

        // 4. 발제의 작성자 정보 배치 조회
        List<String> authorIds = extractMemberIdsFromTopics(topics);
        Map<String, MemberExternalDTO.BasicInfo> authorInfoMap =
                memberAPI.getMemberBasicInfoMapForShare(authorIds);

        // 5. DTO 변환
        List<BookShelfResponseDTO.TopicDetail> topicDetailListDTOS = ClubMeetingConverter.fromTopicListAndAuthorInfoMapAndMemberIdToTopicDTOList(
                topics,
                authorInfoMap,
                memberId // 요청한 회원 ID -> 작성자 본인 확인용
        );
        return ClubMeetingConverter.fromTopicDTOListToTopicListDTOForBookshelf(topicDetailListDTOS, hasNext, nextCursor,
                clubMembershipInfo);
    }

    public BookShelfResponseDTO.BookReviewList getBookReviewList(Long meetingId, Long lastReviewId, int size,
                                                                 String memberId) {
        // 1. 유효성 검증(meeting, clubMember)
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        Membership clubMembershipInfo = clubManagementAPI.getClubMembershipInfo(meeting.getClubId(), memberId);

        // 2. 한줄평 리스트 조회
        List<BookReview> bookReviews =
                clubBookReviewQueryService.findBookReviewsByMeeting(meetingId, lastReviewId, size + 1);

        // 3. 페이징 처리
        boolean hasNext = bookReviews.size() > size;
        if (hasNext) {
            bookReviews = bookReviews.subList(0, size);
        }
        Long nextCursor = hasNext ? bookReviews.get(bookReviews.size() - 1).getId() : null;

        // 4. 한줄평 작성자 정보 배치 조회
        List<String> authorIds = extractMemberIdsFromBookReviews(bookReviews);
        Map<String, MemberExternalDTO.BasicInfo> authorInfoMap = memberAPI.getMemberBasicInfoMapForShare(authorIds);

        // 5. DTO 변환
        List<BookShelfResponseDTO.BookReviewDetail> bookReviewDetailList = mapBookReviewsAndAuthorInfoToDTOs(bookReviews,
                authorInfoMap);
        return ClubMeetingConverter.fromBookReviewDTOListToBookReviewListDTO(bookReviewDetailList, hasNext, nextCursor,
                clubMembershipInfo);
    }

    private List<BookShelfResponseDTO.BookReviewDetail> mapBookReviewsAndAuthorInfoToDTOs(
            List<BookReview> bookReviews,
            Map<String, MemberExternalDTO.BasicInfo> authorInfoMap
    ) {
        return bookReviews.stream()
                .map(review -> ClubMeetingConverter.fromBookReviewAndMemberSharedDTOToBookReviewDTO(
                        review,
                        authorInfoMap.get(review.getMemberId())
                ))
                .toList();
    }

    public MeetingResponseDTO.MeetingList getMeetingsByClub(Long clubId, Long cursorId, Integer size,
                                                            String memberId) {
        // 1. 유효성 검증(club, clubMember)
        clubManagementAPI.getClubInfo(clubId);
        Membership clubMembershipInfo = clubManagementAPI.getClubMembershipInfo(clubId, memberId);

        // 2. 미팅 리스트 조회
        List<Meeting> meetings = clubMeetingQueryService.findMeetingsByClubAndCursor(clubId, cursorId, size + 1);

        // 3. 페이징 처리
        boolean hasNext = meetings.size() > size;
        if (hasNext) {
            meetings = meetings.subList(0, size);
        }
        Long nextCursor = hasNext ? meetings.getLast().getId() : null;

        // 4. 미팅의 모든 도서 기본 정보 배치 조회
        List<String> bookIds = extractBookIdsFromMeetings(meetings);
        Map<String, BookExternalDTO.BasicInfo> bookBasicInfoMap = bookAPI.getBookBasicInfoMapForShare(bookIds);

        // 5. DTO 변환
        List<MeetingResponseDTO.MeetingInfo> meetingInfoList = mapMeetingsWithBookBasicInfoToDTOs(meetings,
                bookBasicInfoMap);
        return ClubMeetingConverter.fromMeetingInfoDTOListToMeetingListDTO(meetingInfoList, hasNext, nextCursor,
                clubMembershipInfo);
    }

    private List<MeetingResponseDTO.MeetingInfo> mapMeetingsWithBookBasicInfoToDTOs(
            List<Meeting> meetings,
            Map<String, BookExternalDTO.BasicInfo> bookBasicInfoMap
    ) {
        return meetings.stream()
                .map(meeting -> ClubMeetingConverter.fromMeetingAndBookSharedDTOToMeetingInfoDTO(
                        meeting,
                        bookBasicInfoMap.get(meeting.getBookId())
                ))
                .toList();
    }

    public MeetingResponseDTO.MeetingDetail findMeetingDetailById(Long meetingId, String memberId) {
        // 1. 유효성 검증(meeting, clubMember)
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        Membership clubMembershipInfo = clubManagementAPI.getClubMembershipInfo(meeting.getClubId(), memberId);

        // 2. [발제 전체보기 - 미리보기] 발제 최신순 상위 4개 토픽 리스트 조회
        List<Topic> topics = clubTopicQueryService.findTopicsByMeeting(meetingId, null,
                TOPIC_PREVIEW_SIZE_FOR_MEETING);

        // 3. [발제 전체보기 - 미리보기] TeamTopic(+Team) 배치 조회
        List<Long> topicIds = extractTopicIds(topics);
        Map<Long, List<Integer>> teamTopicsWithTeamByTopicIds
                = clubMeetingTeamQueryService.findTeamTopicsWithTeamByTopicIds(topicIds);

        // 4. [토론 x조 - 미리보기] 해당하는 미팅의 존재하는 모든 팀 조회
        List<Team> teams = clubMeetingTeamQueryService.findTeamsByMeeting(meetingId);

        // 5. [토론 x조 - 미리보기] 모든 팀의 발제 등록순 상위 4개 토픽 조회
        Map<Integer, List<TeamTopic>> teamNumberToTeamTopics = teams.stream()
                .collect(Collectors.toMap(
                        Team::getTeamNumber, // key: 팀 번호
                        team -> clubMeetingTeamQueryService.findTeamTopicsWithTopicByTeamId(team.getId(),
                                TOPIC_PREVIEW_SIZE_FOR_MEETING) //value : 해당 팀의 발제 최신순 상위 4개 팀 토픽 리스트
                ));

        // 6. 조회한 모든 발제(topics와 teamTopics)의 작성자 id를 중복 없이 리스트 조회
        List<String> authorIds1 = extractMemberIdsFromTopics(topics);
        List<String> authorIds2 = extractMemberIdsFromTeamTopics(teamNumberToTeamTopics);
        List<String> authorIds = Stream.concat(authorIds1.stream(), authorIds2.stream())
                .distinct()
                .toList();

        // 7. 발제의 작성자 정보 배치 조회
        Map<String, MemberExternalDTO.BasicInfo> authorInfoMap = memberAPI.getMemberBasicInfoMapForShare(authorIds);

        // 8. DTO 변환
        return ClubMeetingConverter.fromMeetingAndBookSharedDTOEtcToMeetingDetailDTO(
                meeting, bookAPI.getBookBasicInfoForShare(meeting.getBookId()), // -> MeetingInfoDTO
                topics, teamTopicsWithTeamByTopicIds, // -> List<TopicDTO>
                teams, teamNumberToTeamTopics, // -> List<TeamTopicDTO>
                authorInfoMap, // -> List<TopicDTO>, List<TeamTopicDTO> 작성자 정보
                clubMembershipInfo
        );
    }

    public MeetingResponseDTO.TopicDTO findMeetingTopicsWithTeam(Long meetingId, String memberId) {
        // 1. 유효성 검증(meeting, clubMember)
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        Membership clubMembershipInfo = clubManagementAPI.getClubMembershipInfo(meeting.getClubId(), memberId);

        // 2. 토픽 리스트 조회
        List<Topic> topics = clubTopicQueryService.findTopicsByMeeting(meetingId, null, null);

        // 3. 토픽 작성자 정보 배치 조회
        List<String> authorIds = extractMemberIdsFromTopics(topics);
        Map<String, MemberExternalDTO.BasicInfo> authorInfoMap = memberAPI.getMemberBasicInfoMapForShare(authorIds);

        // 4. TeamTopic과 Team 배치 조회
        List<Long> topicIds = extractTopicIds(topics);
        Map<Long, List<Integer>> topicIdToSelectTeamNumbers = clubMeetingTeamQueryService.findTeamTopicsWithTeamByTopicIds(
                topicIds);

        // 5. DTO 변환
        List<MeetingResponseDTO.Topic> topicList = ClubMeetingConverter.fromTopicListAndTopicSelectionAndMemberSharedDTOToTopicDTOList(
                topics,
                authorInfoMap,
                topicIdToSelectTeamNumbers
        );
        return ClubMeetingConverter.fromTopicDTOListAndMembershipDTOToTopicListDTO(
                topicList,
                clubMembershipInfo
        );
    }

    public MeetingResponseDTO.TeamTopic findMeetingTopicsByTeam(Long meetingId, Integer teamNumber,
                                                                String memberId) {
        // 1. 미팅과 클럽 멤버, 팀 검증
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        Membership clubMembershipInfo = clubManagementAPI.getClubMembershipInfo(meeting.getClubId(), memberId);
        Team team = clubMeetingTeamQueryService.validateTeam(meetingId, teamNumber);

        // 2. 팀 토픽 > 토픽 > 클럽 멤버 정보 전체 조회
        List<TeamTopic> teamTopics = clubMeetingTeamQueryService.findTeamTopicsWithTopicByTeamId(
                team.getId(),
                null);

        // 3. 토픽 작성자 정보 배치 조회
        List<String> authorIds = extractMemberIdsFromTeamTopics(teamTopics);
        Map<String, MemberExternalDTO.BasicInfo> authorInfoMap = memberAPI.getMemberBasicInfoMapForShare(authorIds);

        // 4. TeamTopicDTO로 변환
        List<MeetingResponseDTO.Topic> topicList = ClubMeetingConverter.fromTopicListAndTopicSelectionAndMemberSharedDTOToTopicDTOList(
                extractTopicFromTeamTopics(teamTopics),
                authorInfoMap,
                Map.of() // 팀 토픽은 팀 번호가 필요없으니까 빈 Map 전달
        );

        return ClubMeetingConverter.fromTopicDTOListToTeamTopicDTO(teamNumber, topicList, clubMembershipInfo);
    }

    public MeetingResponseDTO.CalendarMeeting getClubMeetingCalendar(Long clubId, int year, int month,
                                                                     String memberId) {
        // 1. 유효성 검증(club, clubMember)
        clubManagementAPI.getClubInfo(clubId);
        Membership clubMembershipInfo = clubManagementAPI.getClubMembershipInfo(clubId, memberId);

        // 2. 해당 연월의 미팅 리스트 조회
        List<Meeting> meetings = clubMeetingQueryService.getClubMeetingByYearAndMonth(clubId, year, month, memberId);

        // 3. DTO 변환
        return ClubMeetingConverter.fromMeetingListToMCalendarMeetingDTO(meetings, clubMembershipInfo);
    }

    public MeetingResponseDTO.MeetingMemberList findMeetingMembersByMeeting(Long meetingId, Long cursorId,
                                                                            Integer size, String memberId) {
        // 1. 미팅, 클럽 멤버 검증
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        Membership clubMembershipInfo = clubManagementAPI.getClubMembershipInfo(meeting.getClubId(), memberId);
        if (!clubMembershipInfo.isStaff()) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }

        // 2. 클럽의 회원 조회 및 페이징 처리 (이때 PENDING이나 BLOCKED 상태는 제외하고 STAFF나 MEMBER만 조회)
        List<Membership> clubMembership = clubManagementAPI.getClubMembersByStatus(meeting.getClubId(), cursorId,
                size + 1);
        boolean hasNext = clubMembership.size() > size;
        if (hasNext) {
            clubMembership = clubMembership.subList(0, size);
        }
        Long nextCursor = hasNext ? clubMembership.getLast().getClubMemberId() : null;

        // 3. 클럽 멤버에 대한 정보 배치 조회 (ClubMember의 memberId로 MemberExternalDTO.BasicInfoDTO 조회)
        List<String> memberIds = extractMemberIdsFromClubMembers(clubMembership);
        Map<String, MemberExternalDTO.BasicInfo> memberBasicInfoMap
                = memberAPI.getMemberBasicInfoMapForShare(memberIds);

        // 4. 미팅에 존재하는 모든 팀 조회
        List<Team> teams = clubMeetingTeamQueryService.findTeamsByMeeting(meetingId);
        List<Long> teamIds = extractTeamIds(teams);
        Map<Long, Integer> teamIdToTeamNumberMap = mapTeamIdToTeamNumberMap(teams);

        // 5. Map<clubMemberId, teamId> 형태로 모든 팀의 팀원 조회
        Map<Long, Long> memberIdToTeamIdMap = clubMeetingTeamQueryService.getClubMemberIdToTeamIdMap(teamIds);

        // 6. teamId -> teamNumber 맵 구성
        Map<String, Integer> memberIdToTeamNumberMap
                = mapMemberIdToTeamNumberMap(clubMembership, memberIdToTeamIdMap, teamIdToTeamNumberMap);
        List<MeetingResponseDTO.MeetingMember> meetingMemberList = clubMembership.stream()
                .map(membership -> toMeetingMemberDTO(membership, memberBasicInfoMap, memberIdToTeamNumberMap))
                .toList();

        // 6. 리스트 DTO로 래핑
        return ClubMeetingConverter.fromMeetingMemberDTOListToMeetingMemberListDTO(
                meetingMemberList,
                hasNext,
                nextCursor,
                clubMembershipInfo
        );
    }

    private MeetingResponseDTO.MeetingMember toMeetingMemberDTO(
            Membership membership,
            Map<String, MemberExternalDTO.BasicInfo> memberBasicInfoMap,
            Map<String, Integer> memberIdToTeamNumberMap
    ) {
        String memberId = membership.getMemberId();
        MemberExternalDTO.BasicInfo memberInfo = memberBasicInfoMap.get(memberId);
        Integer teamNumber = memberIdToTeamNumberMap.get(memberId);
        return ClubMeetingConverter.fromMemberSharedDTOAndTeamNumberToMeetingMemberDTO(memberInfo, teamNumber);
    }

    private Map<String, Integer> mapMemberIdToTeamNumberMap(
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

    private Map<Long, Integer> mapTeamIdToTeamNumberMap(List<Team> teams) {
        if (teams == null) {
            return Map.of();
        }
        return teams.stream()
                .collect(Collectors.toMap(
                        Team::getId, // key: 팀 ID
                        Team::getTeamNumber // value: 팀 번호
                ));
    }

    public MeetingResponseDTO.TeamMember findTeamMembersByMeeting(Long meetingId, Integer teamNumber,
                                                                  String memberId) {
        // 1. 검증
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        Membership clubMembershipInfo = clubManagementAPI.getClubMembershipInfo(meeting.getClubId(), memberId);
        Team team = clubMeetingTeamQueryService.validateTeam(meetingId, teamNumber);

        // 2. 팀 멤버 조회
        List<ClubMemberTeam> clubMemberTeams = clubMeetingTeamQueryService.getMemberTeamsByTeam(team.getId());

        // 3. 클럽 멤버의 기본 정보 배치 조회
        Set<Long> clubMemberIds = extractClubMemberIdsFromMemberTeams(clubMemberTeams);
        Map<Long, Membership> clubMembership = clubManagementAPI.getClubMembershipInfos(clubMemberIds);
        List<String> memberIds = extractMemberIdsFromClubMembers(clubMembership);
        Map<String, MemberExternalDTO.BasicInfo> memberBasicInfoMap = memberAPI.getMemberBasicInfoMapForShare(
                memberIds);

        // 4. TeamMemberDTO 변환
        return ClubMeetingConverter.fromTeamNumberAndMemberSharedDTOToTeamMemberDTO(teamNumber,
                memberBasicInfoMap.values().stream().toList(), clubMembershipInfo);
    }

    private List<String> extractMemberIdsFromClubMembers(Map<Long, Membership> clubMembershipMap) {
        if (clubMembershipMap == null) {
            return List.of();
        }
        return clubMembershipMap.values().stream()
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

    private List<String> extractBookIdsFromMeetings(List<Meeting> meetings) {
        if (meetings == null) {
            return List.of();
        }
        return meetings.stream()
                .map(Meeting::getBookId)
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

    private List<Long> extractTopicIds(List<Topic> topics) {
        if (topics == null) {
            return List.of();
        }
        return topics.stream()
                .map(checkmo.clubMeeting.internal.entity.Topic::getId)
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

    private List<Topic> extractTopicFromTeamTopics(List<TeamTopic> teamTopics) {
        if (teamTopics == null) {
            return List.of();
        }
        return teamTopics.stream()
                .map(checkmo.clubMeeting.internal.entity.TeamTopic::getTopic)
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

    private List<String> extractMemberIdsFromClubMembers(
            List<Membership> clubMembership) {
        if (clubMembership == null) {
            return List.of();
        }
        return clubMembership.stream()
                .map(Membership::getMemberId)
                .distinct()
                .toList();
    }

}
