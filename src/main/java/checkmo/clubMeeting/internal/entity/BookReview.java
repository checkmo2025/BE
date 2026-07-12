package checkmo.clubMeeting.internal.entity;

import checkmo.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@EqualsAndHashCode(of = "id", callSuper = false)
public class BookReview extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 300, nullable = false)
    private String description;

    private double rate;

    @Column(name = "club_member_id", nullable = false)
    private Long clubMemberId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id")
    private Meeting meeting;

    void updateBookReview(String description, double rate) {
        this.description = description;
        this.rate = rate;
    }

    public boolean isOwnedBy(Long clubMemberId) {
        return this.clubMemberId.equals(clubMemberId);
    }

    void setMeeting(Meeting meeting) {
        if (this.meeting == meeting) {
            return;
        }

        if (this.meeting != null) {
            this.meeting.getBookReviews().remove(this);
        }

        this.meeting = meeting;

        if (!meeting.getBookReviews().contains(this)) {
            meeting.getBookReviews().add(this);
        }
    }

    void removeMeeting() {
        if (this.meeting != null) {
            this.meeting.getBookReviews().remove(this);
            this.meeting = null;
        }
    }
}
