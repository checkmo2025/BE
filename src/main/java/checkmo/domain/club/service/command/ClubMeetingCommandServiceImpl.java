package checkmo.domain.club.service.command;

import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.book.entity.Book;
import checkmo.domain.book.facade.BookCommandFacade;
import checkmo.domain.book.facade.BookQueryFacade;
import checkmo.domain.club.converter.ClubConverter;
import checkmo.domain.club.entity.Club;
import checkmo.domain.club.entity.ClubMember;
import checkmo.domain.club.entity.announcement.Notice;
import checkmo.domain.club.entity.meeting.*;
import checkmo.domain.club.repository.ClubRepository;
import checkmo.domain.club.repository.meeting.*;
import checkmo.domain.club.service.query.ClubMeetingQueryService;
import checkmo.domain.club.service.query.ClubMemberQueryService;
import checkmo.domain.club.service.query.ClubQueryService;
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
    private final BookCommandFacade bookCommandFacade;
    private final BookQueryFacade bookQueryFacade;

    private final ClubQueryService clubQueryService;
    private final ClubMemberQueryService clubMemberQueryService;
    private final ClubMeetingQueryService clubMeetingQueryService;

    private final ClubRepository clubRepository;
    private final MeetingRepository meetingRepository;
    private final TopicRepository topicRepository;
    private final TeamTopicRepository teamTopicRepository;
    private final BookReviewRepository bookReviewRepository;
    private final TeamRepository teamRepository;

    @Override
    public Long createMeeting(Long clubId, String memberId, MeetingRequestDTO.MeetingCreateRequestDTO request) {
        // 1. 검증
        Club club = clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);
        if (!clubMember.isStaff()) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }

        // 2. 책 저장 후 프록시 가져오기
        bookCommandFacade.saveBook(request.getBookInfo());
        Book proxyBook = bookQueryFacade.findBookReferenceById(request.getBookInfo().getIsbn());

        // 3. 미팅 생성 후 proxyBook 연결
        Meeting meeting = ClubConverter.fromMeetingCreateRequestDTOToMeeting(request, proxyBook);

        // 4. 공지 생성
        Notice notice = ClubConverter.fromMeetingToNotice(meeting);

        // 5. 연관관계 설정
        club.addMeeting(meeting); //영속성 컨텍스트 내 객체 상태 동기화
        meeting.addNotice(notice);

        // 6. 미팅 명시적 저장 -> 공지사항도 함께 저장됨
        return meetingRepository.save(meeting).getId();
    }

    @Override
    public Long updateMeeting(Long meetingId, String memberId, MeetingRequestDTO.MeetingUpdateRequestDTO request) {
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(meeting.getClubId(), memberId);
        if (!clubMember.isStaff()) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }

        meeting.updateMeeting(
                request.getTitle(),
                request.getMeetingTime(),
                request.getLocation(),
                request.getContent(),
                request.getGeneration(),
                request.getTag()
        );

        Notice newNotice = ClubConverter.fromMeetingToNotice(meeting);
        meeting.replaceNotice(newNotice);

        meetingRepository.save(meeting);

        return meeting.getId();
    }

    @Override
    public Long createTopic(String memberId, Long meetingId, BookShelfRequestDTO.TopicDTO request) {
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(meeting.getClubId(), memberId);

        Topic topic = ClubConverter.fromTopicDTOToTopic(request);

        meeting.addTopic(topic);
        clubMember.addTopic(topic);

        topicRepository.save(topic);
        return topic.getId();
    }

    @Override
    public Boolean selectOrCancelTopic(String memberId, Long meetingId, Long topicId, MeetingRequestDTO.TopicSelectionDTO request) {
        // 1. 유효성 검증 (meeting, clubMember, topic, team)
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        Team team = clubMeetingQueryService.validateTeam(meetingId, request.getTeamNumber());
        Topic topic = clubMeetingQueryService.validateTopic(topicId, meetingId);
        clubMemberQueryService.validateClubMember(meeting.getClubId(), memberId);

        // 2. 팀 발제가 존재하는지(선택된 상태인지) 확인
        Optional<TeamTopic> existingTeamTopic = teamTopicRepository.findByTeamIdAndTopicId(team.getId(), topicId);
        boolean isSelected = existingTeamTopic.isPresent();

        // 3. 요청과 상태가 같으면 무시
        if (request.getIsSelected() == isSelected) {
            return isSelected;
        }

        // 4. 상태 변경
        if (request.getIsSelected()) {
            // 4-1. 팀 발제 선택
            TeamTopic teamTopic = TeamTopic.builder().team(team).topic(topic).build();
            // 연관관계 설정
            team.addTeamTopic(teamTopic);
            topic.addTeamTopic(teamTopic);
            try {
                teamTopicRepository.saveAndFlush(teamTopic);
            } catch (DataIntegrityViolationException e) {
                // 다른 쓰레드가 먼저 팀 발제를 선택한 경우, 선택 성공으로 간주
                team.removeTeamTopic(teamTopic);
                topic.removeTeamTopic(teamTopic);
                return true;
            }
            return true;
        } else {
            // 4-2. 팀 발제 선택 취소
            try {
                TeamTopic teamTopic = existingTeamTopic.get();
                // 연관관계 해제 및 orphanRemoval로 삭제 처리
                team.removeTeamTopic(teamTopic);
                topic.removeTeamTopic(teamTopic);
                teamTopicRepository.flush();
                return false;
            } catch (OptimisticLockingFailureException e) {
                // 다른 트랜잭션이 이미 삭제했거나 수정한 경우, 선택 해제 성공으로 간주
                return false;
            }
        }
    }

    @Override
    public Long updateTopic(String memberId, Long meetingId, Long topicId, BookShelfRequestDTO.TopicDTO request) {
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(meeting.getClubId(), memberId);

        Topic topic = clubMeetingQueryService.validateTopic(topicId, meetingId);
        if (!topic.isOwnedBy(clubMember)) {
            throw new GeneralException(ErrorStatus.TOPIC_FORBIDDEN);
        }

        topic.updateTopic(
                request.getDescription()
        );

        topicRepository.save(topic);
        return topic.getId();
    }

    @Override
    public void deleteTopic(String memberId, Long meetingId, Long topicId) {
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(meeting.getClubId(), memberId);

        Topic topic = clubMeetingQueryService.validateTopic(topicId, meetingId);
        if (!topic.isOwnedBy(clubMember)) {
            throw new GeneralException(ErrorStatus.TOPIC_FORBIDDEN);
        }

        meeting.removeTopic(topic);
        clubMember.removeTopic(topic);
    }

    @Override
    public void manageTeam(String memberId, Long meetingId, MeetingRequestDTO.TeamManageDTO request) {
        // 1. 미팅과 클럽 멤버 검증
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(meeting.getClubId(), memberId);
        if (!clubMember.isStaff()) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }

        // 2. 요청 teamNumber와 nicknameList 검증 및 정리
        Map<Integer, List<String>> requestTeamNumberToNicknameList = new HashMap<>();
        Set<Integer> requestTeamNumbers = new HashSet<>();
        Set<String> requestNicknames = new LinkedHashSet<>();

        for (MeetingRequestDTO.TeamMemberDTO dto : request.getTeamMemberDTOList()) {
            Integer num = dto.getTeamNumber();

            if (!requestTeamNumbers.add(num)) { // 요청 teamNumber 중 teamNumber가 이미 존재하면 예외 발생
                throw new GeneralException(ErrorStatus.TEAM_NUMBER_DUPLICATED_REQUEST, num.toString()); //TODO: 팀 넘버 포함 예외 메시지
            }

            List<String> names = new ArrayList<>(new LinkedHashSet<>(dto.getNicknameList())); // 닉네임 리스트의 중복 제거
            requestTeamNumberToNicknameList.put(num, names);
            requestNicknames.addAll(names);
        }

        // 3. 해당 미팅의 기존 팀들 조회 후 teamNumber -> Team Map (TeamTopic이 유지되도록 Team은 유지)
        List<Team> existingTeams = teamRepository.findTeamsByMeetingId(meetingId);
        Map<Integer, Team> existingTeamNumberToTeam = existingTeams.stream()
                .collect(Collectors.toMap(Team::getTeamNumber, t -> t));

        // 4. 요청에 있는데 아직 없는 teamNumber는 Team 생성
        for (Integer teamNumber : requestTeamNumbers) {
            if (!existingTeamNumberToTeam.containsKey(teamNumber)) {
                Team team = Team.builder()
                        .teamNumber(teamNumber)
                        .build();
                meeting.addTeam(team);
                existingTeams.add(team);
                existingTeamNumberToTeam.put(teamNumber, team);
            }
        }

        // 5. 요청에는 없는데 존재하는 teamNumber는 Team 삭제
        List<Team> toDeleteTeams = existingTeams.stream()
                .filter(t -> !requestTeamNumbers.contains(t.getTeamNumber()))
                .toList();

        // 미팅과의 양방향 연관 끊기 -> orphanRemoval이 true이므로 미팅이 flush될 때 Team도 삭제됨
        toDeleteTeams.forEach(meeting::removeTeam);

        // 기존 팀, 기존 teamNumber -> Team Map 메모리 컬렉션/맵 동기화
        existingTeams.removeAll(toDeleteTeams);
        existingTeamNumberToTeam.keySet().removeAll(toDeleteTeams.stream()
                .map(Team::getTeamNumber)
                .collect(Collectors.toSet()));

        // 6. 기존 MemberTeam orphanRemoval = true 삭제
        if (!existingTeams.isEmpty()) {
            existingTeams.forEach(Team::clearMemberTeams);
            // 이때 삭제되는 memberTeam의 clubMember도 양방향 연관관계 설정이 다시 필요하지 않나?
        }

        // 7. 닉네임 → memberId → ClubMember 일괄 매핑
        Map<String, ClubMember> nicknameToClubMember = clubMemberQueryService.getNicknameToClubMember(meeting.getClubId(), requestNicknames);

        // 8. 요청대로 MemberTeam 배치 재생성
        for (Map.Entry<Integer, List<String>> e : requestTeamNumberToNicknameList.entrySet()) {
            Team team = existingTeamNumberToTeam.get(e.getKey());
            for (String nick : e.getValue()) {
                ClubMember cm = nicknameToClubMember.get(nick);
                MemberTeam mt = MemberTeam.builder().build();

                team.addMemberTeam(mt);
                cm.addMemberTeam(mt);
            }
        }

        // 9. 기존 팀과 새로 생성된 Team을 명시적으로 저장 (내부적으로 MemberTeam도 저장됨)
        meetingRepository.save(meeting);
        teamRepository.saveAll(existingTeams);
    }

    @Override
    public Long createBookReview(String memberId, Long meetingId, BookShelfRequestDTO.BookReviewDTO request) {
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(meeting.getClubId(), memberId);

        BookReview bookReview = ClubConverter.fromBookReviewDTOToBookReview(request);

        clubMember.addBookReview(bookReview);
        meeting.addBookReview(bookReview);

        bookReviewRepository.save(bookReview);

        meeting.addSumRate(bookReview.getRate());
        return bookReview.getId();
    }

    @Override
    public Long updateBookReview(String memberId, Long meetingId, Long reviewId, BookShelfRequestDTO.BookReviewDTO request) {
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(meeting.getClubId(), memberId);
        BookReview bookReview = bookReviewRepository.findById(reviewId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.BOOK_REVIEW_NOT_FOUND));
        if (!bookReview.getClubMemberId().equals(clubMember.getId())) {
            throw new GeneralException(ErrorStatus.BOOK_REVIEW_FORBIDDEN);
        }

        double oldRate = bookReview.getRate();
        double newRate = request.getRate();

        bookReview.updateBookReview(
                request.getDescription(),
                request.getRate()
        );

        bookReviewRepository.save(bookReview);

        // 별점 업데이트
        if (oldRate != newRate) {
            meeting.subtractSumRate(oldRate);
            meeting.addSumRate(newRate);
        }
        return bookReview.getId();
    }

    @Override
    public void deleteBookReview(String memberId, Long meetingId, Long reviewId) {
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(meeting.getClubId(), memberId);
        BookReview bookReview = bookReviewRepository.findById(reviewId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.BOOK_REVIEW_NOT_FOUND));
        if (!bookReview.getClubMemberId().equals(clubMember.getId())) {
            throw new GeneralException(ErrorStatus.BOOK_REVIEW_FORBIDDEN);
        }

        meeting.subtractSumRate(bookReview.getRate());

        clubMember.removeBookReview(bookReview);
        meeting.removeBookReview(bookReview);
    }
}
