package checkmo.clubNotice.internal.entity;

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
        name = "notice_comment_image",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_notice_comment_image_order",
                        columnNames = {"notice_comment_id", "sort_order"}
                )
        }
)
public class NoticeCommentImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String imageUrl;

    @Column(nullable = false)
    private int sortOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_comment_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private NoticeComment noticeComment;

    public static NoticeCommentImage of(NoticeComment noticeComment, String imageUrl, int sortOrder) {
        return NoticeCommentImage.builder()
                .noticeComment(noticeComment)
                .imageUrl(imageUrl)
                .sortOrder(sortOrder)
                .build();
    }

    public void update(String imageUrl, int sortOrder) {
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
    }
}
