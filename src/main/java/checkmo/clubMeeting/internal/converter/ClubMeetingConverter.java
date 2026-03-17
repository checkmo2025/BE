package checkmo.clubMeeting.internal.converter;

import checkmo.book.BookExternalDTO;
import checkmo.clubManagement.ClubManagementExternalDTO.MembershipInfo;
import checkmo.clubMeeting.ClubMeetingExternalDTO.DetailInfo;
import checkmo.clubMeeting.internal.entity.BookReview;
import checkmo.clubMeeting.internal.entity.Meeting;
import checkmo.clubMeeting.internal.entity.Team;
import checkmo.clubMeeting.internal.entity.Topic;
import checkmo.clubMeeting.web.dto.bookshelf.BookShelfRequestDTO;
import checkmo.clubMeeting.web.dto.bookshelf.BookShelfRequestDTO.BookShelfCreate;
import checkmo.clubMeeting.web.dto.bookshelf.BookShelfResponseDTO;
import checkmo.clubMeeting.web.dto.bookshelf.BookShelfResponseDTO.MeetingDetailInfo;
import checkmo.clubMeeting.web.dto.meeting.MeetingResponseDTO;
import checkmo.clubMeeting.web.dto.meeting.MeetingResponseDTO.MeetingInfo;
import checkmo.clubMeeting.web.dto.meeting.MeetingResponseDTO.TeamKey;
import checkmo.member.MemberExternalDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public static MeetingDetailInfo toMeetingDetailInfo(Meeting meeting) {
        return BookShelfResponseDTO.MeetingDetailInfo.builder()
                .meetingId(meeting.getId())
                .title(meeting.getTitle())
                .meetingTime(meeting.getMeetingTime())
                .location(meeting.getLocation())
                .generation(meeting.getGeneration())
                .tag(meeting.getTag())
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
            Map<String, MemberExternalDTO.BasicInfo> authorInfoMap,
            Set<Long> selectedTopicIds
    ) {
        return MeetingResponseDTO.Topic.builder()
                .topicId(topic.getId())
                .content(topic.getDescription())
                .createdAt(topic.getCreatedAt())
                .author(authorInfoMap.get(topic.getMemberId()))
                .selected(selectedTopicIds.contains(topic.getId()))
                .build();
    }

    public static MeetingInfo toMeetingInfoDTO(
            Meeting meeting,
            List<Team> teams,
            Map<Integer, List<MeetingResponseDTO.MeetingMember>> teamNumberToMembers,
            boolean staff
    ) {
        List<TeamKey> existingTeams = toExistingTeamsDTO(teams);
        Map<Integer, List<MeetingResponseDTO.MeetingMember>> safeTeamNumberToMembers
                = (teamNumberToMembers != null) ? teamNumberToMembers : Map.of();

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

    public static List<MeetingResponseDTO.TeamKey> toExistingTeamsDTO(List<Team> teams) {
        List<Team> safeTeams = (teams != null) ? teams : List.of();
        return safeTeams.stream()
                .map(ClubMeetingConverter::toTeamKeyDTO)
                .sorted(Comparator.comparingInt(MeetingResponseDTO.TeamKey::getTeamNumber))
                .toList();
    }

    public static MeetingResponseDTO.TeamKey toTeamKeyDTO(Team team) {
        if (team == null) {
            return null;
        }
        return MeetingResponseDTO.TeamKey.builder()
                .teamId(team.getId())
                .teamNumber(team.getTeamNumber())
                .build();
    }

    public static List<MeetingResponseDTO.MeetingMember> toMeetingMembersDTO(
            List<MembershipInfo> clubMemberships,
            Map<String, MemberExternalDTO.BasicInfo> memberBasicInfoMap,
            Map<Long, Long> clubMemberIdToTeamIdMap,
            Map<Long, Integer> teamIdToTeamNumberMap
    ) {
        List<MembershipInfo> safeMemberships = (clubMemberships != null) ? clubMemberships : List.of();
        Map<String, MemberExternalDTO.BasicInfo> safeMemberInfoMap =
                (memberBasicInfoMap != null) ? memberBasicInfoMap : Map.of();
        Map<Long, Long> safeClubMemberToTeamIdMap =
                (clubMemberIdToTeamIdMap != null) ? clubMemberIdToTeamIdMap : Map.of();
        Map<Long, Integer> safeTeamIdToTeamNumberMap =
                (teamIdToTeamNumberMap != null) ? teamIdToTeamNumberMap : Map.of();

        return safeMemberships.stream()
                .map(m -> {
                    Long clubMemberId = m.getClubMemberId();
                    String memberId = m.getMemberId();

                    MemberExternalDTO.BasicInfo basic = safeMemberInfoMap.get(memberId);

                    Long teamId = safeClubMemberToTeamIdMap.get(clubMemberId);
                    MeetingResponseDTO.TeamKey teamKey = (teamId == null) ? null
                            : MeetingResponseDTO.TeamKey.builder()
                            .teamId(teamId)
                            .teamNumber(safeTeamIdToTeamNumberMap.get(teamId))
                            .build();

                    return MeetingResponseDTO.MeetingMember.builder()
                            .clubMemberId(clubMemberId)
                            .memberInfo(basic)
                            .teamKey(teamKey)
                            .build();
                })
                .toList();
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
                .generation(meeting.getGeneration())
                .tag(meeting.getTag())
                .averageRate(meeting.calculateAverageRate())
                .bookInfo(bookInfo)
                .build();
    }
}