package checkmo.domain.club.entity.meeting;

import checkmo.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@EqualsAndHashCode(of = "id", callSuper = false)
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"topic_id", "team_id"})
})
public class TeamTopic extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "topic_id", insertable = false, updatable = false)
    private Long topicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @Column(name = "team_id", insertable = false, updatable = false)
    private Long teamId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Version
    @Builder.Default
    private Long version = 0L; // 팀 발제 선택 취소에 대한 동시성 제어를 위한 버전 관리

    // == 연관관계 메서드 == //
    public void setTeam(Team team) {
        if (this.team == team) return;
        if (this.team != null) {
            this.team.getTeamTopics().remove(this);
        }
        this.team = team;
        if (team != null && !team.getTeamTopics().contains(this)) {
            team.getTeamTopics().add(this);
        }
    }

    public void removeTeam() {
        if (this.team != null) {
            this.team.getTeamTopics().remove(this);
            this.team = null;
        }
    }

    public void setTopic(Topic topic) {
        if (this.topic == topic) return;
        if (this.topic != null) {
            this.topic.getTeamTopics().remove(this);
        }
        this.topic = topic;
        if (topic != null && !topic.getTeamTopics().contains(this)) {
            topic.getTeamTopics().add(this);
        }
    }

    public void removeTopic() {
        if (this.topic != null) {
            this.topic.getTeamTopics().remove(this);
            this.topic = null;
        }
    }
}
