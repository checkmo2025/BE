package checkmo.clubManagement.internal.entity;

import checkmo.clubManagement.internal.excepetion.ClubManagementErrorStatus;
import checkmo.clubManagement.internal.excepetion.ClubManagementException;
import checkmo.common.BaseEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
public class Club extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    private String profileImgUrl;

    @Column(nullable = false)
    private boolean isOpen;

    private String region;

    private LocalDateTime lastActivityAt;

    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "club_participants", joinColumns = @JoinColumn(name = "club_id"))
    @Column(name = "participant_type")
    private List<ClubParticipantType> participantTypes = new ArrayList<>();

    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "club_interest_categories",
            joinColumns = @JoinColumn(name = "club_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private Set<ClubInterestCategory> interestCategories = new HashSet<>();

    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "club_contacts", joinColumns = @JoinColumn(name = "club_id"))
    private List<ClubContact> links = new ArrayList<>();

    public void initializeLastActivityAt(LocalDateTime now) {
        if (this.lastActivityAt == null) {
            this.lastActivityAt = now;
        }
    }

    public void updateField(
            String name,
            String description,
            String profileImgUrl,
            boolean isOpen,
            String region,
            List<ClubParticipantType> participantTypes,
            List<ClubContact> links
    ) {
        if (name != null) {
            this.name = name.trim();
        }
        if (description != null) {
            this.description = description;
        }
        if (profileImgUrl != null) {
            this.profileImgUrl = profileImgUrl;
        }
        this.isOpen = isOpen;
        if (participantTypes != null) {
            this.participantTypes.clear();
            this.participantTypes.addAll(participantTypes);
        }
        if (region != null) {
            this.region = region;
        }
        if (links != null) {
            this.links.clear();
            this.links.addAll(links);
        }
    }

    public void updateInterestCategories(Set<ClubInterestCategory> categories) {
        if (categories == null) {
            return;
        }
        this.interestCategories.clear();
        this.interestCategories.addAll(categories);
    }

    public boolean isDifferent(String clubName) {
        return !this.name.equals(clubName);
    }

    // ============= 클럽 멤버 관련 메서드 ==============
    public ClubMember createOwnerMember(String memberId, LocalDateTime now) {
        return ClubMember.ownerOf(this, memberId, now);
    }

    public ClubMember applyForMembership(String memberId, String joinMessage, LocalDateTime now) {
        ClubMemberStatus status = decideInitialStatus();
        return ClubMember.applyTo(this, memberId, status, joinMessage, now);
    }

    public void reApplyMember(ClubMember existing, String message, LocalDateTime now) {
        validateMemberInClub(existing);
        ClubMemberStatus status = decideInitialStatus();
        existing.reApply(status, message, now);
    }

    public void validateJoinRejection(ClubMember actor, ClubMember target) {
        validateStaffMemberInClub(actor);
        validateMemberInClub(target);
        if (!target.isJoinInProgress()) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_MEMBER_INVALID_STATUS);
        }
    }

    public boolean transferOwnerBy(ClubMember actor, ClubMember target) {
        validateOwnerMemberInClub(actor);
        validateActiveMemberInClub(target);
        return transferOwnerIfNeeded(actor, target);
    }

    private boolean transferOwnerIfNeeded(ClubMember actor, ClubMember target) {
        if (target.isOwner()) {
            return false;
        }
        actor.updateStatus(ClubMemberStatus.STAFF);
        target.updateStatus(ClubMemberStatus.OWNER);
        return true;
    }

    private void validateStaffMemberInClub(ClubMember actor) {
        validateMemberInClub(actor);
        if (!actor.isStaff()) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_STAFF_ONLY);
        }
    }

    private void validateOwnerMemberInClub(ClubMember actor) {
        validateMemberInClub(actor);
        if (!actor.isOwner()) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_OWNER_ONLY);
        }
    }

    private void validateActiveMemberInClub(ClubMember target) {
        validateMemberInClub(target);
        if (!target.isActive()) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_MEMBER_IS_NOT_ACTIVE);
        }
    }

    private void validateMemberInClub(ClubMember clubMember) {
        if (clubMember.getClub() == null || !this.id.equals(clubMember.getClub().getId())) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_MEMBER_NOT_IN_CLUB);
        }
    }

    private ClubMemberStatus decideInitialStatus() {
        return this.isOpen
                ? ClubMemberStatus.MEMBER
                : ClubMemberStatus.PENDING;
    }
}
