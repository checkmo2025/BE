package checkmo.book.internal.entity;

import checkmo.common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
}
