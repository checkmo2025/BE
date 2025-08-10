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
import checkmo.domain.club.repository.meeting.BookReviewRepository;
import checkmo.domain.club.repository.meeting.MeetingRepository;
import checkmo.domain.club.repository.meeting.TeamTopicRepository;
import checkmo.domain.club.repository.meeting.TopicRepository;
import checkmo.domain.club.service.query.ClubMeetingQueryService;
import checkmo.domain.club.service.query.ClubMemberQueryService;
import checkmo.domain.club.service.query.ClubQueryService;
import checkmo.domain.club.web.dto.bookshelf.BookShelfRequestDTO;
import checkmo.domain.club.web.dto.meeting.MeetingRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
            teamTopicRepository.save(teamTopic);
            return true;
        } else {
            // 4-2. 팀 발제 선택 취소
            TeamTopic teamTopic = existingTeamTopic.get();
            // 연관관계 해제 및 orphanRemoval로 삭제 처리
            team.removeTeamTopic(teamTopic);
            topic.removeTeamTopic(teamTopic);
            return false;
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
    public Long manageTeam(String memberId, Long meetingId, MeetingRequestDTO.TeamManageDTO request) {
        return 0L;
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

        bookReviewRepository.delete(bookReview);
    }
}
