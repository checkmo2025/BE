package checkmo.domain.club.entity.meeting;

import checkmo.domain.club.entity.ClubMember;
import checkmo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class BookReview extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    private double rate;

    @Column(name = "club_member_id", insertable = false, updatable = false)
    private Long clubMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_member_id")
    @Setter
    private ClubMember clubMember;

    @Column(name = "meeting_id", insertable = false, updatable = false)
    private Long meetingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id")
    @Setter
    private Meeting meeting;

    public void updateBookReview(String description, double rate) {
        this.description = description;
        this.rate = rate;
    }
}
