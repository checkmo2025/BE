package checkmo.clubMeeting.internal.service.command;

import checkmo.book.BookAPI;
import checkmo.clubManagement.ClubManagementAPI;
import checkmo.clubMeeting.ClubMeetingEvent.ClubMeetingCreated;
import checkmo.clubMeeting.ClubMeetingEvent.ClubMeetingDeleted;
import checkmo.clubMeeting.internal.converter.ClubMeetingConverter;
import checkmo.clubMeeting.internal.entity.Meeting;
import checkmo.clubMeeting.internal.entity.Team;
import checkmo.clubMeeting.internal.repository.MeetingRepository;
import checkmo.clubMeeting.internal.repository.TeamRepository;
import checkmo.clubMeeting.internal.service.query.ClubMeetingQueryService;
import checkmo.clubMeeting.web.dto.bookshelf.BookShelfRequestDTO.BookShelfCreate;
import checkmo.clubMeeting.web.dto.bookshelf.BookShelfRequestDTO.BookShelfUpdate;
import checkmo.clubMeeting.web.dto.meeting.MeetingRequestDTO;
import checkmo.clubMeeting.web.dto.meeting.MeetingRequestDTO.TeamManage;
import checkmo.clubMeeting.web.dto.meeting.MeetingRequestDTO.TeamMember;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ClubMeetingCommandService {

    private final BookAPI bookAPI;
    private final ClubManagementAPI clubManagementAPI;

    private final ClubMeetingQueryService clubMeetingQueryService;

    private final MeetingRepository meetingRepository;
    private final TeamRepository teamRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    public void createMeeting(Long clubId, Long memberId, BookShelfCreate request) {
        clubManagementAPI.validateClub(clubId);
        clubManagementAPI.validateStaffClubMember(clubId, memberId);

        String bookId = bookAPI.fetchOrCreateBook(request.getIsbn());

        Meeting meeting = ClubMeetingConverter.toMeeting(request, clubId, bookId);
        Meeting savedMeeting = meetingRepository.saveAndFlush(meeting);

        clubManagementAPI.touchLastActivity(clubId, LocalDateTime.now());
        publishMeetingCreatedNotificationEvent(savedMeeting, clubId);
    }

    private void publishMeetingCreatedNotificationEvent(Meeting meeting, Long clubId) {
        String clubName = clubManagementAPI.fetchClubName(clubId);
        ClubMeetingCreated event = ClubMeetingCreated.builder()
                .eventId(meeting.getId())
                .clubId(clubId)
                .clubName(clubName)
                .build();

        applicationEventPublisher.publishEvent(event);
    }

    public void updateMeeting(Long clubId, Long meetingId, Long memberId, BookShelfUpdate request) {
        clubManagementAPI.validateClub(clubId);
        clubManagementAPI.validateStaffClubMember(clubId, memberId);
        Meeting meeting = clubMeetingQueryService.validateMeeting(clubId, meetingId);

        meeting.updateMeeting(
                request.getTitle(),
                request.getMeetingTime(),
                request.getLocation(),
                request.getGeneration(),
                request.getTag()
        );

        meetingRepository.saveAndFlush(meeting);
        clubManagementAPI.touchLastActivity(clubId, LocalDateTime.now());
    }

    public void deleteMeeting(Long clubId, Long meetingId, Long memberId) {
        clubManagementAPI.validateClub(clubId);
        clubManagementAPI.validateStaffClubMember(clubId, memberId);
        Meeting meeting = clubMeetingQueryService.validateMeeting(clubId, meetingId);
        meetingRepository.delete(meeting);
        ClubMeetingDeleted event = ClubMeetingDeleted.builder()
                .eventId(meetingId)
                .clubId(clubId)
                .meetingId(meetingId)
                .build();
        applicationEventPublisher.publishEvent(event);
    }

    public void manageTeam(Long clubId, Long meetingId, Long memberId, MeetingRequestDTO.TeamManage request) {
        clubManagementAPI.validateClub(clubId);
        clubManagementAPI.validateStaffClubMember(clubId, memberId);
        Meeting meeting = clubMeetingQueryService.validateMeeting(clubId, meetingId);

        List<TeamMember> teamMemberList = request.getTeamMemberList();
        if (teamMemberList == null || teamMemberList.isEmpty()) {
            // 전체 팀 제거
            meeting.removeAllTeams();
            meetingRepository.save(meeting);
            return;
        }

        // 요청 정리: teamNumber -> distinct ClubMemberIds
        Map<Integer, List<Long>> requestTeamNumberToClubMemberIds = normalizeTeamManageRequest(request);
        // 요청 clubMemberIds 배치 검증
        validateRequestClubMembers(clubId, requestTeamNumberToClubMemberIds);

        List<Team> existingTeams = teamRepository.findAllByMeetingIdOrderByTeamNumberAsc(meeting.getId());
        meeting.reconfigureTeams(existingTeams, requestTeamNumberToClubMemberIds);

        meetingRepository.save(meeting);
    }

    private Map<Integer, List<Long>> normalizeTeamManageRequest(TeamManage request) {
        return request.getTeamMemberList().stream()
                .collect(Collectors.toMap(
                        MeetingRequestDTO.TeamMember::getTeamNumber,
                        dto -> dto.getClubMemberIds().stream().filter(Objects::nonNull).distinct().toList(),
                        (a, b) -> b // validator가 중복 방지하지만 방어적으로
                ));
    }

    private void validateRequestClubMembers(Long clubId, Map<Integer, List<Long>> teamManageRequest) {
        Set<Long> requestedClubMemberIds = teamManageRequest.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toSet());
        clubManagementAPI.validateActiveClubMembers(clubId, requestedClubMemberIds);
    }

    public void deleteAll(Long clubId) {
        List<Meeting> meetings = meetingRepository.findAllByClubId(clubId);
        meetingRepository.deleteAll(meetings);
        meetingRepository.flush();
    }
}
