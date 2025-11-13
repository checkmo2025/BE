package checkmo.clubMeeting.internal.converter;

import checkmo.book.BookExternalDTO;
import checkmo.clubManagement.web.dto.MembershipResponseDTO;
import checkmo.clubMeeting.internal.entity.BookReview;
import checkmo.clubMeeting.internal.entity.Meeting;
import checkmo.clubMeeting.internal.entity.Team;
import checkmo.clubMeeting.internal.entity.TeamTopic;
import checkmo.clubMeeting.internal.entity.Topic;
import checkmo.clubMeeting.web.dto.bookshelf.BookShelfRequestDTO;
import checkmo.clubMeeting.web.dto.bookshelf.BookShelfResponseDTO;
import checkmo.clubMeeting.web.dto.meeting.MeetingRequestDTO;
import checkmo.clubMeeting.web.dto.meeting.MeetingResponseDTO;
import checkmo.member.MemberExternalDTO;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
            MeetingRequestDTO.MeetingCreateRequestDTO request,
            String bookId
    ) {
        return Meeting.builder()
                .title(request.getTitle())
                .meetingTime(request.getMeetingTime())
                .location(request.getLocation())
                .content(request.getContent())
                .generation(request.getGeneration())
                .tag(request.getTag())
                .bookId(bookId)
                .build();
    }

    /**
     * BookReviewDTO <-> BookReview 엔티티 변환
     */
    public static BookReview fromBookReviewDTOToBookReview(BookShelfRequestDTO.BookReviewDTO request) {
        return BookReview.builder()
                .description(request.getDescription())
                .rate(request.getRate())
                .build();
    }

    /**
     * TopicDTO -> Topic 엔티티 변환
     */
    public static Topic fromTopicDTOToTopic(
            BookShelfRequestDTO.TopicDTO topicDTO
    ) {
        return Topic.builder()
                .description(topicDTO.getDescription())
                .build();
    }

    /**
     * Topic 리스트 + 작성자 정보 맵 + 회원 ID -> List<BookShelfResponseDTO.TopicDTO> 변환
     */
    public static List<BookShelfResponseDTO.TopicDTO> fromTopicListAndAuthorInfoMapAndMemberIdToTopicDTOList(
            List<Topic> topics,
            Map<String, MemberExternalDTO.BasicInfo> authorInfoMap,
            String memberId
    ) {
        return topics.stream()
                .map(topic -> fromTopicAndMemberSharedDTOToTopicDTO(
                        topic,
                        authorInfoMap.get(topic.getClubMember().getMemberId()),
                        memberId
                ))
                .toList();
    }

    /**
     * Meeting 엔티티 + BookExternalDTO.BasicInfoDTO -> BookShelfResponseDTO.BookShelfInfoDTO 변환
     */
    public static BookShelfResponseDTO.BookShelfInfoDTO fromMeetingAndBookSharedDTOToBookShelfInfoDTO(
            Meeting meeting,
            BookExternalDTO.BasicInfo bookSharedDTO
    ) {
        BookShelfResponseDTO.MeetingInfoDTO meetingInfoDTO = fromMeetingToBookshelfMeetingInfoDTO(meeting);

        return BookShelfResponseDTO.BookShelfInfoDTO.builder()
                .meetingInfo(meetingInfoDTO)
                .bookInfo(bookSharedDTO)
                .build();
    }

    /**
     * Meeting 엔티티 -> BookShelfResponseDTO.MeetingInfoDTO 변환
     */
    public static BookShelfResponseDTO.MeetingInfoDTO fromMeetingToBookshelfMeetingInfoDTO(Meeting meeting) {
        return BookShelfResponseDTO.MeetingInfoDTO.builder()
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
    public static BookShelfResponseDTO.BookShelfDetailDTO fromBookShelfDTOToBookShelfDetailDTO(
            Meeting meeting,
            BookExternalDTO.DetailInfo bookSharedDTO,
            BookShelfResponseDTO.TopicListDTO topicListDTO,
            MembershipResponseDTO.MembershipDTO membershipDTO
    ) {
        return BookShelfResponseDTO.BookShelfDetailDTO.builder()
                .meetingInfo(fromMeetingToBookshelfMeetingInfoDTO(meeting))
                .bookDetailInfo(bookSharedDTO)
                .topicList(topicListDTO)
                .membership(membershipDTO)
                .build();
    }

    /**
     * Meeting 엔티티 + BooksharedDTO -> MeetingInfoDTO 변환
     */
    public static MeetingResponseDTO.MeetingInfoDTO fromMeetingAndBookSharedDTOToMeetingInfoDTO(
            Meeting meeting,
            BookExternalDTO.BasicInfo bookInfo
    ) {
        return MeetingResponseDTO.MeetingInfoDTO.builder()
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
    public static MeetingResponseDTO.CalendarMeetingDTO fromMeetingListToMCalendarMeetingDTO(
            List<Meeting> meetings,
            MembershipResponseDTO.MembershipDTO membershipDTO
    ) {
        return MeetingResponseDTO.CalendarMeetingDTO.builder()
                .meetingInfoList(meetings.stream()
                        .map(meeting -> fromMeetingAndBookSharedDTOToMeetingInfoDTO(meeting, null))
                        .toList())
                .membership(membershipDTO)
                .build();
    }

    /**
     * Topic 엔티티 + MemberExternalDTO.BasicInfoDTO + 팀 번호 리스트 -> MeetingResponseDTO.TopicDTO 변환
     */
    public static MeetingResponseDTO.TopicDTO fromTopicAndMemberSharedDTOAndTeamNumberListToTopicDTO(
            Topic topic,
            MemberExternalDTO.BasicInfo authorSharedDTO,
            List<Integer> teamNumbers
    ) {
        return MeetingResponseDTO.TopicDTO.builder()
                .topicId(topic.getId())
                .content(topic.getDescription())
                .authorInfo(authorSharedDTO)
                .teamNumbers(teamNumbers)
                .build();
    }

    /**
     * MemberExternalDTO.BasicInfoDTO + teamNumber -> MeetingResponseDTO.MeetingMemberDTO 변환
     */
    public static MeetingResponseDTO.MeetingMemberDTO fromMemberSharedDTOAndTeamNumberToMeetingMemberDTO(
            MemberExternalDTO.BasicInfo memberSharedDTO,
            Integer teamNumber
    ) {
        return MeetingResponseDTO.MeetingMemberDTO.builder()
                .memberInfo(memberSharedDTO)
                .teamNumber(teamNumber)
                .build();
    }

    /**
     * Meeting 엔티티 + BookExternalDTO.BasicInfoDTO + Topic 리스트 + 팀별 Topic 리스트 -> MeetingResponseDTO.MeetingDetailDTO 변환
     */
    public static MeetingResponseDTO.MeetingDetailDTO fromMeetingAndBookSharedDTOEtcToMeetingDetailDTO(
            Meeting meeting,
            BookExternalDTO.BasicInfo bookSharedDTO,
            List<Topic> topics,
            Map<Long, List<Integer>> topicIdToSelectTeamNumbers,
            List<Team> teams,
            Map<Integer, List<TeamTopic>> teamTopicsGroupingByTeamNumber,
            Map<String, MemberExternalDTO.BasicInfo> authorInfoMap,
            MembershipResponseDTO.MembershipDTO membershipDTO
    ) {
        MeetingResponseDTO.MeetingInfoDTO meetingInfoDTO = ClubMeetingConverter.fromMeetingAndBookSharedDTOToMeetingInfoDTO(
                meeting, bookSharedDTO);

        List<MeetingResponseDTO.TopicDTO> topicDTOList = fromTopicListAndTopicSelectionAndMemberSharedDTOToTopicDTOList(
                topics, authorInfoMap, topicIdToSelectTeamNumbers);

        List<MeetingResponseDTO.TeamTopicDTO> teamTopicDTOList = teams.stream()
                .sorted(Comparator.comparing(Team::getTeamNumber)) // 팀 번호 기준 정렬
                .map(team -> {
                    List<TeamTopic> teamTopics =
                            teamTopicsGroupingByTeamNumber.get(team.getTeamNumber());

                    List<MeetingResponseDTO.TopicDTO> teamTopicDTOs = teamTopics.stream()
                            .map(tt -> ClubMeetingConverter.fromTopicAndMemberSharedDTOAndTeamNumberListToTopicDTO(
                                    tt.getTopic(),
                                    authorInfoMap.get(tt.getTopic().getClubMember().getMemberId()),
                                    null // TeamTopicDTO-TopicDTO에서는 teamNumbers 필드가 NULL이어야 함
                            ))
                            .toList();

                    return fromTopicDTOListToTeamTopicDTO(team.getTeamNumber(), teamTopicDTOs, null);
                })
                .toList();
        return fromMeetingInfoDTOAndTopicDTOListAndTeamTopicDTOListToTopicDTO(
                meetingInfoDTO,
                topicDTOList,
                teamTopicDTOList,
                membershipDTO
        );
    }

    /**
     * Topic 리스트 + Topic별 팀 선택 정보 + 작성자 정보 맵 -> List<MeetingResponseDTO.TopicDTO> 변환
     */
    public static List<MeetingResponseDTO.TopicDTO> fromTopicListAndTopicSelectionAndMemberSharedDTOToTopicDTOList(
            List<Topic> topics,
            Map<String, MemberExternalDTO.BasicInfo> authorInfoMap,
            Map<Long, List<Integer>> topicIdToSelectTeamNumbers
    ) {
        return topics.stream()
                .map(topic -> ClubMeetingConverter.fromTopicAndMemberSharedDTOAndTeamNumberListToTopicDTO(
                        topic,
                        authorInfoMap.get(topic.getClubMember().getMemberId()),
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
    public static BookShelfResponseDTO.BookReviewDTO fromBookReviewAndMemberSharedDTOToBookReviewDTO(
            BookReview bookReview,
            MemberExternalDTO.BasicInfo memberSharedDTO
    ) {
        return BookShelfResponseDTO.BookReviewDTO.builder()
                .bookReviewId(bookReview.getId())
                .description(bookReview.getDescription())
                .rate(bookReview.getRate())
                .authorInfo(memberSharedDTO)
                .build();
    }

    /**
     * BookReviewDTO 리스트 -> BookReviewListDTO 변환
     */
    public static BookShelfResponseDTO.BookReviewListDTO fromBookReviewDTOListToBookReviewListDTO(
            List<BookShelfResponseDTO.BookReviewDTO> bookReviewList,
            boolean hasNext,
            Long nextCursor,
            MembershipResponseDTO.MembershipDTO membershipDTO
    ) {
        return BookShelfResponseDTO.BookReviewListDTO.builder()
                .bookReviewList(bookReviewList)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .membership(membershipDTO)
                .build();
    }

    /**
     * List<TopicDTO> -> BookShelfResponseDTO.TopicListDTO 변환
     */
    public static BookShelfResponseDTO.TopicListDTO fromTopicDTOListToTopicListDTOForBookshelf(
            List<BookShelfResponseDTO.TopicDTO> topicListDTOs,
            boolean hasNext,
            Long nextCursor,
            MembershipResponseDTO.MembershipDTO membershipDTO
    ) {
        return BookShelfResponseDTO.TopicListDTO.builder()
                .topics(topicListDTOs)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .membership(membershipDTO)
                .build();
    }

    /**
     * List<BookShelfInfoDTO> -> BookShelfListDTO 변환
     */
    public static BookShelfResponseDTO.BookShelfListDTO fromBookShelfInfoDTOListToBookShelfListDTO(
            List<BookShelfResponseDTO.BookShelfInfoDTO> bookShelfInfoDTOs,
            boolean hasNext,
            Long nextCursor,
            MembershipResponseDTO.MembershipDTO membershipDTO
    ) {
        return BookShelfResponseDTO.BookShelfListDTO.builder()
                .bookShelfInfoList(bookShelfInfoDTOs)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .membership(membershipDTO)
                .build();
    }

    /**
     * List<MeetingResponseDTO.MeetingInfoDTO> -> MeetingListDTO 변환
     */
    public static MeetingResponseDTO.MeetingListDTO fromMeetingInfoDTOListToMeetingListDTO(
            List<MeetingResponseDTO.MeetingInfoDTO> meetingInfoDTOList,
            boolean hasNext,
            Long nextCursor,
            MembershipResponseDTO.MembershipDTO membershipDTO
    ) {
        return MeetingResponseDTO.MeetingListDTO.builder()
                .meetingInfoList(meetingInfoDTOList)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .membership(membershipDTO)
                .build();
    }

    /**
     * List<MeetingResponseDTO.TopicDTO> -> MeetingResponseDTO.TopicListDTO 변환
     */
    public static MeetingResponseDTO.TopicListDTO fromTopicDTOListToTopicListDTOForMeeting(
            List<MeetingResponseDTO.TopicDTO> topicList,
            boolean hasNext,
            Long nextCursor
    ) {
        return MeetingResponseDTO.TopicListDTO.builder()
                .topics(topicList)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .build();
    }

    /**
     * List<MeetingResponseDTO.TopicDTO> -> MeetingResponseDTO.TeamTopicDTO 변환
     */
    public static MeetingResponseDTO.TeamTopicDTO fromTopicDTOListToTeamTopicDTO(
            Integer teamNumber,
            List<MeetingResponseDTO.TopicDTO> topicList,
            MembershipResponseDTO.MembershipDTO membershipDTO
    ) {
        return MeetingResponseDTO.TeamTopicDTO.builder()
                .teamNumber(teamNumber)
                .topics(topicList)
                .membership(membershipDTO)
                .build();
    }

    /**
     * MeetingResponseDTO.MeetingInfoDTO + List<TopicDTO> + List<TeamTopicDTO> -> MeetingResponseDTO.MeetingDetailDTO
     * 변환
     */
    public static MeetingResponseDTO.MeetingDetailDTO fromMeetingInfoDTOAndTopicDTOListAndTeamTopicDTOListToTopicDTO(
            MeetingResponseDTO.MeetingInfoDTO meetingInfoDTO,
            List<MeetingResponseDTO.TopicDTO> topicDTOList,
            List<MeetingResponseDTO.TeamTopicDTO> teamTopicDTOList,
            MembershipResponseDTO.MembershipDTO membershipDTO
    ) {
        return MeetingResponseDTO.MeetingDetailDTO.builder()
                .meetingInfo(meetingInfoDTO)
                .topics(topicDTOList)
                .teams(teamTopicDTOList)
                .membership(membershipDTO)
                .build();
    }

    public static MeetingResponseDTO.TopicDTOList fromTopicDTOListAndMembershipDTOToTopicListDTO(
            List<MeetingResponseDTO.TopicDTO> topicDTOs,
            MembershipResponseDTO.MembershipDTO membershipDTO
    ) {
        return MeetingResponseDTO.TopicDTOList.builder()
                .topics(topicDTOs)
                .membership(membershipDTO)
                .build();
    }

    /**
     * 팀 번호 + List<MemberExternalDTO> -> MeetingResponseDTO.TeamMemberDTO 변환
     */
    public static MeetingResponseDTO.TeamMemberDTO fromTeamNumberAndMemberSharedDTOToTeamMemberDTO(
            Integer teamNumber,
            List<MemberExternalDTO.BasicInfo> memberSharedDTOs,
            MembershipResponseDTO.MembershipDTO membershipDTO
    ) {
        return MeetingResponseDTO.TeamMemberDTO.builder()
                .teamNumber(teamNumber)
                .members(memberSharedDTOs)
                .membership(membershipDTO)
                .build();
    }

    /**
     * List<MeetingResponseDTO.MeetingMemberDTO> -> MeetingResponseDTO.MeetingMemberListDTO 변환
     */
    public static MeetingResponseDTO.MeetingMemberListDTO fromMeetingMemberDTOListToMeetingMemberListDTO(
            List<MeetingResponseDTO.MeetingMemberDTO> meetingMemberDTOList,
            boolean hasNext, Long nextCursor,
            MembershipResponseDTO.MembershipDTO membershipDTO
    ) {
        return MeetingResponseDTO.MeetingMemberListDTO.builder()
                .members(meetingMemberDTOList)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .membership(membershipDTO)
                .build();
    }

    // =====================================================
    // Parameter ->  DTO 변환
    // =====================================================

    /**
     * 파라미터 -> MeetingResponseDTO.TopicSelectionDTO 변환
     */
    public static MeetingResponseDTO.TopicSelectionDTO fromParametersToTopicSelectionDTO(Long topicId,
                                                                                         Integer teamNumber,
                                                                                         Boolean isSelected) {
        return MeetingResponseDTO.TopicSelectionDTO.builder()
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
    private static BookShelfResponseDTO.TopicDTO fromTopicAndMemberSharedDTOToTopicDTO(
            Topic topic,
            MemberExternalDTO.BasicInfo authorSharedDTO,
            String memberId
    ) {
        return BookShelfResponseDTO.TopicDTO.builder()
                .topicId(topic.getId())
                .content(topic.getDescription())
                .authorInfo(authorSharedDTO)
                .isAuthor(topic.getClubMember().getMemberId().equals(memberId))
                .build();
    }
}