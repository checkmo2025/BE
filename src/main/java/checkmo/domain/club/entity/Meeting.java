package checkmo.domain.club.entity;

import checkmo.domain.book.entity.Book;
import checkmo.domain.notice.entity.Notice;
import checkmo.domain.team.entity.Team;
import checkmo.domain.vote.entity.Vote;
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
}
