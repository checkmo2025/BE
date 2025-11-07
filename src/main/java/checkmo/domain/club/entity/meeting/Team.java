package checkmo.domain.club.entity.meeting;

import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
import checkmo.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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
    private Integer teamNumber; // 팀 번호

    @Column(name = "meeting_id", insertable = false, updatable = false)
    private Long meetingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @OneToMany(mappedBy = "team", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private List<TeamTopic> teamTopics = new ArrayList<>();

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MemberTeam> memberTeams = new ArrayList<>();

    public void clearMemberTeams() {
        this.memberTeams.clear();
    }

    // == 연관관계 메서드 == //
    public void setMeeting(Meeting meeting) {
        if (meeting == null) {
            throw new GeneralException(ErrorStatus.TEAM_MEETING_REQUIRED);
        }
        if (this.meeting == meeting) return;
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
}
