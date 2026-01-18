package checkmo.notification.internal.entity;

import checkmo.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"member_id"})
})
public class NotificationSetting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private String memberId;

    @Column(name = "book_story_liked", nullable = false)
    @Builder.Default
    private boolean bookStoryLiked = true;

    @Column(name = "book_story_comment", nullable = false)
    @Builder.Default
    private boolean bookStoryComment = true;

    @Column(name = "club_notice_created", nullable = false)
    @Builder.Default
    private boolean clubNoticeCreated = true;

    @Column(name = "club_meeting_created", nullable = false)
    @Builder.Default
    private boolean clubMeetingCreated = true;

    @Column(name = "new_follower", nullable = false)
    @Builder.Default
    private boolean newFollower = true;

    @Column(name = "join_club", nullable = false)
    @Builder.Default
    private boolean joinClub = true;

    public void toggleBookStoryLiked() {
        this.bookStoryLiked = !this.bookStoryLiked;
    }

    public void toggleBookStoryComment() {
        this.bookStoryComment = !this.bookStoryComment;
    }

    public void toggleClubNoticeCreated() {
        this.clubNoticeCreated = !this.clubNoticeCreated;
    }

    public void toggleClubMeetingCreated() {
        this.clubMeetingCreated = !this.clubMeetingCreated;
    }

    public void toggleNewFollower() {
        this.newFollower = !this.newFollower;
    }

    public void toggleJoinClub() {
        this.joinClub = !this.joinClub;
    }

}
