package checkmo.domain.club.entity;

import checkmo.domain.book.entity.BookRecommend;
import checkmo.domain.book.entity.BookReview;
import checkmo.domain.book.entity.Topic;
import checkmo.domain.club.entity.enums.ClubMemberRole;
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

    @Column(nullable = false)
    private boolean isApproved= false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClubMemberRole clubMemberRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Builder.Default
    @OneToMany(mappedBy = "clubMember", cascade = CascadeType.ALL)
    private List<BookReview> bookReviews = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "clubMember", cascade = CascadeType.ALL)
    private List<BookRecommend> bookRecommends = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "clubMember", cascade = CascadeType.ALL)
    private List<Topic> topics = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "clubMember", cascade = CascadeType.ALL)
    private List<ClubTeam> clubTeams = new ArrayList<>();
}
