package checkmo.bookStory.internal.service.command;

import checkmo.bookStory.web.dto.BookStoryRequestDTO;

public interface BookStoryCommentCommandService {
    
    /**
     * 댓글/대댓글 작성
     *
     * @param memberId 작성자 ID
     * @param bookStoryId 책이야기 ID
     * @param parentCommentId 부모 댓글 ID (값이 있으면 대댓글, null이면 일반 댓글)
     * @param request 댓글 내용
     * @return 작성된 책이야기 ID
     */
    Long createComment(String memberId, Long bookStoryId, Long parentCommentId, BookStoryRequestDTO.CommentCreate request);
}