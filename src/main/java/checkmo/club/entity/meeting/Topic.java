package checkmo.club.entity.meeting;

import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
import checkmo.club.entity.ClubMember;
import checkmo.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@EqualsAndHashCode(of = "id", callSuper = false)
public class Topic extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @Column(name = "meeting_id", insertable = false, updatable = false)
    private Long meetingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @Column(name = "club_member_id", insertable = false, updatable = false)
    private Long clubMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_member_id")
    private ClubMember clubMember;

    @Builder.Default
    @OneToMany(mappedBy = "topic", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<TeamTopic> teamTopics = new ArrayList<>();

    public boolean isOwnedBy(ClubMember clubMember) {
        return this.clubMember != null && this.clubMemberId.equals(clubMember.getId());
    }

    public void updateTopic(String description) {
        this.description = description;
    }

    // == 연관관계 메서드 == //
    public void setMeeting(Meeting meeting) {
        if (meeting == null) {
            throw new GeneralException(ErrorStatus.TOPIC_MEETING_REQUIRED);
        }
        if (this.meeting == meeting) return;
        if (this.meeting != null) {
            this.meeting.getTopics().remove(this);
        }
        this.meeting = meeting;
        if (!meeting.getTopics().contains(this)) {
            meeting.getTopics().add(this);
        }
    }

    public void removeMeeting() {
        if (this.meeting != null) {
            this.meeting.getTopics().remove(this);
            this.meeting = null;
        }
    }

    public void setClubMember(ClubMember clubMember) {
        if (clubMember == null) {
            throw new GeneralException(ErrorStatus.TOPIC_CLUB_MEMBER_REQUIRED);
        }
        if (this.clubMember == clubMember) return;
        if (this.clubMember != null) {
            this.clubMember.getTopics().remove(this);
        }
        this.clubMember = clubMember;
        if (!clubMember.getTopics().contains(this)) {
            clubMember.getTopics().add(this);
        }
    }

    public void removeClubMember() {
        if (this.clubMember != null) {
            this.clubMember.getTopics().remove(this);
            this.clubMember = null;
        }
    }
}
