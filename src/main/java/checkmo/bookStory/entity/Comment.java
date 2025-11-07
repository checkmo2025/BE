package checkmo.bookStory.entity;

import checkmo.member.entity.Member;
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
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

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

    @Column(name = "parent_comment_id", insertable = false, updatable = false)
    private Long parentCommentId; // 부모 댓글 ID (대댓글의 경우)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment; // 부모 댓글

    @Builder.Default
    @OneToMany(mappedBy = "parentComment")
    @OrderBy("createdAt ASC")
    private List<Comment> childrenComment = new ArrayList<>(); // 대댓글 리스트들

    public void addChildComment(Comment childComment) {
        childrenComment.add(childComment);
    }
}
