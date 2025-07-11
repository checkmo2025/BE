package checkmo.domain.club.entity.meeting;

import checkmo.domain.book.entity.Book;
import checkmo.domain.club.entity.*;
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

    private double sumRate; //미팅에 대한 평점 총합이 아닌, 모임이 진행된 책에 대한 평점 총합

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book; // null 허용

    @Builder.Default
    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL)
    private List<Team> teams = new ArrayList<>();

    @OneToOne(mappedBy = "meeting", cascade = CascadeType.ALL)
    private Notice notice;

    @Builder.Default
    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL)
    private List<Topic> topics = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL)
    private List<BookReview> bookReviews = new ArrayList<>();

    public void calculateSumRate(double rate) {
        if (this.sumRate == 0) {
            this.sumRate = rate;
        } else {
            this.sumRate += rate;
        }
    }

    public void calculateAverageRate() {
        if (this.bookReviews.isEmpty()) {
            this.sumRate = 0;
        } else {
            this.sumRate /= this.bookReviews.size();
        }
    }
}
