package checkmo.bookStory.internal.service;

import checkmo.book.BookAPI;
import checkmo.book.BookExternalDTO;
import checkmo.bookStory.internal.converter.BookStoryConverter;
import checkmo.bookStory.internal.entity.BookStory;
import checkmo.bookStory.internal.entity.Comment;
import checkmo.bookStory.internal.repository.projection.BookStorySitemapProjection;
import checkmo.bookStory.internal.service.query.BookStoryViewCacheService;
import checkmo.bookStory.internal.service.query.BookStoryQueryService;
import checkmo.bookStory.web.dto.BookStoryRequestDTO;
import checkmo.bookStory.web.dto.BookStoryResponseDTO;
import checkmo.bookStory.web.dto.BookStoryResponseDTO.CommentInfo;
import checkmo.bookStory.web.dto.BookStoryResponseDTO.DetailInfo;
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
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookStoryQueryFacade {

    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int ADMIN_PAGE_SIZE = 12;
    private static final int DEFAULT_SITEMAP_LIMIT = 1000;
    private static final int MAX_SITEMAP_LIMIT = 5000;

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
    public DetailInfo fetchBookStoryDetailInfo(Long memberId, Long bookStoryId) {
        return fetchBookStoryDetailInfoInternal(memberId, bookStoryId, true, true);
    }

    public DetailInfo fetchBookStoryDetailInfoForAdmin(Long memberId, Long bookStoryId) {
        return fetchBookStoryDetailInfoInternal(memberId, bookStoryId, false, false);
    }

    private DetailInfo fetchBookStoryDetailInfoInternal(
            Long memberId,
            Long bookStoryId,
            boolean increaseViewCount,
            boolean validateBlockRelation
    ) {
        // 1. Service에서 BookStory 엔티티 조회
        BookStory bookStory = bookStoryQueryService.retrieveAccessibleBookStory(memberId, bookStoryId);

        if (validateBlockRelation) {
            memberAPI.validateProfileAccessible(memberId, bookStory.getMemberId());
        }

        // 2. 사용자 상세 조회에서만 조회 수 카운트 증가
        if (increaseViewCount && bookStory.isPublished()) {
            viewCacheService.incrementViewCount(bookStoryId, memberId);
        }

        // 3. 책 정보 조회
        BookExternalDTO.BasicInfo bookInfo = bookAPI.fetchBookBasicInfo(bookStory.getBookId());

        // 4. 작성자 정보 조회
        BasicInfoWithFollow authorInfo = memberAPI.fetchMemberBasicInfoWithFollow(
                bookStory.getMemberId(),
                memberId
        );

        // 5. 좋아요 여부 조회
        Boolean isLiked = bookStoryQueryService.checkBookStoryLikeByMemberId(memberId, List.of(bookStory))
                .getOrDefault(bookStory.getId(), false);

        // 6. 댓글 조회 (부모 댓글만, 대댓글은 컨버터에서 DTO 변환 시 자동 포함)
        List<Comment> comments = bookStoryQueryService.retrieveBookStoryComments(bookStoryId);

        // 7. 댓글 작성자들 정보 조회
        // 7-1. 댓글 작성자들 Id 목록 조회 (Set으로 중복 제거)
        Set<Long> commentMemberIds = comments.stream()
                .flatMap(comment -> Stream.concat(
                        Stream.of(comment.getMemberId()),
                        comment.getChildrenComment().stream().map(Comment::getMemberId)
                ))
                .collect(Collectors.toSet());

        // 7-2. 댓글 작성자들 정보를 배치 조회 (6-1에서 조회된 정보를 리스트로 변환 후 한번에 조회)
        Map<Long, MemberExternalDTO.BasicInfo> commentMemberInfoMap =
                commentMemberIds.isEmpty() ? Map.of() :
                        memberAPI.fetchMemberBasicInfoByMemberIds(new ArrayList<>(commentMemberIds));

        // 8. 댓글 DTO 변환
        Set<Long> blockedMemberIds = Set.copyOf(memberAPI.fetchBlockRelatedMemberIds(memberId));
        List<CommentInfo> commentDTOList =
                BookStoryConverter.toCommentDetailList(comments, memberId, commentMemberInfoMap, blockedMemberIds);

        // 9. 책이야기 작성자의 이전, 다음 책이야기 아이디 조회
        var bookStoryPrevNextProjection = bookStory.isPublished()
                ? bookStoryQueryService.retrievePrevNextBookStoryId(bookStory.getMemberId(), bookStoryId)
                : null;

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
     * 전체 책이야기 목록을 조회합니다.
     */
    public BookStoryResponseDTO.BookStoryList fetchAllBookStories(Long memberId, Long cursorId) {
        return fetchBookStoriesInternal(memberId, BookStoryRequestDTO.BookStoryScope.ALL, null, null, cursorId);
    }

    /**
     * 내가 작성한 책이야기 목록을 조회합니다.
     */
    public BookStoryResponseDTO.BookStoryList fetchMyBookStories(Long memberId, Long cursorId) {
        return fetchBookStoriesInternal(memberId, BookStoryRequestDTO.BookStoryScope.MY, null, null, cursorId);
    }

    /**
     * 팔로우한 회원들의 책이야기 목록을 조회합니다.
     */
    public BookStoryResponseDTO.BookStoryList fetchFollowingBookStories(Long memberId, Long cursorId) {
        return fetchBookStoriesInternal(memberId, BookStoryRequestDTO.BookStoryScope.FOLLOWING, null, null, cursorId);
    }

    /**
     * 특정 회원의 책이야기 목록을 조회합니다.
     */
    public BookStoryResponseDTO.BookStoryList fetchMemberBookStories(
            Long memberId,
            String targetNickname,
            Long cursorId
    ) {
        Long targetMemberId = memberAPI.fetchMemberId(targetNickname);
        memberAPI.validateProfileAccessible(memberId, targetMemberId);
        return fetchBookStoriesInternal(memberId, BookStoryRequestDTO.BookStoryScope.TARGET, null, targetMemberId, cursorId);
    }

    /**
     * 특정 클럽 멤버들의 책이야기 목록을 조회합니다.
     */
    public BookStoryResponseDTO.BookStoryList fetchClubBookStories(
            Long memberId,
            Long clubId,
            Long cursorId
    ) {
        return fetchBookStoriesInternal(memberId, BookStoryRequestDTO.BookStoryScope.CLUB, clubId, null, cursorId);
    }

    public BookStoryResponseDTO.SitemapPage fetchBookStorySitemap(Long cursorId, Integer limit) {
        int pageSize = normalizeSitemapLimit(limit);
        CursorResult<BookStorySitemapProjection> sitemapCursorResult = CursorPagingHelper.getPage(
                requestedPageSize -> bookStoryQueryService.retrieveSitemapItems(cursorId, requestedPageSize),
                BookStorySitemapProjection::getId,
                pageSize
        );

        return BookStoryResponseDTO.SitemapPage.builder()
                .items(sitemapCursorResult.content().stream()
                        .map(this::toSitemapItem)
                        .toList())
                .hasNext(sitemapCursorResult.hasNext())
                .nextCursor(sitemapCursorResult.nextCursor())
                .pageSize(pageSize)
                .build();
    }

    private BookStoryResponseDTO.SitemapItem toSitemapItem(BookStorySitemapProjection projection) {
        return BookStoryResponseDTO.SitemapItem.builder()
                .id(projection.getId())
                .updatedAt(projection.getUpdatedAt())
                .build();
    }

    private int normalizeSitemapLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_SITEMAP_LIMIT;
        }
        if (limit < 1) {
            throw new IllegalArgumentException("Sitemap limit must be at least 1.");
        }
        return Math.min(limit, MAX_SITEMAP_LIMIT);
    }

    /**
     * scope에 따라 책 이야기 목록을 조회합니다. 비즈니스 로직을 Facade에서 처리하여 컨트롤러는 단순히 호출만 담당
     *
     * @param memberId       조회하는 회원의 ID
     * @param scope          조회 범위 ("ALL", "MY", "FOLLOWING", "CLUB", "TARGET")
     * @param clubId         클럽 ID (scope가 "CLUB"일 때 필수)
     * @param targetMemberId 대상 회원 ID (scope가 "TARGET"일 때 필수)
     * @param cursorId       커서 ID
     * @return scope에 따른 책 이야기 목록 DTO
     */
    private BookStoryResponseDTO.BookStoryList fetchBookStoriesInternal(
            Long memberId,
            BookStoryRequestDTO.BookStoryScope scope,
            Long clubId,
            Long targetMemberId,
            Long cursorId
    ) {
        List<Long> excludedMemberIds = memberId != null && requiresBlockFilter(scope)
                ? memberAPI.fetchBlockRelatedMemberIds(memberId)
                : List.of();
        List<Long> followingMemberIds = memberId != null && scope == BookStoryRequestDTO.BookStoryScope.FOLLOWING
                ? memberAPI.fetchFollowingIds(memberId)
                : List.of();

        // 1. BookStory 리스트 조회
        CursorResult<BookStory> bookStoryCursorResult = CursorPagingHelper.getPage(
                (pageSize) -> bookStoryQueryService.retrieveBookStories(
                        memberId, excludedMemberIds, followingMemberIds, scope, clubId, targetMemberId, cursorId, pageSize
                ),
                BookStory::getId,
                DEFAULT_PAGE_SIZE
        );
        List<BookStory> bookStories = bookStoryCursorResult.content();

        // 2. 좋아요 여부 조회
        Map<Long, Boolean> isLikedMap = fetchLikedInfo(memberId, bookStories);

        // 3. 책 정보 조회
        Map<String, BookExternalDTO.BasicInfo> bookInfoMap = fetchBookInfo(bookStories);

        // 4. 작성자 정보 조회
        Map<Long, BasicInfoWithFollow> authorInfoMap = fetchAuthorInfo(memberId, bookStories);

        // 5. DTO 변환
        List<BookStoryResponseDTO.BasicInfo> basicInfoList = convertToBookStoryResponses(memberId,
                bookStories, isLikedMap, bookInfoMap, authorInfoMap);

        return BookStoryResponseDTO.BookStoryList.builder()
                .basicInfoList(basicInfoList)
                .hasNext(bookStoryCursorResult.hasNext())
                .nextCursor(bookStoryCursorResult.nextCursor())
                .pageSize(DEFAULT_PAGE_SIZE)
                .build();
    }

    private boolean requiresBlockFilter(BookStoryRequestDTO.BookStoryScope scope) {
        return scope == BookStoryRequestDTO.BookStoryScope.ALL
                || scope == BookStoryRequestDTO.BookStoryScope.FOLLOWING;
    }

    /**
     * 좋아요 정보 배치 조회
     */
    private Map<Long, Boolean> fetchLikedInfo(Long memberId, List<BookStory> bookStories) {
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
    private Map<Long, BasicInfoWithFollow> fetchAuthorInfo(
            Long memberId,
            List<BookStory> bookStories
    ) {
        List<Long> targetMemberIds = bookStories.stream()
                .map(checkmo.bookStory.internal.entity.BookStory::getMemberId)
                .distinct()
                .toList();
        return memberAPI.fetchMemberBasicInfoWithFollowByMemberId(targetMemberIds, memberId);
    }

    /**
     * BookStory 엔티티들을 Response DTO로 변환
     */
    private List<BookStoryResponseDTO.BasicInfo> convertToBookStoryResponses(
            Long memberId,
            List<BookStory> bookStoryList,
            Map<Long, Boolean> isLikedMap,
            Map<String, BookExternalDTO.BasicInfo> bookInfoMap,
            Map<Long, BasicInfoWithFollow> authorInfoMap
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
     * 특정 책으로 작성된 책 이야기 목록을 조회합니다.
     *
     * @param memberId 조회하는 회원의 ID
     * @param bookId   책 ID
     * @param cursorId 무한 스크롤을 위한 커서 ID
     * @return bookId에 따른 책 이야기 목록 DTO
     */
    public BookStoryResponseDTO.BookStoryList fetchBookStoriesByBook(
            Long memberId,
            String bookId,
            Long cursorId
    ) {

        // 1. BookStory 리스트 조회
        List<Long> excludedMemberIds = memberId == null
                ? List.of()
                : memberAPI.fetchBlockRelatedMemberIds(memberId);
        CursorResult<BookStory> bookStoryCursorResult = CursorPagingHelper.getPage(
                (pageSize) -> bookStoryQueryService.retrieveBookStories(
                        bookId, excludedMemberIds, cursorId, pageSize
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
        Map<Long, BasicInfoWithFollow> authorInfoMap = fetchAuthorInfo(memberId, bookStories);

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

    public BookStoryResponseDTO.AdminBookStoryList fetchBookStoriesForAdmin(String keyword, int page) {
        int safePage = Math.max(page, 1);
        Page<BookStory> bookStoryPage =
                bookStoryQueryService.retrieveBookStoriesForAdmin(keyword, safePage - 1, ADMIN_PAGE_SIZE);

        List<BookStory> bookStories = bookStoryPage.getContent();
        List<Long> memberIds = bookStories.stream()
                .map(BookStory::getMemberId)
                .distinct()
                .toList();
        List<String> bookIds = bookStories.stream()
                .map(BookStory::getBookId)
                .distinct()
                .toList();

        Map<Long, MemberExternalDTO.DetailInfo> authorInfoMap =
                memberAPI.fetchMemberDetailInfoByMemberIds(memberIds);
        Map<String, BookExternalDTO.BasicInfo> bookInfoMap =
                bookAPI.fetchBookBasicInfoByBookIds(bookIds);

        List<BookStoryResponseDTO.AdminBasicInfo> basicInfoList = bookStories.stream()
                .map(bookStory -> BookStoryConverter.toAdminBasicInfo(
                        bookStory,
                        authorInfoMap.get(bookStory.getMemberId()),
                        bookInfoMap.get(bookStory.getBookId())
                ))
                .toList();

        return BookStoryResponseDTO.AdminBookStoryList.builder()
                .basicInfoList(basicInfoList)
                .page(safePage)
                .pageSize(bookStoryPage.getSize())
                .totalPages(bookStoryPage.getTotalPages())
                .totalElements(bookStoryPage.getTotalElements())
                .hasNext(bookStoryPage.hasNext())
                .build();
    }

}
