package checkmo.domain.club.entity.meeting;

import checkmo.global.entity.BaseEntity;
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
    @Setter
    private Topic topic;

    @Column(name = "team_id", insertable = false, updatable = false)
    private Long teamId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    @Setter
    private Team team;

    @Version
    @Builder.Default
    private Long version = 0L; // 팀 발제 선택 취소에 대한 동시성 제어를 위한 버전 관리
}
