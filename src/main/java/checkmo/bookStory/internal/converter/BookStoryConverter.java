package checkmo.bookStory.internal.converter;

import checkmo.book.BookExternalDTO;
import checkmo.bookStory.BookStoryExternalDTO;
import checkmo.bookStory.internal.entity.BookStory;
import checkmo.bookStory.internal.entity.Comment;
import checkmo.bookStory.web.dto.BookStoryRequestDTO;
import checkmo.member.MemberExternalDTO;
import checkmo.member.MemberExternalDTO.BasicInfoWithFollow;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BookStoryConverter {

    public static BookStory toBookStory(
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

    public static BookStoryExternalDTO.BasicInfo toBookStoryDetailDTO(
            BookStory bookStory,
            String currentMemberId,
            BookExternalDTO.BasicInfo bookInfo,
            BasicInfoWithFollow authorInfo,
            boolean isLiked,
            int commentCount
    ) {
        return BookStoryExternalDTO.BasicInfo.builder()
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

    public static BookStoryExternalDTO.DetailInfo toBookStoryDetailWithComment(
            BookStory bookStory,
            String currentMemberId,
            BookExternalDTO.BasicInfo bookInfo,
            BasicInfoWithFollow authorInfo,
            boolean isLiked,
            List<BookStoryExternalDTO.CommentInfo> commentList
    ) {
        return BookStoryExternalDTO.DetailInfo.builder()
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

    public static List<BookStoryExternalDTO.CommentInfo> toCommentDetailList(
            List<Comment> comments,
            String currentMemberId,
            java.util.Map<String, MemberExternalDTO.BasicInfo> memberInfoMap
    ) {
        return comments.stream()
                .map(comment -> {
                    // 대댓글들 변환
                    List<BookStoryExternalDTO.CommentInfo> replies = comment.getChildrenComment().stream()
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

    private static BookStoryExternalDTO.CommentInfo fromCommentToResponse(
            Comment comment,
            String currentMemberId,
            MemberExternalDTO.BasicInfo authorInfo,
            List<BookStoryExternalDTO.CommentInfo> replies
    ) {
        return BookStoryExternalDTO.CommentInfo.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .authorInfo(authorInfo)
                .createdAt(comment.getCreatedAt())
                .writtenByMe(comment.getMemberId().equals(currentMemberId))
                .replies(replies)
                .build();
    }
}
