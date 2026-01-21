package checkmo.bookStory.internal.service;

import static checkmo.clubManagement.ClubManagementExternalDTO.BasicInfo;
import static checkmo.clubManagement.ClubManagementExternalDTO.ClubList;

import checkmo.book.BookAPI;
import checkmo.book.BookExternalDTO;
import checkmo.bookStory.internal.converter.BookStoryConverter;
import checkmo.bookStory.internal.entity.BookStory;
import checkmo.bookStory.internal.entity.Comment;
import checkmo.bookStory.internal.exception.BookStoryErrorStatus;
import checkmo.bookStory.internal.exception.BookStoryException;
import checkmo.bookStory.internal.service.query.BookStoryViewCacheService;
import checkmo.bookStory.internal.service.query.BookStoryQueryService;
import checkmo.bookStory.web.dto.BookStoryRequestDTO;
import checkmo.bookStory.web.dto.BookStoryResponseDTO;
import checkmo.bookStory.web.dto.BookStoryResponseDTO.CommentInfo;
import checkmo.bookStory.web.dto.BookStoryResponseDTO.DetailInfo;
import checkmo.clubManagement.ClubManagementAPI;
import checkmo.common.template.CursorPagingHelper;
import checkmo.common.template.CursorResult;
import checkmo.member.MemberAPI;
import checkmo.member.MemberExternalDTO;
import checkmo.member.MemberExternalDTO.BasicInfoWithFollow;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookStoryQueryFacade {

    public static final int DEFAULT_PAGE_SIZE = 10;

    private final ClubManagementAPI clubManagementAPI;
    private final MemberAPI memberAPI;
    private final BookAPI bookAPI;

    private final BookStoryQueryService bookStoryQueryService;
    private final BookStoryViewCacheService viewCacheService;

    /**
     * 특정 책 이야기의 상세 정보를 조회합니다.
     *
     * @param memberId    조회하는 회원의 ID
     * @param bookStoryId 조회할 책 이야기 ID
     * @return 조회된 책 이야기 상세 정보 DTO
     */
    public DetailInfo fetchBookStoryDetailInfo(String memberId, Long bookStoryId) {
        // 1. Service에서 BookStory 엔티티 조회
        BookStory bookStory = bookStoryQueryService.retrieveBookStory(bookStoryId);

        // 2. 레디스에 조회 수 카운트 증가
        viewCacheService.incrementViewCount(bookStoryId, memberId);

        // 3. 책 정보 조회
        BookExternalDTO.BasicInfo bookInfo = bookAPI.fetchBookBasicInfo(bookStory.getBookId());

        // 4. 작성자 정보 조회
        BasicInfoWithFollow authorInfo = memberAPI.fetchMemberBasicInfoWithFollow(
                bookStory.getMemberId(), memberId);

        // 5. 좋아요 여부 조회
        Boolean isLiked = bookStoryQueryService.checkBookStoryLikeByMemberId(memberId, List.of(bookStory))
                .getOrDefault(bookStory.getId(), false);

        // 6. 댓글 조회 (부모 댓글만, 대댓글은 컨버터에서 DTO 변환 시 자동 포함)
        List<Comment> comments = bookStoryQueryService.retrieveBookStoryComments(bookStoryId);

        // 7. 댓글 작성자들 정보 조회
        // 7-1. 댓글 작성자들 Id 목록 조회 (Set으로 중복 제거)
        Set<String> commentMemberIds = comments.stream()
                .flatMap(comment -> Stream.concat(
                        Stream.of(comment.getMemberId()),
                        comment.getChildrenComment().stream().map(Comment::getMemberId)
                ))
                .collect(Collectors.toSet());

        // 7-2. 댓글 작성자들 정보를 배치 조회 (6-1에서 조회된 정보를 리스트로 변환 후 한번에 조회)
        Map<String, MemberExternalDTO.BasicInfo> commentMemberInfoMap =
                commentMemberIds.isEmpty() ? Map.of() :
                        memberAPI.fetchMemberBasicInfoByMemberIds(new ArrayList<>(commentMemberIds));

        // 8. 댓글 DTO 변환
        List<CommentInfo> commentDTOList =
                BookStoryConverter.toCommentDetailList(comments, memberId, commentMemberInfoMap);

        // 9. 책이야기 작성자의 이전, 다음 책이야기 아이디 조회
        var bookStoryPrevNextProjection
                = bookStoryQueryService.retrievePrevNextBookStoryId(bookStory.getMemberId(), bookStoryId);

        // 10. DTO 변환
        return BookStoryConverter.toBookStoryDetailWithComment(
                bookStory,
                memberId,
                bookInfo,
                authorInfo,
                isLiked,
                commentDTOList,
                bookStoryPrevNextProjection
        );
    }

    /**
     * scope에 따라 책 이야기 목록을 조회합니다. 비즈니스 로직을 Facade에서 처리하여 컨트롤러는 단순히 호출만 담당
     *
     * @param memberId             조회하는 회원의 ID
     * @param scope                조회 범위 ("ALL", "MY", "FOLLOWING", "CLUB", "TARGET")
     * @param clubId               클럽 ID (scope가 "CLUB"일 때 필수)
     * @param targetMemberNickname 대상 회원 닉네임 (scope가 "TARGET"일 때 필수)
     * @param cursorId             페이지 번호 (1부터 시작)
     * @return scope에 따른 책 이야기 목록 DTO
     */
    public BookStoryResponseDTO.BookStoryList fetchBookStories(
            String memberId,
            BookStoryRequestDTO.BookStoryScope scope,
            Long clubId, String targetMemberNickname,
            Long cursorId
    ) {
        // 1. targetMemberId 조회 (SCOPE=TARGET인 경우)
        String targetMemberId = resolveTargetMemberId(scope, targetMemberNickname);

        // 2. BookStory 리스트 조회
        CursorResult<BookStory> bookStoryCursorResult = CursorPagingHelper.getPage(
                (pageSize) -> bookStoryQueryService.retrieveBookStories(
                        memberId, scope, clubId, targetMemberId, cursorId, pageSize
                ),
                BookStory::getId,
                DEFAULT_PAGE_SIZE
        );
        List<BookStory> bookStories = bookStoryCursorResult.content();

        // 좋아요 여부 조회
        Map<Long, Boolean> isLikedMap = fetchLikedInfo(memberId, bookStories);

        // 책 정보 조회
        Map<String, BookExternalDTO.BasicInfo> bookInfoMap = fetchBookInfo(bookStories);

        // 작성자 정보 조회
        Map<String, BasicInfoWithFollow> authorInfoMap = fetchAuthorInfo(memberId, bookStories);

        // DTO 변환
        List<BookStoryResponseDTO.BasicInfo> basicInfoList = convertToBookStoryResponses(memberId,
                bookStories, isLikedMap, bookInfoMap, authorInfoMap);

        // 클럽 정보 조회
        ClubList clubList = clubManagementAPI.fetchMyClubs(memberId);
        BasicInfo basicInfo = findClubInfoForScope(scope, clubId, clubList);

        // 스코프 정보 변환 및 최종 응답 DTO 변환
        var scopeInfo = BookStoryResponseDTO.ScopeInfo.builder()
                .scope(scope)
                .selectedClub(basicInfo)
                .build();

        return BookStoryResponseDTO.BookStoryList.builder()
                .scopeInfo(scopeInfo)
                .memberClubList(clubList)
                .basicInfoList(basicInfoList)
                .hasNext(bookStoryCursorResult.hasNext())
                .nextCursor(bookStoryCursorResult.nextCursor())
                .pageSize(DEFAULT_PAGE_SIZE)
                .build();
    }

    /**
     * TARGET 스코프인 경우 닉네임으로 targetMemberId 조회
     */
    private String resolveTargetMemberId(BookStoryRequestDTO.BookStoryScope scope, String targetMemberNickname) {
        if (scope == BookStoryRequestDTO.BookStoryScope.TARGET) {
            return memberAPI.fetchMemberId(targetMemberNickname);
        }
        return null;
    }

    /**
     * 좋아요 정보 배치 조회
     */
    private Map<Long, Boolean> fetchLikedInfo(String memberId, List<BookStory> bookStories) {
        return bookStoryQueryService.checkBookStoryLikeByMemberId(memberId, bookStories);
    }

    /**
     * 책 정보 배치 조회
     */
    private Map<String, BookExternalDTO.BasicInfo> fetchBookInfo(List<BookStory> bookStories) {
        List<String> bookIds = bookStories.stream()
                .map(checkmo.bookStory.internal.entity.BookStory::getBookId)
                .distinct()
                .toList();
        return bookAPI.fetchBookBasicInfoByBookIds(bookIds);
    }

    /**
     * 작성자 정보 배치 조회
     */
    private Map<String, BasicInfoWithFollow> fetchAuthorInfo(
            String memberId,
            List<BookStory> bookStories
    ) {
        List<String> targetMemberIds = bookStories.stream()
                .map(checkmo.bookStory.internal.entity.BookStory::getMemberId)
                .distinct()
                .toList();
        return memberAPI.fetchMemberBasicInfoWithFollowByMemberId(targetMemberIds, memberId);
    }

    /**
     * BookStory 엔티티들을 Response DTO로 변환
     */
    private List<BookStoryResponseDTO.BasicInfo> convertToBookStoryResponses(
            String memberId,
            List<BookStory> bookStoryList,
            Map<Long, Boolean> isLikedMap,
            Map<String, BookExternalDTO.BasicInfo> bookInfoMap,
            Map<String, BasicInfoWithFollow> authorInfoMap
    ) {

        return bookStoryList.stream()
                .map(bookStory -> BookStoryConverter.toBookStoryDetailDTO(
                        bookStory,
                        memberId,
                        bookInfoMap.get(bookStory.getBookId()),
                        authorInfoMap.get(bookStory.getMemberId()),
                        isLikedMap.getOrDefault(bookStory.getId(), false)
                )).toList();
    }

    /**
     * 스코프에 해당하는 클럽 정보 조회
     */
    private BasicInfo findClubInfoForScope(
            BookStoryRequestDTO.BookStoryScope scope,
            Long clubId,
            ClubList clubList
    ) {
        if (scope == BookStoryRequestDTO.BookStoryScope.CLUB) {
            return clubList.getClubList().stream()
                    .filter(club -> club.getClubId().equals(clubId))
                    .findFirst()
                    .orElseThrow(() -> new BookStoryException(BookStoryErrorStatus.CLUB_ACCESS_DENIED));
        }
        return null;
    }

    /**
     * 특정 책으로 작성된 책 이야기 목록을 조회합니다.
     *
     * @param memberId 조회하는 회원의 ID
     * @param bookId   책 ID
     * @param cursorId 무한 스크롤을 위한 커서 ID
     * @return bookId에 따른 책 이야기 목록 DTO
     */
    public BookStoryResponseDTO.BookStoryList fetchBookStoriesByBook(
            String memberId,
            String bookId,
            Long cursorId
    ) {

        // 1. BookStory 리스트 조회
        CursorResult<BookStory> bookStoryCursorResult = CursorPagingHelper.getPage(
                (pageSize) -> bookStoryQueryService.retrieveBookStories(
                        bookId, cursorId, pageSize
                ),
                BookStory::getId,
                DEFAULT_PAGE_SIZE
        );
        List<BookStory> bookStories = bookStoryCursorResult.content();

        // 좋아요 여부 조회
        Map<Long, Boolean> isLikedMap = fetchLikedInfo(memberId, bookStories);

        // 책 정보 조회
        Map<String, BookExternalDTO.BasicInfo> bookInfoMap = fetchBookInfo(bookStories);

        // 작성자 정보 조회
        Map<String, BasicInfoWithFollow> authorInfoMap = fetchAuthorInfo(memberId, bookStories);

        // DTO 변환
        List<BookStoryResponseDTO.BasicInfo> basicInfoList = convertToBookStoryResponses(memberId,
                bookStories, isLikedMap, bookInfoMap, authorInfoMap);

        return BookStoryResponseDTO.BookStoryList.builder()
                .basicInfoList(basicInfoList)
                .hasNext(bookStoryCursorResult.hasNext())
                .nextCursor(bookStoryCursorResult.nextCursor())
                .pageSize(DEFAULT_PAGE_SIZE)
                .build();
    }
}