package checkmo.bookStory.internal.service.command;

import checkmo.bookStory.BookStoryEvent;
import checkmo.bookStory.internal.entity.BookStory;
import checkmo.bookStory.internal.entity.BookStoryLiked;
import checkmo.bookStory.internal.exception.BookStoryErrorStatus;
import checkmo.bookStory.internal.exception.BookStoryException;
import checkmo.bookStory.internal.repository.BookStoryLikedRepository;
import checkmo.bookStory.internal.repository.BookStoryRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookStorySocialCommandServiceImpl implements BookStorySocialCommandService {

    private final BookStoryRepository bookStoryRepository;
    private final BookStoryLikedRepository bookStoryLikedRepository;

    // 이벤트 발행을 위한 ApplicationEventPublisher
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public boolean toggleLikeOnBookStory(String memberId, Long bookStoryId) {
        BookStory bookStory = bookStoryRepository.findById(bookStoryId)
                .orElseThrow(() -> new BookStoryException(BookStoryErrorStatus.BOOK_STORY_NOT_FOUND));

        return bookStoryLikedRepository.findBookStoryLikedByBookStoryIdAndMemberId(bookStoryId, memberId)
                .map(bookStoryLiked -> {
                    // 좋아요가 이미 있다면 제거하고 false 반환
                    deleteBookStoryLiked(bookStory, bookStoryLiked);
                    return false;
                })
                .orElseGet(() -> {
                    // 사전 체크: 대부분의 중복을 사전 차단 (race condition 최소화)
                    if (bookStoryLikedRepository.existsByMemberIdAndBookStoryId(memberId, bookStoryId)) {
                        return true; // 이미 좋아요 존재
                    }

                    // 좋아요가 없다면 새로 생성
                    Optional<BookStoryLiked> createdLiked = createAndSaveBookStoryLiked(bookStory, memberId);
                    createdLiked.ifPresent(liked -> {
                        // 실제로 생성되었고, 좋아요를 누른 사람이 책이야기를 작성한 사람과 다를 때만 이벤트 발행
                        if (!memberId.equals(bookStory.getMemberId())) {
                            eventPublisher.publishEvent(
                                    BookStoryEvent.BookStoryLiked.builder()
                                            .eventId(liked.getId())
                                            .senderId(memberId)
                                            .receiverId(bookStory.getMemberId())
                                            .bookStoryId(bookStoryId)
                                            .build()
                            );
                        }
                    });
                    return true; // 생성되었거나 중복이거나, 최종적으로 좋아요 존재
                });
    }

    private void deleteBookStoryLiked(BookStory bookStory, BookStoryLiked bookStoryLiked) {
        bookStoryLikedRepository.delete(bookStoryLiked);
        bookStory.removeBookStoryLiked(bookStoryLiked);
    }

    private Optional<BookStoryLiked> createAndSaveBookStoryLiked(BookStory bookStory, String memberId) {
        try {
            BookStoryLiked bookStoryLiked = BookStoryLiked.builder()
                    .bookStory(bookStory)
                    .memberId(memberId)
                    .build();

            bookStoryLikedRepository.save(bookStoryLiked);
            bookStory.addBookStoryLiked(bookStoryLiked);
            return Optional.of(bookStoryLiked); // 이번 호출에서 새로 생성함
        } catch (DataIntegrityViolationException e) {
            // 유니크 제약 조건 위반 = 좋아요가 이미 존재 (exists와 save 사이의 race condition)
            return Optional.empty(); // 이번 호출에서는 생성하지 않았음 (하지만 좋아요는 존재함)
        }
    }
}
