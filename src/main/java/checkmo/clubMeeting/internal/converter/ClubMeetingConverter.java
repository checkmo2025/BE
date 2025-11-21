package checkmo.clubMeeting.internal.converter;

import checkmo.book.BookExternalDTO;
import checkmo.clubMeeting.ClubMeetingExternalDTO;
import checkmo.clubMeeting.internal.entity.BookReview;
import checkmo.clubMeeting.internal.entity.Meeting;
import checkmo.clubMeeting.internal.entity.Topic;
import checkmo.clubMeeting.web.dto.bookshelf.BookShelfRequestDTO;
import checkmo.clubMeeting.web.dto.bookshelf.BookShelfResponseDTO;
import checkmo.clubMeeting.web.dto.meeting.MeetingRequestDTO;
import checkmo.clubMeeting.web.dto.meeting.MeetingResponseDTO;
import checkmo.member.MemberExternalDTO;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClubMeetingConverter {

    // =====================================================
    // ?? -> 엔티티 변환
    // =====================================================

    public static Meeting toMeeting(MeetingRequestDTO.MeetingCreate request, Long clubId, String bookId) {
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
                .isAuthor(topic.isOwnedBy(memberId))
                .build();
    }

    public static BookShelfResponseDTO.BookShelfInfo toBookshelfInfoDTO(
            Meeting meeting,
            BookExternalDTO.BasicInfo bookInfo
    ) {
        BookShelfResponseDTO.MeetingInfo meetingInfo = toMeetingInfoDTO(meeting);
        return BookShelfResponseDTO.BookShelfInfo.builder()
                .meetingInfo(meetingInfo)
                .bookInfo(bookInfo)
                .build();
    }

    public static BookShelfResponseDTO.MeetingInfo toMeetingInfoDTO(Meeting meeting) {
        return BookShelfResponseDTO.MeetingInfo.builder()
                .meetingId(meeting.getId())
                .generation(meeting.getGeneration())
                .tag(meeting.getTag())
                .averageRate(meeting.calculateAverageRate())
                .build();
    }

    public static BookShelfResponseDTO.BookReviewDetail toBookReviewDetailDTO(
            BookReview bookReview,
            MemberExternalDTO.BasicInfo memberInfo
    ) {
        return BookShelfResponseDTO.BookReviewDetail.builder()
                .bookReviewId(bookReview.getId())
                .description(bookReview.getDescription())
                .rate(bookReview.getRate())
                .authorInfo(memberInfo)
                .build();
    }

    // =====================================================
    // ?? -> MeetingResponseDTO 변환
    // =====================================================

    public static MeetingResponseDTO.MeetingInfo toMeetingInfoDTO(Meeting meeting, BookExternalDTO.BasicInfo bookInfo) {
        return MeetingResponseDTO.MeetingInfo.builder()
                .meetingId(meeting.getId())
                .title(meeting.getTitle())
                .meetingTime(meeting.getMeetingTime())
                .location(meeting.getLocation())
                .generation(meeting.getGeneration())
                .tag(meeting.getTag())
                .bookInfo(bookInfo)
                .build();
    }

    public static MeetingResponseDTO.Topic toTopicDTO(
            Topic topic,
            MemberExternalDTO.BasicInfo authorInfo,
            List<Integer> teamNumbers
    ) {
        return MeetingResponseDTO.Topic.builder()
                .topicId(topic.getId())
                .content(topic.getDescription())
                .authorInfo(authorInfo)
                .teamNumbers(teamNumbers)
                .build();
    }

    // =====================================================
    // ?? -> ClubMeetingExternalDTO 변환
    // =====================================================

    public static ClubMeetingExternalDTO.MeetingInfo toMeetingInfoExternalDTO(
            Meeting meeting,
            BookExternalDTO.BasicInfo bookInfo
    ) {
        return ClubMeetingExternalDTO.MeetingInfo.builder()
                .meetingId(meeting.getId())
                .title(meeting.getTitle())
                .meetingTime(meeting.getMeetingTime())
                .location(meeting.getLocation())
                .generation(meeting.getGeneration())
                .tag(meeting.getTag())
                .content(meeting.getContent())
                .bookInfo(bookInfo)
                .build();
    }
}