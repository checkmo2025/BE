package checkmo.domain.vote.entity;

import checkmo.domain.club.entity.Club;
import checkmo.domain.club.entity.Meeting;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String item1;

    @Column(nullable = false)
    private String item2;

    private String item3;

    private String item4;

    private String item5;

    @Column(nullable = false)
    private boolean isAnonymity;

    @Column(nullable = false)
    private boolean isDuplication;

    private LocalDateTime endTime;

    @Builder.Default
    @OneToMany(mappedBy = "vote", cascade = CascadeType.ALL)
    private List<MemberVote> memberVotes = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;
}
