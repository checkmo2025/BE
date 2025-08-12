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
public class Team extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer TeamNumber; // 팀 번호

    @Column(name = "meeting_id", insertable = false, updatable = false)
    private Long meetingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    @Setter
    private Meeting meeting;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
    @Builder.Default
    private List<TeamTopic> teamTopics = new ArrayList<>();

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MemberTeam> memberTeams = new ArrayList<>();

    public void addMemberTeam(MemberTeam memberTeam) {
        this.memberTeams.add(memberTeam);
        memberTeam.setTeam(this);
    }

    public void clearMemberTeams() {
        this.memberTeams.clear();
    }
}
