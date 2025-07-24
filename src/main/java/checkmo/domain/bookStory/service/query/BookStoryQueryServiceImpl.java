package checkmo.domain.bookStory.service.query;

import checkmo.domain.book.facade.BookQueryFacade;
import checkmo.domain.bookStory.converter.BookStoryConverter;
import checkmo.domain.bookStory.entity.BookStory;
import checkmo.domain.bookStory.repository.BookStoryLikedRepository;
import checkmo.domain.bookStory.repository.BookStoryRepository;
import checkmo.domain.bookStory.web.dto.BookStoryRequestDTO;
import checkmo.domain.bookStory.web.dto.BookStoryResponseDTO;
import checkmo.domain.club.facade.ClubQueryFacade;
import checkmo.domain.member.facade.MemberQueryFacade;
import checkmo.global.dto.ClubSharedDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookStoryQueryServiceImpl implements BookStoryQueryService {

    public static final int DEFAULT_PAGE_SIZE = 20;

    private final MemberQueryFacade memberQueryFacade;
    private final BookQueryFacade bookQueryFacade;
    private final ClubQueryFacade clubQueryFacade;
    private final BookStoryRepository bookStoryRepository;
    private final BookStoryLikedRepository bookStoryLikedRepository;

    @Override
    public BookStoryResponseDTO.BookStoryResponse getBookStory(String memberId, Long bookStoryId) {
        BookStory bookStory = bookStoryRepository.findById(bookStoryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 책 이야기입니다."));

        return BookStoryConverter.fromBookStoryToResponse(
                bookStory,
                memberId,
                bookQueryFacade.getBookBasicInfoForShare(bookStory.getBookId()),
                memberQueryFacade.getMemberWithFollowStatusForShare(bookStory.getMemberId(), memberId),
                bookStoryLikedRepository.existsByMemberIdAndBookStoryId(memberId, bookStory.getId())
        );
    }

    @Override
    public BookStoryResponseDTO.BookStoryListResponse getMyBookStories(String memberId, String targetMemberNickname, Long cursorId) {
        return null;
    }

    @Override
    public BookStoryResponseDTO.BookStoryListResponse getBookStoriesByScope(String memberId, BookStoryRequestDTO.BookStoryScope scope, Long clubId, Long cursorId) {

        List<BookStory> bookStories = bookStoryRepository.searchBookStories(memberId, scope, clubId, cursorId, DEFAULT_PAGE_SIZE + 1);

        boolean hasNext = bookStories.size() > DEFAULT_PAGE_SIZE;
        Long nextCursor = null;

        if (hasNext) {
            bookStories.removeLast();
            nextCursor = bookStories.getLast().getId();
        }

        var list = bookStories.stream().map(
                bookStory ->
                        BookStoryConverter.fromBookStoryToResponse(
                                bookStory,
                                memberId,
                                bookQueryFacade.getBookBasicInfoForShare(bookStory.getBookId()),
                                memberQueryFacade.getMemberWithFollowStatusForShare(bookStory.getMemberId(), memberId),
                                bookStoryLikedRepository.existsByMemberIdAndBookStoryId(memberId, bookStory.getId())
                        )).toList();

        var myClubList = clubQueryFacade.getMyClubListForShare(memberId);
        
        ClubSharedDTO.MyClubInfoDTO myClubInfoDTO = null;
        
        // CLUB scope일 때만 선택된 클럽 정보를 찾음
        if (scope == BookStoryRequestDTO.BookStoryScope.CLUB) {
            myClubInfoDTO = myClubList.getClubList().stream()
                    .filter(club -> club.getClubId().equals(clubId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 클럽이거나 가입하지 않은 클럽입니다."));
        }

        var scopeInfo = BookStoryConverter.fromScopeInfo(scope, myClubInfoDTO);

        return BookStoryConverter.fromBookStoryResponses(list, hasNext, nextCursor, DEFAULT_PAGE_SIZE, scopeInfo, myClubList);
    }
}
