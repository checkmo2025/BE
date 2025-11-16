package checkmo.bookStory.internal.converter;

import checkmo.book.BookExternalDTO;
import checkmo.bookStory.BookStoryExternalDTO;
import checkmo.bookStory.internal.entity.BookStory;
import checkmo.bookStory.internal.entity.Comment;
import checkmo.bookStory.web.dto.BookStoryRequestDTO;
import checkmo.clubManagement.ClubManagementExternalDTO;
import checkmo.member.MemberExternalDTO;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BookStoryConverter {

    // =====================================================
    // BookStoryRequestDTO → BookStory 변환
    // =====================================================

    /**
     * BookStoryCreateRequestDTO → BookStory 변환
     */
    public static BookStory fromBookStoryRequestDTO(
            BookStoryRequestDTO.BookStoryCreate request,
            String memberId,
            String bookId
    ) {
        return checkmo.bookStory.internal.entity.BookStory.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .memberId(memberId)
                .bookId(bookId)
                .build();
    }

    // =====================================================
    // BookStory → BookStoryExternalDTO 변환
    // =====================================================

    /**
     * BookStoryResponseDTO -> BookStoryListResponse 변환
     */
    public static BookStoryExternalDTO.BookStoryList fromBookStoryResponses(
            List<BookStoryExternalDTO.BookStoryDetail> bookStoryDetailList,
            boolean hasNext,
            Long nextCursor,
            int pageSize,
            BookStoryExternalDTO.ScopeInfo scopeInfo,
            ClubManagementExternalDTO.MyClubList myClubList
    ) {
        return BookStoryExternalDTO.BookStoryList.builder()
                .scopeInfo(scopeInfo)
                .memberClubList(myClubList)
                .bookStoryDetailList(bookStoryDetailList)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .pageSize(pageSize)
                .build();
    }

    /**
     * BookStoryScope + MyClubInfoDTO -> ScopeInfo 변환
     */
    public static BookStoryExternalDTO.ScopeInfo fromScopeInfo(
            BookStoryRequestDTO.BookStoryScope scope,
            ClubManagementExternalDTO.MyClubInfo selectedClub
    ) {
        return BookStoryExternalDTO.ScopeInfo.builder()
                .scope(scope)
                .selectedClub(selectedClub)
                .build();
    }

    /**
     * BookStory -> BookStoryResponseDTO 변환
     */
    public static BookStoryExternalDTO.BookStoryDetail fromBookStoryToResponse(
            BookStory bookStory,
            String currentMemberId,
            BookExternalDTO.BasicInfo bookInfo,
            MemberExternalDTO.WithFollowStatus authorInfo,
            boolean isLiked,
            int commentCount
    ) {
        return BookStoryExternalDTO.BookStoryDetail.builder()
                .bookStoryId(bookStory.getId())
                .bookInfo(bookInfo)
                .authorInfo(authorInfo)
                .bookStoryTitle(bookStory.getTitle())
                .description(bookStory.getDescription())
                .likes(bookStory.getLikes())
                .likedByMe(isLiked)
                .createdAt(bookStory.getCreatedAt())
                .writtenByMe(bookStory.getMemberId().equals(currentMemberId))
                .commentCount(commentCount)
                .build();
    }

    /**
     * BookStory -> BookStoryDetailResponse 변환
     */
    public static BookStoryExternalDTO.BookStoryDetailWithComment fromBookStoryToDetailResponse(
            BookStory bookStory,
            String currentMemberId,
            BookExternalDTO.BasicInfo bookInfo,
            MemberExternalDTO.WithFollowStatus authorInfo,
            boolean isLiked,
            List<BookStoryExternalDTO.CommentDetail> commentList
    ) {
        return BookStoryExternalDTO.BookStoryDetailWithComment.builder()
                .bookStoryId(bookStory.getId())
                .bookInfo(bookInfo)
                .authorInfo(authorInfo)
                .bookStoryTitle(bookStory.getTitle())
                .description(bookStory.getDescription())
                .likes(bookStory.getLikes())
                .likedByMe(isLiked)
                .createdAt(bookStory.getCreatedAt())
                .writtenByMe(bookStory.getMemberId().equals(currentMemberId))
                .commentCount(bookStory.getCommentsCount())
                .comments(commentList)
                .build();
    }

    // =====================================================
    // CommentCreateRequestDTO → Comment 변환
    // =====================================================

    /**
     * CommentCreateRequestDTO -> Comment 변환
     */
    public static Comment fromCommentCreateRequestDTO(
            BookStoryRequestDTO.CommentCreate request,
            String memberId,
            BookStory bookStory,
            Comment parentComment
    ) {
        return Comment.builder()
                .content(request.getContent())
                .memberId(memberId)
                .bookStory(bookStory)
                .parentComment(parentComment)
                .build();
    }

    /**
     * List<Comments> -> CommentResponse
     */
    public static List<BookStoryExternalDTO.CommentDetail> fromCommentsToResponses(
            List<Comment> comments,
            String currentMemberId,
            java.util.Map<String, MemberExternalDTO.BasicInfo> memberInfoMap
    ) {
        return comments.stream()
                .map(comment -> {
                    // 대댓글들 변환
                    List<BookStoryExternalDTO.CommentDetail> replies = comment.getChildrenComment().stream()
                            .map(reply -> fromCommentToResponse(
                                    reply,
                                    currentMemberId,
                                    memberInfoMap.get(reply.getMemberId()),
                                    List.of() // 대댓글의 대댓글은 없으므로 빈 리스트
                            )).toList();

                    // 부모 댓글 변환
                    return fromCommentToResponse(
                            comment,
                            currentMemberId,
                            memberInfoMap.get(comment.getMemberId()),
                            replies
                    );
                }).toList();
    }

    /**
     * Comment -> CommentResponse
     */
    private static BookStoryExternalDTO.CommentDetail fromCommentToResponse(
            Comment comment,
            String currentMemberId,
            MemberExternalDTO.BasicInfo authorInfo,
            List<BookStoryExternalDTO.CommentDetail> replies
    ) {
        return BookStoryExternalDTO.CommentDetail.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .authorInfo(authorInfo)
                .createdAt(comment.getCreatedAt())
                .writtenByMe(comment.getMemberId().equals(currentMemberId))
                .replies(replies)
                .build();
    }
}
