package checkmo.domain.book.entity;

import checkmo.domain.bookStory.entity.BookStory;
import checkmo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Book extends BaseEntity {

    @Id
    private Long id;

    private String title;

    private String author;

    private String imgUrl;

    private String publisher;

    private String description;

    @Builder.Default
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private List<BookStory> bookStories = new ArrayList<>();
}
