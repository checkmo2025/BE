package checkmo.clubMeeting.internal.converter;

import checkmo.book.BookExternalDTO;
import checkmo.clubMeeting.ClubMeetingExternalDTO.DetailInfo;
import checkmo.clubMeeting.internal.entity.BookReview;
import checkmo.clubMeeting.internal.entity.Meeting;
import checkmo.clubMeeting.internal.entity.Team;
import checkmo.clubMeeting.internal.entity.Topic;
import checkmo.clubMeeting.web.dto.bookshelf.BookShelfRequestDTO;
import checkmo.clubMeeting.web.dto.bookshelf.BookShelfRequestDTO.BookShelfCreate;
import checkmo.clubMeeting.web.dto.bookshelf.BookShelfResponseDTO;
import checkmo.clubMeeting.web.dto.meeting.MeetingResponseDTO;
import checkmo.clubMeeting.web.dto.meeting.MeetingResponseDTO.MeetingInfo;
import checkmo.clubMeeting.web.dto.meeting.MeetingResponseDTO.TeamKey;
import checkmo.member.MemberExternalDTO;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClubMeetingConverter {

    // =====================================================
    // ?? -> 엔티티 변환
    // =====================================================

    public static Meeting toMeeting(BookShelfCreate request, Long clubId, String bookId) {
        return Meeting.builder()
                .title(request.getTitle())
                .meetingTime(request.getMeetingTime())
                .location(request.getLocation())
                .generation(request.getGeneration())
                .tag(request.getTag())
                .clubId(clubId)
                .bookId(bookId)
                .build();
    }

    public static BookReview toBookReview(
            BookShelfRequestDTO.BookReviewCreate request,
            Long clubMemberId,
            String memberId
    ) {
        return BookReview.builder()
                .description(request.getDescription())
                .rate(request.getRate())
                .clubMemberId(clubMemberId)
                .memberId(memberId)
                .build();
    }

    public static Topic toTopic(
            BookShelfRequestDTO.TopicCreate topicCreateDTO,
            String memberId,
            Long clubMemberId
    ) {
        return Topic.builder()
                .description(topicCreateDTO.getDescription())
                .clubMemberId(clubMemberId)
                .memberId(memberId)
                .build();
    }

    // =====================================================
    // ?? -> BookshelfDTO 변환
    // =====================================================

    public static BookShelfResponseDTO.TopicDetail toTopicDetailDTO(
            Topic topic,
            MemberExternalDTO.BasicInfo authorInfo,
            String memberId
    ) {
        return BookShelfResponseDTO.TopicDetail.builder()
                .topicId(topic.getId())
                .content(topic.getDescription())
                .authorInfo(authorInfo)
                .author(topic.isOwnedBy(memberId))
                .build();
    }

    public static BookShelfResponseDTO.BookShelfInfo toBookshelfInfoDTO(
            Meeting meeting,
            BookExternalDTO.BasicInfo bookInfo
    ) {
        BookShelfResponseDTO.MeetingInfo meetingInfo = toMeetingInfoDTOForBookshelves(meeting);
        return BookShelfResponseDTO.BookShelfInfo.builder()
                .meetingInfo(meetingInfo)
                .bookInfo(bookInfo)
                .build();
    }

    public static BookShelfResponseDTO.MeetingInfo toMeetingInfoDTOForBookshelves(Meeting meeting) {
        return BookShelfResponseDTO.MeetingInfo.builder()
                .meetingId(meeting.getId())
                .generation(meeting.getGeneration())
                .tag(meeting.getTag())
                .averageRate(meeting.calculateAverageRate())
                .build();
    }

    public static BookShelfResponseDTO.BookReviewDetail toBookReviewDetailDTO(
            BookReview bookReview,
            MemberExternalDTO.BasicInfo memberInfo,
            boolean isAuthor
    ) {
        return BookShelfResponseDTO.BookReviewDetail.builder()
                .bookReviewId(bookReview.getId())
                .description(bookReview.getDescription())
                .rate(bookReview.getRate())
                .authorInfo(memberInfo)
                .author(isAuthor)
                .build();
    }

    // =====================================================
    // ?? -> MeetingResponseDTO 변환
    // =====================================================

    public static MeetingResponseDTO.Topic toTopicDTO(
            Topic topic,
            MemberExternalDTO.BasicInfo authorInfo,
            boolean isSelected
    ) {
        return MeetingResponseDTO.Topic.builder()
                .topicId(topic.getId())
                .content(topic.getDescription())
                .createdAt(topic.getCreatedAt())
                .author(authorInfo)
                .selected(isSelected)
                .build();
    }

    public static MeetingInfo toMeetingInfoWithTeams(
            Meeting meeting,
            List<Team> teams,
            Map<Integer, List<MeetingResponseDTO.MeetingMember>> teamNumberToMembers,
            boolean staff
    ) {
        List<Team> safeTeams = (teams != null) ? teams : List.of();
        Map<Integer, List<MeetingResponseDTO.MeetingMember>> safeTeamNumberToMembers
                = (teamNumberToMembers != null) ? teamNumberToMembers : Map.of();

        List<MeetingResponseDTO.TeamKey> existingTeams = safeTeams.stream()
                .map(team -> MeetingResponseDTO.TeamKey.builder()
                        .teamId(team.getId())
                        .teamNumber(team.getTeamNumber())
                        .build())
                .sorted(Comparator.comparingInt(TeamKey::getTeamNumber))
                .toList();

        return MeetingInfo.builder()
                .meetingId(meeting.getId())
                .title(meeting.getTitle())
                .meetingTime(meeting.getMeetingTime())
                .location(meeting.getLocation())
                .existingTeams(existingTeams)
                .teamMembers(existingTeams.stream()
                        .map(teamKey -> MeetingResponseDTO.TeamMember.builder()
                                .teamKey(teamKey)
                                .members(safeTeamNumberToMembers.getOrDefault(teamKey.getTeamNumber(), List.of()))
                                .build())
                        .toList())
                .staff(staff)
                .build();

    }

    // =====================================================
    // ?? -> ClubMeetingExternalDTO 변환
    // =====================================================
    public static DetailInfo toMeetingInfoExternalDTO(
            Meeting meeting,
            BookExternalDTO.BasicInfo bookInfo
    ) {
        return DetailInfo.builder()
                .meetingId(meeting.getId())
                .title(meeting.getTitle())
                .meetingTime(meeting.getMeetingTime())
                .location(meeting.getLocation())
                .generation(meeting.getGeneration())
                .tag(meeting.getTag())
                .bookInfo(bookInfo)
                .build();
    }
}