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

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uk_notice_image_order", columnNames = {"notice_id", "order_index"})
})
public class NoticeImage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String imageUrl;

    @Column(nullable = false)
    private int sortOrder; // 0-based index

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id", nullable = false)
    private Notice notice;

    public static NoticeImage of(Notice notice, String imageUrl, int sortOrder) {
        return NoticeImage.builder()
                .notice(notice)
                .imageUrl(imageUrl)
                .sortOrder(sortOrder)
                .build();
    }

    public void update(String imageUrl, int sortOrder) {
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
    }

    public void setNotice(Notice notice) {
        if (notice == null) {
            return;
        }
        this.notice = notice;
    }
}
