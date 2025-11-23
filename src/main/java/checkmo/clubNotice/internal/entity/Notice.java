package checkmo.clubNotice.internal.entity;

import checkmo.clubNotice.internal.exception.ClubNoticeErrorStatus;
import checkmo.clubNotice.internal.exception.ClubNoticeException;
import checkmo.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 1000)
    private String content;

    private boolean important;

    @Column(nullable = false)
    private String tag;

    @Column(name = "meeting_id")
    private Long meetingId;

    @Column(name = "meeting_version")
    private Long meetingVersion;

    @Column(name = "club_id", nullable = false)
    private Long clubId;

    public boolean isNotOlderThan(Long meetingVersion) {
        if (meetingVersion == null) {
            throw new ClubNoticeException(ClubNoticeErrorStatus.NOTICE_MEETING_VERSION_NOT_NULL);
        }

        return this.meetingVersion >= meetingVersion;
    }

    public boolean isCreatedAfter(LocalDateTime anotherCreatedAt) {
        return this.getCreatedAt().isAfter(anotherCreatedAt);
    }
}
