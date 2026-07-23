package checkmo.bookStory.internal.entity;

import checkmo.bookStory.internal.exception.BookStoryErrorStatus;
import checkmo.bookStory.internal.exception.BookStoryException;
import checkmo.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import java.time.LocalDateTime;
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

    private static final int MAX_IMAGE_COUNT = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 300)
    private String content;

    @JoinColumn(name = "member_id", nullable = false)
    private Long memberId;

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

    @Builder.Default
    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<CommentImage> images = new ArrayList<>();

    @Builder.Default
    private boolean deleted = false;

    private LocalDateTime deletedAt;

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

    public void verifyOwner(Long memberId) {
        if (!this.memberId.equals(memberId)) {
            throw new BookStoryException(BookStoryErrorStatus.COMMENT_NOT_AUTHORIZED);
        }
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void softDelete() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public List<String> replaceImages(List<String> imageUrls) {
        if (imageUrls == null) {
            return List.of();
        }

        if (imageUrls.size() > MAX_IMAGE_COUNT) {
            throw new BookStoryException(BookStoryErrorStatus.COMMENT_IMAGE_LIMIT_EXCEEDED);
        }

        List<String> removedImages = getImageUrls().stream()
                .filter(url -> !imageUrls.contains(url))
                .toList();

        int commonSize = Math.min(this.images.size(), imageUrls.size());
        for (int i = 0; i < commonSize; i++) {
            this.images.get(i).update(imageUrls.get(i), i);
        }

        for (int i = commonSize; i < imageUrls.size(); i++) {
            this.images.add(CommentImage.of(this, imageUrls.get(i), i));
        }

        for (int i = this.images.size() - 1; i >= imageUrls.size(); i--) {
            this.images.remove(i);
        }

        return removedImages;
    }

    public List<String> getImageUrls() {
        return this.images.stream()
                .map(CommentImage::getImageUrl)
                .toList();
    }
}
