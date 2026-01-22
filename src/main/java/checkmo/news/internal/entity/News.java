package checkmo.news.internal.entity;

import checkmo.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
public class News extends BaseEntity {

    private static final int MAX_IMAGE_COUNT = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String title;

    private String requesterEmail;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(length = 500)
    private String thumbnailUrl;

    @Column(length = 500)
    private String originalLink;

    @Column(nullable = false)
    private LocalDate publishStartAt;

    @Column(nullable = false)
    private LocalDate publishEndAt;

    @Builder.Default
    @OneToMany(mappedBy = "news", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<NewsImage> images = new ArrayList<>();

    // ========== 이미지 ==========
    public List<String> replaceImages(List<String> imageUrls) {
        if (imageUrls == null) {
            return List.of();
        }

        if (imageUrls.size() > MAX_IMAGE_COUNT) {
            throw new IllegalArgumentException("이미지는 최대 " + MAX_IMAGE_COUNT + "개까지 등록 가능합니다.");
        }

        List<String> oldImages = getImageUrls();
        List<String> removedImages = oldImages.stream()
                .filter(url -> !imageUrls.contains(url))
                .toList();

        int commonSize = Math.min(this.images.size(), imageUrls.size());
        for (int i = 0; i < commonSize; i++) {
            this.images.get(i).update(imageUrls.get(i), i);
        }

        for (int i = commonSize; i < imageUrls.size(); i++) {
            this.images.add(NewsImage.builder()
                    .news(this)
                    .imageUrl(imageUrls.get(i))
                    .sortOrder(i)
                    .build());
        }

        for (int i = this.images.size() - 1; i >= imageUrls.size(); i--) {
            this.images.remove(i);
        }

        return removedImages;
    }

    public List<String> getImageUrls() {
        return this.images.stream().map(NewsImage::getImageUrl).toList();
    }
}
