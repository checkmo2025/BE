package checkmo.bookStory.internal.entity;

import checkmo.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "book_story_image",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_book_story_image_order",
                        columnNames = {"book_story_id", "sort_order"}
                )
        }
)
public class BookStoryImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String imageUrl;

    @Column(nullable = false)
    private int sortOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_story_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private BookStory bookStory;

    public static BookStoryImage of(BookStory bookStory, String imageUrl, int sortOrder) {
        return BookStoryImage.builder()
                .bookStory(bookStory)
                .imageUrl(imageUrl)
                .sortOrder(sortOrder)
                .build();
    }

    public void update(String imageUrl, int sortOrder) {
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
    }
}
