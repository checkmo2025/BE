package checkmo.bookStory.entity;

import checkmo.member.entity.Member;
import checkmo.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"member_id", "book_story_id"})
        })
public class BookStoryLiked extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", insertable = false, updatable = false)
    private String memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "book_story_id", insertable = false, updatable = false)
    private Long bookStoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_story_id")
    private BookStory bookStory;
}
