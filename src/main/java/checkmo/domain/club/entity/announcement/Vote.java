package checkmo.domain.club.entity.announcement;

import checkmo.domain.club.entity.Club;
import checkmo.global.entity.BaseEntity;
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
public class Vote extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String content;

    @Builder.Default
    private String tag = "투표";

    private boolean important;

    @Column(nullable = false)
    private String item1;

    @Column(nullable = false)
    private String item2;

    private String item3;

    private String item4;

    private String item5;

    @Column(nullable = false)
    private boolean anonymity;

    @Column(nullable = false)
    private boolean duplication;

    private LocalDateTime startTime;

    private LocalDateTime deadline;

    @Column(name = "club_id", insertable = false, updatable = false)
    private Long clubId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;

    @Builder.Default
    @OneToMany(mappedBy = "vote", cascade = CascadeType.ALL)
    private List<MemberVote> memberVotes = new ArrayList<>();

    public List<String> getItems() {
        List<String> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);
        if (item3 != null) items.add(item3);
        if (item4 != null) items.add(item4);
        if (item5 != null) items.add(item5);
        return items;
    }

}
