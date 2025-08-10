package checkmo.domain.club.entity;

import checkmo.domain.club.entity.meeting.BookReview;
import checkmo.domain.club.entity.meeting.MemberTeam;
import checkmo.domain.club.entity.meeting.Topic;
import checkmo.domain.member.entity.Member;
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
    @OneToMany(mappedBy = "clubMember", cascade = CascadeType.ALL)
    private List<MemberTeam> memberTeams = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "clubMember", cascade = CascadeType.REMOVE)
    private List<Topic> topics = new ArrayList<>();

    public boolean isStaff() {
        return this.clubMemberStatus == ClubMemberStatus.STAFF;
    }

    public void addBookReview(BookReview bookReview) {
        this.bookReviews.add(bookReview);
        bookReview.setClubMember(this);
    }

    public void addTopic(Topic topic) {
        this.topics.add(topic);
        topic.setClubMember(this);
    }

    public void removeTopic(Topic topic) {
        this.topics.remove(topic);
        topic.setClubMember(null);
    }

    public void removeBookReview(BookReview bookReview) {
        this.bookReviews.remove(bookReview);
        bookReview.setClubMember(null);
    }

    public enum ClubMemberStatus {
        MEMBER, STAFF, PENDING, BLOCKED
    }
}
