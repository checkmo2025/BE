package checkmo.domain.club.service.command;

import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.club.converter.ClubConverter;
import checkmo.domain.club.entity.Club;
import checkmo.domain.club.entity.ClubMember;
import checkmo.domain.club.entity.announcement.Notice;
import checkmo.domain.club.entity.meeting.*;
import checkmo.domain.club.repository.meeting.*;
import checkmo.domain.club.service.query.ClubMeetingQueryService;
import checkmo.domain.club.service.query.ClubMemberQueryService;
import checkmo.domain.club.web.dto.bookshelf.BookShelfRequestDTO;
import checkmo.domain.club.web.dto.meeting.MeetingRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ClubMeetingCommandServiceImpl implements ClubMeetingCommandService {

    // 자신의 QueryService
    private final ClubMemberQueryService clubMemberQueryService;
    private final ClubMeetingQueryService clubMeetingQueryService;

    // 자신의 Repository
    private final MeetingRepository meetingRepository;
    private final TopicRepository topicRepository;
    private final TeamRepository teamRepository;
    private final TeamTopicRepository teamTopicRepository;
    private final BookReviewRepository bookReviewRepository;

    // TODO: 전체적으로 Meeting 존재 여부 검증을 Service에서 해야 함 -> 따라서 API endpoint를 club/{clubId}/meeting/{meetingId}/... 이런 식으로 바꿔야 함

    @Override
    public Long createMeeting(Club club, ClubMember clubMember, Meeting meeting) {
        // 1. 운영진 여부 검증
        if (!clubMember.isStaff()) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }

        // 2. 미팅 기반 공지사항 생성
        Notice notice = ClubConverter.fromMeetingToNotice(meeting, club);
        meeting.addNotice(notice);

        // 3. 미팅 명시적 저장 -> 공지사항도 함께 저장됨
        return meetingRepository.save(meeting).getId();
    }

    @Override
    public Long updateMeeting(Meeting meeting, Club club, ClubMember clubMember, MeetingRequestDTO.MeetingUpdateRequestDTO request) {
        // 1. 운영진 여부 검증
        if (!clubMember.isStaff()) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }

        // 2. 미팅 정보 수정
        meeting.updateMeeting(
                request.getTitle(),
                request.getMeetingTime(),
                request.getLocation(),
                request.getContent(),
                request.getGeneration(),
                request.getTag()
        );

        // 3. 새로운 공지사항 생성 및 교체(고아객체 자동 삭제)
        Notice newNotice = ClubConverter.fromMeetingToNotice(meeting, club);
        meeting.replaceNotice(newNotice);

        return meeting.getId();
    }

    @Override
    public Long createTopic(Meeting meeting, ClubMember clubMember, BookShelfRequestDTO.TopicDTO request) {
        // 1. 발제 생성자 활성화 여부 확인
        if (!clubMember.isActive()) {
            throw new GeneralException(ErrorStatus.CLUB_MEMBER_IS_NOT_ACTIVE);
        }

        // 2. 발제 생성
        Topic topic = ClubConverter.fromTopicDTOToTopic(request);
        topic.setMeeting(meeting);
        topic.setClubMember(clubMember);

        // 3. 발제 저장
        return topicRepository.save(topic).getId();
    }

    @Override
    public Long updateTopic(Long clubId, ClubMember clubMember, Long meetingId, Long topicId, BookShelfRequestDTO.TopicDTO request) {
        // 1. 발제 수정자 활성화 여부 확인
        if (!clubMember.isActive()) {
            throw new GeneralException(ErrorStatus.CLUB_MEMBER_IS_NOT_ACTIVE);
        }

        // 2. 발제 조회 및 존재 여부 확인
        Topic topic = clubMeetingQueryService.validateTopic(topicId, meetingId);

        // 3. 발제 작성자와 수정자가 같은지 확인
        if (!topic.isOwnedBy(clubMember)) {
            throw new GeneralException(ErrorStatus.TOPIC_FORBIDDEN);
        }

        // 4. 발제 수정
        topic.updateTopic(
                request.getDescription()
        );

        return topic.getId();
    }

    @Override
    public void deleteTopic(Long clubId, ClubMember clubMember, Long meetingId, Long topicId) {
        // 1. 발제 삭제자 활성화 여부 확인
        if (!clubMember.isActive()) {
            throw new GeneralException(ErrorStatus.CLUB_MEMBER_IS_NOT_ACTIVE);
        }

        // 2. 발제 조회 및 존재 여부 확인
        Topic topic = clubMeetingQueryService.validateTopic(topicId, meetingId);

        // 3. 발제 작성자와 삭제자가 같은지 확인
        if (!topic.isOwnedBy(clubMember)) {
            throw new GeneralException(ErrorStatus.TOPIC_FORBIDDEN);
        }

        // 4. 발제 삭제
        topic.removeMeeting();
        topic.removeClubMember();
    }

    @Override
    public Boolean selectOrCancelTopic(ClubMember clubMember, Long meetingId, Long topicId, MeetingRequestDTO.TopicSelectionDTO request) {
        // 1. 발제 선택자 활성화 여부 확인
        if (!clubMember.isActive()) {
            throw new GeneralException(ErrorStatus.CLUB_MEMBER_IS_NOT_ACTIVE);
        }

        // 2. 팀, 발제 존재 여부 및 일치 여부 확인
        Team team = clubMeetingQueryService.validateTeam(meetingId, request.getTeamNumber());
        Topic topic = clubMeetingQueryService.validateTopic(topicId, meetingId);

        // 3. 팀 발제가 존재하는지(선택된 상태인지) 확인
        Optional<TeamTopic> existingTeamTopic = teamTopicRepository.findByTeamIdAndTopicId(team.getId(), topic.getId());
        boolean isSelected = existingTeamTopic.isPresent();

        // 4. 요청과 상태가 같으면 무시
        if (request.getIsSelected() == isSelected) {
            return isSelected;
        }

        // 5. 상태 변경
        if (request.getIsSelected()) {
            // 5-1. 팀 발제 선택
            TeamTopic teamTopic = TeamTopic.builder()
                    .team(team)
                    .topic(topic)
                    .build();
            teamTopic.setTeam(team);
            teamTopic.setTopic(topic);

            try {
                teamTopicRepository.saveAndFlush(teamTopic);
            } catch (DataIntegrityViolationException e) {
                // 다른 쓰레드가 먼저 팀 발제를 선택한 경우, 선택 성공으로 간주
                teamTopic.removeTeam();
                teamTopic.removeTopic();
                return true;
            }
            return true;
        } else {
            // 5-2. 팀 발제 선택 취소
            try {
                TeamTopic teamTopic = existingTeamTopic.get();
                // 연관관계 해제 및 orphanRemoval로 삭제 처리
                teamTopic.removeTeam();
                teamTopic.removeTopic();
                teamTopicRepository.flush();
                return false;
            } catch (OptimisticLockingFailureException e) {
                // 다른 트랜잭션이 이미 삭제했거나 수정한 경우, 선택 해제 성공으로 간주
                return false;
            }
        }
    }

    @Override
    public void manageTeam(ClubMember clubMember, Meeting meeting, MeetingRequestDTO.TeamManageDTO request) {
        // 1. 운영진 여부 검증
        if (!clubMember.isStaff()) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }

        // 2. 요청 teamNumber와 nicknameList 검증 및 정리
        Map<Integer, List<String>> requestTeamNumberToNicknameList =
                request.getTeamMemberDTOList().stream()
                        .collect(Collectors.toMap(
                                MeetingRequestDTO.TeamMemberDTO::getTeamNumber,
                                dto -> dto.getNicknameList().stream().distinct().toList()
                        ));
        Set<Integer> requestTeamNumbers = requestTeamNumberToNicknameList.keySet();
        Set<String> requestNicknames = requestTeamNumberToNicknameList.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // 3. 해당 미팅의 기존 팀들 조회 후 teamNumber -> Team Map (TeamTopic이 유지되도록 Team은 유지)
        List<Team> existingTeams = teamRepository.findAllByMeetingIdOrderByTeamNumberAsc(meeting.getId());
        Map<Integer, Team> existingTeamNumberToTeam = existingTeams.stream()
                .collect(Collectors.toMap(Team::getTeamNumber, t -> t));

        // 4. 요청에 있는데 아직 없는 teamNumber는 Team 생성
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

        // 5. 요청에는 없는데 존재하는 teamNumber는 Team 삭제
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

        // 6. 기존 MemberTeam orphanRemoval = true 삭제
        if (!existingTeams.isEmpty()) {
            existingTeams.forEach(Team::clearMemberTeams);
            // 기존 멤버 삭제 시 소유자만 끊고 orphanRemoval=true로 고아 삭제를 걸면 DB 행은 사라지고,
            // clubMember.memberTeams는 LAZY 초기화를 해서 굳이 연관관계를 설정하지 않는다.
            // 이때 이 하나의 트랜잭션에서 clubMember.memberTeams를 사용하지 않습니다!!!
        }

        // 7. 닉네임 → memberId → ClubMember 일괄 매핑
        Map<String, ClubMember> nicknameToClubMember = clubMemberQueryService.getNicknameToClubMember(meeting.getClubId(), requestNicknames.stream().toList());

        // 8. 요청대로 MemberTeam 배치 재생성
        for (Map.Entry<Integer, List<String>> e : requestTeamNumberToNicknameList.entrySet()) {
            Team team = existingTeamNumberToTeam.get(e.getKey());
            for (String nick : e.getValue()) {
                ClubMember cm = nicknameToClubMember.get(nick);
                MemberTeam mt = MemberTeam.builder().build();
                mt.setClubMember(cm);
                mt.setTeam(team);
            }
        }

        // 9. 기존 팀과 새로 생성된 Team을 명시적으로 저장 (내부적으로 MemberTeam도 저장됨)
        meetingRepository.save(meeting);
        teamRepository.saveAll(existingTeams);
    }

    @Override
    public Long createBookReview(ClubMember clubMember, Meeting meeting, BookShelfRequestDTO.BookReviewDTO request) {
        // 1. 한줄평 작성자 활성화 여부 확인
        if (!clubMember.isActive()) {
            throw new GeneralException(ErrorStatus.CLUB_MEMBER_IS_NOT_ACTIVE);
        }

        // 2. 한줄평 생성
        BookReview bookReview = ClubConverter.fromBookReviewDTOToBookReview(request);
        bookReview.setClubMember(clubMember);
        bookReview.setMeeting(meeting);

        // 3. 미팅의 별점 합산
        meeting.addSumRate(bookReview.getRate());

        // 4. 한줄평 저장
        return bookReviewRepository.save(bookReview).getId();
    }

    @Override
    public Long updateBookReview(ClubMember clubMember, Meeting meeting, Long reviewId, BookShelfRequestDTO.BookReviewDTO request) {
        // 1. 한줄평 수정자 활성화 여부 확인
        if (!clubMember.isActive()) {
            throw new GeneralException(ErrorStatus.CLUB_MEMBER_IS_NOT_ACTIVE);
        }

        // 2. 한줄평 조회 및 존재 여부 확인
        BookReview bookReview = clubMeetingQueryService.validateBookReview(reviewId, meeting.getId());

        // 3. 한줄평 작성자와 수정자가 같은지 확인
        if (!bookReview.getClubMemberId().equals(clubMember.getId())) {
            throw new GeneralException(ErrorStatus.BOOK_REVIEW_FORBIDDEN);
        }

        // 4. 한줄평 수정
        double oldRate = bookReview.getRate();
        double newRate = request.getRate();

        bookReview.updateBookReview(
                request.getDescription(),
                request.getRate()
        );

        // 5. 별점이 변경된 경우에만 미팅의 별점 합산
        if (oldRate != newRate) {
            meeting.subtractSumRate(oldRate);
            meeting.addSumRate(newRate);
        }

        return bookReview.getId();
    }

    @Override
    public void deleteBookReview(ClubMember clubMember, Meeting meeting, Long reviewId) {
        // 1. 한줄평 삭제자 활성화 여부 확인
        if (!clubMember.isActive()) {
            throw new GeneralException(ErrorStatus.CLUB_MEMBER_IS_NOT_ACTIVE);
        }

        // 2. 한줄평 조회 및 존재 여부 확인
        BookReview bookReview = clubMeetingQueryService.validateBookReview(reviewId, meeting.getId());

        // 3. 한줄평 작성자와 삭제자가 같은지 확인
        if (!bookReview.getClubMemberId().equals(clubMember.getId())) {
            throw new GeneralException(ErrorStatus.BOOK_REVIEW_FORBIDDEN);
        }

        // 4. 미팅의 별점 합산에서 제외
        meeting.subtractSumRate(bookReview.getRate());

        // 5. 한줄평 삭제
        bookReview.removeClubMember();
        bookReview.removeMeeting();
    }
}
