package checkmo.bookStory.internal.entity;

import checkmo.bookStory.internal.exception.BookStoryErrorStatus;
import checkmo.bookStory.internal.exception.BookStoryException;
import checkmo.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
import org.hibernate.annotations.BatchSize;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class BookStory extends BaseEntity {

    private static final int MAX_IMAGE_COUNT = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Builder.Default
    private int likes = 0;

    @Column(nullable = false)
    @Builder.Default
    private int commentsCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private int viewCount = 0;

    @Builder.Default
    private boolean deleted = false;

    private LocalDateTime deletedAt;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BookStoryStatus status = BookStoryStatus.PUBLISHED;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "book_id", nullable = false)
    private String bookId;

    @Builder.Default
    @OneToMany(mappedBy = "bookStory", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "bookStory", cascade = CascadeType.ALL)
    private List<BookStoryLiked> bookStoryLikedList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "bookStory", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @BatchSize(size = 50)
    private List<BookStoryImage> images = new ArrayList<>();

    public Long update(String title, String description, String bookId, BookStoryStatus status) {
        this.title = title;
        this.description = description;
        if (bookId != null) {
            this.bookId = bookId;
        }
        this.status = status;
        return this.id;
    }

    public boolean isDraft() {
        return this.status == BookStoryStatus.DRAFT;
    }

    public boolean isPublished() {
        return this.status == BookStoryStatus.PUBLISHED;
    }

    public void addCommentToList(Comment comment) {
        this.comments.add(comment);
        this.commentsCount++;
    }

    public void addBookStoryLiked(BookStoryLiked bookStoryLiked) {
        this.bookStoryLikedList.add(bookStoryLiked);
        this.likes++;
    }

    public void removeBookStoryLiked(BookStoryLiked bookStoryLiked) {
        this.bookStoryLikedList.remove(bookStoryLiked);
        if (this.likes > 0) {
            this.likes--;
        }
    }

    public boolean verifyOwner(Long memberId) {
        return this.memberId.equals(memberId);
    }

    public List<String> replaceImages(List<String> imageUrls) {
        if (imageUrls == null) {
            return List.of();
        }

        if (imageUrls.size() > MAX_IMAGE_COUNT) {
            throw new BookStoryException(BookStoryErrorStatus.BOOK_STORY_IMAGE_LIMIT_EXCEEDED);
        }

        List<String> removedImages = getImageUrls().stream()
                .filter(url -> !imageUrls.contains(url))
                .toList();

        int commonSize = Math.min(this.images.size(), imageUrls.size());
        for (int i = 0; i < commonSize; i++) {
            this.images.get(i).update(imageUrls.get(i), i);
        }

        for (int i = commonSize; i < imageUrls.size(); i++) {
            this.images.add(BookStoryImage.of(this, imageUrls.get(i), i));
        }

        for (int i = this.images.size() - 1; i >= imageUrls.size(); i--) {
            this.images.remove(i);
        }

        return removedImages;
    }

    public List<String> getImageUrls() {
        return this.images.stream()
                .map(BookStoryImage::getImageUrl)
                .toList();
    }
}
