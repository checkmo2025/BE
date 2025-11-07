package checkmo.club.facade;

import checkmo.club.entity.meeting.*;
import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
import checkmo.book.facade.BookQueryFacade;
import checkmo.club.converter.ClubConverter;
import checkmo.club.entity.BookRecommend;
import checkmo.club.entity.Club;
import checkmo.club.entity.ClubCategory;
import checkmo.club.entity.ClubMember;
import checkmo.club.entity.announcement.MemberVote;
import checkmo.club.entity.announcement.Notice;
import checkmo.club.entity.announcement.Vote;
import checkmo.club.service.query.*;
import checkmo.club.web.dto.MembershipResponseDTO;
import checkmo.club.web.dto.bookshelf.BookShelfResponseDTO;
import checkmo.club.web.dto.club.ClubRequestDTO;
import checkmo.club.web.dto.club.ClubResponseDTO;
import checkmo.club.web.dto.meeting.MeetingResponseDTO;
import checkmo.member.facade.MemberQueryFacade;
import checkmo.book.BookSharedDTO;
import checkmo.club.ClubSharedDTO;
import checkmo.member.MemberSharedDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
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
    private static final int TOPIC_PREVIEW_SIZE_FOR_BOOKSHELF = 3;
    private static final int TOPIC_PREVIEW_SIZE_FOR_MEETING = 4;

    // 태그 상수 정의
    private static final String TAG_NOTICE = "공지";
    private static final String TAG_MEETING = "모임";
    private static final String TAG_VOTE = "투표";

    // Domain level 2
    private final MemberQueryFacade memberQueryFacade;
    // Domain level 1
    private final BookQueryFacade bookQueryFacade;

    // 자신의 QueryService
    private final ClubMeetingQueryService clubMeetingQueryService;
    private final ClubMemberQueryService clubMemberQueryService;
    private final ClubQueryService clubQueryService;
    private final ClubBookRecommendQueryService clubBookRecommendQueryService;
    private final ClubNoticeQueryService clubNoticeQueryService;
    private final ClubCategoryQueryService clubCategoryQueryService;

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
    public ClubResponseDTO.MyPageClubListDTO getMyPageClubList(String memberId, Long cursorId, Integer size) {

        // 1. 기본 사이즈 처리
        if (size == null) size = DEFAULT_PAGE_SIZE;

        // 2. 서비스 호출 (size+1로 조회 → hasNext 판단)
        List<ClubMember> clubMembers = clubMemberQueryService.getMyPageClubList(memberId, cursorId, size + 1);

        // 3. 페이징 처리
        boolean hasNext = clubMembers.size() > size;
        if (hasNext) {
            clubMembers = clubMembers.subList(0, size);
        }
        Long nextCursor = hasNext ? clubMembers.get(clubMembers.size() - 1).getId() : null;

        // 4. 클럽 ID 수집
        List<Long> clubIds = clubMembers.stream()
                .map(cm -> cm.getClub().getId())
                .toList();

        // 5. 카테고리 배치 조회
        Map<Long, List<String>> clubCategoryNamesMap =
                ClubConverter.fromClubCategoriesToCategoryNamesMap(
                        clubCategoryQueryService.findCategoriesByClubIds(clubIds)
                );

        // 6. DTO 변환
        List<ClubResponseDTO.ClubDetailResponseDTO> dtoList = clubMembers.stream()
                .map(cm -> ClubConverter.fromClubToResponseDTOWithCategoryNames(
                        cm.getClub(),
                        clubCategoryNamesMap.getOrDefault(cm.getClub().getId(), Collections.emptyList()),
                        cm.isStaff()
                ))
                .toList();

        // 7. DTO 감싸서 반환
        return ClubConverter.toMyPageClubListDTO(dtoList, hasNext, nextCursor);
    }

    @Override
    public ClubSharedDTO.MyClubList getMyClubListForShare(String memberId) {
        return clubMemberQueryService.getMyClubList(memberId);
    }

    /**
     * ClubQueryService
     * 조건에 맞는 독서 모임 목록을 검색합니다.
     *
     * @param memberId 요청자 회원 ID (해당 클럽 회원인지 확인용)
     * @param filter 검색 필터 (keyword, name, region, participants)
     * @param pageRequest 페이징 요청 (cursorId, size)
     * @return 검색된 모임 목록 DTO
     */
    @Override
    public ClubResponseDTO.ClubListDTO getClubList(String memberId, ClubRequestDTO.ClubSearchFilter filter, ClubRequestDTO.CursorPageRequest pageRequest) {

        // 1. 커서 초기화
        Long cursorId = (pageRequest.cursorId() == null || pageRequest.cursorId() == 0L) ? Long.MAX_VALUE : pageRequest.cursorId();

        // 2. 페이지 크기 결정 (size가 null 또는 0 이하이면 기본값 사용)
        int pageSize = (pageRequest.size() == null || pageRequest.size() <= 0) ? DEFAULT_PAGE_SIZE : pageRequest.size();

        // 3. Service에서 순수 엔티티 조회
        List<Club> clubs = clubQueryService.getClubList(filter, cursorId, pageSize);

        // 4. 클럽 ID 리스트 추출
        List<Long> clubIds = clubs.stream()
                .map(Club::getId)
                .toList();

        // 5. 클럽별 멤버 상태 배치 조회
        Map<Long, ClubMember.ClubMemberStatus> statusMap = clubMemberQueryService.getMemberStatuses(memberId, clubIds);

        // 6. 클럽별 카테고리 ID 배치 조회
        List<ClubCategory> allClubCategories = clubCategoryQueryService.findCategoriesByClubIds(clubIds);
        Map<Long, List<Long>> categoryIdMap = ClubConverter.fromClubCategoriesToCategoryIdMap(allClubCategories);

        // 7. DTO 변환
        List<ClubResponseDTO.ClubWithMyStatusDTO> clubList = clubs.stream()
                .map(club -> toClubWithMyStatusDTO(club, statusMap, categoryIdMap))
                .toList();

        // 8. 페이징 처리 (마지막 ID를 기반으로 다음 페이지 존재 여부 확인)
        Long lastId = clubs.isEmpty() ? null : clubs.getLast().getId();
        boolean hasNext = !clubs.isEmpty() && clubs.size() == pageSize;

        // 9. 최종 DTO 변환
        return ClubConverter.toClubListDTO(clubList, hasNext, lastId);
    }

    /**
     * ClubQueryService
     * 독서 모임의 상세 정보를 조회합니다.
     *
     * @param clubId 조회할 모임 ID
     * @param memberId 조회자 회원 ID (운영진 권한 확인용)
     * @return 모임 상세 정보 DTO
     */
    @Override
    public ClubResponseDTO.ClubDetailDTO getClubInfo(Long clubId, String memberId) {

        // 1. Service에서 순수 엔티티 조회
        Club club = clubQueryService.getClubInfo(clubId);

        // 2. 운영진 권한 확인
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);
        boolean isStaff = clubMember.isStaff();
        if (!isStaff) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }

        // 3. 카테고리 ID 리스트 조회
        List<ClubCategory> clubCategories = clubCategoryQueryService.findCategoriesByClub(clubId);
        List<Long> categoryIds = clubCategories.stream()
                .map(ClubCategory::getCategoryId)
                .toList();

        // 4. DTO 변환 후 반환
        return ClubConverter.fromClubToClubDetailDTO(club, categoryIds, isStaff);
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
        Map<String, MemberSharedDTO.BasicInfo> memberInfoMap = memberQueryFacade.getMemberBasicInfoMapForShare(memberIds);

        // 6. DTO 변환
        List<ClubResponseDTO.ClubMemberDTO> dtoList = members.stream()
                .map(cm -> {
                    MemberSharedDTO.BasicInfo memberInfo = memberInfoMap.get(cm.getMemberId());
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

        // 4. 공지사항과 투표 각각 조회
        List<Notice> notices = clubNoticeQueryService.getNoticeList(clubId, onlyImportant, cursor, pageable);
        List<Vote> votes = clubNoticeQueryService.getVoteList(clubId, onlyImportant, cursor, pageable);

        // 5. 생성시간 순으로 병합 및 DTO 변환
        List<ClubResponseDTO.NoticeItem> noticeItems = mergeNoticesAndVotes(notices, votes, pageSize);

        // 6. 페이징
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

        // 3. 회원이 가입한 클럽 ID 리스트 조회
        List<Long> clubIds = clubMemberQueryService.getMyClubListIds(memberId);

        if (clubIds.isEmpty()) {
            return ClubConverter.toMemberNoticeListDTO(Collections.emptyList(), false, null);
        }

        // 4. 공지사항과 투표 각각 조회
        List<Notice> notices = clubNoticeQueryService.getNoticeListByClubIds(clubIds, onlyImportant, cursor, pageable);
        List<Vote> votes = clubNoticeQueryService.getVoteListByClubIds(clubIds, onlyImportant, cursor, pageable);

        // 5. 생성시간 순으로 병합 및 DTO 변환 (클럽 정보 포함)
        List<ClubResponseDTO.ClubNoticeWithClubDTO> memberNoticeItems = mergeNoticesAndVotesWithClub(notices, votes, pageSize);

        // 6. 페이징
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
        // 1. 검증
        clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        return switch (tag) {
            case TAG_NOTICE -> getPureNoticeDetail(clubId, noticeId, clubMember);
            case TAG_MEETING -> getMeetingNoticeDetail(clubId, noticeId, clubMember);
            case TAG_VOTE -> getVoteDetail(clubId, noticeId, memberId, clubMember);
            default -> throw new GeneralException(ErrorStatus.CLUB_INVALID_TAG_TYPE);
        };
    }

    /**
     * 순수 공지사항 상세 조회
     */
    private ClubResponseDTO.ClubNoticeDetailDTO getPureNoticeDetail(Long clubId, Long itemId, ClubMember clubMember) {
        Notice notice = clubNoticeQueryService.getNotice(clubId, itemId);

        if (TAG_MEETING.equals(notice.getTag())) {
            throw new GeneralException(ErrorStatus.NOTICE_NOT_FOUND);
        }

        return ClubResponseDTO.ClubNoticeDetailDTO.builder()
                .isStaff(clubMember.isStaff())
                .noticeItem(ClubConverter.toPureNoticeDTO(notice))
                .build();
    }

    /**
     * 모임 공지사항 상세 조회
     */
    private ClubResponseDTO.ClubNoticeDetailDTO getMeetingNoticeDetail(Long clubId, Long itemId, ClubMember clubMember) {
        Notice notice = clubNoticeQueryService.getNoticeWithMeeting(clubId, itemId);

        if (TAG_NOTICE.equals(notice.getTag())) {
            throw new GeneralException(ErrorStatus.NOTICE_NOT_FOUND);
        }

        BookSharedDTO.BasicInfo bookInfo = bookQueryFacade.getBookBasicInfoForShare(notice.getMeeting().getBookId());

        return ClubResponseDTO.ClubNoticeDetailDTO.builder()
                .isStaff(clubMember.isStaff())
                .noticeItem(ClubConverter.toMeetingNoticeDTO(notice, bookInfo))
                .build();
    }

    /**
     * 투표 상세 조회
     */
    private ClubResponseDTO.ClubNoticeDetailDTO getVoteDetail(Long clubId, Long itemId, String memberId, ClubMember clubMember) {
        Vote vote = clubNoticeQueryService.getVote(clubId, itemId);
        List<String> voteItems = vote.getItems();
        int itemCount = voteItems.size();

        // 전체 투표 결과
        List<MemberVote> memberVotes = clubNoticeQueryService.getMemberVotesByVoteId(vote.getId());

        // 항목별 투표자 정보 수집
        List<List<MemberSharedDTO.BasicInfo>> votedMembersByItem = collectVotedMembersByItem(vote, memberVotes, itemCount);

        // 본인 투표 정보
        MemberVote myVote = clubNoticeQueryService.getMyVote(vote.getId(), memberId);

        // 투표 항목 DTO 생성
        List<ClubResponseDTO.EachItemDTO> itemDTOs = createVoteItemDTOs(voteItems, myVote, votedMembersByItem, itemCount);

        ClubResponseDTO.VoteDTO voteDTO = ClubConverter.toVoteDTO(vote, itemDTOs);

        return ClubResponseDTO.ClubNoticeDetailDTO.builder()
                .isStaff(clubMember.isStaff())
                .noticeItem(voteDTO)
                .build();
    }

    /**
     * 투표 항목별 투표자 정보 수집
     */
    private List<List<MemberSharedDTO.BasicInfo>> collectVotedMembersByItem(
            Vote vote, List<MemberVote> memberVotes, int itemCount
    ) {
        // 항목별 투표자 정보 리스트 초기화
        List<List<MemberSharedDTO.BasicInfo>> votedMembersByItem = new ArrayList<>();
        for (int i = 0; i < itemCount; i++) {
            votedMembersByItem.add(new ArrayList<>());
        }

        // 각 MemberVote에 대해 항목별 투표 여부 확인 후 추가
        for (MemberVote mv : memberVotes) {
            MemberSharedDTO.BasicInfo memberInfo = getMemberInfoForVote(vote, mv);

            if (mv.isItem1()) votedMembersByItem.get(0).add(memberInfo);
            if (itemCount >= 2 && mv.isItem2()) votedMembersByItem.get(1).add(memberInfo);
            if (itemCount >= 3 && mv.isItem3()) votedMembersByItem.get(2).add(memberInfo);
            if (itemCount >= 4 && mv.isItem4()) votedMembersByItem.get(3).add(memberInfo);
            if (itemCount >= 5 && mv.isItem5()) votedMembersByItem.get(4).add(memberInfo);
        }

        return votedMembersByItem;
    }

    /**
     * 투표자의 멤버 정보 조회 (익명 여부에 따라 다르게 처리)
     */
    private MemberSharedDTO.BasicInfo getMemberInfoForVote(Vote vote, MemberVote memberVote) {
        if (vote.isAnonymity()) {
            String voterName = "익명";
            String profileImageUrl = "https://avatars.githubusercontent.com/u/217887881?s=200&v=4";
            return new MemberSharedDTO.BasicInfo(voterName, profileImageUrl);
        } else {
            return memberQueryFacade.getMemberBasicInfoForShare(memberVote.getMemberId());
        }
    }

    /**
     * 투표 항목 DTO 리스트 생성
     */
    private List<ClubResponseDTO.EachItemDTO> createVoteItemDTOs(
            List<String> voteItems, MemberVote myVote,
            List<List<MemberSharedDTO.BasicInfo>> votedMembersByItem, int itemCount
    ) {
        List<ClubResponseDTO.EachItemDTO> itemDTOs = new ArrayList<>();
        for (int i = 0; i < itemCount; i++) {
            boolean isSelected = isItemSelected(myVote, i);
            itemDTOs.add(
                    ClubConverter.toEachItemDTO(
                            voteItems.get(i),
                            isSelected,
                            votedMembersByItem.get(i)
                    )
            );
        }
        return itemDTOs;
    }

    /**
     * 특정 항목이 선택되었는지 확인
     */
    private boolean isItemSelected(MemberVote myVote, int itemIndex) {
        if (myVote == null) {
            return false;
        }
        return switch (itemIndex) {
            case 0 -> myVote.isItem1();
            case 1 -> myVote.isItem2();
            case 2 -> myVote.isItem3();
            case 3 -> myVote.isItem4();
            case 4 -> myVote.isItem5();
            default -> false;
        };
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
        // 1. Service에서 순수 엔티티 조회
        BookRecommend bookRecommend = clubBookRecommendQueryService.getBookRecommendEntity(clubId, bookRecommendId, memberId);

        // 2. ClubMember 조회 (isStaff 확인용)
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        // 3. 외부 도메인 정보 조회 (Facade에서 처리)
        var bookInfo = bookQueryFacade.getBookBasicInfoForShare(bookRecommend.getBookId());
        var authorInfo = memberQueryFacade.getMemberBasicInfoForShare(bookRecommend.getClubMember().getMemberId());
        var currentMemberInfo = memberQueryFacade.getMemberBasicInfoForShare(memberId);

        // 4. DTO 변환 후 반환
        return ClubConverter.toBookRecommendDetailDTO(
                bookRecommend,
                bookInfo,
                authorInfo,
                currentMemberInfo.getNickname(),
                clubMember.isStaff()
        );
    }

    @Override
    public BookShelfResponseDTO.BookShelfListDTO getBookShelfList(Long clubId, Long cursorId, Integer size, Integer generation, String memberId) {
        // 1. 유효성 검증(club, clubMember)
        clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        // 2. [책장] 미팅 리스트 조회
        List<Meeting> meetings = clubMeetingQueryService.getBookShelfList(clubId, generation, cursorId, size + 1);

        // 3. 페이징 처리
        boolean hasNext = meetings.size() > size;
        if (hasNext) {
            meetings = meetings.subList(0, size);
        }
        Long nextCursor = hasNext ? meetings.getLast().getId() : null;

        // 4. DTO 변환
        List<BookShelfResponseDTO.BookShelfInfoDTO> bookShelfInfoDTOS = meetings.stream()
                .map(meeting ->
                        ClubConverter.fromMeetingAndBookSharedDTOToBookShelfInfoDTO(
                                meeting,
                                bookQueryFacade.getBookBasicInfoForShare(meeting.getBookId()) // TODO: 미팅에 사용된 책 배치 조회
                        )
                )
                .toList();
        MembershipResponseDTO.MembershipDTO membershipDTO = ClubConverter.fromClubMembertoMembershipDTO(clubMember);
        return ClubConverter.fromBookShelfInfoDTOListToBookShelfListDTO(bookShelfInfoDTOS, hasNext, nextCursor, membershipDTO);
    }

    @Override
    // TODO: getBookShelftDetail, findTopicsByMeeting 간 중복 제거
    public BookShelfResponseDTO.BookShelfDetailDTO getBookShelfDetail(Long meetingId, String memberId) {
        // 1. 유효성 검증(meeting, clubMember)
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(meeting.getClubId(), memberId);

        // 2. [발제 미리보기] 발제 리스트 조회
        List<Topic> topics = clubMeetingQueryService.findTopicsWithClubMemberByMeeting(meetingId, null, TOPIC_PREVIEW_SIZE_FOR_BOOKSHELF + 1);

        // 3. 페이징 처리
        boolean hasNext = topics.size() > TOPIC_PREVIEW_SIZE_FOR_BOOKSHELF;
        if (hasNext) {
            topics = topics.subList(0, TOPIC_PREVIEW_SIZE_FOR_BOOKSHELF);
        }
        Long nextCursor = hasNext ? topics.getLast().getId() : null;

        // 4. 발제의 작성자 정보 배치 조회
        List<String> authorIds = extractMemberIdsFromTopics(topics);
        Map<String, MemberSharedDTO.BasicInfo> authorInfoMap =
                memberQueryFacade.getMemberBasicInfoMapForShare(authorIds);

        // 5. DTO 변환
        List<BookShelfResponseDTO.TopicDTO> topicListDTOs = ClubConverter.fromTopicListAndAuthorInfoMapAndMemberIdToTopicDTOList(
                topics,
                authorInfoMap,
                memberId // 요청한 회원 ID -> 작성자 본인 확인용
        );
        MembershipResponseDTO.MembershipDTO membershipDTO = ClubConverter.fromClubMembertoMembershipDTO(clubMember);
        return ClubConverter.fromBookShelfDTOToBookShelfDetailDTO(
                meeting,
                bookQueryFacade.getBookDetailInfoForShare(meeting.getBookId()), // TODO: 책 상세정보 배치 조회로 변경
                ClubConverter.fromTopicDTOListToTopicListDTOForBookshelf(topicListDTOs, hasNext, nextCursor, null),
                membershipDTO
        );
    }

    @Override
    public BookShelfResponseDTO.TopicListDTO findTopicsByMeeting(Long meetingId, Long cursorId, Integer size, String memberId) {
        // 1. 유효성 검증(meeting, clubMember)
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(meeting.getClubId(), memberId);

        // 2. 발제 리스트 조회
        List<Topic> topics = clubMeetingQueryService.findTopicsWithClubMemberByMeeting(meetingId, cursorId, size + 1);

        // 3. 페이징 처리
        boolean hasNext = topics.size() > size;
        if (hasNext) {
            topics = topics.subList(0, size);
        }
        Long nextCursor = hasNext ? topics.getLast().getId() : null;

        // 4. 발제의 작성자 정보 배치 조회
        List<String> authorIds = extractMemberIdsFromTopics(topics);
        Map<String, MemberSharedDTO.BasicInfo> authorInfoMap =
                memberQueryFacade.getMemberBasicInfoMapForShare(authorIds);

        // 5. DTO 변환
        List<BookShelfResponseDTO.TopicDTO> topicListDTOs = ClubConverter.fromTopicListAndAuthorInfoMapAndMemberIdToTopicDTOList(
                topics,
                authorInfoMap,
                memberId // 요청한 회원 ID -> 작성자 본인 확인용
        );
        MembershipResponseDTO.MembershipDTO membershipDTO = ClubConverter.fromClubMembertoMembershipDTO(clubMember);
        return ClubConverter.fromTopicDTOListToTopicListDTOForBookshelf(topicListDTOs, hasNext, nextCursor, membershipDTO);
    }

    @Override
    public BookShelfResponseDTO.BookReviewListDTO getBookReviewList(Long meetingId, Long lastReviewId, int size, String memberId) {
        // 1. 유효성 검증(meeting, clubMember)
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(meeting.getClubId(), memberId);

        // 2. 한줄평 리스트 조회
        List<BookReview> bookReviews = clubMeetingQueryService.findBookReviewsByMeeting(meetingId, lastReviewId, size + 1);

        // 3. 페이징 처리
        boolean hasNext = bookReviews.size() > size;
        if (hasNext) {
            bookReviews = bookReviews.subList(0, size);
        }
        Long nextCursor = hasNext ? bookReviews.get(bookReviews.size() - 1).getId() : null;

        // 4. 한줄평 작성자 정보 배치 조회
        List<String> authorIds = extractMemberIdsFromBookReviews(bookReviews);
        Map<String, MemberSharedDTO.BasicInfo> authorInfoMap = memberQueryFacade.getMemberBasicInfoMapForShare(authorIds);

        // 5. DTO 변환
        List<BookShelfResponseDTO.BookReviewDTO> bookReviewDTOList = mapBookReviewsAndAuthorInfoToDTOs(bookReviews, authorInfoMap);
        MembershipResponseDTO.MembershipDTO membershipDTO = ClubConverter.fromClubMembertoMembershipDTO(clubMember);
        return ClubConverter.fromBookReviewDTOListToBookReviewListDTO(bookReviewDTOList, hasNext, nextCursor, membershipDTO);
    }

    private List<BookShelfResponseDTO.BookReviewDTO> mapBookReviewsAndAuthorInfoToDTOs(
            List<BookReview> bookReviews,
            Map<String, MemberSharedDTO.BasicInfo> authorInfoMap
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
        // 1. 유효성 검증(club, clubMember)
        clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        // 2. 미팅 리스트 조회
        List<Meeting> meetings = clubMeetingQueryService.findMeetingsByClubAndCursor(clubId, cursorId, size + 1);

        // 3. 페이징 처리
        boolean hasNext = meetings.size() > size;
        if (hasNext) {
            meetings = meetings.subList(0, size);
        }
        Long nextCursor = hasNext ? meetings.getLast().getId() : null;

        // 4. 미팅의 모든 도서 기본 정보 배치 조회
        List<String> bookIds = extractBookIdsFromMeetings(meetings);
        Map<String, BookSharedDTO.BasicInfo> bookBasicInfoMap = bookQueryFacade.getBookBasicInfoMapForShare(bookIds);

        // 5. DTO 변환
        List<MeetingResponseDTO.MeetingInfoDTO> meetingInfoDTOList = mapMeetingsWithBookBasicInfoToDTOs(meetings, bookBasicInfoMap);
        MembershipResponseDTO.MembershipDTO membershipDTO = ClubConverter.fromClubMembertoMembershipDTO(clubMember);
        return ClubConverter.fromMeetingInfoDTOListToMeetingListDTO(meetingInfoDTOList, hasNext, nextCursor, membershipDTO);
    }

    private List<MeetingResponseDTO.MeetingInfoDTO> mapMeetingsWithBookBasicInfoToDTOs(
            List<Meeting> meetings,
            Map<String, BookSharedDTO.BasicInfo> bookBasicInfoMap
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
        // 1. 유효성 검증(meeting, clubMember)
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(meeting.getClubId(), memberId);

        // 2. [발제 전체보기 - 미리보기] 발제 최신순 상위 4개 토픽 리스트 조회
        List<Topic> topics = clubMeetingQueryService.findTopicsWithClubMemberByMeeting(meetingId, null, TOPIC_PREVIEW_SIZE_FOR_MEETING);

        // 3. [발제 전체보기 - 미리보기] TeamTopic(+Team) 배치 조회
        List<Long> topicIds = extractTopicIds(topics);
        Map<Long, List<Integer>> teamTopicsWithTeamByTopicIds = clubMeetingQueryService.findTeamTopicsWithTeamByTopicIds(topicIds);

        // 4. [토론 x조 - 미리보기] 해당하는 미팅의 존재하는 모든 팀 조회
        List<Team> teams = clubMeetingQueryService.findTeamsByMeeting(meetingId);

        // 5. [토론 x조 - 미리보기] 모든 팀의 발제 등록순 상위 4개 토픽 조회
        Map<Integer, List<TeamTopic>> teamNumberToTeamTopics = teams.stream()
                .collect(Collectors.toMap(
                        Team::getTeamNumber, // key: 팀 번호
                        team -> clubMeetingQueryService.findTeamTopicsWithTopicAndClubMemberByTeamId(team.getId(), TOPIC_PREVIEW_SIZE_FOR_MEETING) //value : 해당 팀의 발제 최신순 상위 4개 팀 토픽 리스트
                ));

        // 6. 조회한 모든 발제(topics와 teamTopics)의 작성자 id를 중복 없이 리스트 조회
        List<String> authorIds1 = extractMemberIdsFromTopics(topics);
        List<String> authorIds2 = extractMemberIdsFromTeamTopics(teamNumberToTeamTopics);
        List<String> authorIds = Stream.concat(authorIds1.stream(), authorIds2.stream())
                .distinct()
                .toList();

        // 7. 발제의 작성자 정보 배치 조회
        Map<String, MemberSharedDTO.BasicInfo> authorInfoMap = memberQueryFacade.getMemberBasicInfoMapForShare(authorIds);

        // 8. DTO 변환
        MembershipResponseDTO.MembershipDTO membershipDTO = ClubConverter.fromClubMembertoMembershipDTO(clubMember);
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
        // 1. 유효성 검증(meeting, clubMember)
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(meeting.getClubId(), memberId);

        // 2. 토픽 리스트 조회
        List<Topic> topics = clubMeetingQueryService.findTopicsWithClubMemberByMeeting(meetingId, null, null);

        // 3. 토픽 작성자 정보 배치 조회
        List<String> authorIds = extractMemberIdsFromTopics(topics);
        Map<String, MemberSharedDTO.BasicInfo> authorInfoMap = memberQueryFacade.getMemberBasicInfoMapForShare(authorIds);

        // 4. TeamTopic과 Team 배치 조회
        List<Long> topicIds = extractTopicIds(topics);
        Map<Long, List<Integer>> topicIdToSelectTeamNumbers = clubMeetingQueryService.findTeamTopicsWithTeamByTopicIds(topicIds);

        // 5. DTO 변환
        MembershipResponseDTO.MembershipDTO membershipDTO = ClubConverter.fromClubMembertoMembershipDTO(clubMember);
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
        Map<String, MemberSharedDTO.BasicInfo> authorInfoMap = memberQueryFacade.getMemberBasicInfoMapForShare(authorIds);

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
        // 1. 유효성 검증(club, clubMember)
        Club club = clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        // 2. 해당 연월의 미팅 리스트 조회
        List<Meeting> meetings = clubMeetingQueryService.getClubMeetingByYearAndMonth(clubId, year, month, memberId);

        // 3. DTO 변환
        MembershipResponseDTO.MembershipDTO membershipDTO = ClubConverter.fromClubMembertoMembershipDTO(clubMember);
        return ClubConverter.fromMeetingListToMCalendarMeetingDTO(meetings, membershipDTO);
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
        Map<String, MemberSharedDTO.BasicInfo> memberBasicInfoMap = memberQueryFacade.getMemberBasicInfoMapForShare(memberIds);

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
            Map<String, MemberSharedDTO.BasicInfo> memberBasicInfoMap,
            Map<String, Integer> memberIdToTeamNumberMap
    ) {
        String memberId = clubMember.getMemberId();
        MemberSharedDTO.BasicInfo memberInfo = memberBasicInfoMap.get(memberId);
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
        Team team = clubMeetingQueryService.validateTeam(meetingId, teamNumber);

        // 2. 팀 멤버 조회
        List<MemberTeam> memberTeams = clubMeetingQueryService.getMemberTeamsByTeam(team.getId());

        // 3. 클럽 멤버의 기본 정보 배치 조회
        List<String> memberIds = extractMemberIdsFromMemberTeams(memberTeams);
        Map<String, MemberSharedDTO.BasicInfo> memberBasicInfoMap = memberQueryFacade.getMemberBasicInfoMapForShare(memberIds);

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

    /**
     * Club 엔티티를 ClubWithMyStatusDTO로 변환합니다.
     *
     * @param club 클럽 엔티티
     * @param statusMap 클럽별 멤버 상태 맵
     * @param categoryIdMap 클럽별 카테고리 ID 맵
     * @return ClubWithMyStatusDTO
     */
    private ClubResponseDTO.ClubWithMyStatusDTO toClubWithMyStatusDTO(
            Club club,
            Map<Long, ClubMember.ClubMemberStatus> statusMap,
            Map<Long, List<Long>> categoryIdMap
    ) {
        ClubMember.ClubMemberStatus status = statusMap.get(club.getId());
        boolean isStaff = status == ClubMember.ClubMemberStatus.STAFF;
        boolean isMember = status != null;

        List<Long> categoryIds = categoryIdMap.getOrDefault(club.getId(), List.of());

        ClubResponseDTO.ClubDetailDTO clubDetailDTO = ClubConverter.fromClubToClubDetailDTO(club, categoryIds, isStaff);

        return ClubResponseDTO.ClubWithMyStatusDTO.builder()
                .club(clubDetailDTO)
                .isMember(isMember)
                .build();
    }

    /**
     * 공지사항과 투표를 생성 시간 순서대로 병합하는 로직
     */
    private List<ClubResponseDTO.NoticeItem> mergeNoticesAndVotes(
            List<Notice> notices, List<Vote> votes, int pageSize
    ) {
        List<ClubResponseDTO.NoticeItem> resultList = new ArrayList<>();
        int n = notices.size();
        int m = votes.size();

        int i = 0, j = 0;
        while (resultList.size() < pageSize + 1 && (i < n || j < m)) {
            if (i < n && (j >= m || notices.get(i).getCreatedAt().isAfter(votes.get(j).getCreatedAt()))) {
                Notice notice = notices.get(i++);
                ClubResponseDTO.NoticeItem dto;

                if (notice.getMeeting() != null) {
                    BookSharedDTO.BasicInfo bookInfo = bookQueryFacade.getBookBasicInfoForShare(notice.getMeeting().getBookId());
                    dto = ClubConverter.toMeetingNoticeDTO(notice, bookInfo);
                } else {
                    dto = ClubConverter.toPureNoticeDTO(notice);
                }

                resultList.add(dto);
            } else if (j < m) {
                Vote vote = votes.get(j++);
                List<ClubResponseDTO.EachItemDTO> itemDTOs = ClubConverter.toEachItemDTOListFromItems(vote.getItems());
                ClubResponseDTO.VoteDTO voteDTO = ClubConverter.toVoteDTO(vote, itemDTOs);
                resultList.add(voteDTO);
            }
        }

        return resultList;
    }

    /**
     * 공지사항과 투표를 생성 시간 순서대로 병합하는 로직 (클럽 정보 포함)
     */
    private List<ClubResponseDTO.ClubNoticeWithClubDTO> mergeNoticesAndVotesWithClub(
            List<Notice> notices, List<Vote> votes, int pageSize
    ) {
        List<ClubResponseDTO.ClubNoticeWithClubDTO> resultList = new ArrayList<>();

        int i = 0, j = 0;
        while (resultList.size() < pageSize + 1 && (i < notices.size() || j < votes.size())) {
            if (i < notices.size() && (j >= votes.size() || notices.get(i).getCreatedAt().isAfter(votes.get(j).getCreatedAt()))) {
                Notice notice = notices.get(i++);
                ClubResponseDTO.NoticeItem dto;

                if (notice.getMeeting() != null) {
                    BookSharedDTO.BasicInfo bookInfo = bookQueryFacade.getBookBasicInfoForShare(notice.getMeeting().getBookId());
                    dto = ClubConverter.toMeetingNoticeDTO(notice, bookInfo);
                } else {
                    dto = ClubConverter.toPureNoticeDTO(notice);
                }
                resultList.add(ClubConverter.toClubNoticeWithClubDTO(notice, dto));
            } else if (j < votes.size()) {
                Vote vote = votes.get(j++);
                List<ClubResponseDTO.EachItemDTO> itemDTOs = ClubConverter.toEachItemDTOListFromItems(vote.getItems());
                ClubResponseDTO.VoteDTO voteDTO = ClubConverter.toVoteDTO(vote, itemDTOs);
                resultList.add(ClubConverter.toClubNoticeWithClubDTO(vote, voteDTO));
            }
        }
        return resultList;
    }
}
