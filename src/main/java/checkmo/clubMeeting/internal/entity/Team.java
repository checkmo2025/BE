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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"meeting_id", "team_number"})
})
public class Team extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer teamNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @BatchSize(size = 12)
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TeamTopic> teamTopics = new ArrayList<>();

    @BatchSize(size = 12)
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ClubMemberTeam> clubMemberTeams = new ArrayList<>();

    // == 연관관계 메서드 == //
    public void setMeeting(Meeting meeting) {
        if (meeting == null) {
            throw new ClubMeetingException(ClubMeetingErrorStatus.TEAM_MEETING_REQUIRED);
        }
        if (this.meeting == meeting) {
            return;
        }
        if (this.meeting != null) {
            this.meeting.getTeams().remove(this);
        }
        this.meeting = meeting;
        if (!meeting.getTeams().contains(this)) {
            meeting.getTeams().add(this);
        }
    }

    public void removeMeeting() {
        if (this.meeting != null) {
            this.meeting.getTeams().remove(this);
            this.meeting = null;
        }
    }

    public void replaceMembers(List<Long> clubMemberIds) {
        if (hasSameMembers(clubMemberIds)) {
            return;
        }
        this.clubMemberTeams.clear();
        for (Long clubMemberId : clubMemberIds) {
            addClubMemberTeam(ClubMemberTeam.builder()
                    .clubMemberId(clubMemberId)
                    .build());
        }
    }

    private boolean hasSameMembers(List<Long> clubMemberIds) {
        if (clubMemberTeams.size() != clubMemberIds.size()) {
            return false;
        }
        List<Long> unmatchedClubMemberIds = new ArrayList<>(clubMemberIds);
        for (ClubMemberTeam clubMemberTeam : clubMemberTeams) {
            if (!unmatchedClubMemberIds.remove(clubMemberTeam.getClubMemberId())) {
                return false;
            }
        }
        return unmatchedClubMemberIds.isEmpty();
    }

    private void addClubMemberTeam(ClubMemberTeam clubMemberTeam) {
        if (clubMemberTeam == null) {
            return;
        }
        clubMemberTeam.setTeam(this);
    }
}
