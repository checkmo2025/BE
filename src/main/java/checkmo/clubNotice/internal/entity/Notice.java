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

    public boolean isNotOlderThan(Long meetingVersion) {
        if (meetingVersion == null) {
            throw new ClubNoticeException(ClubNoticeErrorStatus.NOTICE_MEETING_VERSION_NOT_NULL);
        }

        return this.meetingVersion >= meetingVersion;
    }

}
