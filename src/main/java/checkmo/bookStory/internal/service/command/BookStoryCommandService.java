package checkmo.bookStory.internal.service.command;

import checkmo.book.BookAPI;
import checkmo.bookStory.BookStoryEvent;
import checkmo.bookStory.internal.converter.BookStoryConverter;
import checkmo.bookStory.internal.entity.BookStory;
import checkmo.bookStory.internal.entity.BookStoryStatus;
import checkmo.bookStory.internal.exception.BookStoryErrorStatus;
import checkmo.bookStory.internal.exception.BookStoryException;
import checkmo.bookStory.internal.repository.BookStoryLikedRepository;
import checkmo.bookStory.internal.repository.BookStoryRepository;
import checkmo.bookStory.internal.repository.CommentImageRepository;
import checkmo.bookStory.internal.repository.CommentRepository;
import checkmo.bookStory.web.dto.BookStoryRequestDTO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BookStoryCommandService {

    private final BookAPI bookAPI;

    private final BookStoryRepository bookStoryRepository;
    private final CommentRepository commentRepository;
    private final CommentImageRepository commentImageRepository;
    private final BookStoryLikedRepository bookStoryLikedRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 책이야기를 작성
     *
     * @param memberId 책이야기를 작성하는 회원의 ID
     * @param request  책이야기 요청 DTO
     */
    public Long createBookStory(Long memberId, BookStoryRequestDTO.BookStoryCreate request) {
        BookStoryStatus status = resolveStatus(request.getStatus());
        validateDescriptionForPublished(status, request.getDescription());

        String bookId = bookAPI.fetchOrCreateBook(request.getIsbn());

        BookStory bookStory = BookStoryConverter.toBookStory(request, memberId, bookId, status);
        BookStory savedBookStory = bookStoryRepository.save(bookStory);

        return savedBookStory.getId();
    }

    /**
     * 책이야기를 수정
     *
     * @param memberId    수정 요청 회원 ID
     * @param bookStoryId 수정할 책 이야기의 ID
     * @param request     수정할 책 이야기 정보 DTO
     */
    public Long updateBookStory(Long memberId, Long bookStoryId, BookStoryRequestDTO.BookStoryUpdate request) {
        BookStory bookStory = bookStoryRepository.findById(bookStoryId)
                .orElseThrow(() -> new BookStoryException(BookStoryErrorStatus.BOOK_STORY_NOT_FOUND));

        if (!bookStory.verifyOwner(memberId)) {
            throw new BookStoryException(BookStoryErrorStatus.BOOK_STORY_NOT_AUTHORIZED);
        }

        boolean wasDraft = bookStory.isDraft();
        BookStoryStatus requestedStatus = resolveStatus(request.getStatus());
        validateStatusTransition(bookStory, requestedStatus);
        validateDescriptionForPublished(requestedStatus, request.getDescription());

        String bookId = request.getIsbn() == null ? null : bookAPI.fetchOrCreateBook(request.getIsbn());
        Long updatedBookStoryId = bookStory.update(request.getTitle(), request.getDescription(), bookId, requestedStatus);
        publishDeletedImages(bookStory.replaceImages(request.getImageUrls()));

        if (requestedStatus == BookStoryStatus.DRAFT || wasDraft && requestedStatus == BookStoryStatus.PUBLISHED) {
            bookStoryRepository.updateCreatedAtToNow(bookStoryId);
        }

        return updatedBookStoryId;
    }

    /**
     * 책이야기를 삭제
     *
     * @param bookStoryId 삭제할 책이야기의 ID
     */
    public void deleteBookStory(Long memberId, Long bookStoryId) {
        BookStory bookStory = bookStoryRepository.findById(bookStoryId)
                .orElseThrow(() -> new BookStoryException(BookStoryErrorStatus.BOOK_STORY_NOT_FOUND));

        if (!bookStory.verifyOwner(memberId)) {
            throw new BookStoryException(BookStoryErrorStatus.BOOK_STORY_NOT_AUTHORIZED);
        }

        deleteBookStoryInternal(bookStory);
    }

    /**
     * 관리자가 책이야기를 삭제
     *
     * @param bookStoryId 삭제할 책이야기의 ID
     */
    public void deleteBookStoryByAdmin(Long bookStoryId) {
        BookStory bookStory = bookStoryRepository.findById(bookStoryId)
                .orElseThrow(() -> new BookStoryException(BookStoryErrorStatus.BOOK_STORY_NOT_FOUND));

        deleteBookStoryInternal(bookStory);
    }

    private void deleteBookStoryInternal(BookStory bookStory) {
        Long bookStoryId = bookStory.getId();
        List<String> imageUrls = java.util.stream.Stream.concat(
                        bookStory.getImageUrls().stream(),
                        commentImageRepository.findImageUrlsByBookStoryId(bookStoryId).stream()
                )
                .distinct()
                .toList();

        commentRepository.deleteChildCommentsByBookStoryId(bookStoryId);
        commentRepository.deleteParentCommentsByBookStoryId(bookStoryId);
        bookStoryLikedRepository.deleteByBookStoryId(bookStoryId);
        bookStoryRepository.delete(bookStory);
        publishDeletedImages(imageUrls);
    }

    /**
     * 회원 탈퇴 시 해당 회원의 모든 책이야기를 삭제(완전 삭제 아님)
     *
     * @param memberId 탈퇴하는 회원의 ID
     */
    public void softDeleteAllByMemberId(Long memberId) {
        bookStoryRepository.softDeleteAllByMemberId(memberId);
    }

    private BookStoryStatus resolveStatus(BookStoryStatus status) {
        return status == null ? BookStoryStatus.PUBLISHED : status;
    }

    private void validateStatusTransition(BookStory bookStory, BookStoryStatus requestedStatus) {
        if (bookStory.isPublished() && requestedStatus == BookStoryStatus.DRAFT) {
            throw new BookStoryException(BookStoryErrorStatus.BOOK_STORY_INVALID_STATUS);
        }
    }

    private void validateDescriptionForPublished(BookStoryStatus status, String description) {
        if (status == BookStoryStatus.PUBLISHED && (description == null || description.isBlank())) {
            throw new BookStoryException(BookStoryErrorStatus.BOOK_STORY_DESCRIPTION_REQUIRED);
        }
    }

    private void publishDeletedImages(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }
        eventPublisher.publishEvent(
                BookStoryEvent.DeleteBookStoryImage.builder()
                        .imageUrls(imageUrls)
                        .build()
        );
    }
}
