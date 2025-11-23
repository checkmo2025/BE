package checkmo.clubNotice.internal.entity;

import checkmo.clubNotice.internal.exception.ClubNoticeErrorStatus;
import checkmo.clubNotice.internal.exception.ClubNoticeException;
import checkmo.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
    @Enumerated(EnumType.STRING)
    private NoticeTag tag = NoticeTag.VOTE;

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

    @Column(name = "club_id", nullable = false)
    private Long clubId;

    @Builder.Default
    @OneToMany(mappedBy = "vote", cascade = CascadeType.ALL)
    private List<ClubMemberVote> clubMemberVotes = new ArrayList<>();

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

    public boolean isWithinVotingPeriod(LocalDateTime localDateTime) {
        if (startTime != null && localDateTime.isBefore(startTime)) {
            return false;
        }
        if (deadline != null && localDateTime.isAfter(deadline)) {
            return false;
        }
        return true;
    }

    public void validateVoteRequest(int selectedItems) {
        if (!this.duplication && selectedItems > 1) {
            throw new ClubNoticeException(ClubNoticeErrorStatus.MULTIPLE_SELECTION_NOT_ALLOWED);
        }
    }

}
