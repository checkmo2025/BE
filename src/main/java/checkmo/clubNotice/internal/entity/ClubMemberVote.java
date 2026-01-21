package checkmo.clubNotice.internal.entity;

import checkmo.clubNotice.internal.exception.ClubNoticeErrorStatus;
import checkmo.clubNotice.internal.exception.ClubNoticeException;
import checkmo.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"club_member_id", "vote_id"})
})
public class ClubMemberVote extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean item1;
    private boolean item2;
    private boolean item3;
    private boolean item4;
    private boolean item5;
    private boolean item6;

    @Column(name = "club_member_id", nullable = false)
    private Long clubMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_id")
    private Vote vote;

    public void setVote(Vote vote) {
        if (vote == null) {
            return;
        }
        this.vote = vote;
    }

    public void updateSelectedItems(List<Integer> selectedItems) {
        this.item1 = false;
        this.item2 = false;
        this.item3 = false;
        this.item4 = false;
        this.item5 = false;
        this.item6 = false;

        if (selectedItems == null) {
            return;
        }

        for (Integer num : selectedItems) {
            if (num == null) {
                continue;
            }
            switch (num) {
                case 1 -> this.item1 = true;
                case 2 -> this.item2 = true;
                case 3 -> this.item3 = true;
                case 4 -> this.item4 = true;
                case 5 -> this.item5 = true;
                case 6 -> this.item6 = true;
                default -> {
                    throw new ClubNoticeException(ClubNoticeErrorStatus.VOTE_ITEM_NOT_FOUND);
                }
            }
        }
    }
}
