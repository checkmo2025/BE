package checkmo.club.entity;

import checkmo.club.entity.meeting.BookReview;
import checkmo.club.entity.meeting.MemberTeam;
import checkmo.club.entity.meeting.Topic;
import checkmo.member.entity.Member;
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

    @Column(name = "member_id", insertable = false, updatable = false)
    private String memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Builder.Default
    @OneToMany(mappedBy = "clubMember", cascade = CascadeType.REMOVE)
    private List<BookReview> bookReviews = new ArrayList<>();

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
