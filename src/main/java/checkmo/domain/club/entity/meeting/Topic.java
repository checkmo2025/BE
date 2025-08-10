package checkmo.domain.club.entity.meeting;

import checkmo.domain.club.entity.ClubMember;
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
public class Topic extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @Column(name = "meeting_id", insertable = false, updatable = false)
    private Long meetingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    @Setter
    private Meeting meeting;

    @Column(name = "club_member_id", insertable = false, updatable = false)
    private Long clubMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_member_id")
    @Setter
    private ClubMember clubMember;

    @Builder.Default
    @OneToMany(mappedBy = "topic", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<TeamTopic> teamTopics = new ArrayList<>();

    public boolean isOwnedBy(ClubMember clubMember) {
        return this.clubMember != null && this.clubMember.equals(clubMember);
    }

    public void updateTopic(String description) {
        this.description = description;
    }

    public void addTeamTopic(TeamTopic teamTopic) {
        this.teamTopics.add(teamTopic);
        teamTopic.setTopic(this);
    }

    public void removeTeamTopic(TeamTopic teamTopic) {
        this.teamTopics.remove(teamTopic);
        teamTopic.setTopic(null);
    }
}
