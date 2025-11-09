package checkmo.book.entity;

import checkmo.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

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
}
