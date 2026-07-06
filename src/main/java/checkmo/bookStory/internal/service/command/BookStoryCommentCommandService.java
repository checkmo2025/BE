package checkmo.bookStory.internal.service.command;

import checkmo.bookStory.BookStoryEvent;
import checkmo.bookStory.internal.entity.BookStory;
import checkmo.bookStory.internal.entity.Comment;
import checkmo.bookStory.internal.exception.BookStoryErrorStatus;
import checkmo.bookStory.internal.exception.BookStoryException;
import checkmo.bookStory.internal.repository.CommentRepository;
import checkmo.bookStory.internal.service.query.BookStoryQueryService;
import checkmo.bookStory.web.dto.BookStoryRequestDTO;
import checkmo.member.MemberAPI;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BookStoryCommentCommandService {

    private final BookStoryQueryService bookStoryQueryService;
    private final CommentRepository commentRepository;
    private final MemberAPI memberAPI;
    private final ApplicationEventPublisher eventPublisher;

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
            Long memberId,
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

        validateNotBlockedByCommentTarget(bookStory, parentComment, memberId);

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

        // 7. 댓글 작성자가 책이야기 작성자와 다를 때만 알림 이벤트 발행
        Long receiverId = parentComment != null
                ? parentComment.getMemberId()
                : bookStory.getMemberId();
        if (!memberId.equals(receiverId)) {
            eventPublisher.publishEvent(
                    BookStoryEvent.BookStoryComment.builder()
                            .eventId(comment.getId())
                            .senderId(memberId)
                            .receiverId(receiverId)
                            .bookStoryId(bookStoryId)
                            .build()
            );
        }

        // 8. 댓글 작성된 책이야기 ID 반환
        return bookStoryId;
    }

    private void validateNotBlockedByCommentTarget(BookStory bookStory, Comment parentComment, Long memberId) {
        if (!memberId.equals(bookStory.getMemberId())
                && memberAPI.hasBlockBetween(bookStory.getMemberId(), memberId)) {
            throw new BookStoryException(BookStoryErrorStatus.COMMENT_BLOCKED);
        }

        if (parentComment != null
                && !memberId.equals(parentComment.getMemberId())
                && memberAPI.hasBlockBetween(parentComment.getMemberId(), memberId)) {
            throw new BookStoryException(BookStoryErrorStatus.COMMENT_BLOCKED);
        }
    }

    /**
     * 댓글 수정
     *
     * @param memberId    수정 요청자 ID
     * @param bookStoryId 책이야기 ID
     * @param commentId   수정할 댓글 ID
     * @param request     수정할 댓글 내용
     * @return 수정된 댓글 ID
     */
    public Long updateComment(
            Long memberId,
            Long bookStoryId,
            Long commentId,
            BookStoryRequestDTO.CommentUpdate request
    ) {
        // 1. 책이야기 존재 여부 확인
        bookStoryQueryService.retrieveBookStory(bookStoryId);

        // 2. 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BookStoryException(BookStoryErrorStatus.COMMENT_NOT_FOUND));

        // 3. 댓글이 해당 책이야기에 속하는지 확인
        comment.verifyBookStory(bookStoryId);

        // 4. 댓글 작성자 검증
        comment.verifyOwner(memberId);

        // 5. 댓글 내용 수정
        comment.updateContent(request.getContent());

        // 6. 수정된 댓글 ID 반환
        return commentId;
    }

    /**
     * 댓글 삭제 (소프트 삭제)
     *
     * @param memberId    삭제 요청자 ID
     * @param bookStoryId 책이야기 ID
     * @param commentId   삭제할 댓글 ID
     * @return 삭제된 댓글 ID
     */
    public Long deleteComment(
            Long memberId,
            Long bookStoryId,
            Long commentId
    ) {
        // 1. 책이야기 존재 여부 확인
        bookStoryQueryService.retrieveBookStory(bookStoryId);

        // 2. 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BookStoryException(BookStoryErrorStatus.COMMENT_NOT_FOUND));

        // 3. 댓글이 해당 책이야기에 속하는지 확인
        comment.verifyBookStory(bookStoryId);

        // 4. 댓글 작성자 검증
        comment.verifyOwner(memberId);

        // 5. 소프트 삭제 처리
        comment.softDelete();

        // 6. 삭제된 댓글 ID 반환
        return commentId;
    }

    /**
     * 관리자가 댓글 삭제 (소프트 삭제)
     *
     * @param bookStoryId 책이야기 ID
     * @param commentId   삭제할 댓글 ID
     * @return 삭제된 댓글 ID
     */
    public Long deleteCommentByAdmin(
            Long bookStoryId,
            Long commentId
    ) {
        // 1. 책이야기 존재 여부 확인
        bookStoryQueryService.retrieveBookStory(bookStoryId);

        // 2. 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BookStoryException(BookStoryErrorStatus.COMMENT_NOT_FOUND));

        // 3. 댓글이 해당 책이야기에 속하는지 확인
        comment.verifyBookStory(bookStoryId);

        // 4. 소프트 삭제 처리
        comment.softDelete();

        // 5. 삭제된 댓글 ID 반환
        return commentId;
    }

    /**
     * 회원 탈퇴 시 해당 회원의 모든 댓글을 삭제(완전 삭제 아님)
     *
     * @param memberId 탈퇴하는 회원의 ID
     */
    public void softDeleteAllByMemberId(Long memberId) {
        commentRepository.softDeleteAllByMemberId(memberId);
    }
}
