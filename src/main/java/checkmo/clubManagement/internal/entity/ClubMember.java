package checkmo.clubManagement.internal.entity;

import checkmo.clubMeeting.internal.entity.MemberTeam;
import checkmo.clubMeeting.internal.entity.Topic;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class ClubMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClubMemberStatus clubMemberStatus;

    private String joinMessage;

    @Column(name = "club_id", insertable = false, updatable = false)
    private Long clubId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    @Setter
    private Club club;

    @JoinColumn(name = "member_id", nullable = false)
    private String memberId;

    @Builder.Default
    @OneToMany(mappedBy = "clubMember", cascade = CascadeType.ALL)
    private List<BookRecommend> bookRecommends = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "clubMember", cascade = CascadeType.REMOVE)
    private List<MemberTeam> memberTeams = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "clubMember", cascade = CascadeType.REMOVE)
    private List<Topic> topics = new ArrayList<>();

    public boolean isStaff() {
        return this.clubMemberStatus == ClubMemberStatus.STAFF;
    }

    public boolean isActive() {
        return this.clubMemberStatus == ClubMemberStatus.MEMBER || this.clubMemberStatus == ClubMemberStatus.STAFF;
    }

    public void updateStatus(ClubMemberStatus newStatus) {
        this.clubMemberStatus = newStatus;
    }

    public enum ClubMemberStatus {
        MEMBER, STAFF, PENDING, BLOCKED
    }

}
