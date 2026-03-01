package checkmo.book.internal.entity;

import checkmo.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
public class Book extends BaseEntity {

    @Id
    private String id;

    private String title;

    private String author;

    private String imgUrl;

    private String publisher;

    private String description;

    @Builder.Default
    private int likes = 0;

    @Builder.Default
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BookLiked> bookLikedList = new ArrayList<>();

    public void addBookLiked(BookLiked bookLiked) {
        this.bookLikedList.add(bookLiked);
        this.likes++;
    }

    public void removeBookLiked(BookLiked bookLiked) {
        this.bookLikedList.remove(bookLiked);
        if (this.likes > 0) {
            this.likes--;
        }
    }
}
