package checkmo.domain.club.entity.announcement;

import checkmo.domain.club.entity.Club;
import checkmo.domain.club.entity.meeting.Meeting;
import checkmo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Notice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String content;

    private boolean important;

    @Column(nullable = false)
    private String tag; //TODO: "공지", "모임" 2개 값만 가능

    @Column(name = "meeting_id", insertable = false, updatable = false)
    private Long meetingId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id")
    @Setter
    private Meeting meeting;

    @Column(name = "club_id", insertable = false, updatable = false)
    private Long clubId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;

}
