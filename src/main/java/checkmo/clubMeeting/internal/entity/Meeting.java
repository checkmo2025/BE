package checkmo.clubMeeting.internal.entity;

import checkmo.clubMeeting.internal.exception.ClubMeetingErrorStatus;
import checkmo.clubMeeting.internal.exception.ClubMeetingException;
import checkmo.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
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
public class Meeting extends BaseEntity {
    private static final int CHAT_AVAILABLE_DAYS_AFTER_MEETING = 180;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private LocalDateTime meetingTime;

    private String location;

    private Integer generation;

    private String tag;

    @Builder.Default
    private double sumRate = 0;

    @Version
    @Builder.Default
    private Long version = 0L; // sumRate 동시성 문제 해결을 위한 버전 관리(낙관적 락)

    @Column(name = "club_id", nullable = false)
    private Long clubId;

    @Column(name = "book_id", nullable = false)
    private String bookId;

    @Getter(AccessLevel.PACKAGE)
    @Builder.Default
    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Team> teams = new ArrayList<>();

    @Getter(AccessLevel.PACKAGE)
    @Builder.Default
    @OneToMany(mappedBy = "meeting", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Topic> topics = new ArrayList<>();

    @Getter(AccessLevel.PACKAGE)
    @Builder.Default
    @OneToMany(mappedBy = "meeting", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<BookReview> bookReviews = new ArrayList<>();

    // ========= Domain Mehtods =========
    public void updateMeeting(
            String title,
            LocalDateTime meetingTime,
            String location,
            Integer generation,
            String tag
    ) {
        this.title = title;
        this.meetingTime = meetingTime;
        this.location = location;
        this.generation = generation;
        this.tag = tag;
    }

    public void addBookReview(BookReview review) {
        review.setMeeting(this);
        addSumRate(review.getRate());
    }

    public void reviseBookReviewBy(
            ClubMeetingActor actor,
            BookReview review,
            String description,
            double newRate
    ) {
        validateBookReviewAuthorOrStaff(actor, review);
        reviseBookReview(review, description, newRate);
    }

    public void removeBookReviewBy(ClubMeetingActor actor, BookReview review) {
        validateBookReviewAuthorOrStaff(actor, review);
        removeBookReview(review);
    }

    private void validateBookReviewAuthorOrStaff(ClubMeetingActor actor, BookReview review) {
        if (!review.isOwnedBy(actor.clubMemberId()) && !actor.staff()) {
            throw new ClubMeetingException(ClubMeetingErrorStatus.BOOK_REVIEW_FORBIDDEN);
        }
    }

    private void reviseBookReview(BookReview review, String description, double newRate) {
        double oldRate = review.getRate();
        review.updateBookReview(description, newRate);

        if (oldRate != newRate) {
            subtractSumRate(oldRate);
            addSumRate(newRate);
        }
    }

    private void removeBookReview(BookReview review) {
        subtractSumRate(review.getRate());
        review.removeMeeting();
    }

    private void addSumRate(double rate) {
        this.sumRate += rate;
    }

    private void subtractSumRate(double rate) {
        if (this.sumRate < rate) {
            this.sumRate = this.bookReviews.stream()
                    .mapToDouble(BookReview::getRate)
                    .sum();
        }
        this.sumRate -= rate;
    }

    public double calculateAverageRate() {
        return this.bookReviews.isEmpty() ? 0 : this.sumRate / this.bookReviews.size();
    }

    public LocalDateTime getChatDeadline() {
        return this.getMeetingTime().plusDays(CHAT_AVAILABLE_DAYS_AFTER_MEETING);
    }

    public void organizeTeams(Map<Integer, List<Long>> requestedMembersByTeamNumber) {
        Map<Integer, Team> existingTeamsByTeamNumber = this.teams.stream()
                .collect(Collectors.toMap(Team::getTeamNumber, Function.identity()));

        for (Map.Entry<Integer, List<Long>> entry : requestedMembersByTeamNumber.entrySet()) {
            Team team = existingTeamsByTeamNumber.get(entry.getKey());
            if (team == null) {
                team = Team.builder()
                        .teamNumber(entry.getKey())
                        .build();
                addTeam(team);
            }
            team.replaceMembers(entry.getValue());
        }

        for (Team team : new ArrayList<>(this.teams)) {
            if (!requestedMembersByTeamNumber.containsKey(team.getTeamNumber())) {
                removeTeam(team);
            }
        }
    }

    // ========= 연관관계 메서드 =========
    private void addTeam(Team team) {
        if (team == null) {
            return;
        }
        team.setMeeting(this);
    }

    public void removeAllTeams() {
        for (Team team : new ArrayList<>(this.teams)) {
            removeTeam(team);
        }
    }

    private void removeTeam(Team team) {
        if (team == null) {
            return;
        }
        team.removeMeeting();
    }
}
