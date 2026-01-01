package checkmo.clubManagement.internal.entity;

import checkmo.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class ClubMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClubMemberStatus clubMemberStatus;

    private String joinMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    @Setter
    private Club club;

    @Column(name = "member_id", nullable = false)
    private String memberId;

    public boolean isStaff() {
        return this.clubMemberStatus == ClubMemberStatus.STAFF;
    }

    public boolean isActive() {
        return this.clubMemberStatus == ClubMemberStatus.MEMBER || this.clubMemberStatus == ClubMemberStatus.STAFF;
    }

    public void updateStatus(ClubMemberStatus newStatus) {
        this.clubMemberStatus = newStatus;
    }

    public enum ClubMemberStatus {
        MEMBER, STAFF, PENDING, BLOCKED
    }

}
