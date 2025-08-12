package checkmo.domain.club.entity.meeting;

import checkmo.domain.book.entity.Book;
import checkmo.domain.club.entity.Club;
import checkmo.domain.club.entity.announcement.Notice;
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
public class Meeting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private LocalDateTime meetingTime;

    private String location;

    private String content;

    @Column(nullable = false)
    private int generation;

    private String tag;

    @Builder.Default
    private double sumRate = 0;

    @Version
    @Builder.Default
    private Long version = 0L; // sumRate 동시성 문제 해결을 위한 버전 관리(낙관적 락)

    @Column(name = "club_id", insertable = false, updatable = false)
    private Long clubId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    @Setter
    private Club club;

    @Column(name = "book_id", insertable = false, updatable = false)
    private String bookId; // null 허용

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    @Setter
    private Book book; // null 허용

    @Builder.Default
    @OneToMany(mappedBy = "meeting", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Team> teams = new ArrayList<>();

    @OneToOne(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private Notice notice;

    @Builder.Default
    @OneToMany(mappedBy = "meeting")
    private List<Topic> topics = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL)
    private List<BookReview> bookReviews = new ArrayList<>();

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
        if (this.bookReviews.isEmpty()) {
            return 0;
        } else {
            return this.sumRate / this.bookReviews.size();
        }
    }

    public void updateMeeting(String title, LocalDateTime meetingTime,
                              String location, String content, int generation, String tag) {
        this.title = title;
        this.meetingTime = meetingTime;
        this.location = location;
        this.content = content;
        this.generation = generation;
        this.tag = tag;
    }

    public void addNotice(Notice notice) {
        this.notice = notice;
        notice.setMeeting(this); // 주인 쪽에도 세팅
    }

    public void addBookReview(BookReview bookReview) {
        this.bookReviews.add(bookReview);
        bookReview.setMeeting(this); // 주인 쪽에도 세팅
    }

    public void replaceNotice(Notice newNotice) {
        // 기존 Notice 연결 끊기 (orphanRemoval = true면 자동 삭제됨)
        if (this.notice != null) {
            this.notice.setMeeting(null);
        }

        this.notice = newNotice;
        if (newNotice != null) {
            newNotice.setMeeting(this);
        }
    }

    public void addTopic(Topic topic) {
        this.topics.add(topic);
        topic.setMeeting(this); // 주인 쪽에도 세팅
    }

    public void addTeam(Team team) {
        if (team == null) return;
        this.teams.add(team);
        team.setMeeting(this);
    }

    public void removeTeam(Team team) {
        if (team == null) return;
        this.teams.remove(team);
        team.setMeeting(null);
    }
}
