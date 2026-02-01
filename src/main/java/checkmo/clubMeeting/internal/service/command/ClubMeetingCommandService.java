package checkmo.clubMeeting.internal.service.command;

import checkmo.book.BookAPI;
import checkmo.clubManagement.ClubManagementAPI;
import checkmo.clubMeeting.ClubMeetingEvent.ClubMeetingCreated;
import checkmo.clubMeeting.internal.converter.ClubMeetingConverter;
import checkmo.clubMeeting.internal.entity.ClubMemberTeam;
import checkmo.clubMeeting.internal.entity.Meeting;
import checkmo.clubMeeting.internal.entity.Team;
import checkmo.clubMeeting.internal.repository.MeetingRepository;
import checkmo.clubMeeting.internal.repository.TeamRepository;
import checkmo.clubMeeting.internal.service.query.ClubMeetingQueryService;
import checkmo.clubMeeting.web.dto.bookshelf.BookShelfRequestDTO.BookShelfCreate;
import checkmo.clubMeeting.web.dto.bookshelf.BookShelfRequestDTO.BookShelfUpdate;
import checkmo.clubMeeting.web.dto.meeting.MeetingRequestDTO;
import checkmo.clubMeeting.web.dto.meeting.MeetingRequestDTO.TeamManage;
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

    public void createMeeting(Long clubId, String memberId, BookShelfCreate request) {
        clubManagementAPI.validateClub(clubId);
        clubManagementAPI.validateStaffClubMember(clubId, memberId);

        String bookId = bookAPI.fetchOrCreateBook(request.getBookInfo());

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

    public void updateMeeting(Long clubId, Long meetingId, String memberId, BookShelfUpdate request) {
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

    public void manageTeam(Long clubId, Long meetingId, String memberId, MeetingRequestDTO.TeamManage request) {
        clubManagementAPI.validateClub(clubId);
        clubManagementAPI.validateStaffClubMember(clubId, memberId);
        Meeting meeting = clubMeetingQueryService.validateMeeting(clubId, meetingId);

        // 요청 정리: teamNumber -> distinct ClubMemberIds
        Map<Integer, List<Long>> requestTeamNumberToClubMemberIds = normalizeTeamManageRequest(request);
        Set<Integer> requestTeamNumbers = requestTeamNumberToClubMemberIds.keySet();

        // 요청 clubMemberIds 배치 검증
        validateRequestClubMembers(clubId, requestTeamNumberToClubMemberIds);

        // 기존 팀 조회 후 teamNumber -> Team Map (TeamTopic이 유지되도록 Team은 유지)
        List<Team> existingTeams = teamRepository.findAllByMeetingIdOrderByTeamNumberAsc(meeting.getId());
        Map<Integer, Team> existingTeamNumberToTeam = existingTeams.stream()
                .collect(Collectors.toMap(Team::getTeamNumber, t -> t));

        // 요청에 있는데 아직 없는 teamNumber는 Team 생성 후 meeting에 추가
        for (Integer teamNumber : requestTeamNumbers) {
            if (!existingTeamNumberToTeam.containsKey(teamNumber)) {
                Team team = Team.builder()
                        .teamNumber(teamNumber)
                        .build();
                meeting.addTeam(team);
                existingTeamNumberToTeam.put(teamNumber, team);
            }
        }

        // 요청에는 없는데 존재하는 팀은 meeting에서 제거
        removeTeamsNotInRequest(existingTeams, requestTeamNumbers, meeting);

        // 기존 ClubMemberTeam 모두 제거
        meeting.getTeams().forEach(Team::clearClubMemberTeam);

        // 요청 ClubMemberTeam 재생성
        for (Map.Entry<Integer, List<Long>> e : requestTeamNumberToClubMemberIds.entrySet()) {
            Team team = existingTeamNumberToTeam.get(e.getKey());
            for (Long cmId : e.getValue()) {
                ClubMemberTeam mt = ClubMemberTeam.builder()
                        .clubMemberId(cmId)
                        .build();
                team.addClubMemberTeam(mt);
            }
        }

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

    private void removeTeamsNotInRequest(List<Team> existingTeams, Set<Integer> requestTeamNumbers, Meeting meeting) {
        List<Team> toRemove = existingTeams.stream()
                .filter(t -> !requestTeamNumbers.contains(t.getTeamNumber()))
                .toList();
        toRemove.forEach(meeting::removeTeam);
    }
}
