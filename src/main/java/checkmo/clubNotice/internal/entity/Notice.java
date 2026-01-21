package checkmo.clubNotice.internal.entity;

import checkmo.clubNotice.internal.exception.ClubNoticeErrorStatus;
import checkmo.clubNotice.internal.exception.ClubNoticeException;
import checkmo.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import java.time.LocalDateTime;
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
public class Notice extends BaseEntity {

    private static final int MAX_IMAGE_COUNT = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 1000)
    private String content;

    private boolean important;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NoticeTag tag;

    @Column(name = "meeting_id")
    private Long meetingId;

    @Column(name = "meeting_version")
    private Long meetingVersion;

    @Column(name = "club_id", nullable = false)
    private Long clubId;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "vote_id", unique = true, nullable = true)
    private Vote vote;

    @Builder.Default
    @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NoticeComment> comments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<NoticeImage> images = new ArrayList<>();

    // ========== 공지사항 ==========
    public void update(
            String title,
            String content,
            boolean important,
            Long meetingId,
            Long meetingVersion
    ) {
        this.title = title;
        this.content = content;
        this.important = important;
        this.meetingId = meetingId;
        this.meetingVersion = meetingVersion;
        this.tag = NoticeTag.decideTag(this.vote != null, meetingId != null);
    }

    public boolean isNotOlderThan(Long meetingVersion) {
        if (meetingVersion == null) {
            throw new ClubNoticeException(ClubNoticeErrorStatus.NOTICE_MEETING_VERSION_NOT_NULL);
        }

        return this.meetingVersion >= meetingVersion;
    }

    // ========== 투표 ==========
    public void attachVote(
            String title, String content,
            String item1, String item2, String item3, String item4, String item5, String item6,
            boolean anonymity, boolean duplication,
            LocalDateTime startTime, LocalDateTime deadline
    ) {
        if (this.vote != null) {
            throw new ClubNoticeException(ClubNoticeErrorStatus.VOTE_ALREADY_EXISTS);
        }
        this.vote = Vote.of(
                title, content, item1, item2, item3, item4, item5, item6, anonymity, duplication, startTime, deadline
        );
    }

    public void updateVoteDeadline(LocalDateTime voteDeadline) {
        this.vote.update(voteDeadline);
    }

    // ========== 댓글 ==========
    public void addComment(NoticeComment comment) {
        if (comment == null) {
            return;
        }
        comment.setNotice(this);
        this.comments.add(comment);
    }

    public void removeComment(NoticeComment noticeComment) {
        if (noticeComment == null) {
            return;
        }
        this.comments.remove(noticeComment);
        noticeComment.setNotice(null);
    }

    // ========== 이미지 ==========
    public List<String> replaceImages(List<String> images) {
        List<String> newImages = images == null ? List.of() : images;
        if (newImages.size() > MAX_IMAGE_COUNT) {
            throw new ClubNoticeException(ClubNoticeErrorStatus.NOTICE_IMAGE_LIMIT_EXCEEDED);
        }

        List<String> oldImages = getImageUrls();

        List<String> removedImages = oldImages.stream()
                .filter(url -> !newImages.contains(url))
                .toList();

        // Hibernate flush 순서에 따른 Duplicate entry 문제를 피하기 위해 기존 이미지 업데이트 후 나머지 삽입/삭제
        int commonSize = Math.min(this.images.size(), newImages.size());
        for (int i = 0; i < commonSize; i++) {
            NoticeImage img = this.images.get(i);
            img.update(newImages.get(i), i);
        }

        for (int i = commonSize; i < newImages.size(); i++) {
            this.images.add(NoticeImage.of(this, newImages.get(i), i));
        }

        for (int i = this.images.size() - 1; i >= newImages.size(); i--) {
            this.images.remove(i);
        }

        // 삭제 요청을 보낼 이미지 URL 반환
        return removedImages;
    }

    public List<String> getImageUrls() {
        return this.images.stream().map(NoticeImage::getImageUrl).toList();
    }
}
