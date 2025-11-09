package checkmo.clubMeeting.entity;

import checkmo.clubManagement.entity.ClubMember;
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

    private String description;

    private double rate;

    @Column(name = "club_member_id", insertable = false, updatable = false)
    private Long clubMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_member_id")
    private ClubMember clubMember;

    @Column(name = "meeting_id", insertable = false, updatable = false)
    private Long meetingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id")
    private Meeting meeting;

    public void updateBookReview(String description, double rate) {
        this.description = description;
        this.rate = rate;
    }

    // == 연관관계 메서드 == //
    public void setClubMember(ClubMember clubMember) {
        if (this.clubMember == clubMember) {
            return; // 동일 객체 설정 방지
        }

        // 1. 이전 부모 객체로부터 분리
        if (this.clubMember != null) {
            this.clubMember.getBookReviews().remove(this);
        }

        // 2. 새로운 부모 객체와 연결
        this.clubMember = clubMember;

        // 3. null 검사 & 새로운 부모 객체 연결
        if (clubMember != null && !clubMember.getBookReviews().contains(this)) {
            clubMember.getBookReviews().add(this);
        }
    }

    public void removeClubMember() {
        if (this.clubMember != null) {
            this.clubMember.getBookReviews().remove(this);
            this.clubMember = null;
        }
    }

    public void setMeeting(Meeting meeting) {
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

    public void removeMeeting() {
        if (this.meeting != null) {
            this.meeting.getBookReviews().remove(this);
            this.meeting = null;
        }
    }
}
