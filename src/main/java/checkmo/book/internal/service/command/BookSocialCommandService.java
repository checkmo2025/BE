package checkmo.book.internal.service.command;

import checkmo.book.BookExternalDTO;
import checkmo.book.internal.entity.Book;
import checkmo.book.internal.entity.BookLiked;
import checkmo.book.internal.exception.BookErrorStatus;
import checkmo.book.internal.exception.BookException;
import checkmo.book.internal.repository.BookRepository;
import checkmo.book.internal.repository.BookLikedRepository;
import checkmo.book.web.dto.BookResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BookSocialCommandService {

    private final BookCommandService bookCommandService;
    private final BookRepository bookRepository;
    private final BookLikedRepository bookLikedRepository;

    /**
     * 책 좋아요를 토글(추가/제거)합니다.
     *
     * @param memberId 좋아요를 누르는 회원 ID
     * @param request  좋아요 대상 책 정보
     * @return 토글 결과 DTO
     */
    public BookResponseDTO.LikeResult toggleLikeOnBook(String memberId, BookExternalDTO.BookCreate request) {
        Book book = retrieveOrCreateBook(request);

        BookLiked existingLike = bookLikedRepository.findByBookAndMember(book.getId(), memberId)
                .orElse(null);
        if (existingLike != null) {
            deleteBookLiked(book, existingLike);
            syncBookLikes(book.getId());
            return toLikeResult(book.getId(), false);
        }

        if (!bookLikedRepository.existsByMemberIdAndBookId(memberId, book.getId())) {
            addBookLiked(book, memberId);
        }
        syncBookLikes(book.getId());
        return toLikeResult(book.getId(), true);
    }

    private Book retrieveOrCreateBook(BookExternalDTO.BookCreate request) {
        if (request == null || request.getIsbn() == null || request.getIsbn().isBlank()) {
            throw new BookException(BookErrorStatus.BOOK_INVALID_REQUEST);
        }

        return bookRepository.findById(request.getIsbn())
                .orElseGet(() -> {
                    String bookId = bookCommandService.saveBook(request);
                    return bookRepository.findById(bookId)
                            .orElseThrow(() -> new BookException(BookErrorStatus.BOOK_NOT_FOUND));
                });
    }

    private void deleteBookLiked(Book book, BookLiked bookLiked) {
        bookLikedRepository.delete(bookLiked);
        book.removeBookLiked(bookLiked);
    }

    private void addBookLiked(Book book, String memberId) {
        BookLiked bookLiked = BookLiked.builder()
                .book(book)
                .memberId(memberId)
                .build();
        bookLikedRepository.save(bookLiked);
        book.addBookLiked(bookLiked);
    }

    private void syncBookLikes(String bookId) {
        bookRepository.syncLikesByBookId(bookId);
    }

    private BookResponseDTO.LikeResult toLikeResult(String bookId, boolean liked) {
        Book refreshedBook = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookException(BookErrorStatus.BOOK_NOT_FOUND));
        return BookResponseDTO.LikeResult.builder()
                .isbn(refreshedBook.getId())
                .liked(liked)
                .likes(refreshedBook.getLikes())
                .build();
    }
}
