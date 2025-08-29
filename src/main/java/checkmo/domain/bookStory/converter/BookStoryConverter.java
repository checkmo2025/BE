package checkmo.domain.bookStory.converter;

import checkmo.domain.book.entity.Book;
import checkmo.domain.bookStory.entity.BookStory;
import checkmo.domain.bookStory.entity.Comment;
import checkmo.domain.bookStory.web.dto.BookStoryRequestDTO;
import checkmo.global.dto.BookStorySharedDTO;
import checkmo.domain.member.entity.Member;
import checkmo.global.dto.BookSharedDTO;
import checkmo.global.dto.ClubSharedDTO;
import checkmo.global.dto.MemberSharedDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BookStoryConverter {

    // =====================================================
    // BookStoryRequestDTO → BookStory 변환
    // =====================================================

    /**
     * BookStoryCreateRequestDTO → BookStory 변환
     */
    public static BookStory fromBookStoryRequestDTO(BookStoryRequestDTO.BookStoryCreateRequest request, Member proxyMember, Book proxyBook) {
        return BookStory.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .member(proxyMember)
                .book(proxyBook)
                .build();
    }

    // =====================================================
    // BookStory → BookStorySharedDTO 변환
    // =====================================================

    /**
     * BookStoryResponseDTO -> BookStoryListResponse 변환
     */
    public static BookStorySharedDTO.BookStoryListResponse fromBookStoryResponses(
            List<BookStorySharedDTO.BookStoryResponse> bookStoryResponses,
            boolean hasNext,
            Long nextCursor,
            int pageSize,
            BookStorySharedDTO.ScopeInfo scopeInfo,
            ClubSharedDTO.MyClubList myClubList
    ) {
        return BookStorySharedDTO.BookStoryListResponse.builder()
                .scopeInfo(scopeInfo)
                .memberClubList(myClubList)
                .bookStoryResponses(bookStoryResponses)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .pageSize(pageSize)
                .build();
    }

    /**
     * BookStoryScope + MyClubInfoDTO -> ScopeInfo 변환
     */
    public static BookStorySharedDTO.ScopeInfo fromScopeInfo(
            BookStoryRequestDTO.BookStoryScope scope,
            ClubSharedDTO.MyClubInfo selectedClub
    ) {
        return BookStorySharedDTO.ScopeInfo.builder()
                .scope(scope)
                .selectedClub(selectedClub)
                .build();
    }

    /**
     * BookStory -> BookStoryResponseDTO 변환
     */
    public static BookStorySharedDTO.BookStoryResponse fromBookStoryToResponse(
            BookStory bookStory,
            String currentMemberId,
            BookSharedDTO.BasicInfo bookInfo,
            MemberSharedDTO.WithFollowStatus authorInfo,
            boolean isLiked,
            int commentCount
    ) {
        return BookStorySharedDTO.BookStoryResponse.builder()
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
    public static BookStorySharedDTO.BookStoryDetailResponse fromBookStoryToDetailResponse(
            BookStory bookStory,
            String currentMemberId,
            BookSharedDTO.BasicInfo bookInfo,
            MemberSharedDTO.WithFollowStatus authorInfo,
            boolean isLiked,
            List<BookStorySharedDTO.CommentResponse> commentList
    ) {
        // 댓글 + 대댓글 전체 개수 계산
        int totalCommentCount = commentList.stream()
                .mapToInt(comment -> 1 + comment.getReplies().size())
                .sum();
        
        return BookStorySharedDTO.BookStoryDetailResponse.builder()
                .bookStoryId(bookStory.getId())
                .bookInfo(bookInfo)
                .authorInfo(authorInfo)
                .bookStoryTitle(bookStory.getTitle())
                .description(bookStory.getDescription())
                .likes(bookStory.getLikes())
                .likedByMe(isLiked)
                .createdAt(bookStory.getCreatedAt())
                .writtenByMe(bookStory.getMemberId().equals(currentMemberId))
                .commentCount(totalCommentCount)
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
            BookStoryRequestDTO.CommentCreateRequest request,
            Member proxyMember,
            BookStory bookStory,
            Comment parentComment
    ) {
        return Comment.builder()
                .content(request.getContent())
                .member(proxyMember)
                .bookStory(bookStory)
                .parentComment(parentComment)
                .build();
    }

    /**
     * List<Comments> -> CommentResponse
     */
    public static List<BookStorySharedDTO.CommentResponse> fromCommentsToResponses(
            List<Comment> comments,
            String currentMemberId,
            java.util.Map<String, MemberSharedDTO.BasicInfo> memberInfoMap
    ) {
        return comments.stream()
                .map(comment -> {
                    // 대댓글들 변환
                    List<BookStorySharedDTO.CommentResponse> replies = comment.getChildrenComment().stream()
                            .map(reply -> fromCommentToResponse(
                                    reply,
                                    currentMemberId,
                                    memberInfoMap.get(reply.getMemberId()),
                                    null // 대댓글의 대댓글은 없으므로 빈 리스트
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
    private static BookStorySharedDTO.CommentResponse fromCommentToResponse(
            Comment comment,
            String currentMemberId,
            MemberSharedDTO.BasicInfo authorInfo,
            List<BookStorySharedDTO.CommentResponse> replies
    ) {
        return BookStorySharedDTO.CommentResponse.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .authorInfo(authorInfo)
                .createdAt(comment.getCreatedAt())
                .writtenByMe(comment.getMemberId().equals(currentMemberId))
                .replies(replies)
                .build();
    }
}
