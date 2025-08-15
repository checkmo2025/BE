package checkmo.domain.club.facade;

import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.book.facade.BookQueryFacade;
import checkmo.domain.club.converter.ClubConverter;
import checkmo.domain.club.entity.Club;
import checkmo.domain.club.entity.ClubMember;
import checkmo.domain.club.entity.meeting.*;
import checkmo.domain.club.repository.ClubRepository;
import checkmo.domain.club.service.query.*;
import checkmo.domain.club.web.dto.MembershipResponseDTO;
import checkmo.domain.club.web.dto.bookshelf.BookShelfResponseDTO;
import checkmo.domain.club.web.dto.club.ClubResponseDTO;
import checkmo.domain.club.web.dto.meeting.MeetingResponseDTO;
import checkmo.domain.member.facade.MemberQueryFacade;
import checkmo.global.dto.BookSharedDTO;
import checkmo.global.dto.ClubSharedDTO;
import checkmo.global.dto.MemberSharedDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubQueryFacadeImpl implements ClubQueryFacade {

    // 페이징 기본 크기 상수
    private static final int DEFAULT_PAGE_SIZE = 10;
    private final ClubMeetingQueryService clubMeetingQueryService;
    private final ClubMemberQueryService clubMemberQueryService;
    private final ClubQueryService clubQueryService;
    private final ClubBookRecommendQueryService clubBookRecommendQueryService;
    private final ClubCommunicationQueryService clubCommunicationQueryService;
    private final ClubRepository clubRepository;
    private final MemberQueryFacade memberQueryFacade;
    private final BookQueryFacade bookQueryFacade;

    /**
     * 특정 회원이 가입한 모임 목록을 조회합니다. (내부용)
     *
     * 피그마 참고 페이지 : #독서모임 - 내 모임 바로가기
     *
     * @param memberId 회원 ID -> 로그인한 회원의 ID를 사용
     * @return 내가 가입한 독서 클럽 목록 DTO
     */
    @Override
    public ClubResponseDTO.MyClubListDTO getMyClubList(String memberId) {

        // 1. 회원이 가입한 모임 목록 조회
        List<ClubSharedDTO.MyClubInfo> myClubs = clubMemberQueryService.getMyClubList(memberId).getClubList();

        // 2. 모임 정보 DTO로 변환
        List<ClubResponseDTO.ClubInfoDTO> clubInfoDTOList = myClubs.stream()
                .map(ClubConverter::toClubInfoDTOFromMyClubInfo)
                .toList();

        // 3. 최종 DTO 반환
        return ClubResponseDTO.MyClubListDTO.builder()
                .clubList(clubInfoDTOList)
                .build();
    }

    /**
     * 특정 회원이 가입한 모임 목록을 조회합니다. (내부용)
     *
     * 피그마 참고 페이지 : #마이페이지
     *
     * @param memberId 회원 ID -> 로그인한 회원의 ID를 사용
     * @return 내가 가입한 독서 클럽 목록 DTO
     */
    @Override
    public ClubResponseDTO.MyPageClubListDTO getMyPageClubList(String memberId) {

        // 1. 회원이 가입한 모임 목록 조회
        List<ClubResponseDTO.ClubDetailResponseDTO> myClubs =
                clubMemberQueryService.getMyPageClubList(memberId).getClubList();

        // 2. MyPageClubListDTO로 감싸서 반환
        return ClubResponseDTO.MyPageClubListDTO.builder()
                .clubList(myClubs)
                .build();
    }

    @Override
    public ClubSharedDTO.MyClubList getMyClubListForShare(String memberId) {
        return clubMemberQueryService.getMyClubList(memberId);
    }

    /**
     * ClubQueryService
     * 조건에 맞는 독서 모임 목록을 검색합니다. (내부용)
     *
     * @param memberId 요청자 회원 ID (해당 클럽 회원인지 확인용)
     * @param keyword 검색 키워드 (모임명 등)
     * @param name 클럽명 필터링 여부
     * @param region 지역 필터링 여부
     * @param participants 대상 필터링 여부
     * @param cursorId 페이징 커서 ID
     * @return 검색된 모임 목록 DTO
     */
    @Override
    public ClubResponseDTO.ClubListDTO getClubList(String memberId, String keyword, int name, int region, int participants, Long cursorId, Integer size) {

        // 1. 커서 초기화
        Long cursor = (cursorId == null || cursorId == 0L) ? Long.MAX_VALUE : cursorId;

        // 2. 페이지 크기 결정 (size가 null 또는 0 이하이면 기본값 사용)
        int pageSize = (size == null || size <= 0) ? DEFAULT_PAGE_SIZE : size;
        Pageable pageable = PageRequest.of(0, pageSize);

        // 2. 클럽 리스트 조회
        List<ClubResponseDTO.ClubWithMyStatusDTO> clubList = clubQueryService.getClubList(memberId, keyword, name, region, participants, cursor, pageable);

        // 3. 페이징 처리
        boolean hasNext = clubList.size() > pageSize;  // clubList의 크기가 PAGE_SIZE보다 크면 다음 페이지가 존재한다고 판단
        if (hasNext) {
            clubList = clubList.subList(0, pageSize); // 다음 페이지를 위해 마지막은 제거
        }
        Long nextCursor = hasNext && !clubList.isEmpty() ?
                clubList.get(clubList.size() - 1).getClub().getClubId() : null; // 다음 커서 설정

        // 4. 최종 DTO 변환
        return ClubConverter.toClubListDTO(clubList, hasNext, nextCursor);
    }

    /**
     * ClubQueryService
     * 독서 모임의 상세 정보를 조회합니다. (내부용)
     *
     * @param clubId 조회할 모임 ID
     * @param memberId 조회자 회원 ID
     * @return 모임 상세 정보 DTO
     */
    @Override
    public ClubResponseDTO.ClubDetailDTO getClubInfo(Long clubId, String memberId) {
        return clubQueryService.getClubInfo(clubId, memberId);
    }

    /**
     * ClubQueryService
     * 특정 상태의 모임 회원 목록을 조회합니다. (내부용)
     *
     * @param clubId 모임 ID
     * @param memberId 요청자(운영진) 회원 ID
     * @param clubMemberStatus 조회할 회원 상태
     * @param cursorId 페이징 커서 ID
     * @return 해당 상태의 회원 목록 DTO
     */
    @Override
    public ClubResponseDTO.ClubMemberListDTO getClubMemberListByStatus(Long clubId, String memberId, String clubMemberStatus, Long cursorId, Integer size) {
        // 1. 클럽 멤버 리스트 조회
        clubQueryService.validateClub(clubId);
        ClubMember requester = clubMemberQueryService.validateClubMember(clubId, memberId);
        if (!requester.isStaff()) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }

        // 2. 만약 size가 null이면 기본값 사용 후 size+1만큼 조회
        if (size == null) size = DEFAULT_PAGE_SIZE;
        List<ClubMember> members = clubMemberQueryService.getClubMemberListByStatus(clubId, clubMemberStatus, cursorId, size + 1);

        // 3. 페이징 처리
        boolean hasNext = members.size() > size;
        if (hasNext) {
            members = members.subList(0, size);
        }
        Long nextCursor = hasNext ? members.getLast().getId() : null;

        // 4. memberId 추출
        List<String> memberIds = extractMemberIds(members);

        // 5. 기본 정보 배치 조회
        Map<String, MemberSharedDTO.BasicInfoDTO> memberInfoMap = memberQueryFacade.getMemberBasicInfoMapForShare(memberIds);

        // 6. DTO 변환
        List<ClubResponseDTO.ClubMemberDTO> dtoList = members.stream()
                .map(cm -> {
                    MemberSharedDTO.BasicInfoDTO memberInfo = memberInfoMap.get(cm.getMemberId());
                    return ClubConverter.toClubMemberDTO(cm, memberInfo);
                })
                .toList();

        return ClubConverter.toClubMemberListDTO(dtoList, hasNext, nextCursor);
    }

    /**
     * ClubQueryService
     * 모임 이름의 중복 여부를 확인합니다. (내부용)
     *
     * @param clubName 확인할 모임 이름
     * @return 중복 시 true
     */
    @Override
    public boolean isDuplicateClubName(String clubName) {
        return clubQueryService.isDuplicateClubName(clubName);
    }

    /**
     * ClubQueryService
     * 모임의 전체 공지사항 목록을 최신순으로 조회합니다. (내부용)
     *
     * @param clubId 모임 ID
     * @param memberId 조회자 회원 ID
     * @param cursorId 페이징 커서 ID
     * @param onlyImportant 중요 공지사항만 조회할지 여부
     * @param size 조회할 개수
     * @return 전체 공지사항 목록 DTO
     */
    @Override
    public ClubResponseDTO.ClubNoticeListDTO getLatestNotices(Long clubId, String memberId, Long cursorId, boolean onlyImportant, Integer size) {

        // 1. 검증 -> 소식은 클럽에 속한 사람만 조회할 수 있음
        clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);
        boolean isStaff = clubMember.isStaff();

        // 2. 커서 초기화
        Long cursor = (cursorId == null || cursorId == 0L) ? Long.MAX_VALUE : cursorId;

        // 3. 페이지 크기 결정 (size가 null 또는 0 이하이면 기본값 사용)
        int pageSize = (size == null || size <= 0) ? DEFAULT_PAGE_SIZE : size;
        Pageable pageable = PageRequest.of(0, pageSize + 1);

        // 3. 공지(일반, 모임) + 투표 조회 및 변환
        List<ClubResponseDTO.NoticeItem> noticeItems = clubCommunicationQueryService.getAllNoticesAndVotes(clubId, onlyImportant, cursor, pageable);

        // 4. 페이징
        boolean hasNext = noticeItems.size() > pageSize;
        if (hasNext) {
            noticeItems = noticeItems.subList(0, pageSize);  // pageSize 만큼만 남기기
        }
        Long nextCursor = hasNext && noticeItems.size() >= pageSize
                ? noticeItems.get(pageSize - 1).getId()
                : null;

        return ClubConverter.toClubNoticeListDTO(noticeItems, hasNext, nextCursor, isStaff);
    }

    @Override
    public ClubResponseDTO.MemberNoticeListDTO getNoticeForHome(String memberId, Long cursorId, boolean onlyImportant, Integer size) {

        // 1. 커서 초기화
        Long cursor = (cursorId == null || cursorId == 0L) ? Long.MAX_VALUE : cursorId;

        // 2. 페이지 크기 결정 (size가 null 또는 0 이하이면 기본값 사용)
        int pageSize = (size == null || size <= 0) ? DEFAULT_PAGE_SIZE : size;
        Pageable pageable = PageRequest.of(0, pageSize + 1);

        // 3. 공지(일반, 모임) + 투표 조회 및 변환
        List<ClubResponseDTO.ClubNoticeWithClubDTO> memberNoticeItems = clubCommunicationQueryService.getMemberNoticesAndVotes(memberId, onlyImportant, cursor, pageable);

        // 4. 페이징
        boolean hasNext = memberNoticeItems.size() > pageSize;
        if (hasNext) {
            memberNoticeItems = memberNoticeItems.subList(0, pageSize);  // pageSize 만큼만 남기기
        }
        Long nextCursor = hasNext && memberNoticeItems.size() >= pageSize
                ? memberNoticeItems.get(pageSize - 1).getNotice().getId()
                : null;

        return ClubConverter.toMemberNoticeListDTO(memberNoticeItems, hasNext, nextCursor);
    }

    /**
     * ClubQueryService
     * 공지사항(투표 포함)의 상세 정보를 조회합니다. (내부용)
     *
     * @param clubId 모임 ID
     * @param noticeId 조회할 공지사항 ID
     * @return 공지사항 상세 정보 DTO
     */
    @Override
    public ClubResponseDTO.ClubNoticeDetailDTO getNoticeDetail(Long clubId, Long noticeId, String tag, String memberId) {
        return clubCommunicationQueryService.getNoticeOrVoteDetail(clubId, noticeId, tag, memberId);
    }

    /**
     * ClubBookRecommendQueryService
     * 모임의 추천 책 목록을 조회합니다. (내부용)
     *
     * @param clubId 모임 ID
     * @param cursorId 페이징 커서 ID
     * @return 추천 책 목록 DTO
     */
    @Override
    public ClubResponseDTO.BookRecommendListDTO getRecommendedBooks(Long clubId, Long cursorId, String memberId) {

        // 1. 클럽 검증
        clubQueryService.validateClub(clubId);

        // 2. 클럽 멤버 검증
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        // 3. 커서 초기화 (페이징 로직)
        Long cursor = (cursorId == null || cursorId == 0L) ? Long.MAX_VALUE : cursorId;

        // 4. ServiceImpl에서 순수 엔티티 조회
        var bookRecommends = clubBookRecommendQueryService.getRecommendedBooks(clubId, cursor, memberId);

        // 5. 외부 도메인 정보 조합 (Facade에서 처리)
        var currentMemberNickname = memberQueryFacade.getMemberBasicInfoForShare(memberId).getNickname();

        var dtoList = bookRecommends.stream()
                .map(bookRecommend -> {
                    var bookInfo = bookQueryFacade.getBookBasicInfoForShare(bookRecommend.getBookId());
                    var authorInfo = memberQueryFacade.getMemberBasicInfoForShare(bookRecommend.getClubMember().getMemberId());
                    return ClubConverter.toBookRecommendDetailDTO(bookRecommend, bookInfo, authorInfo, currentMemberNickname, clubMember.isStaff());
                }).toList();

        // 6. 페이징 처리 (Facade에서)
        Long lastId = bookRecommends.isEmpty() ? null : bookRecommends.get(bookRecommends.size() - 1).getId();
        boolean hasNext = clubBookRecommendQueryService.hasNextPage(clubId, lastId);

        return ClubConverter.toBookRecommendListDTO(dtoList, hasNext, lastId);
    }

    /**
     * ClubBookRecommendQueryService
     * 추천 책의 상세 정보를 조회합니다. (내부용)
     *
     * @param clubId 모임 ID
     * @param bookRecommendId 추천 책 ID
     * @return 추천 책 상세 정보 DTO
     */
    @Override
    public ClubResponseDTO.BookRecommendDetailDTO getRecommendedBookDetail(Long clubId, Long bookRecommendId, String memberId) {
        return clubBookRecommendQueryService.getRecommendedBookDetail(clubId, memberId, bookRecommendId);
    }

    @Override
    public BookShelfResponseDTO.BookShelfListDTO getBookShelfList(Long clubId, Long cursorId, Integer size, Integer generation, String memberId) {
        clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        List<Meeting> meetings = clubMeetingQueryService.getBookShelfList(clubId, generation, cursorId, size + 1, memberId);
        boolean hasNext = meetings.size() > size;
        if (hasNext) {
            meetings = meetings.subList(0, size);
        }
        Long nextCursor = hasNext ? meetings.getLast().getId() : null;

        List<BookShelfResponseDTO.BookShelfInfoDTO> bookShelfInfoDTOS = meetings.stream()
                .map(meeting ->
                        ClubConverter.fromMeetingAndBookSharedDTOToBookShelfInfoDTO(
                                meeting,
                                bookQueryFacade.getBookBasicInfoForShare(meeting.getBookId())
                        )
                )
                .toList();

        MembershipResponseDTO.MembershipDTO membershipDTO = ClubConverter.fromClubMembertoMembershipDTO(clubMember);
        return ClubConverter.fromBookShelfInfoDTOListToBookShelfListDTO(bookShelfInfoDTOS, hasNext, nextCursor, membershipDTO);
    }

    @Override
    // TODO: getBookShelftDetail, findTopicsByMeeting 간 중복 제거
    public BookShelfResponseDTO.BookShelfDetailDTO getBookShelfDetail(Long meetingId, String memberId) {
        final Integer TOPIC_SIZE = 3;

        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(meeting.getClubId(), memberId);

        List<Topic> topics = clubMeetingQueryService.findTopicsWithClubMemberByMeeting(meetingId, null, TOPIC_SIZE + 1);

        boolean hasNext = topics.size() > TOPIC_SIZE;
        if (hasNext) {
            topics = topics.subList(0, TOPIC_SIZE);
        }
        Long nextCursor = hasNext ? topics.getLast().getId() : null;

        List<String> authorIds = extractMemberIdsFromTopics(topics);

        Map<String, MemberSharedDTO.BasicInfoDTO> authorInfoMap =
                memberQueryFacade.getMemberBasicInfoMapForShare(authorIds);

        List<BookShelfResponseDTO.TopicDTO> topicListDTOs = ClubConverter.fromTopicListAndAuthorInfoMapAndMemberIdToTopicDTOList(
                topics,
                authorInfoMap,
                memberId
        );

        MembershipResponseDTO.MembershipDTO membershipDTO = ClubConverter.fromClubMembertoMembershipDTO(clubMember);
        return ClubConverter.fromBookShelfDTOToBookShelfDetailDTO(
                meeting,
                bookQueryFacade.getBookDetailInfoForShare(meeting.getBookId()),
                ClubConverter.fromTopicDTOListToTopicListDTOForBookshelf(topicListDTOs, hasNext, nextCursor, null),
                membershipDTO
        );
    }

    @Override
    public BookShelfResponseDTO.TopicListDTO findTopicsByMeeting(Long meetingId, Long cursorId, Integer size, String memberId) {
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(meeting.getClubId(), memberId);

        List<Topic> topics = clubMeetingQueryService.findTopicsWithClubMemberByMeeting(meetingId, cursorId, size + 1);

        boolean hasNext = topics.size() > size;
        if (hasNext) {
            topics = topics.subList(0, size);
        }
        Long nextCursor = hasNext ? topics.getLast().getId() : null;

        List<String> authorIds = extractMemberIdsFromTopics(topics);

        Map<String, MemberSharedDTO.BasicInfoDTO> authorInfoMap =
                memberQueryFacade.getMemberBasicInfoMapForShare(authorIds);

        List<BookShelfResponseDTO.TopicDTO> topicListDTOs = ClubConverter.fromTopicListAndAuthorInfoMapAndMemberIdToTopicDTOList(
                topics,
                authorInfoMap,
                memberId
        );

        MembershipResponseDTO.MembershipDTO membershipDTO = ClubConverter.fromClubMembertoMembershipDTO(clubMember);
        return ClubConverter.fromTopicDTOListToTopicListDTOForBookshelf(topicListDTOs, hasNext, nextCursor, membershipDTO);
    }

    @Override
    public BookShelfResponseDTO.BookReviewListDTO getBookReviewList(Long meetingId, Long lastReviewId, int size, String memberId) {
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(meeting.getClubId(), memberId);

        List<BookReview> bookReviews = clubMeetingQueryService.findBookReviewsByMeeting(meetingId, lastReviewId, size);

        boolean hasNext = bookReviews.size() > size;
        if (hasNext) {
            bookReviews = bookReviews.subList(0, size);
        }
        Long nextCursor = hasNext ? bookReviews.get(bookReviews.size() - 1).getId() : null;

        List<String> authorIds = extractMemberIdsFromBookReviews(bookReviews);
        Map<String, MemberSharedDTO.BasicInfoDTO> authorInfoMap = memberQueryFacade.getMemberBasicInfoMapForShare(authorIds);

        List<BookShelfResponseDTO.BookReviewDTO> bookReviewDTOList = mapBookReviewsAndAuthorInfoToDTOs(bookReviews, authorInfoMap);

        MembershipResponseDTO.MembershipDTO membershipDTO = ClubConverter.fromClubMembertoMembershipDTO(clubMember);
        return ClubConverter.fromBookReviewDTOListToBookReviewListDTO(bookReviewDTOList, hasNext, nextCursor, membershipDTO);
    }

    private List<BookShelfResponseDTO.BookReviewDTO> mapBookReviewsAndAuthorInfoToDTOs(
            List<BookReview> bookReviews,
            Map<String, MemberSharedDTO.BasicInfoDTO> authorInfoMap
    ) {
        return bookReviews.stream()
                .map(review -> ClubConverter.fromBookReviewAndMemberSharedDTOToBookReviewDTO(
                        review,
                        authorInfoMap.get(review.getClubMember().getMemberId())
                ))
                .toList();
    }

    @Override
    public MeetingResponseDTO.MeetingListDTO getMeetingsByClub(Long clubId, Long cursorId, Integer size, String memberId) {
        clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        List<Meeting> meetings = clubMeetingQueryService.findMeetingsByClubAndCursor(clubId, cursorId, size + 1);
        boolean hasNext = meetings.size() > size;
        if (hasNext) {
            meetings = meetings.subList(0, size);
        }
        Long nextCursor = hasNext ? meetings.getLast().getId() : null;

        List<String> bookIds = extractBookIdsFromMeetings(meetings);
        Map<String, BookSharedDTO.BasicInfoDTO> bookBasicInfoMap = bookQueryFacade.getBookBasicInfoMapForShare(bookIds);

        List<MeetingResponseDTO.MeetingInfoDTO> meetingInfoDTOList = mapMeetingsWithBookBasicInfoToDTOs(meetings, bookBasicInfoMap);
        MembershipResponseDTO.MembershipDTO membershipDTO = ClubConverter.fromClubMembertoMembershipDTO(clubMember);
        return ClubConverter.fromMeetingInfoDTOListToMeetingListDTO(meetingInfoDTOList, hasNext, nextCursor, membershipDTO);
    }

    private List<MeetingResponseDTO.MeetingInfoDTO> mapMeetingsWithBookBasicInfoToDTOs(
            List<Meeting> meetings,
            Map<String, BookSharedDTO.BasicInfoDTO> bookBasicInfoMap
    ) {
        return meetings.stream()
                .map(meeting -> ClubConverter.fromMeetingAndBookSharedDTOToMeetingInfoDTO(
                        meeting,
                        bookBasicInfoMap.get(meeting.getBookId())
                ))
                .toList();
    }

    @Override
    public MeetingResponseDTO.MeetingDetailDTO findMeetingDetailById(Long meetingId, String memberId) {
        // 1. 미팅과 클럽 멤버 검증
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(meeting.getClubId(), memberId);

        // 2. [발제 전체보기 - 미리보기] 발제 최신순 상위 4개 토픽 리스트 조회
        List<Topic> topics = clubMeetingQueryService.findTopicsWithClubMemberByMeeting(meetingId, null, 4);

        // 3. [발제 전체보기 - 미리보기] TeamTopic과 Team 배치 조회
        List<Long> topicIds = extractTopicIds(topics);
        Map<Long, List<Integer>> teamTopicsWithTeamByTopicIds = clubMeetingQueryService.findTeamTopicsWithTeamByTopicIds(topicIds);

        // 4. [토론 x조 - 미리보기] 해당하는 미팅의 존재하는 모든 팀 조회
        List<Team> teams = clubMeetingQueryService.findTeamsByMeeting(meetingId);

        // 5. [토론 x조 - 미리보기] 모든 팀의 발제 등록순 상위 4개 토픽 조회
        Map<Integer, List<TeamTopic>> teamNumberToTeamTopics = teams.stream()
                .collect(Collectors.toMap(
                        Team::getTeamNumber, // key: 팀 번호
                        team -> clubMeetingQueryService.findTeamTopicsWithTopicAndClubMemberByTeamId(team.getId(), 4) //value : 해당 팀의 발제 최신순 상위 4개 팀 토픽 리스트
                ));

        // 6. 조회한 모든 발제(topics와 teamTopics)의 작성자 id를 중복 없이 리스트 조회
        List<String> authorIds1 = extractMemberIdsFromTopics(topics);
        List<String> authorIds2 = extractMemberIdsFromTeamTopics(teamNumberToTeamTopics);
        List<String> authorIds = Stream.concat(authorIds1.stream(), authorIds2.stream())
                .distinct()
                .toList();

        // 7. 발제의 작성자 정보 배치 조회
        Map<String, MemberSharedDTO.BasicInfoDTO> authorInfoMap = memberQueryFacade.getMemberBasicInfoMapForShare(authorIds);

        MembershipResponseDTO.MembershipDTO membershipDTO = ClubConverter.fromClubMembertoMembershipDTO(clubMember);

        // 7. DTO 변환
        return ClubConverter.fromMeetingAndBookSharedDTOEtcToMeetingDetailDTO(
                meeting, bookQueryFacade.getBookBasicInfoForShare(meeting.getBookId()), // -> MeetingInfoDTO
                topics, teamTopicsWithTeamByTopicIds, // -> List<TopicDTO>
                teams, teamNumberToTeamTopics, // -> List<TeamTopicDTO>
                authorInfoMap, // -> List<TopicDTO>, List<TeamTopicDTO> 작성자 정보
                membershipDTO
        );
    }

    @Override
    public MeetingResponseDTO.TopicDTOList findMeetingTopicsWithTeam(Long meetingId, String memberId) {
        // 1. 미팅과 클럽 멤버 검증
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(meeting.getClubId(), memberId);

        // 2. 토픽 리스트 조회
        List<Topic> topics = clubMeetingQueryService.findTopicsWithClubMemberByMeeting(meetingId, null, null);

        // 3. 토픽 작성자 정보 배치 조회
        List<String> authorIds = extractMemberIdsFromTopics(topics);
        Map<String, MemberSharedDTO.BasicInfoDTO> authorInfoMap = memberQueryFacade.getMemberBasicInfoMapForShare(authorIds);

        // 4. TeamTopic과 Team 배치 조회
        List<Long> topicIds = extractTopicIds(topics);
        Map<Long, List<Integer>> topicIdToSelectTeamNumbers = clubMeetingQueryService.findTeamTopicsWithTeamByTopicIds(topicIds);

        MembershipResponseDTO.MembershipDTO membershipDTO = ClubConverter.fromClubMembertoMembershipDTO(clubMember);

        // 5. MeetingResponseDTO.TopicListDTO 변환
        List<MeetingResponseDTO.TopicDTO> topicDTOList = ClubConverter.fromTopicListAndTopicSelectionAndMemberSharedDTOToTopicDTOList(
                topics,
                authorInfoMap,
                topicIdToSelectTeamNumbers
        );

        return ClubConverter.fromTopicDTOListAndMembershipDTOToTopicListDTO(
                topicDTOList,
                membershipDTO
        );
    }

    public MeetingResponseDTO.TeamTopicDTO findMeetingTopicsByTeam(Long meetingId, Integer teamNumber, String memberId) {
        // 1. 미팅과 클럽 멤버, 팀 검증
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(meeting.getClubId(), memberId);
        Team team = clubMeetingQueryService.validateTeam(meetingId, teamNumber);

        // 2. 팀 토픽 > 토픽 > 클럽 멤버 정보 전체 조회
        List<TeamTopic> teamTopics = clubMeetingQueryService.findTeamTopicsWithTopicAndClubMemberByTeamId(team.getId(), null);

        // 3. 토픽 작성자 정보 배치 조회
        List<String> authorIds = extractMemberIdsFromTeamTopics(teamTopics);
        Map<String, MemberSharedDTO.BasicInfoDTO> authorInfoMap = memberQueryFacade.getMemberBasicInfoMapForShare(authorIds);

        // 4. TeamTopicDTO로 변환
        List<MeetingResponseDTO.TopicDTO> topicDTOList = ClubConverter.fromTopicListAndTopicSelectionAndMemberSharedDTOToTopicDTOList(
                extractTopicFromTeamTopics(teamTopics),
                authorInfoMap,
                Map.of() // 팀 토픽은 팀 번호가 필요없으니까 빈 Map 전달
        );

        MembershipResponseDTO.MembershipDTO membershipDTO = ClubConverter.fromClubMembertoMembershipDTO(clubMember);

        return ClubConverter.fromTopicDTOListToTeamTopicDTO(teamNumber, topicDTOList, membershipDTO);
    }

    @Override
    public MeetingResponseDTO.CalendarMeetingDTO getClubMeetingCalendar(Long clubId, int year, int month, String memberId) {
        return clubMeetingQueryService.getClubMeetingByYearAndMonth(clubId, year, month, memberId);
    }

    @Override
    public MeetingResponseDTO.MeetingMemberListDTO findMeetingMembersByMeeting(Long meetingId, Long cursorId, Integer size, String memberId) {
        // 1. 미팅, 클럽 멤버 검증
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(meeting.getClubId(), memberId);
        if (!clubMember.isStaff()) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }

        // 2. 클럽의 회원 조회 및 페이징 처리 (이때 PENDING이나 BLOCKED 상태는 제외하고 STAFF나 MEMBER만 조회)
        List<ClubMember> clubMembers = clubMemberQueryService.getClubMemberListByStatus(meeting.getClubId(), "ACTIVE", cursorId, size + 1);
        boolean hasNext = clubMembers.size() > size;
        if (hasNext) {
            clubMembers = clubMembers.subList(0, size);
        }
        Long nextCursor = hasNext ? clubMembers.getLast().getId() : null;

        // 3. 클럽 멤버에 대한 정보 배치 조회 (ClubMember의 memberId로 MemberSharedDTO.BasicInfoDTO 조회)
        List<String> memberIds = extractMemberIdsFromClubMembers(clubMembers);
        Map<String, MemberSharedDTO.BasicInfoDTO> memberBasicInfoMap = memberQueryFacade.getMemberBasicInfoMapForShare(memberIds);

        // 4. 미팅에 존재하는 모든 팀 조회
        List<Team> teams = clubMeetingQueryService.findTeamsByMeeting(meetingId);
        List<Long> teamIds = extractTeamIds(teams);
        Map<Long, Integer> teamIdToTeamNumberMap = mapTeamIdToTeamNumberMap(teams);

        // 5. Map<memberId, teamId> 형태로 모든 팀의 팀원 조회
        Map<String, Long> memberIdToTeamIdMap = clubMeetingQueryService.getMemberIdToTeamIdMap(teamIds);

        // 6. 응답 DTO로 변환
        Map<String, Integer> memberIdToTeamNumberMap = mapMemberIdToTeamNumberMap(memberIdToTeamIdMap, teamIdToTeamNumberMap);
        MembershipResponseDTO.MembershipDTO membershipDTO = ClubConverter.fromClubMembertoMembershipDTO(clubMember);
        List<MeetingResponseDTO.MeetingMemberDTO> meetingMemberDTOList = clubMembers.stream()
                .map(cm -> toMeetingMemberDTO(cm, memberBasicInfoMap, memberIdToTeamNumberMap))
                .toList();
        return ClubConverter.fromMeetingMemberDTOListToMeetingMemberListDTO(meetingMemberDTOList, hasNext, nextCursor, membershipDTO);
    }

    private MeetingResponseDTO.MeetingMemberDTO toMeetingMemberDTO(
            ClubMember clubMember,
            Map<String, MemberSharedDTO.BasicInfoDTO> memberBasicInfoMap,
            Map<String, Integer> memberIdToTeamNumberMap
    ) {
        String memberId = clubMember.getMemberId();
        MemberSharedDTO.BasicInfoDTO memberInfo = memberBasicInfoMap.get(memberId);
        Integer teamNumber = memberIdToTeamNumberMap.get(memberId);
        return ClubConverter.fromMemberSharedDTOAndTeamNumberToMeetingMemberDTO(memberInfo, teamNumber);
    }

    private Map<String, Integer> mapMemberIdToTeamNumberMap(Map<String, Long> memberIdToTeamIdMap, Map<Long, Integer> teamIdToTeamNumberMap) {
        if (memberIdToTeamIdMap == null || memberIdToTeamIdMap.isEmpty()) {
            return Map.of();
        }
        if (teamIdToTeamNumberMap == null || teamIdToTeamNumberMap.isEmpty()) {
            return Map.of();
        }
        Map<String, Integer> memberIdToTeamNumber = new HashMap<>();
        memberIdToTeamIdMap.forEach((memberId, teamId) -> {
            Integer teamNumber = (teamId == null) ? null : teamIdToTeamNumberMap.get(teamId);
            memberIdToTeamNumber.put(memberId, teamNumber);
        });
        return memberIdToTeamNumber;
    }

    private Map<Long, Integer> mapTeamIdToTeamNumberMap(List<Team> teams) {
        if (teams == null) {
            return Map.of();
        }
        return teams.stream()
                .collect(Collectors.toMap(
                        Team::getId, // key: 팀 ID
                        Team::getTeamNumber // value: 팀 번호
                ));
    }

    @Override
    public MeetingResponseDTO.TeamMemberDTO findTeamMembersByMeeting(Long meetingId, Integer teamNumber, String memberId) {
        // 1. 검증
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(meeting.getClubId(), memberId);
        if (!clubMember.isStaff()) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }
        Team team = clubMeetingQueryService.validateTeam(meetingId, teamNumber);

        // 2. 팀 멤버 조회
        List<MemberTeam> memberTeams = clubMeetingQueryService.getMemberTeamsByTeam(team.getId());

        // 3. 클럽 멤버의 기본 정보 배치 조회
        List<String> memberIds = extractMemberIdsFromMemberTeams(memberTeams);
        Map<String, MemberSharedDTO.BasicInfoDTO> memberBasicInfoMap = memberQueryFacade.getMemberBasicInfoMapForShare(memberIds);

        // 4. TeamMemberDTO 변환
        MembershipResponseDTO.MembershipDTO membershipDTO = ClubConverter.fromClubMembertoMembershipDTO(clubMember);
        return ClubConverter.fromTeamNumberAndMemberSharedDTOToTeamMemberDTO(teamNumber, memberBasicInfoMap.values().stream().toList(), membershipDTO);
    }

    private List<String> extractMemberIds(List<ClubMember> members) {
        if (members == null) {
            return List.of();
        }
        return members.stream()
                .map(ClubMember::getMemberId)
                .distinct()
                .toList();
    }

    private List<String> extractMemberIdsFromMemberTeams(List<MemberTeam> memberTeams) {
        if (memberTeams == null) {
            return List.of();
        }
        return memberTeams.stream()
                .map(mt -> mt.getClubMember().getMemberId())
                .distinct()
                .toList();
    }

    private List<String> extractMemberIdsFromBookReviews(List<BookReview> bookReviews) {
        return bookReviews.stream()
                .map(bookReview -> bookReview.getClubMember().getMemberId())
                .distinct()
                .toList();
    }

    private List<String> extractMemberIdsFromTopics(List<Topic> topics) {
        if (topics == null) {
            return List.of();
        }
        return topics.stream()
                .map(topic -> topic.getClubMember().getMemberId())
                .distinct()
                .toList();
    }

    private List<String> extractBookIdsFromMeetings(List<Meeting> meetings) {
        if (meetings == null) {
            return List.of();
        }
        return meetings.stream()
                .map(Meeting::getBookId)
                .distinct()
                .toList();
    }


    private List<String> extractMemberIdsFromTeamTopics(Map<Integer, List<TeamTopic>> teamNumberToTeamTopics) {
        if (teamNumberToTeamTopics == null) {
            return List.of();
        }
        return teamNumberToTeamTopics.values().stream()
                .flatMap(List::stream)
                .map(tt -> tt.getTopic().getClubMember().getMemberId())
                .distinct()
                .toList();
    }

    private List<Long> extractTopicIds(List<Topic> topics) {
        if (topics == null) {
            return List.of();
        }
        return topics.stream()
                .map(Topic::getId)
                .distinct()
                .toList();
    }


    private List<String> extractMemberIdsFromTeamTopics(List<TeamTopic> teamTopics) {
        if (teamTopics == null) {
            return List.of();
        }
        return teamTopics.stream()
                .map(tt -> tt.getTopic().getClubMember().getMemberId())
                .distinct()
                .toList();
    }

    private List<Topic> extractTopicFromTeamTopics(List<TeamTopic> teamTopics) {
        if (teamTopics == null) {
            return List.of();
        }
        return teamTopics.stream()
                .map(TeamTopic::getTopic)
                .distinct()
                .toList();
    }


    private List<Long> extractTeamIds(List<Team> teams) {
        if (teams == null) {
            return List.of();
        }
        return teams.stream()
                .map(Team::getId)
                .distinct()
                .toList();
    }

    private List<String> extractMemberIdsFromClubMembers(List<ClubMember> clubMembers) {
        if (clubMembers == null) {
            return List.of();
        }
        return clubMembers.stream()
                .map(ClubMember::getMemberId)
                .distinct()
                .toList();
    }

    @Override
    public Boolean checkStaffStatus(Long clubId, String memberId) {
        clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);
        return clubMember.isStaff();
    }

    @Override
    public Club findClubReferenceById(Long clubId) {
        return clubRepository.getReferenceById(clubId);
    }
}
