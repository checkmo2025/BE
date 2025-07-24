package checkmo.domain.bookStory.facade;

import checkmo.domain.bookStory.converter.BookStoryConverter;
import checkmo.domain.bookStory.entity.BookStory;
import checkmo.domain.bookStory.service.query.BookStoryQueryService;
import checkmo.domain.bookStory.web.dto.BookStoryRequestDTO;
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

    public static final int DEFAULT_PAGE_SIZE = 10;

    private final BookStoryQueryService bookStoryQueryService;

    @Override
    public BookStorySharedDTO.BookStoryResponse getBookStory(String memberId, Long bookStoryId) {
        return bookStoryQueryService.getBookStory(memberId, bookStoryId);
    }

    @Override
    public BookStorySharedDTO.BookStoryListResponse getBookStoriesByNickname(String memberId, String targetMemberNickname, Long cursorId) {
        return null;
    }

    @Override
    public BookStorySharedDTO.BookStoryListResponse getBookStoriesByScope(String memberId, BookStoryRequestDTO.BookStoryScope scope, Long clubId, Long cursorId) {
        // 1. 책이야기 목록 조회
        List<BookStory> bookStories = bookStoryQueryService.findBookStories(memberId, scope, clubId, cursorId, DEFAULT_PAGE_SIZE);

        // 2. Facade에서 페이지네이션 로직 처리
        boolean hasNext = bookStories.size() > DEFAULT_PAGE_SIZE;
        Long nextCursor = null;
        if (hasNext) {
            bookStories.removeLast();
            nextCursor = bookStories.getLast().getId();
        }

        // 3. Service를 통해 나머지 정보들을 가져옴
        Map<Long, Boolean> isLikedMap = bookStoryQueryService.checkLikesForBookStories(memberId, bookStories);
        Map<String, BookSharedDTO.BasicInfoDTO> bookInfoMap = bookStoryQueryService.findBookInfos(bookStories);
        Map<String, MemberSharedDTO.WithFollowStatusDTO> authorInfoMap = bookStoryQueryService.findAuthorInfos(memberId, bookStories);

        // 4. DTO 변환
        var list = bookStories.stream()
                .map(bookStory -> BookStoryConverter.fromBookStoryToResponse(
                        bookStory,
                        memberId,
                        bookInfoMap.get(bookStory.getBookId()),
                        authorInfoMap.get(bookStory.getMemberId()),
                        isLikedMap.getOrDefault(bookStory.getId(), false)
                )).toList();

        // 5. 클럽 정보 조회 (책 이야기 페이지에서 맨 위에 보여줄 정보)
        var myClubList = bookStoryQueryService.findMyClubs(memberId);

        ClubSharedDTO.MyClubInfoDTO myClubInfoDTO = null;

        // 5.1 클럽 스코프인 경우 클럽 정보 조회
        if (scope == BookStoryRequestDTO.BookStoryScope.CLUB) {
            myClubInfoDTO = myClubList.getClubList().stream()
                    .filter(club -> club.getClubId().equals(clubId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 클럽이거나 가입하지 않은 클럽입니다."));
        }

        // 6. 스코프 정보 변환 (CLUB 스코프인 경우 선택된 클럽 정보 포함)
        var scopeInfo = BookStoryConverter.fromScopeInfo(scope, myClubInfoDTO);

        // 7. 최종 응답 DTO 변환
        return BookStoryConverter.fromBookStoryResponses(list, hasNext, nextCursor, DEFAULT_PAGE_SIZE, scopeInfo, myClubList);
    }
}
