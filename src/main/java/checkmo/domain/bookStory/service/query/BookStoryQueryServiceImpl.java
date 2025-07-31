package checkmo.domain.bookStory.service.query;

import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.book.facade.BookQueryFacade;
import checkmo.domain.bookStory.converter.BookStoryConverter;
import checkmo.domain.bookStory.entity.BookStory;
import checkmo.domain.bookStory.repository.BookStoryLikedRepository;
import checkmo.domain.bookStory.repository.BookStoryRepository;
import checkmo.domain.bookStory.web.dto.BookStoryRequestDTO;
import checkmo.global.dto.BookSharedDTO;
import checkmo.global.dto.BookStorySharedDTO;
import checkmo.domain.club.facade.ClubQueryFacade;
import checkmo.domain.member.facade.MemberQueryFacade;
import checkmo.global.dto.ClubSharedDTO;
import checkmo.global.dto.MemberSharedDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookStoryQueryServiceImpl implements BookStoryQueryService {

    private final MemberQueryFacade memberQueryFacade;
    private final BookQueryFacade bookQueryFacade;
    private final ClubQueryFacade clubQueryFacade;
    private final BookStoryRepository bookStoryRepository;
    private final BookStoryLikedRepository bookStoryLikedRepository;

    @Override
    public List<BookStory> findBookStories(String memberId, BookStoryRequestDTO.BookStoryScope scope, Long clubId, String targetMemberId, Long cursorId, int pageSize) {
        // TODO: 현재 내부에서 외부 도메인의 Q클래스를 호출해서 QueryDSL 사용하고 있는데, 이 부분도 리팩토링 필요
        return bookStoryRepository.searchBookStories(memberId, scope, clubId, targetMemberId, cursorId, pageSize + 1);
    }

    @Override
    public Map<Long, Boolean> checkLikesForBookStories(String memberId, List<BookStory> bookStories) {
        // TODO: 나중에 리팩토링으로 N+1 문제 해결
        return bookStories.stream()
                .collect(Collectors.toMap(
                        BookStory::getId,
                        story -> bookStoryLikedRepository.existsByMemberIdAndBookStoryId(memberId, story.getId())
                ));
    }

    @Override
    public ClubSharedDTO.MyClubList findMyClubs(String memberId) {
        return clubQueryFacade.getMyClubListForShare(memberId);
    }

    @Override
    public Map<String, BookSharedDTO.BasicInfoDTO> findBookInfos(List<BookStory> bookStories) {
        // TODO: 나중에 리팩토링으로 N+1 문제 해결, 외부 도메인이라 QueryDSL에서 fetchJoin 사용 하지않고 하려니 장난 아니게 어려움..
        return bookStories.stream()
                .map(BookStory::getBookId)
                .distinct()
                .collect(Collectors.toMap(
                        bookId -> bookId,
                        bookQueryFacade::getBookBasicInfoForShare
                ));
    }

    @Override
    public Map<String, MemberSharedDTO.WithFollowStatusDTO> findAuthorInfos(String currentMemberId, List<BookStory> bookStories) {
        // TODO: 나중에 리팩토링으로 N+1 문제 해결, 외부 도메인이라 QueryDSL에서 fetchJoin 사용 하지않고 하려니 장난 아니게 어려움..
        return bookStories.stream()
                .map(BookStory::getMemberId)
                .distinct()
                .collect(Collectors.toMap(
                        authorId -> authorId,
                        authorId -> memberQueryFacade.getMemberWithFollowStatusForShare(authorId, currentMemberId)
                ));
    }

    @Override
    public BookStorySharedDTO.BookStoryResponse getBookStory(String memberId, Long bookStoryId) {
        BookStory bookStory = bookStoryRepository.findById(bookStoryId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.BOOK_STORY_NOT_FOUND));

        return BookStoryConverter.fromBookStoryToResponse(
                bookStory,
                memberId,
                bookQueryFacade.getBookBasicInfoForShare(bookStory.getBookId()),
                memberQueryFacade.getMemberWithFollowStatusForShare(bookStory.getMemberId(), memberId),
                bookStoryLikedRepository.existsByMemberIdAndBookStoryId(memberId, bookStory.getId())
        );
    }
}
