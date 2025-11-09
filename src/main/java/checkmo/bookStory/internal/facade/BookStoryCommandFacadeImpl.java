package checkmo.bookStory.internal.facade;

import checkmo.bookStory.internal.service.command.BookStoryCommandService;
import checkmo.bookStory.internal.service.command.BookStoryCommentCommandService;
import checkmo.bookStory.internal.service.command.BookStorySocialCommandService;
import checkmo.bookStory.web.dto.BookStoryRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BookStoryCommandFacadeImpl implements BookStoryCommandFacade {

    // 자신의 CommandService
    private final BookStoryCommandService bookStoryCommandService;
    private final BookStorySocialCommandService bookStorySocialCommandService;
    private final BookStoryCommentCommandService bookStoryCommentCommandService;

    @Override
    public Long createBookStory(String memberId, BookStoryRequestDTO.BookStoryCreateRequest request) {
        return bookStoryCommandService.createBookStory(memberId, request);
    }

    @Override
    public Long updateBookStory(String memberId, Long bookStoryId, BookStoryRequestDTO.BookStoryUpdateRequest request) {
        return bookStoryCommandService.updateBookStory(memberId, bookStoryId, request);
    }

    @Override
    public void deleteBookStory(String memberId, Long bookStoryId) {
        bookStoryCommandService.deleteBookStory(memberId, bookStoryId);
    }

    @Override
    public boolean toggleLikeOnBookStory(String memberId, Long bookStoryId) {
        return bookStorySocialCommandService.toggleLikeOnBookStory(memberId, bookStoryId);
    }

    @Override
    public Long createComment(String memberId, Long bookStoryId, Long parentCommentId,
                              BookStoryRequestDTO.CommentCreateRequest request) {
        return bookStoryCommentCommandService.createComment(memberId, bookStoryId, parentCommentId, request);
    }
}
