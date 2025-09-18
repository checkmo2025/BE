package checkmo.domain.club.entity.meeting;

import checkmo.global.entity.BaseEntity;
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
