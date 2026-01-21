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
    private static final int MIN_ITEM_COUNT = 2;

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

    private String item6;

    @Column(nullable = false)
    private boolean anonymity;

    @Column(nullable = false)
    private boolean duplication;

    private LocalDateTime startTime;

    private LocalDateTime deadline;

    @Builder.Default
    @OneToMany(mappedBy = "vote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClubMemberVote> clubMemberVotes = new ArrayList<>();

    // ========== 정적 팩토리 메서드 ==========
    public static Vote of(
            String title, String content,
            String item1, String item2, String item3, String item4, String item5, String item6,
            boolean anonymity, boolean duplication,
            LocalDateTime startTime, LocalDateTime deadline
    ) {
        Vote vote = Vote.builder()
                .title(title)
                .content(content)
                .item1(item1)
                .item2(item2)
                .item3(item3)
                .item4(item4)
                .item5(item5)
                .item6(item6)
                .anonymity(anonymity)
                .duplication(duplication)
                .startTime(startTime)
                .deadline(deadline)
                .build();
        vote.validateVotePeriod();
        return vote;
    }

    // ========== 조회 메서드 ==========
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
        if (hasText(item6)) {
            itemNumbers.add(6);
        }

        if (itemNumbers.size() < MIN_ITEM_COUNT) {
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
            case 6 -> item6;
            default -> null;
        };
    }

    // ========== 업데이트 메서드 ==========
    public void upsertClubMemberVote(
            Long clubMemberId,
            List<Integer> selectedItemNumbers,
            ClubMemberVote created
    ) {
        List<Integer> safeNumbers = selectedItemNumbers == null ? List.of() : selectedItemNumbers;

        ClubMemberVote existing = clubMemberVotes.stream()
                .filter(vote -> clubMemberId.equals(vote.getClubMemberId()))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.updateSelectedItems(safeNumbers);
            return;
        }
        clubMemberVotes.add(created);
    }

    public void update(LocalDateTime deadline) {
        this.deadline = deadline;
        validateVotePeriod();
    }

    // ========== 검증 메서드 ==========
    public void validateVotingTime(LocalDateTime localDateTime) {
        if (startTime == null || deadline == null) {
            throw new ClubNoticeException(ClubNoticeErrorStatus.VOTE_PERIOD_REQUIRED);
        }
        if (localDateTime.isBefore(startTime) || localDateTime.isAfter(deadline)) {
            throw new ClubNoticeException(ClubNoticeErrorStatus.VOTE_TIME_INVALID);
        }
    }

    public void validateChoiceCountBasedOnDuplication(int selectedItems) {
        if (!this.duplication && selectedItems > 1) {
            throw new ClubNoticeException(ClubNoticeErrorStatus.MULTIPLE_SELECTION_NOT_ALLOWED);
        }
    }

    public void validateSelectedItemNumbersExist(List<Integer> selectedItemNumbers) {
        List<Integer> validItemNumbers = getItemNumbers();
        List<Integer> itemNumbers = selectedItemNumbers == null ? List.of() : selectedItemNumbers;
        boolean inValid = itemNumbers.stream()
                .anyMatch(num -> !validItemNumbers.contains(num));
        if (inValid) {
            throw new ClubNoticeException(ClubNoticeErrorStatus.VOTE_ITEM_NOT_FOUND);
        }
    }

    private void validateVotePeriod() {
        if (startTime == null || deadline == null) {
            throw new ClubNoticeException(ClubNoticeErrorStatus.VOTE_PERIOD_REQUIRED);
        }
        if (!startTime.isBefore(deadline)) {
            throw new ClubNoticeException(ClubNoticeErrorStatus.VOTE_START_AFTER_DEADLINE);
        }
    }

    // ========== 헬퍼 메서드 ==========
    private boolean hasText(String text) {
        return text != null && !text.isBlank();
    }

}
