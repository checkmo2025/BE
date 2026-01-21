package checkmo.bookStory.internal.entity;

import checkmo.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
public class BookStory extends BaseEntity {

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

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "member_id", nullable = false)
    private String memberId;

    @Column(name = "book_id", nullable = false)
    private String bookId;

    @Builder.Default
    @OneToMany(mappedBy = "bookStory", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "bookStory", cascade = CascadeType.ALL)
    private List<BookStoryLiked> bookStoryLikedList = new ArrayList<>();

    public Long updateDescription(String description) {
        this.description = description;
        return this.id;
    }

    public void addCommentToList(Comment comment) {
        this.comments.add(comment);
        this.commentsCount++;
    }

    public void removeCommentFromList(Comment comment) {
        this.comments.remove(comment);
        if (this.commentsCount > 0) {
            this.commentsCount--;
        }
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

    public boolean verifyOwner(String memberId) {
        return this.memberId.equals(memberId);
    }
}
