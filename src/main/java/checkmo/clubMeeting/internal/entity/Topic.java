package checkmo.clubMeeting.internal.entity;

import checkmo.clubMeeting.internal.exception.ClubMeetingErrorStatus;
import checkmo.clubMeeting.internal.exception.ClubMeetingException;
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
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@EqualsAndHashCode(of = "id", callSuper = false)
public class Topic extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 300, nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @Column(name = "club_member_id", nullable = false)
    private Long clubMemberId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Builder.Default
    @OneToMany(mappedBy = "topic", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<TeamTopic> teamTopics = new ArrayList<>();

    public boolean isOwnedBy(String anotherMemberId) {
        return this.memberId.equals(anotherMemberId);
    }

    public boolean isOwnedBy(Long anotherClubMemberId) {
        return this.clubMemberId.equals(anotherClubMemberId);
    }

    public void updateTopic(String description) {
        this.description = description;
    }

    // == 연관관계 메서드 == //
    public void setMeeting(Meeting meeting) {
        if (meeting == null) {
            throw new ClubMeetingException(ClubMeetingErrorStatus.TOPIC_MEETING_REQUIRED);
        }
        if (this.meeting == meeting) {
            return;
        }
        if (this.meeting != null) {
            this.meeting.getTopics().remove(this);
        }
        this.meeting = meeting;
        if (!meeting.getTopics().contains(this)) {
            meeting.getTopics().add(this);
        }
    }

    public void removeMeeting() {
        if (this.meeting != null) {
            this.meeting.getTopics().remove(this);
            this.meeting = null;
        }
    }
}
