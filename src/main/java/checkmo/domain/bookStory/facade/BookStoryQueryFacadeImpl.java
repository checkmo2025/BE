package checkmo.domain.bookStory.facade;

import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.bookStory.converter.BookStoryConverter;
import checkmo.domain.bookStory.entity.BookStory;
import checkmo.domain.bookStory.entity.Comment;
import checkmo.domain.bookStory.service.query.BookStoryQueryService;
import checkmo.domain.bookStory.web.dto.BookStoryRequestDTO;
import checkmo.domain.book.facade.BookQueryFacade;
import checkmo.domain.club.facade.ClubQueryFacade;
import checkmo.domain.member.facade.MemberQueryFacade;
import checkmo.global.dto.BookSharedDTO;
import checkmo.global.dto.BookStorySharedDTO;
import checkmo.global.dto.ClubSharedDTO;
import checkmo.global.dto.MemberSharedDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookStoryQueryFacadeImpl implements BookStoryQueryFacade {

    // 페이징 기본 크기 상수
    public static final int DEFAULT_PAGE_SIZE = 10;

    // Domain level 3
    private final ClubQueryFacade clubQueryFacade;
    // Domain level 2
    private final MemberQueryFacade memberQueryFacade;
    // Domain level 1
    private final BookQueryFacade bookQueryFacade;

    // 자신의 QueryService
    private final BookStoryQueryService bookStoryQueryService;

    @Override
    public BookStorySharedDTO.BookStoryDetailResponse getBookStory(String memberId, Long bookStoryId) {
        // 1. Service에서 BookStory 엔티티 조회
        BookStory bookStory = bookStoryQueryService.findBookStoryById(bookStoryId);
        
        // 2. 책 정보 조회
        BookSharedDTO.BasicInfo bookInfo = bookQueryFacade.getBookBasicInfoForShare(bookStory.getBookId());

        // 3. 작성자 정보 조회
        MemberSharedDTO.WithFollowStatus authorInfo = memberQueryFacade.getMemberWithFollowStatusForShare(bookStory.getMemberId(), memberId);

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
        Map<String, MemberSharedDTO.BasicInfo> commentMemberInfoMap =
                commentMemberIds.isEmpty() ? Map.of() :
                memberQueryFacade.getMemberBasicInfoMapForShare(new ArrayList<>(commentMemberIds));
        
        // 7. 댓글 DTO 변환
        List<BookStorySharedDTO.CommentResponse> commentDTOList =
                BookStoryConverter.fromCommentsToResponses(comments, memberId, commentMemberInfoMap);
        
        // 8. DTO 변환
        return BookStoryConverter.fromBookStoryToDetailResponse(
                bookStory,
                memberId,
                bookInfo,
                authorInfo,
                isLiked,
                commentDTOList
        );
    }

    @Override
    public BookStorySharedDTO.BookStoryListResponse getBookStoriesByScope(String memberId, BookStoryRequestDTO.BookStoryScope scope, Long clubId, String targetMemberNickname, Long cursorId) {
        // 1. targetMemberId 조회 (SCOPE=TARGET인 경우)
        String targetMemberId = resolveTargetMemberId(scope, targetMemberNickname);
        
        // 2. BookStory 리스트 조회
        List<BookStory> bookStories = bookStoryQueryService.findBookStories(memberId, scope, clubId, targetMemberId, cursorId, DEFAULT_PAGE_SIZE);

        // 3. 페이징 처리
        boolean hasNext = bookStories.size() > DEFAULT_PAGE_SIZE;
        Long nextCursor = null;
        if (hasNext) {
            bookStories.removeLast();
            nextCursor = bookStories.getLast().getId();
        }

        // 4. 좋아요 여부 조회
        Map<Long, Boolean> isLikedMap = fetchLikedInfo(memberId, bookStories);

        // 5.책 정보 조회
        Map<String, BookSharedDTO.BasicInfo> bookInfoMap = fetchBookInfo(bookStories);

        // 6. 작성자 정보 조회
        Map<String, MemberSharedDTO.WithFollowStatus> authorInfoMap = fetchAuthorInfo(memberId, bookStories);

        // 7. DTO 변환
        List<BookStorySharedDTO.BookStoryResponse> bookStoryResponses = convertToBookStoryResponses(memberId, bookStories, isLikedMap, bookInfoMap, authorInfoMap);

        // 8. 클럽 정보 조회
        ClubSharedDTO.MyClubList myClubList = clubQueryFacade.getMyClubListForShare(memberId);
        ClubSharedDTO.MyClubInfo myClubInfo = findClubInfoForScope(scope, clubId, myClubList);

        // 9. 스코프 정보 변환 및 최종 응답 DTO 변환
        var scopeInfo = BookStoryConverter.fromScopeInfo(scope, myClubInfo);
        return BookStoryConverter.fromBookStoryResponses(bookStoryResponses, hasNext, nextCursor, DEFAULT_PAGE_SIZE, scopeInfo, myClubList);
    }

    /**
     * TARGET 스코프인 경우 닉네임으로 targetMemberId 조회
     */
    private String resolveTargetMemberId(BookStoryRequestDTO.BookStoryScope scope, String targetMemberNickname) {
        if (scope == BookStoryRequestDTO.BookStoryScope.TARGET) {
            return memberQueryFacade.getMemberIdByNickname(targetMemberNickname);
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
    private Map<String, BookSharedDTO.BasicInfo> fetchBookInfo(List<BookStory> bookStories) {
        List<String> bookIds = bookStories.stream()
                .map(BookStory::getBookId)
                .distinct()
                .toList();
        return bookQueryFacade.getBookBasicInfoMapForShare(bookIds);
    }

    /**
     * 작성자 정보 배치 조회
     */
    private Map<String, MemberSharedDTO.WithFollowStatus> fetchAuthorInfo(String memberId, List<BookStory> bookStories) {
        List<String> memberIds = bookStories.stream()
                .map(BookStory::getMemberId)
                .distinct()
                .toList();
        return memberQueryFacade.getMemberWithFollowStatusMapForShare(memberIds, memberId);
    }

    /**
     * BookStory 엔티티들을 Response DTO로 변환
     */
    private List<BookStorySharedDTO.BookStoryResponse> convertToBookStoryResponses(
            String memberId, 
            List<BookStory> bookStories, 
            Map<Long, Boolean> isLikedMap, 
            Map<String, BookSharedDTO.BasicInfo> bookInfoMap,
            Map<String, MemberSharedDTO.WithFollowStatus> authorInfoMap
    ) {
        
        return bookStories.stream()
                .map(bookStory -> {
                    // 부모 댓글 목록 가져오기
                    List<Comment> comments = bookStoryQueryService.findCommentsByBookStoryId(bookStory.getId());

                    // 대댓글의 갯수까지 한번에 계산
                    int totalCommentCount = comments.stream()
                            .mapToInt(comment -> 1 + comment.getChildrenComment().size())
                            .sum();

                    return BookStoryConverter.fromBookStoryToResponse(
                            bookStory,
                            memberId,
                            bookInfoMap.get(bookStory.getBookId()),
                            authorInfoMap.get(bookStory.getMemberId()),
                            isLikedMap.getOrDefault(bookStory.getId(), false),
                            totalCommentCount
                    );
                }).toList();
    }

    /**
     * 스코프에 해당하는 클럽 정보 조회
     */
    private ClubSharedDTO.MyClubInfo findClubInfoForScope(BookStoryRequestDTO.BookStoryScope scope, Long clubId, ClubSharedDTO.MyClubList myClubList) {
        if (scope == BookStoryRequestDTO.BookStoryScope.CLUB) {
            return myClubList.getClubList().stream()
                    .filter(club -> club.getClubId().equals(clubId))
                    .findFirst()
                    .orElseThrow(() -> new GeneralException(ErrorStatus.CLUB_NOT_FOUND));
        }
        return null;
    }
}
