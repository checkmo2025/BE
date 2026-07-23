package checkmo.clubNotice.internal.entity;

import checkmo.clubNotice.internal.exception.ClubNoticeErrorStatus;
import checkmo.clubNotice.internal.exception.ClubNoticeException;
import checkmo.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
public class NoticeComment extends BaseEntity {

    private static final int MAX_IMAGE_COUNT = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 300, nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id")
    private Notice notice;

    private Long clubMemberId;

    @Builder.Default
    @OneToMany(mappedBy = "noticeComment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<NoticeCommentImage> images = new ArrayList<>();

    public void setNotice(Notice notice) {
        if (notice == null) {
            return;
        }
        this.notice = notice;
    }

    public boolean isAuthor(Long clubMemberId) {
        return Objects.equals(this.clubMemberId, clubMemberId);
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public List<String> replaceImages(List<String> imageUrls) {
        if (imageUrls == null) {
            return List.of();
        }

        if (imageUrls.size() > MAX_IMAGE_COUNT) {
            throw new ClubNoticeException(ClubNoticeErrorStatus.NOTICE_COMMENT_IMAGE_LIMIT_EXCEEDED);
        }

        List<String> removedImages = getImageUrls().stream()
                .filter(url -> !imageUrls.contains(url))
                .toList();

        int commonSize = Math.min(this.images.size(), imageUrls.size());
        for (int i = 0; i < commonSize; i++) {
            this.images.get(i).update(imageUrls.get(i), i);
        }

        for (int i = commonSize; i < imageUrls.size(); i++) {
            this.images.add(NoticeCommentImage.of(this, imageUrls.get(i), i));
        }

        for (int i = this.images.size() - 1; i >= imageUrls.size(); i--) {
            this.images.remove(i);
        }

        return removedImages;
    }

    public List<String> getImageUrls() {
        return this.images.stream()
                .map(NoticeCommentImage::getImageUrl)
                .toList();
    }
}
