package checkmo.bookStory.internal;

import checkmo.book.BookAPI;
import checkmo.book.BookExternalDTO;
import checkmo.bookStory.BookStoryAPI;
import checkmo.bookStory.BookStoryExternalDTO;
import checkmo.bookStory.internal.converter.BookStoryConverter;
import checkmo.bookStory.internal.entity.BookStory;
import checkmo.bookStory.internal.entity.Comment;
import checkmo.bookStory.internal.exception.BookStoryErrorStatus;
import checkmo.bookStory.internal.exception.BookStoryException;
import checkmo.bookStory.internal.service.query.BookStoryQueryService;
import checkmo.bookStory.web.dto.BookStoryRequestDTO;
import checkmo.clubManagement.ClubManagementAPI;
import checkmo.clubManagement.ClubManagementExternalDTO;
import checkmo.common.template.CursorPagingHelper;
import checkmo.common.template.CursorResult;
import checkmo.member.MemberAPI;
import checkmo.member.MemberExternalDTO;
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
public class BookStoryAPIImpl implements BookStoryAPI {

    // 페이징 기본 크기 상수
    public static final int DEFAULT_PAGE_SIZE = 10;

    // Domain level 3
    private final ClubManagementAPI clubManagementAPI;
    // Domain level 2
    private final MemberAPI memberAPI;
    // Domain level 1
    private final BookAPI bookAPI;

    private final BookStoryQueryService bookStoryQueryService;

    @Override
    public BookStoryExternalDTO.DetailWithComment fetchBookStoryDetailInfo(String memberId, Long bookStoryId) {
        // 1. Service에서 BookStory 엔티티 조회
        BookStory bookStory = bookStoryQueryService.findBookStoryById(bookStoryId);

        // 2. 책 정보 조회
        BookExternalDTO.BasicInfo bookInfo = bookAPI.fetchBookBasicInfo(bookStory.getBookId());

        // 3. 작성자 정보 조회
        MemberExternalDTO.WithFollowStatus authorInfo = memberAPI.getMemberWithFollowStatusForShare(
                bookStory.getMemberId(), memberId);

        // 4. 좋아요 여부 조회
        Boolean isLiked = bookStoryQueryService.checkLikesForBookStories(memberId, List.of(bookStory))
                .getOrDefault(bookStory.getId(), false);

        // 5. 댓글 조회 (부모 댓글만, 대댓글은 컨버터에서 DTO 변환 시 자동 포함)
        List<Comment> comments = bookStoryQueryService.findCommentsByBookStoryId(bookStoryId);

        // 6. 댓글 작성자들 정보 조회
        // 6-1. 댓글 작성자들 Id 목록 조회 (Set으로 중복 제거)
        Set<String> commentMemberIds = comments.stream()
                .flatMap(comment -> Stream.concat(
                        Stream.of(comment.getMemberId()),
                        comment.getChildrenComment().stream().map(Comment::getMemberId)
                ))
                .collect(Collectors.toSet());

        // 6-2. 댓글 작성자들 정보를 배치 조회 (6-1에서 조회된 정보를 리스트로 변환 후 한번에 조회)
        Map<String, MemberExternalDTO.BasicInfo> commentMemberInfoMap =
                commentMemberIds.isEmpty() ? Map.of() :
                        memberAPI.getMemberBasicInfoMapForShare(new ArrayList<>(commentMemberIds));

        // 7. 댓글 DTO 변환
        List<BookStoryExternalDTO.CommentDetail> commentDTOList =
                BookStoryConverter.toCommentDetailList(comments, memberId, commentMemberInfoMap);

        // 8. DTO 변환
        return BookStoryConverter.toBookStoryDetailWithComment(
                bookStory,
                memberId,
                bookInfo,
                authorInfo,
                isLiked,
                commentDTOList
        );
    }

    @Override
    public BookStoryExternalDTO.BookStoryList retrieveBookStories(
            String memberId,
            BookStoryRequestDTO.BookStoryScope scope,
            Long clubId, String targetMemberNickname,
            Long cursorId
    ) {
        // 1. targetMemberId 조회 (SCOPE=TARGET인 경우)
        String targetMemberId = resolveTargetMemberId(scope, targetMemberNickname);

        // 2. BookStory 리스트 조회
        CursorResult<BookStory> bookStoryCursorResult = CursorPagingHelper.getPage(
                (pageSize) -> bookStoryQueryService.findBookStories(
                        memberId, scope, clubId, targetMemberId, cursorId, pageSize
                ),
                BookStory::getId,
                DEFAULT_PAGE_SIZE
        );
        List<BookStory> bookStories = bookStoryCursorResult.content();

        /*
        List<BookStory> bookStories = bookStoryQueryService.findBookStories(memberId, scope, clubId, targetMemberId,
                cursorId, DEFAULT_PAGE_SIZE);
        boolean hasNext = bookStories.size() > DEFAULT_PAGE_SIZE;
        Long nextCursor = null;
        if (hasNext) {
            bookStories.removeLast();
            nextCursor = bookStories.getLast().getId();
        }
        */

        // 좋아요 여부 조회
        Map<Long, Boolean> isLikedMap = fetchLikedInfo(memberId, bookStories);

        // 책 정보 조회
        Map<String, BookExternalDTO.BasicInfo> bookInfoMap = fetchBookInfo(bookStories);

        // 작성자 정보 조회
        Map<String, MemberExternalDTO.WithFollowStatus> authorInfoMap = fetchAuthorInfo(memberId, bookStories);

        // DTO 변환
        List<BookStoryExternalDTO.BookStoryDetail> bookStoryDetailList = convertToBookStoryResponses(memberId,
                bookStories, isLikedMap, bookInfoMap, authorInfoMap);

        // 클럽 정보 조회
        ClubManagementExternalDTO.MyClubList myClubList = clubManagementAPI.getMyClubListForShare(memberId);
        ClubManagementExternalDTO.MyClubInfo myClubInfo = findClubInfoForScope(scope, clubId, myClubList);

        // 스코프 정보 변환 및 최종 응답 DTO 변환
        var scopeInfo = BookStoryExternalDTO.ScopeInfo.builder()
                .scope(scope)
                .selectedClub(myClubInfo)
                .build();

        return BookStoryExternalDTO.BookStoryList.builder()
                .scopeInfo(scopeInfo)
                .memberClubList(myClubList)
                .bookStoryDetailList(bookStoryDetailList)
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
            return memberAPI.getMemberIdByNickname(targetMemberNickname);
        }
        return null;
    }

    /**
     * 좋아요 정보 배치 조회
     */
    private Map<Long, Boolean> fetchLikedInfo(String memberId, List<BookStory> bookStories) {
        return bookStoryQueryService.checkLikesForBookStories(memberId, bookStories);
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
    private Map<String, MemberExternalDTO.WithFollowStatus> fetchAuthorInfo(
            String memberId,
            List<BookStory> bookStories
    ) {
        List<String> targetMemberIds = bookStories.stream()
                .map(checkmo.bookStory.internal.entity.BookStory::getMemberId)
                .distinct()
                .toList();
        return memberAPI.getMemberWithFollowStatusMapForShare(targetMemberIds, memberId);
    }

    /**
     * BookStory 엔티티들을 Response DTO로 변환
     */
    private List<BookStoryExternalDTO.BookStoryDetail> convertToBookStoryResponses(
            String memberId,
            List<BookStory> bookStoryList,
            Map<Long, Boolean> isLikedMap,
            Map<String, BookExternalDTO.BasicInfo> bookInfoMap,
            Map<String, MemberExternalDTO.WithFollowStatus> authorInfoMap
    ) {

        return bookStoryList.stream()
                .map(bookStory -> BookStoryConverter.toBookStoryDetailDTO(
                        bookStory,
                        memberId,
                        bookInfoMap.get(bookStory.getBookId()),
                        authorInfoMap.get(bookStory.getMemberId()),
                        isLikedMap.getOrDefault(bookStory.getId(), false),
                        bookStory.getCommentsCount()
                )).toList();
    }

    /**
     * 스코프에 해당하는 클럽 정보 조회
     */
    private ClubManagementExternalDTO.MyClubInfo findClubInfoForScope(
            BookStoryRequestDTO.BookStoryScope scope,
            Long clubId,
            ClubManagementExternalDTO.MyClubList myClubList
    ) {
        if (scope == BookStoryRequestDTO.BookStoryScope.CLUB) {
            return myClubList.getClubList().stream()
                    .filter(club -> club.getClubId().equals(clubId))
                    .findFirst()
                    .orElseThrow(() -> new BookStoryException(BookStoryErrorStatus.CLUB_ACCESS_DENIED));
        }
        return null;
    }
}
