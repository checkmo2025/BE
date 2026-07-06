package checkmo.bookStory.internal;

import checkmo.bookStory.BookStoryAPI;
import checkmo.bookStory.internal.entity.BookStory;
import checkmo.bookStory.internal.entity.Comment;
import checkmo.bookStory.internal.service.query.BookStoryQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookStoryAPIImpl implements BookStoryAPI {
    private final BookStoryQueryService bookStoryQueryService;

    @Override
    public void validateBookStory(Long bookStoryId) {
        bookStoryQueryService.retrieveBookStory(bookStoryId);
    }

    @Override
    public Long fetchBookStoryIdByBookStoryCommentId(Long bookStoryCommentId) {
        Comment comment = bookStoryQueryService.retrieveBookStoryComment(bookStoryCommentId);
        return comment.getBookStory().getId();
    }

    @Override
    public Long fetchBookStoryAuthorId(Long bookStoryId) {
        BookStory bookStory = bookStoryQueryService.retrieveBookStory(bookStoryId);
        return bookStory.getMemberId();
    }

    @Override
    public Long fetchBookStoryCommentAuthorId(Long commentId) {
        Comment comment = bookStoryQueryService.retrieveBookStoryComment(commentId);
        return comment.getMemberId();
    }
}
