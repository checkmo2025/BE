package checkmo.domain.bookStory.entity;

import checkmo.domain.book.entity.Book;
import checkmo.domain.member.entity.Member;
import checkmo.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "member_id", insertable = false, updatable = false)
    private String memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "book_id", insertable = false, updatable = false)
    private String bookId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

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

    public void addLike() {
        this.likes++;
    }

    public void removeLike() {
        if (this.likes > 0) {
            this.likes--;
        }
    }

    public void addComment() {
        this.commentsCount++;
    }

    public void removeComment() {
        if (this.commentsCount > 0) {
            this.commentsCount--;
        }
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
}
