package checkmo.bookStory.internal.service.command;

import checkmo.bookStory.internal.entity.BookStory;
import checkmo.bookStory.internal.entity.Comment;
import checkmo.bookStory.internal.exception.BookStoryErrorStatus;
import checkmo.bookStory.internal.exception.BookStoryException;
import checkmo.bookStory.internal.repository.CommentRepository;
import checkmo.bookStory.internal.service.query.BookStoryQueryService;
import checkmo.bookStory.web.dto.BookStoryRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BookStoryCommentCommandService {

    private final BookStoryQueryService bookStoryQueryService;
    private final CommentRepository commentRepository;

    /**
     * 댓글/대댓글 작성
     *
     * @param memberId        작성자 ID
     * @param bookStoryId     책이야기 ID
     * @param parentCommentId 부모 댓글 ID (값이 있으면 대댓글, null이면 일반 댓글)
     * @param request         댓글 내용
     * @return 작성된 책이야기 ID
     */
    public Long createComment(
            String memberId,
            Long bookStoryId,
            Long parentCommentId,
            BookStoryRequestDTO.CommentCreate request
    ) {
        // 1. 책이야기 존재 여부 확인
        BookStory bookStory = bookStoryQueryService.retrieveBookStory(bookStoryId);

        // 2. 부모 댓글 검증 (대댓글인 경우)
        Comment parentComment = null;
        if (parentCommentId != null) {
            parentComment = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new BookStoryException(BookStoryErrorStatus.COMMENT_NOT_FOUND));

            // 부모 댓글이 같은 책이야기에 속하는지 확인
            parentComment.verifyBookStory(bookStoryId);

            // 대댓글의 대댓글은 금지! (2단계까지만 허용)
            parentComment.verifyNotChildComment();
        }

        // 3. 댓글 생성
        Comment comment = Comment.builder()
                .content(request.getContent())
                .memberId(memberId)
                .bookStory(bookStory)
                .parentComment(parentComment)
                .build();

        // 4. 부모 댓글의 자식 리스트에 추가 (대댓글인 경우)
        if (parentComment != null) {
            parentComment.addChildComment(comment);
        }

        // 5. 책이야기의 댓글 리스트에 추가 및 댓글 수 증가
        bookStory.addCommentToList(comment);

        // 6. 댓글 저장
        commentRepository.save(comment);

        // 7. 댓글 작성된 책이야기 ID 반환
        return bookStoryId;
    }
}