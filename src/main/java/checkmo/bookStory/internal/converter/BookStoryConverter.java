package checkmo.bookStory.internal.converter;

import checkmo.book.BookExternalDTO;
import checkmo.bookStory.internal.entity.BookStory;
import checkmo.bookStory.internal.entity.BookStoryStatus;
import checkmo.bookStory.internal.entity.Comment;
import checkmo.bookStory.internal.repository.projection.BookStoryPrevNextProjection;
import checkmo.bookStory.web.dto.BookStoryRequestDTO;
import checkmo.bookStory.web.dto.BookStoryResponseDTO;
import checkmo.member.MemberExternalDTO;
import checkmo.member.MemberExternalDTO.BasicInfoWithFollow;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BookStoryConverter {

    private static final String BLOCKED_USER_MESSAGE = "차단된 사용자입니다";

    public static BookStory toBookStory(
            BookStoryRequestDTO.BookStoryCreate request,
            String memberId,
            String bookId,
            BookStoryStatus status
    ) {
        return BookStory.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(status)
                .memberId(memberId)
                .bookId(bookId)
                .build();
    }

    public static BookStoryResponseDTO.BasicInfo toBookStoryDetailDTO(
            BookStory bookStory,
            String currentMemberId,
            BookExternalDTO.BasicInfo bookInfo,
            BasicInfoWithFollow authorInfo,
            boolean isLiked
    ) {
        return BookStoryResponseDTO.BasicInfo.builder()
                .bookStoryId(bookStory.getId())
                .bookInfo(bookInfo)
                .authorInfo(authorInfo)
                .bookStoryTitle(bookStory.getTitle())
                .description(bookStory.getDescription())
                .likes(bookStory.getLikes())
                .status(bookStory.getStatus())
                .canContinue(bookStory.isDraft() && bookStory.getMemberId().equals(currentMemberId))
                .likedByMe(isLiked)
                .createdAt(bookStory.getCreatedAt())
                .writtenByMe(bookStory.getMemberId().equals(currentMemberId))
                .viewCount(bookStory.getViewCount())
                .commentCount(bookStory.getCommentsCount())
                .build();
    }

    public static BookStoryResponseDTO.DetailInfo toBookStoryDetailWithComment(
            BookStory bookStory,
            String currentMemberId,
            BookExternalDTO.BasicInfo bookInfo,
            BasicInfoWithFollow authorInfo,
            boolean isLiked,
            List<BookStoryResponseDTO.CommentInfo> commentList,
            BookStoryPrevNextProjection bookStoryPrevNextProjection
    ) {
        return BookStoryResponseDTO.DetailInfo.builder()
                .bookStoryId(bookStory.getId())
                .bookInfo(bookInfo)
                .authorInfo(authorInfo)
                .bookStoryTitle(bookStory.getTitle())
                .description(bookStory.getDescription())
                .likes(bookStory.getLikes())
                .status(bookStory.getStatus())
                .canContinue(bookStory.isDraft() && bookStory.getMemberId().equals(currentMemberId))
                .likedByMe(isLiked)
                .createdAt(bookStory.getCreatedAt())
                .writtenByMe(bookStory.getMemberId().equals(currentMemberId))
                .viewCount(bookStory.getViewCount())
                .commentCount(bookStory.getCommentsCount())
                .comments(commentList)
                .prevBookStoryId(bookStoryPrevNextProjection == null ? null : bookStoryPrevNextProjection.getPrevId())
                .nextBookStoryId(bookStoryPrevNextProjection == null ? null : bookStoryPrevNextProjection.getNextId())
                .build();
    }

    public static BookStoryResponseDTO.AdminBasicInfo toAdminBasicInfo(
            BookStory bookStory,
            MemberExternalDTO.DetailInfo authorInfo,
            BookExternalDTO.BasicInfo bookInfo
    ) {
        return BookStoryResponseDTO.AdminBasicInfo.builder()
                .bookStoryId(bookStory.getId())
                .bookStoryTitle(bookStory.getTitle())
                .authorEmail(authorInfo != null ? authorInfo.getEmail() : null)
                .authorNickname(authorInfo != null ? authorInfo.getNickname() : null)
                .bookTitle(bookInfo != null ? bookInfo.getTitle() : null)
                .createdAt(bookStory.getCreatedAt())
                .build();
    }

    public static List<BookStoryResponseDTO.CommentInfo> toCommentDetailList(
            List<Comment> comments,
            String currentMemberId,
            Map<String, MemberExternalDTO.BasicInfo> memberInfoMap,
            Set<String> blockedMemberIds
    ) {
        return comments.stream()
                .map(comment -> {
                    // 대댓글들 변환
                    List<BookStoryResponseDTO.CommentInfo> replies = comment.getChildrenComment().stream()
                            .map(reply -> fromCommentToResponse(
                                    reply,
                                    currentMemberId,
                                    memberInfoMap.get(reply.getMemberId()),
                                    isBlocked(reply.getMemberId(), blockedMemberIds),
                                    List.of() // 대댓글의 대댓글은 없으므로 빈 리스트
                            )).toList();

                    // 부모 댓글 변환
                    return fromCommentToResponse(
                            comment,
                            currentMemberId,
                            memberInfoMap.get(comment.getMemberId()),
                            isBlocked(comment.getMemberId(), blockedMemberIds),
                            replies
                    );
                }).toList();
    }

    private static BookStoryResponseDTO.CommentInfo fromCommentToResponse(
            Comment comment,
            String currentMemberId,
            MemberExternalDTO.BasicInfo authorInfo,
            boolean blocked,
            List<BookStoryResponseDTO.CommentInfo> replies
    ) {
        if (comment.isDeleted()) {
            return BookStoryResponseDTO.CommentInfo.builder()
                    .commentId(comment.getId())
                    .content(null)
                    .authorInfo(null)
                    .createdAt(comment.getCreatedAt())
                    .writtenByMe(false)
                    .deleted(true)
                    .replies(replies)
                    .build();
        }

        MemberExternalDTO.BasicInfo displayAuthorInfo = blocked ? blockedBasicInfo() : authorInfo;
        String displayContent = blocked ? BLOCKED_USER_MESSAGE : comment.getContent();

        return BookStoryResponseDTO.CommentInfo.builder()
                .commentId(comment.getId())
                .content(displayContent)
                .authorInfo(displayAuthorInfo)
                .createdAt(comment.getCreatedAt())
                .writtenByMe(comment.getMemberId().equals(currentMemberId))
                .deleted(false)
                .replies(replies)
                .build();
    }

    private static boolean isBlocked(String memberId, Set<String> blockedMemberIds) {
        return memberId != null && blockedMemberIds != null && blockedMemberIds.contains(memberId);
    }

    private static MemberExternalDTO.BasicInfo blockedBasicInfo() {
        return MemberExternalDTO.BasicInfo.builder()
                .nickname(BLOCKED_USER_MESSAGE)
                .profileImageUrl(null)
                .build();
    }
}
