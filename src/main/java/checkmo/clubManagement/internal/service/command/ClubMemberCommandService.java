package checkmo.clubManagement.internal.service.command;

import checkmo.clubManagement.ClubManagementEvent.JoinClubEvent;
import checkmo.clubManagement.internal.entity.Club;
import checkmo.clubManagement.internal.entity.ClubMember;
import checkmo.clubManagement.internal.entity.ClubMemberStatus;
import checkmo.clubManagement.internal.excepetion.ClubManagementErrorStatus;
import checkmo.clubManagement.internal.excepetion.ClubManagementException;
import checkmo.clubManagement.internal.repository.ClubMemberRepository;
import checkmo.clubManagement.internal.service.query.ClubManagementQueryService;
import checkmo.clubManagement.internal.service.query.ClubMemberQueryService;
import checkmo.clubManagement.web.dto.ClubRequestDTO.ClubMemberStatusAction;
import checkmo.clubManagement.web.dto.ClubRequestDTO.JoinClub;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ClubMemberCommandService {

    private final ClubManagementQueryService clubManagementQueryService;
    private final ClubMemberQueryService clubMemberQueryService;

    private final ClubMemberRepository clubMemberRepository;

    private final ApplicationEventPublisher eventPublisher;

    public void joinClub(Long clubId, String memberId, JoinClub request) {
        Club club = clubManagementQueryService.validateClub(clubId);
        LocalDateTime now = LocalDateTime.now();

        clubMemberRepository.findByClubIdAndMemberId(club.getId(), memberId)
                .ifPresentOrElse(existing -> { // 이미 가입 이력이 있는 경우, 재가입
                            club.reapplyMember(existing, request.getJoinMessage(), now);
                            if (club.isOpen() && existing.isActive()) {
                                publishJoinClubEvent(memberId, club, existing);
                            }
                        },
                        () -> {
                            ClubMember created = club.applyMember(memberId, request.getJoinMessage(), now);
                            clubMemberRepository.save(created);
                            if (club.isOpen() && created.isActive()) {
                                publishJoinClubEvent(memberId, club, created);
                            }
                        });
    }

    public void updateClubMemberStatus(Long clubId, String actorId, Long targetId, ClubMemberStatusAction request) {
        Club club = clubManagementQueryService.validateClub(clubId);
        ClubMember actor = clubMemberQueryService.validateClubMember(clubId, actorId);
        if (!actor.isStaff()) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_STAFF_ONLY);
        }

        ClubMember target = clubMemberQueryService.validateClubMember(clubId, targetId);
        LocalDateTime now = LocalDateTime.now();

        switch (request.getCommand()) {
            case APPROVE -> approveJoin(club, target, now);
            case REJECT -> rejectJoin(club, target);
            case CHANGE_ROLE -> changeRole(actor, target, request.getStatus());
            case TRANSFER_OWNER -> transferOwner(actor, target);
            case KICK -> kickMember(target, now);
        }
    }

    public void leaveClub(Long clubId, String memberId) {
        clubManagementQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);
        clubMember.leave(LocalDateTime.now());
    }

    private void approveJoin(Club club, ClubMember target, LocalDateTime now) {
        target.join(now);
        publishJoinClubEvent(target.getMemberId(), club, target);
    }

    private void rejectJoin(Club club, ClubMember target) {
        if (!target.getClubMemberStatus().isJoinInProgress()) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_MEMBER_INVALID_STATUS);
        }
        club.removeMember(target);
    }

    private void changeRole(ClubMember actor, ClubMember target, ClubMemberStatus newStatus) {
        if (actor.getId().equals(target.getId())) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_MEMBER_CANNOT_CHANGE_OWN_ROLE);
        }
        if (!target.isActive()) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_MEMBER_IS_NOT_ACTIVE);
        }
        if (target.isOwner()) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_OWNER_ROLE_CHANGE_NOT_ALLOWED);
        }
        target.updateStatus(newStatus);
    }

    private void transferOwner(ClubMember actor, ClubMember target) {
        if (!actor.isOwner()) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_OWNER_ONLY);
        }
        if (!target.isActive()) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_MEMBER_IS_NOT_ACTIVE);
        }
        if (target.isOwner()) {
            return;
        }
        actor.updateStatus(ClubMemberStatus.STAFF);
        target.updateStatus(ClubMemberStatus.OWNER);
    }

    private void kickMember(ClubMember target, LocalDateTime now) {
        target.kick(now);
    }

    private void publishJoinClubEvent(String memberId, Club club, ClubMember clubMember) {
        JoinClubEvent joinClubEvent = new JoinClubEvent(clubMember.getId(), memberId, club.getId(), club.getName());
        eventPublisher.publishEvent(joinClubEvent);
    }
}
