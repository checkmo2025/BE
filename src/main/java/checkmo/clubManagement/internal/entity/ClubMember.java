package checkmo.clubManagement.internal.entity;

import checkmo.clubManagement.internal.excepetion.ClubManagementErrorStatus;
import checkmo.clubManagement.internal.excepetion.ClubManagementException;
import checkmo.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    private LocalDateTime appliedAt; // 이번 가입 신청일

    private LocalDateTime joinedAt; // 이번 가입 승인일

    private LocalDateTime endedAt; // 탈퇴/강퇴일

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;

    @Column(name = "member_id", nullable = false)
    private String memberId;

    /**
     * package-private으로 엔티티 외부에서 함부로 호출 불가능하게 설정
     * <p>
     * 클럽장 전용 메서드
     */
    static ClubMember ownerOf(Club club, String memberId, LocalDateTime now) {
        return ClubMember.builder()
                .club(club)
                .memberId(memberId)
                .clubMemberStatus(ClubMemberStatus.OWNER)
                .appliedAt(now)
                .joinedAt(now)
                .build();
    }

    /**
     * package-private으로 엔티티 외부에서 함부로 호출 불가능하게 설정
     * <p>
     * 신규 가입 신청 전용 메서드
     */
    static ClubMember applyTo(Club club, String memberId, ClubMemberStatus status, String message, LocalDateTime now) {
        return ClubMember.builder()
                .club(club)
                .memberId(memberId)
                .clubMemberStatus(status)
                .joinMessage(message)
                .appliedAt(now)
                .joinedAt(status == ClubMemberStatus.MEMBER ? now : null)
                .build();
    }

    // 재가입 전용 메서드
    public void reApply(ClubMemberStatus status, String message, LocalDateTime now) {
        if (this.isActive()) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_MEMBER_ALREADY_JOINED);
        }
        this.clubMemberStatus = status;
        this.joinMessage = message;
        this.appliedAt = now;
        this.joinedAt = (status == ClubMemberStatus.MEMBER) ? now : null;
    }

    // 가입 승인 전용 메서드
    public void approveJoin(LocalDateTime now) {
        if (!this.getClubMemberStatus().isJoinInProgress()) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_MEMBER_INVALID_STATUS);
        }
        this.clubMemberStatus = ClubMemberStatus.MEMBER;
        this.joinedAt = now;
    }

    // 탈퇴 전용 메서드
    public void leave(LocalDateTime now) {
        if (this.isOwner()) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_OWNER_CANNOT_LEAVE);
        }
        if (!this.isActive()) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_MEMBER_INVALID_STATUS);
        }
        this.clubMemberStatus = ClubMemberStatus.WITHDRAWN;
        this.endedAt = now;
    }

    // 강퇴 전용 메서드
    public void kick(LocalDateTime now) {
        if (this.getClubMemberStatus().isOwner()) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_OWNER_CANNOT_BE_KICKED);
        }
        if (!this.isActive()) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_MEMBER_INVALID_STATUS);
        }
        this.clubMemberStatus = ClubMemberStatus.KICKED;
        this.endedAt = now;
    }

    public void changeRoleBy(ClubMember actor, ClubMemberStatus newStatus) {
        if (isSameMember(actor)) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_MEMBER_CANNOT_CHANGE_OWN_ROLE);
        }
        if (!this.isActive()) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_MEMBER_IS_NOT_ACTIVE);
        }
        if (this.isOwner()) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_OWNER_ROLE_CHANGE_NOT_ALLOWED);
        }
        this.clubMemberStatus = newStatus;
    }

    private boolean isSameMember(ClubMember other) {
        return this == other || (this.id != null && this.id.equals(other.getId()));
    }

    public void updateStatus(ClubMemberStatus newStatus) {
        this.clubMemberStatus = newStatus;
    }

    // 상태 확인 메서드
    public boolean isOwner() {
        return this.clubMemberStatus.isOwner();
    }

    public boolean isStaff() {
        return this.clubMemberStatus.isStaff();
    }

    public boolean isActive() {
        return this.clubMemberStatus.isActive();
    }

    public boolean isJoinInProgress() {
        return this.clubMemberStatus.isJoinInProgress();
    }
}
