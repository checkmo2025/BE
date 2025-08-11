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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        if (bookStories == null || bookStories.isEmpty()) {
            return Map.of();
        }

        List<Long> bookStoryIds = bookStories.stream()
                .map(BookStory::getId)
                .distinct()
                .toList();

        // 배치로 좋아요한 BookStory ID 목록 조회
        List<Long> likedBookStoryIds = bookStoryLikedRepository.findLikedBookStoryIdsByMemberIdAndBookStoryIds(memberId, bookStoryIds);
        Set<Long> likedIdSet = new HashSet<>(likedBookStoryIds);

        // 모든 BookStory에 대해 좋아요 여부 매핑
        return bookStoryIds.stream()
                .collect(Collectors.toMap(
                        bookStoryId -> bookStoryId,
                        likedIdSet::contains
                ));
    }

    @Override
    public ClubSharedDTO.MyClubList findMyClubs(String memberId) {
        return clubQueryFacade.getMyClubListForShare(memberId);
    }

    @Override
    public Map<String, BookSharedDTO.BasicInfoDTO> findBookInfos(List<BookStory> bookStories) {
        if (bookStories == null || bookStories.isEmpty()) {
            return Map.of();
        }

        List<String> bookIds = bookStories.stream()
                .map(BookStory::getBookId)
                .distinct()
                .toList();

        // 배치로 책 정보 조회
        return bookQueryFacade.getBookBasicInfoMapForShare(bookIds);
    }

    @Override
    public Map<String, MemberSharedDTO.WithFollowStatusDTO> findAuthorInfos(String currentMemberId, List<BookStory> bookStories) {
        if (bookStories == null || bookStories.isEmpty()) {
            return Map.of();
        }

        List<String> memberIds = bookStories.stream()
                .map(BookStory::getMemberId)
                .distinct()
                .toList();

        // 배치로 회원 정보와 팔로우 상태 조회
        return memberQueryFacade.getMemberWithFollowStatusMapForShare(memberIds, currentMemberId);
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
