package checkmo.clubMeeting.internal.entity;

import checkmo.common.BaseEntity;
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
public class Meeting extends BaseEntity {

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

    @Builder.Default
    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Team> teams = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "meeting", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Topic> topics = new ArrayList<>();

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

    public void addSumRate(double rate) {
        this.sumRate += rate;
    }

    public void subtractSumRate(double rate) {
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
        return this.getMeetingTime().plusDays(3);
    }

    // ========= 연관관계 메서드 =========
    public void addTeam(Team team) {
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

    public void removeTeam(Team team) {
        if (team == null) {
            return;
        }
        team.removeMeeting();
    }

}
