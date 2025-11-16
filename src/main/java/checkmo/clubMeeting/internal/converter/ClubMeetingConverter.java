package checkmo.clubMeeting.internal.converter;

import checkmo.book.BookExternalDTO;
import checkmo.book.BookExternalDTO.BasicInfo;
import checkmo.clubManagement.ClubManagementExternalDTO;
import checkmo.clubMeeting.ClubMeetingExternalDTO;
import checkmo.clubMeeting.internal.entity.*;
import checkmo.clubMeeting.web.dto.bookshelf.BookShelfRequestDTO;
import checkmo.clubMeeting.web.dto.bookshelf.BookShelfResponseDTO;
import checkmo.clubMeeting.web.dto.meeting.MeetingRequestDTO;
import checkmo.clubMeeting.web.dto.meeting.MeetingResponseDTO;
import checkmo.member.MemberExternalDTO;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClubMeetingConverter {

    // =====================================================
    // Entity ↔ DTO 변환
    // =====================================================

    /**
     * MeetingCreateRequestDTO -> Meeting 엔티티 변환
     */
    public static Meeting fromMeetingCreateRequestDTOToMeeting(
            MeetingRequestDTO.MeetingCreate request,
            Long clubId,
            String bookId
    ) {
        return Meeting.builder()
                .title(request.getTitle())
                .meetingTime(request.getMeetingTime())
                .location(request.getLocation())
                .content(request.getContent())
                .generation(request.getGeneration())
                .tag(request.getTag())
                .clubId(clubId)
                .bookId(bookId)
                .build();
    }

    /**
     * BookReviewDTO <-> BookReview 엔티티 변환
     */
    public static BookReview fromBookReviewDTOToBookReview(BookShelfRequestDTO.BookReviewCreate request,
                                                           Long clubMemberId, String memberId) {
        return checkmo.clubMeeting.internal.entity.BookReview.builder()
                .description(request.getDescription())
                .rate(request.getRate())
                .build();
    }

    /**
     * TopicDTO -> Topic 엔티티 변환
     */
    public static Topic fromTopicDTOToTopic(
            BookShelfRequestDTO.TopicCreate topicCreateDTO,
            Long clubMemberId
    ) {
        return checkmo.clubMeeting.internal.entity.Topic.builder()
                .description(topicCreateDTO.getDescription())
                .clubMemberId(clubMemberId)
                .build();
    }

    /**
     * Topic 리스트 + 작성자 정보 맵 + 회원 ID -> List<BookShelfResponseDTO.TopicDTO> 변환
     */
    public static List<BookShelfResponseDTO.TopicDetail> fromTopicListAndAuthorInfoMapAndMemberIdToTopicDTOList(
            List<Topic> topics,
            Map<String, MemberExternalDTO.BasicInfo> authorInfoMap,
            String memberId
    ) {
        return topics.stream()
                .map(topic -> fromTopicAndMemberSharedDTOToTopicDTO(
                        topic,
                        authorInfoMap.get(topic.getMemberId()),
                        memberId
                ))
                .toList();
    }

    /**
     * Meeting 엔티티 + BookExternalDTO.BasicInfoDTO -> BookShelfResponseDTO.BookShelfInfoDTO 변환
     */
    public static BookShelfResponseDTO.BookShelfInfo fromMeetingAndBookSharedDTOToBookShelfInfoDTO(
            Meeting meeting,
            BookExternalDTO.BasicInfo bookSharedDTO
    ) {
        BookShelfResponseDTO.MeetingInfo meetingInfo = fromMeetingToBookshelfMeetingInfoDTO(meeting);

        return BookShelfResponseDTO.BookShelfInfo.builder()
                .meetingInfo(meetingInfo)
                .bookInfo(bookSharedDTO)
                .build();
    }

    /**
     * Meeting 엔티티 -> BookShelfResponseDTO.MeetingInfoDTO 변환
     */
    public static BookShelfResponseDTO.MeetingInfo fromMeetingToBookshelfMeetingInfoDTO(Meeting meeting) {
        return BookShelfResponseDTO.MeetingInfo.builder()
                .meetingId(meeting.getId())
                .generation(meeting.getGeneration())
                .tag(meeting.getTag())
                .averageRate(meeting.calculateAverageRate())
                .build();
    }

    /**
     * Meeting 엔티티 + BookExternalDTO.DetailInfoDTO + BookShelfResponseDTO.TopicListDTO ->
     * BookShelfResponseDTO.BookShelfDetailDTO 변환
     */
    public static BookShelfResponseDTO.BookShelfDetail fromBookShelfDTOToBookShelfDetailDTO(
            Meeting meeting,
            BookExternalDTO.DetailInfo bookSharedDTO,
            BookShelfResponseDTO.TopicList topicList,
            ClubManagementExternalDTO.Membership membership
    ) {
        return BookShelfResponseDTO.BookShelfDetail.builder()
                .meetingInfo(fromMeetingToBookshelfMeetingInfoDTO(meeting))
                .bookDetailInfo(bookSharedDTO)
                .topicList(topicList)
                .membership(membership)
                .build();
    }

    /**
     * Meeting 엔티티 + BooksharedDTO -> MeetingInfoDTO 변환
     */
    public static MeetingResponseDTO.MeetingInfo fromMeetingAndBookSharedDTOToMeetingInfoDTO(
            Meeting meeting,
            BookExternalDTO.BasicInfo bookInfo
    ) {
        return MeetingResponseDTO.MeetingInfo.builder()
                .meetingId(meeting.getId())
                .title(meeting.getTitle())
                .meetingTime(meeting.getMeetingTime())
                .location(meeting.getLocation())
                .content(meeting.getContent())
                .generation(meeting.getGeneration())
                .tag(meeting.getTag())
                .bookInfo(bookInfo)
                .build();
    }

    /**
     * List<Meeting> -> List<MeetingResponseDTO.MeetingInfoDTO> 변환
     */
    public static MeetingResponseDTO.CalendarMeeting fromMeetingListToMCalendarMeetingDTO(
            List<Meeting> meetings,
            ClubManagementExternalDTO.Membership membership
    ) {
        return MeetingResponseDTO.CalendarMeeting.builder()
                .meetingInfoList(meetings.stream()
                        .map(meeting -> fromMeetingAndBookSharedDTOToMeetingInfoDTO(meeting, null))
                        .toList())
                .membership(membership)
                .build();
    }

    /**
     * Topic 엔티티 + MemberExternalDTO.BasicInfoDTO + 팀 번호 리스트 -> MeetingResponseDTO.TopicDTO 변환
     */
    public static MeetingResponseDTO.Topic fromTopicAndMemberSharedDTOAndTeamNumberListToTopicDTO(
            Topic topic,
            MemberExternalDTO.BasicInfo authorSharedDTO,
            List<Integer> teamNumbers
    ) {
        return MeetingResponseDTO.Topic.builder()
                .topicId(topic.getId())
                .content(topic.getDescription())
                .authorInfo(authorSharedDTO)
                .teamNumbers(teamNumbers)
                .build();
    }

    /**
     * MemberExternalDTO.BasicInfoDTO + teamNumber -> MeetingResponseDTO.MeetingMemberDTO 변환
     */
    public static MeetingResponseDTO.MeetingMember fromMemberSharedDTOAndTeamNumberToMeetingMemberDTO(
            MemberExternalDTO.BasicInfo memberSharedDTO,
            Integer teamNumber
    ) {
        return MeetingResponseDTO.MeetingMember.builder()
                .memberInfo(memberSharedDTO)
                .teamNumber(teamNumber)
                .build();
    }

    /**
     * Meeting 엔티티 + BookExternalDTO.BasicInfoDTO + Topic 리스트 + 팀별 Topic 리스트 -> MeetingResponseDTO.MeetingDetailDTO 변환
     */
    public static MeetingResponseDTO.MeetingDetail fromMeetingAndBookSharedDTOEtcToMeetingDetailDTO(
            Meeting meeting,
            BookExternalDTO.BasicInfo bookSharedDTO,
            List<Topic> topics,
            Map<Long, List<Integer>> topicIdToSelectTeamNumbers,
            List<Team> teams,
            Map<Integer, List<TeamTopic>> teamTopicsGroupingByTeamNumber,
            Map<String, MemberExternalDTO.BasicInfo> authorInfoMap,
            ClubManagementExternalDTO.Membership membership
    ) {
        MeetingResponseDTO.MeetingInfo meetingInfo = ClubMeetingConverter.fromMeetingAndBookSharedDTOToMeetingInfoDTO(
                meeting, bookSharedDTO);

        List<MeetingResponseDTO.Topic> topicList = fromTopicListAndTopicSelectionAndMemberSharedDTOToTopicDTOList(
                topics, authorInfoMap, topicIdToSelectTeamNumbers);

        List<MeetingResponseDTO.TeamTopic> teamTopicList = teams.stream()
                .sorted(Comparator.comparing(Team::getTeamNumber)) // 팀 번호 기준 정렬
                .map(team -> {
                    List<TeamTopic> teamTopics =
                            teamTopicsGroupingByTeamNumber.get(team.getTeamNumber());

                    List<MeetingResponseDTO.Topic> teamTopicDTOs = teamTopics.stream()
                            .map(tt -> ClubMeetingConverter.fromTopicAndMemberSharedDTOAndTeamNumberListToTopicDTO(
                                    tt.getTopic(),
                                    authorInfoMap.get(tt.getTopic().getMemberId()),
                                    null // TeamTopicDTO-TopicDTO에서는 teamNumbers 필드가 NULL이어야 함
                            ))
                            .toList();

                    return fromTopicDTOListToTeamTopicDTO(team.getTeamNumber(), teamTopicDTOs, null);
                })
                .toList();
        return fromMeetingInfoDTOAndTopicDTOListAndTeamTopicDTOListToTopicDTO(
                meetingInfo,
                topicList,
                teamTopicList,
                membership
        );
    }

    /**
     * Topic 리스트 + Topic별 팀 선택 정보 + 작성자 정보 맵 -> List<MeetingResponseDTO.TopicDTO> 변환
     */
    public static List<MeetingResponseDTO.Topic> fromTopicListAndTopicSelectionAndMemberSharedDTOToTopicDTOList(
            List<Topic> topics,
            Map<String, MemberExternalDTO.BasicInfo> authorInfoMap,
            Map<Long, List<Integer>> topicIdToSelectTeamNumbers
    ) {
        return topics.stream()
                .map(topic -> ClubMeetingConverter.fromTopicAndMemberSharedDTOAndTeamNumberListToTopicDTO(
                        topic,
                        authorInfoMap.get(topic.getMemberId()),
                        topicIdToSelectTeamNumbers.getOrDefault(topic.getId(), List.of())
                ))
                .toList();
    }

    // =====================================================
    // DTO -> DTO 변환
    // =====================================================

    /**
     * BookReview 엔티티 + MemberExternalDTO -> BookReviewDTO 변환
     */
    public static BookShelfResponseDTO.BookReviewDetail fromBookReviewAndMemberSharedDTOToBookReviewDTO(
            BookReview bookReview,
            MemberExternalDTO.BasicInfo memberSharedDTO
    ) {
        return BookShelfResponseDTO.BookReviewDetail.builder()
                .bookReviewId(bookReview.getId())
                .description(bookReview.getDescription())
                .rate(bookReview.getRate())
                .authorInfo(memberSharedDTO)
                .build();
    }

    /**
     * BookReviewDTO 리스트 -> BookReviewListDTO 변환
     */
    public static BookShelfResponseDTO.BookReviewList fromBookReviewDTOListToBookReviewListDTO(
            List<BookShelfResponseDTO.BookReviewDetail> bookReviewDetailList,
            boolean hasNext,
            Long nextCursor,
            ClubManagementExternalDTO.Membership membership
    ) {
        return BookShelfResponseDTO.BookReviewList.builder()
                .bookReviewDetailList(bookReviewDetailList)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .membership(membership)
                .build();
    }

    /**
     * List<TopicDTO> -> BookShelfResponseDTO.TopicListDTO 변환
     */
    public static BookShelfResponseDTO.TopicList fromTopicDTOListToTopicListDTOForBookshelf(
            List<BookShelfResponseDTO.TopicDetail> topicDetailList,
            boolean hasNext,
            Long nextCursor,
            ClubManagementExternalDTO.Membership membership
    ) {
        return BookShelfResponseDTO.TopicList.builder()
                .topicDetailList(topicDetailList)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .membership(membership)
                .build();
    }

    /**
     * List<BookShelfInfoDTO> -> BookShelfListDTO 변환
     */
    public static BookShelfResponseDTO.BookShelfList fromBookShelfInfoDTOListToBookShelfListDTO(
            List<BookShelfResponseDTO.BookShelfInfo> bookShelfInfos,
            boolean hasNext,
            Long nextCursor,
            ClubManagementExternalDTO.Membership membership
    ) {
        return BookShelfResponseDTO.BookShelfList.builder()
                .bookShelfInfoList(bookShelfInfos)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .membership(membership)
                .build();
    }

    /**
     * List<MeetingResponseDTO.MeetingInfoDTO> -> MeetingListDTO 변환
     */
    public static MeetingResponseDTO.MeetingList fromMeetingInfoDTOListToMeetingListDTO(
            List<MeetingResponseDTO.MeetingInfo> meetingInfoList,
            boolean hasNext,
            Long nextCursor,
            ClubManagementExternalDTO.Membership membership
    ) {
        return MeetingResponseDTO.MeetingList.builder()
                .meetingInfoList(meetingInfoList)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .membership(membership)
                .build();
    }

    /**
     * List<MeetingResponseDTO.TopicDTO> -> MeetingResponseDTO.TopicListDTO 변환
     */
    public static MeetingResponseDTO.TopicList fromTopicDTOListToTopicListDTOForMeeting(
            List<MeetingResponseDTO.Topic> topicList,
            boolean hasNext,
            Long nextCursor
    ) {
        return MeetingResponseDTO.TopicList.builder()
                .topics(topicList)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .build();
    }

    /**
     * List<MeetingResponseDTO.TopicDTO> -> MeetingResponseDTO.TeamTopicDTO 변환
     */
    public static MeetingResponseDTO.TeamTopic fromTopicDTOListToTeamTopicDTO(
            Integer teamNumber,
            List<MeetingResponseDTO.Topic> topicList,
            ClubManagementExternalDTO.Membership membership
    ) {
        return MeetingResponseDTO.TeamTopic.builder()
                .teamNumber(teamNumber)
                .topics(topicList)
                .membership(membership)
                .build();
    }

    /**
     * MeetingResponseDTO.MeetingInfoDTO + List<TopicDTO> + List<TeamTopicDTO> -> MeetingResponseDTO.MeetingDetailDTO
     * 변환
     */
    public static MeetingResponseDTO.MeetingDetail fromMeetingInfoDTOAndTopicDTOListAndTeamTopicDTOListToTopicDTO(
            MeetingResponseDTO.MeetingInfo meetingInfo,
            List<MeetingResponseDTO.Topic> topicList,
            List<MeetingResponseDTO.TeamTopic> teamTopicList,
            ClubManagementExternalDTO.Membership membership
    ) {
        return MeetingResponseDTO.MeetingDetail.builder()
                .meetingInfo(meetingInfo)
                .topics(topicList)
                .teams(teamTopicList)
                .membership(membership)
                .build();
    }

    public static MeetingResponseDTO.TopicDTO fromTopicDTOListAndMembershipDTOToTopicListDTO(
            List<MeetingResponseDTO.Topic> topics,
            ClubManagementExternalDTO.Membership membership
    ) {
        return MeetingResponseDTO.TopicDTO.builder()
                .topics(topics)
                .membership(membership)
                .build();
    }

    /**
     * 팀 번호 + List<MemberExternalDTO> -> MeetingResponseDTO.TeamMemberDTO 변환
     */
    public static MeetingResponseDTO.TeamMember fromTeamNumberAndMemberSharedDTOToTeamMemberDTO(
            Integer teamNumber,
            List<MemberExternalDTO.BasicInfo> memberSharedDTOs,
            ClubManagementExternalDTO.Membership membership
    ) {
        return MeetingResponseDTO.TeamMember.builder()
                .teamNumber(teamNumber)
                .members(memberSharedDTOs)
                .membership(membership)
                .build();
    }

    /**
     * List<MeetingResponseDTO.MeetingMemberDTO> -> MeetingResponseDTO.MeetingMemberListDTO 변환
     */
    public static MeetingResponseDTO.MeetingMemberList fromMeetingMemberDTOListToMeetingMemberListDTO(
            List<MeetingResponseDTO.MeetingMember> meetingMemberList,
            boolean hasNext, Long nextCursor,
            ClubManagementExternalDTO.Membership membership
    ) {
        return MeetingResponseDTO.MeetingMemberList.builder()
                .members(meetingMemberList)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .membership(membership)
                .build();
    }

    /**
     * Meeting 리스트 + BookBasicInfo 맵 -> MeetingInfo 리스트 변환
     */
    public static Map<Long, ClubMeetingExternalDTO.MeetingInfo> fromMeetingListToMeetingInfoList(
            List<Meeting> meetings,
            Map<String, BasicInfo> bookBasicInfoMapForShare
    ) {
        return meetings.stream()
                .collect(Collectors.toMap(
                        Meeting::getId,
                        meeting -> fromMeetingToMeetingInfo(
                                meeting,
                                bookBasicInfoMapForShare.get(meeting.getBookId())
                        )
                ));
    }

    /**
     * /** Meeting 엔티티 + BookBasicInfo -> MeetingInfo 변환
     */
    public static ClubMeetingExternalDTO.MeetingInfo fromMeetingToMeetingInfo(Meeting meeting, BasicInfo bookBasicInfoForShare) {
        return ClubMeetingExternalDTO.MeetingInfo.builder()
                .meetingId(meeting.getId())
                .title(meeting.getTitle())
                .meetingTime(meeting.getMeetingTime())
                .location(meeting.getLocation())
                .content(meeting.getContent())
                .generation(meeting.getGeneration())
                .tag(meeting.getTag())
                .bookInfo(bookBasicInfoForShare)
                .build();
    }

    // =====================================================
    // Parameter ->  DTO 변환

    // =====================================================

    /**
     * 파라미터 -> MeetingResponseDTO.TopicSelectionDTO 변환
     */
    public static MeetingResponseDTO.TopicSelection fromParametersToTopicSelectionDTO(Long topicId,
                                                                                      Integer teamNumber,
                                                                                      Boolean isSelected) {
        return MeetingResponseDTO.TopicSelection.builder()
                .topicId(topicId)
                .teamNumber(teamNumber)
                .isSelected(isSelected)
                .build();
    }

    // =====================================================
    // Private Methods
    // =====================================================

    /**
     * Topic 엔티티 -> BookShelfResponseDTO.TopicDTO 변환
     */
    private static BookShelfResponseDTO.TopicDetail fromTopicAndMemberSharedDTOToTopicDTO(
            Topic topic,
            MemberExternalDTO.BasicInfo authorSharedDTO,
            String memberId
    ) {
        return BookShelfResponseDTO.TopicDetail.builder()
                .topicId(topic.getId())
                .content(topic.getDescription())
                .authorInfo(authorSharedDTO)
                .isAuthor(topic.getMemberId().equals(memberId))
                .build();
    }
}