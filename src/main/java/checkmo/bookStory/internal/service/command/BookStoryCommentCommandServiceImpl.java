package checkmo.bookStory.internal.service.command;

import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
import checkmo.bookStory.converter.BookStoryConverter;
import checkmo.bookStory.entity.BookStory;
import checkmo.bookStory.entity.Comment;
import checkmo.bookStory.repository.CommentRepository;
import checkmo.bookStory.internal.service.query.BookStoryQueryService;
import checkmo.bookStory.web.dto.BookStoryRequestDTO;
import checkmo.member.entity.Member;
import checkmo.member.MemberAPI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BookStoryCommentCommandServiceImpl implements BookStoryCommentCommandService {

    // Domain level 2
    private final MemberAPI memberAPI;

    // 자신의 QueryService
    private final BookStoryQueryService bookStoryQueryService;

    // 자신의 Repository
    private final CommentRepository commentRepository;

    @Override
    public Long createComment(String memberId, Long bookStoryId, Long parentCommentId, BookStoryRequestDTO.CommentCreateRequest request) {
        // 1. 책이야기 존재 여부 확인
        BookStory bookStory = bookStoryQueryService.findBookStoryById(bookStoryId);
        
        // 2. 댓글 작성자 proxy 참조 조회
        Member proxyMember = memberAPI.findMemberReferenceById(memberId);
        
        // 3. 부모 댓글 검증 (대댓글인 경우)
        Comment parentComment = null;
        if (parentCommentId != null) {
            parentComment = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new GeneralException(ErrorStatus.COMMENT_NOT_FOUND));
            
            // 부모 댓글이 같은 책이야기에 속하는지 확인
            if (!parentComment.getBookStoryId().equals(bookStoryId)) {
                throw new GeneralException(ErrorStatus.INVALID_PARENT_COMMENT);
            }
            
            // 대댓글의 대댓글은 금지! (2단계까지만 허용)
            if (parentComment.getParentCommentId() != null) {
                throw new GeneralException(ErrorStatus.COMMENT_DEPTH_LIMIT_EXCEEDED);
            }
        }
        
        // 4. 댓글 생성
        Comment comment = BookStoryConverter.fromCommentCreateRequestDTO(request, proxyMember, bookStory, parentComment);

        // 5. 부모 댓글의 자식 리스트에 추가 (대댓글인 경우)
        if (parentComment != null) {
            parentComment.addChildComment(comment);
        }

        // 6. 책이야기의 댓글 리스트에 추가 및 댓글 수 증가
        bookStory.addCommentToList(comment);

        // 7. 댓글 저장
        commentRepository.save(comment);

        // 8. 댓글 작성된 책이야기 ID 반환
        return bookStoryId;
    }
}