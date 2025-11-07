package checkmo.club.entity.meeting;

import checkmo.club.entity.ClubMember;
import checkmo.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class MemberTeam extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "club_member_id", insertable = false, updatable = false)
    private Long clubMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_member_id")
    private ClubMember clubMember;

    @Column(name = "team_id", insertable = false, updatable = false)
    private Long teamId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    // == 연관관계 메서드 == //
    public void setClubMember(ClubMember clubMember) {
        this.clubMember = clubMember;
        if (!clubMember.getMemberTeams().contains(this)) {
            clubMember.getMemberTeams().add(this);
        }
    }

    public void setTeam(Team team) {
        this.team = team;
        if (!team.getMemberTeams().contains(this)) {
            team.getMemberTeams().add(this);
        }
    }
}
