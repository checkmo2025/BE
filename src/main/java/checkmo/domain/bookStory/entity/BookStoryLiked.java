package checkmo.domain.bookStory.entity;

import checkmo.domain.member.entity.Member;
import checkmo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
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
