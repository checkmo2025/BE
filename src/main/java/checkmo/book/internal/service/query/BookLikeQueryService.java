package checkmo.book.internal.service.query;

import checkmo.book.internal.converter.BookConverter;
import checkmo.book.internal.entity.Book;
import checkmo.book.internal.entity.BookLiked;
import checkmo.book.internal.repository.BookLikedRepository;
import checkmo.book.web.dto.BookResponseDTO;
import checkmo.common.template.CursorPagingHelper;
import checkmo.common.template.CursorResult;
import checkmo.member.MemberAPI;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookLikeQueryService {

    private static final int DEFAULT_PAGE_SIZE = 18;

    private final BookLikedRepository bookLikedRepository;
    private final MemberAPI memberAPI;

    public BookResponseDTO.LikedBookList retrieveMyLikedBooks(Long memberId, Long cursorId) {
        return retrieveLikedBooksByMemberId(memberId, memberId, cursorId);
    }

    public BookResponseDTO.LikedBookList retrieveMemberLikedBooks(String memberNickname, Long currentMemberId, Long cursorId) {
        Long targetMemberId = Long.valueOf(memberAPI.fetchMemberId(memberNickname));
        memberAPI.validateProfileAccessible(toMemberApiId(currentMemberId), toMemberApiId(targetMemberId));
        return retrieveLikedBooksByMemberId(targetMemberId, currentMemberId, cursorId);
    }

    private BookResponseDTO.LikedBookList retrieveLikedBooksByMemberId(Long memberId, Long currentMemberId, Long cursorId) {
        CursorResult<BookLiked> cursorResult = CursorPagingHelper.getPage(
                size -> retrieveBookLikes(memberId, cursorId, size),
                BookLiked::getId,
                DEFAULT_PAGE_SIZE
        );

        List<String> bookIds = cursorResult.content().stream()
                .map(liked -> liked.getBook().getId())
                .toList();
        Set<String> likedBookIdSet = bookLikedRepository.findLikedBookIdSet(currentMemberId, bookIds);

        List<BookResponseDTO.LikedBookInfo> books = cursorResult.content().stream()
                .map(liked -> toLikedBookInfo(liked, likedBookIdSet))
                .toList();

        return BookResponseDTO.LikedBookList.builder()
                .books(books)
                .hasNext(cursorResult.hasNext())
                .nextCursor(cursorResult.nextCursor())
                .build();
    }

    private List<BookLiked> retrieveBookLikes(Long memberId, Long cursorId, int pageSize) {
        Pageable pageable = PageRequest.of(0, pageSize);
        if (cursorId == null) {
            return bookLikedRepository.findByMemberIdOrderByIdDesc(memberId, pageable);
        }
        return bookLikedRepository.findByMemberIdAndIdLessThanOrderByIdDesc(memberId, cursorId, pageable);
    }

    private BookResponseDTO.LikedBookInfo toLikedBookInfo(BookLiked liked, Set<String> likedBookIdSet) {
        Book book = liked.getBook();
        return BookConverter.toLikedBookInfo(book, likedBookIdSet.contains(book.getId()));
    }

    private String toMemberApiId(Long memberId) {
        return String.valueOf(memberId);
    }
}
