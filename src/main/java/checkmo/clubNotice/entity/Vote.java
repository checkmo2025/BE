package checkmo.clubNotice.entity;

import checkmo.clubManagement.entity.Club;
import checkmo.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
        if (item3 != null) {
            items.add(item3);
        }
        if (item4 != null) {
            items.add(item4);
        }
        if (item5 != null) {
            items.add(item5);
        }
        return items;
    }

}
