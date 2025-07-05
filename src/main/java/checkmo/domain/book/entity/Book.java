package checkmo.domain.book.entity;

import checkmo.domain.bookStory.entity.BookStory;
import checkmo.domain.club.entity.Meeting;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String author;

    private String imgUrl;

    @Builder.Default
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private List<BookStory> bookStories = new ArrayList<>();
}
