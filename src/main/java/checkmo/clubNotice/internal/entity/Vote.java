package checkmo.clubNotice.internal.entity;

import checkmo.clubNotice.internal.exception.ClubNoticeErrorStatus;
import checkmo.clubNotice.internal.exception.ClubNoticeException;
import checkmo.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @Builder.Default
    @OneToMany(mappedBy = "vote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClubMemberVote> clubMemberVotes = new ArrayList<>();

    public static Vote of(
            String title, String content,
            String item1, String item2, String item3, String item4, String item5,
            boolean anonymity, boolean duplication,
            LocalDateTime startTime, LocalDateTime deadline
    ) {
        Vote vote = Vote.builder()
                .title(title)
                .content(content)
                .item1(item1)
                .item2(item2)
                .item3(item4)
                .item4(item4)
                .item5(item5)
                .anonymity(anonymity)
                .duplication(duplication)
                .startTime(startTime)
                .deadline(deadline)
                .build();
        vote.validateVotePeriod();
        return vote;
    }

    public List<Integer> getItemNumbers() {
        List<Integer> itemNumbers = new ArrayList<>();
        if (hasText(item1)) {
            itemNumbers.add(1);
        }
        if (hasText(item2)) {
            itemNumbers.add(2);
        }
        if (hasText(item3)) {
            itemNumbers.add(3);
        }
        if (hasText(item4)) {
            itemNumbers.add(4);
        }
        if (hasText(item5)) {
            itemNumbers.add(5);
        }

        if (itemNumbers.size() < 2) {
            throw new ClubNoticeException(ClubNoticeErrorStatus.INSUFFICIENT_VOTE_ITEMS);
        }
        return itemNumbers;
    }

    public String getItemByNumber(Integer itemNumber) {
        if (itemNumber == null) {
            return null;
        }
        return switch (itemNumber) {
            case 1 -> item1;
            case 2 -> item2;
            case 3 -> item3;
            case 4 -> item4;
            case 5 -> item5;
            default -> null;
        };
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

    public void validateChoiceCountBasedOnDuplication(int selectedItems) {
        if (!this.duplication && selectedItems > 1) {
            throw new ClubNoticeException(ClubNoticeErrorStatus.MULTIPLE_SELECTION_NOT_ALLOWED);
        }
    }

    private boolean hasText(String text) {
        return text != null && !text.isBlank();
    }

    public void upsertClubMemberVote(
            Long clubMemberId,
            List<Integer> selectedItemNumbers,
            ClubMemberVote created
    ) {
        List<Integer> safeNumbers = selectedItemNumbers == null ? List.of() : selectedItemNumbers;

        ClubMemberVote exisiting = clubMemberVotes.stream()
                .filter(vote -> clubMemberId.equals(vote.getClubMemberId()))
                .findFirst()
                .orElse(null);

        if (exisiting != null) {
            exisiting.updateSelectedItems(safeNumbers);
            return;
        }
        clubMemberVotes.add(created);
    }

    public void update(LocalDateTime deadline) {
        this.deadline = deadline;
        validateVotePeriod();
    }

    private void validateVotePeriod() {
        if (startTime == null || deadline == null) {
            throw new ClubNoticeException(ClubNoticeErrorStatus.VOTE_PERIOD_REQUIRED);
        }
        if (!startTime.isBefore(deadline)) {
            throw new ClubNoticeException(ClubNoticeErrorStatus.VOTE_START_AFTER_DEADLINE);
        }
    }

}
