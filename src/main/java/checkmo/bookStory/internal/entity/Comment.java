package checkmo.bookStory.internal.entity;

import checkmo.bookStory.internal.exception.BookStoryErrorStatus;
import checkmo.bookStory.internal.exception.BookStoryException;
import checkmo.common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    @JoinColumn(name = "member_id", nullable = false)
    private String memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_story_id")
    private BookStory bookStory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment; // 부모 댓글

    @Builder.Default
    @OneToMany(mappedBy = "parentComment")
    @OrderBy("createdAt ASC")
    private List<Comment> childrenComment = new ArrayList<>(); // 대댓글 리스트들

    public void addChildComment(Comment childComment) {
        childrenComment.add(childComment);
    }

    public void verifyBookStory(Long bookStoryId) {
        if (!this.bookStory.getId().equals(bookStoryId)) {
            throw new BookStoryException(BookStoryErrorStatus.INVALID_PARENT_COMMENT);
        }
    }

    public void verifyNotChildComment() {
        if (this.parentComment != null) {
            throw new BookStoryException(BookStoryErrorStatus.COMMENT_DEPTH_LIMIT_EXCEEDED);
        }
    }

    public void verifyOwner(String memberId) {
        if (!this.memberId.equals(memberId)) {
            throw new BookStoryException(BookStoryErrorStatus.COMMENT_NOT_AUTHORIZED);
        }
    }

    public void updateContent(String content) {
        this.content = content;
    }
}
