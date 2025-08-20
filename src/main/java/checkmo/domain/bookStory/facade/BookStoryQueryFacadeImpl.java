package checkmo.domain.bookStory.facade;

import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.bookStory.converter.BookStoryConverter;
import checkmo.domain.bookStory.entity.BookStory;
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

import java.util.List;
import java.util.Map;

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
    public BookStorySharedDTO.BookStoryResponse getBookStory(String memberId, Long bookStoryId) {
        // 1. Service에서 BookStory 엔티티 조회
        BookStory bookStory = bookStoryQueryService.findBookStoryById(bookStoryId);
        
        // 2. 책 정보 조회
        BookSharedDTO.BasicInfoDTO bookInfo = bookQueryFacade.getBookBasicInfoForShare(bookStory.getBookId());

        // 3. 작성자 정보 조회
        MemberSharedDTO.WithFollowStatusDTO authorInfo = memberQueryFacade.getMemberWithFollowStatusForShare(bookStory.getMemberId(), memberId);

        // 4. 좋아요 여부 조회
        Boolean isLiked = bookStoryQueryService.checkLikesForBookStories(memberId, List.of(bookStory))
                .getOrDefault(bookStory.getId(), false);
        
        // 5. DTO 변환
        return BookStoryConverter.fromBookStoryToResponse(
                bookStory,
                memberId,
                bookInfo,
                authorInfo,
                isLiked
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
        Map<String, BookSharedDTO.BasicInfoDTO> bookInfoMap = fetchBookInfo(bookStories);

        // 6. 작성자 정보 조회
        Map<String, MemberSharedDTO.WithFollowStatusDTO> authorInfoMap = fetchAuthorInfo(memberId, bookStories);

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
    private Map<String, BookSharedDTO.BasicInfoDTO> fetchBookInfo(List<BookStory> bookStories) {
        List<String> bookIds = bookStories.stream()
                .map(BookStory::getBookId)
                .distinct()
                .toList();
        return bookQueryFacade.getBookBasicInfoMapForShare(bookIds);
    }

    /**
     * 작성자 정보 배치 조회
     */
    private Map<String, MemberSharedDTO.WithFollowStatusDTO> fetchAuthorInfo(String memberId, List<BookStory> bookStories) {
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
            Map<String, BookSharedDTO.BasicInfoDTO> bookInfoMap,
            Map<String, MemberSharedDTO.WithFollowStatusDTO> authorInfoMap) {
        
        return bookStories.stream()
                .map(bookStory -> BookStoryConverter.fromBookStoryToResponse(
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
