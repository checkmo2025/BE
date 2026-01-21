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
import checkmo.clubMeeting.web.dto.meeting.MeetingRequestDTO;
import checkmo.clubMeeting.web.dto.meeting.MeetingRequestDTO.MeetingCreate;
import checkmo.clubMeeting.web.dto.meeting.MeetingRequestDTO.MeetingUpdate;
import java.util.List;
import java.util.Map;
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

    // Domain level 1
    private final BookAPI bookAPI;

    private final ClubManagementAPI clubManagementAPI;

    private final ClubMeetingQueryService clubMeetingQueryService;

    private final MeetingRepository meetingRepository;
    private final TeamRepository teamRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    // TODO: 전체적으로 Meeting 존재 여부 검증을 Service에서 해야 함 -> 따라서 API endpoint를 club/{clubId}/meeting/{meetingId}/... 이런 식으로 바꿔야 함

    public Long createMeeting(Long clubId, String memberId, MeetingCreate request) {
        clubManagementAPI.validateClub(clubId);
        clubManagementAPI.validateStaffClubMember(clubId, memberId);

        String bookId = bookAPI.fetchOrCreateBook(request.getBookInfo());

        Meeting meeting = ClubMeetingConverter.toMeeting(request, clubId, bookId);
        meetingRepository.saveAndFlush(meeting);

        // 미팅 생성 알림 이벤트 발행
        publishMeetingCreatedNotificationEvent(meeting, clubId);

        return meeting.getId();
    }

    public Long updateMeeting(Long meetingId, String memberId, MeetingUpdate request) {
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        clubManagementAPI.validateClub(meeting.getClubId());
        clubManagementAPI.validateStaffClubMember(meeting.getClubId(), memberId);

        meeting.updateMeeting(
                request.getTitle(),
                request.getMeetingTime(),
                request.getLocation(),
                request.getContent(),
                request.getGeneration(),
                request.getTag()
        );

        meetingRepository.saveAndFlush(meeting);

        return meeting.getId();
    }

    public void manageTeam(Long meetingId, String memberId, MeetingRequestDTO.TeamManage request) {
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        clubManagementAPI.validateStaffClubMember(meeting.getClubId(), memberId);

        // 요청 teamNumber와 nicknameList 검증 및 정리
        Map<Integer, List<Long>> requestTeamNumberToClubMemberIds =
                request.getTeamMemberList().stream()
                        .collect(Collectors.toMap(
                                MeetingRequestDTO.TeamMember::getTeamNumber,
                                dto -> dto.getClubMemberIds().stream().distinct().toList()
                        ));
        Set<Integer> requestTeamNumbers = requestTeamNumberToClubMemberIds.keySet();

        // 해당 미팅의 기존 팀들 조회 후 teamNumber -> Team Map (TeamTopic이 유지되도록 Team은 유지)
        List<Team> existingTeams = teamRepository.findAllByMeetingIdOrderByTeamNumberAsc(meeting.getId());
        Map<Integer, Team> existingTeamNumberToTeam = existingTeams.stream()
                .collect(Collectors.toMap(Team::getTeamNumber, t -> t));

        // 요청에 있는데 아직 없는 teamNumber는 Team 생성
        requestTeamNumbers.stream()
                .filter(teamNumber -> !existingTeamNumberToTeam.containsKey(teamNumber))
                .forEach(teamNumber -> {
                    Team team = Team.builder()
                            .teamNumber(teamNumber)
                            .build();
                    team.setMeeting(meeting);
                    existingTeams.add(team);
                    existingTeamNumberToTeam.put(teamNumber, team);
                });

        // 요청에는 없는데 존재하는 teamNumber는 Team 삭제
        List<Team> toDeleteTeams = existingTeams.stream()
                .filter(t -> !requestTeamNumbers.contains(t.getTeamNumber()))
                .toList();
        // 미팅과의 양방향 연관 끊기 -> orphanRemoval이 true이므로 미팅이 flush될 때 Team도 삭제됨
        toDeleteTeams.forEach(Team::removeMeeting);
        // 기존 팀, 기존 teamNumber -> Team Map 메모리 컬렉션/맵 동기화
        existingTeams.removeAll(toDeleteTeams);
        existingTeamNumberToTeam.keySet().removeAll(toDeleteTeams.stream()
                .map(Team::getTeamNumber)
                .collect(Collectors.toSet()));

        // 기존 ClubMemberTeam orphanRemoval = true 삭제
        if (!existingTeams.isEmpty()) {
            existingTeams.forEach(Team::clearMemberTeams);
            // 기존 멤버 삭제 시 소유자만 끊고 orphanRemoval=true로 고아 삭제를 걸면 DB 행은 사라지고,
            // clubMember.memberTeams는 LAZY 초기화를 해서 굳이 연관관계를 설정하지 않는다.
            // 이때 이 하나의 트랜잭션에서 clubMember.memberTeams를 사용하지 않습니다!!!
        }

        // 요청대로 ClubMemberTeam 배치 재생성
        for (Map.Entry<Integer, List<Long>> e : requestTeamNumberToClubMemberIds.entrySet()) {
            Team team = existingTeamNumberToTeam.get(e.getKey());
            for (Long cmId : e.getValue()) {
                ClubMemberTeam mt = ClubMemberTeam.builder()
                        .clubMemberId(cmId)
                        .build();
                mt.setTeam(team);
            }
        }

        // 10. 기존 팀과 새로 생성된 Team을 명시적으로 저장 (내부적으로 ClubMemberTeam도 저장됨)
        meetingRepository.save(meeting);
        teamRepository.saveAll(existingTeams);
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
}
