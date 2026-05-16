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
@Transactional
public class BookStorySocialCommandService {

    private final BookStoryRepository bookStoryRepository;
    private final BookStoryLikedRepository bookStoryLikedRepository;

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 책이야기에 좋아요를 토글(추가/제거) 이미 좋아요가 있으면 제거, 없으면 추가
     *
     * @param memberId    토글하는 사용자의 ID
     * @param bookStoryId 토글할 책이야기의 ID
     * @return 좋아요가 추가됐는지/제거됐는지 여부
     */
    public boolean toggleLikeOnBookStory(String memberId, Long bookStoryId) {
        BookStory bookStory = bookStoryRepository.findById(bookStoryId)
                .orElseThrow(() -> new BookStoryException(BookStoryErrorStatus.BOOK_STORY_NOT_FOUND));

        if (bookStory.isDeleted() || bookStory.isDraft()) {
            throw new BookStoryException(BookStoryErrorStatus.BOOK_STORY_NOT_FOUND);
        }

        return bookStoryLikedRepository.findByBookStoryAndMember(bookStoryId, memberId)
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
                    Optional<BookStoryLiked> createdLiked = createBookStoryLiked(bookStory, memberId);
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

    private Optional<BookStoryLiked> createBookStoryLiked(BookStory bookStory, String memberId) {
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
