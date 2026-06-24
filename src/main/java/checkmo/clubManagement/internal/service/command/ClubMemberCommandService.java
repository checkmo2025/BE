package checkmo.clubManagement.internal.service.command;

import checkmo.clubManagement.ClubManagementEvent.JoinClubEvent;
import checkmo.clubManagement.internal.entity.Club;
import checkmo.clubManagement.internal.entity.ClubMember;
import checkmo.clubManagement.internal.repository.ClubMemberRepository;
import checkmo.clubManagement.internal.service.query.ClubManagementQueryService;
import checkmo.clubManagement.internal.service.query.ClubMemberQueryService;
import checkmo.clubManagement.web.dto.ClubRequestDTO.ClubMemberStatusAction;
import checkmo.clubManagement.web.dto.ClubRequestDTO.JoinClub;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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
                            club.reApplyMember(existing, request.getJoinMessage(), now);
                            if (club.isOpen() && existing.isActive()) {
                                publishJoinClubEvent(memberId, club, existing);
                            }
                        },
                        () -> {
                            ClubMember created = club.applyForMembership(memberId, request.getJoinMessage(), now);
                            clubMemberRepository.save(created);
                            if (club.isOpen() && created.isActive()) {
                                publishJoinClubEvent(memberId, club, created);
                            }
                        });
    }

    public void updateClubMemberStatus(Long clubId, String actorId, Long targetId, ClubMemberStatusAction request) {
        Club club = clubManagementQueryService.validateClub(clubId);
        ClubMember actor = clubMemberQueryService.validateClubMember(clubId, actorId);
        ClubMember target = clubMemberQueryService.validateClubMember(clubId, targetId);
        LocalDateTime now = LocalDateTime.now();

        switch (request.getCommand()) {
            case APPROVE -> approveJoin(club, actor, target, now);
            case REJECT -> rejectJoin(club, actor, target);
            case CHANGE_ROLE -> target.changeRoleBy(actor, request.getStatus());
            case TRANSFER_OWNER -> transferOwner(club, actor, target);
            case KICK -> kickMember(club, actor, target, now);
        }
    }

    public void leaveClub(Long clubId, String memberId) {
        clubManagementQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);
        clubMember.leave(LocalDateTime.now());
    }

    private void approveJoin(Club club, ClubMember actor, ClubMember target, LocalDateTime now) {
        target.approveJoinBy(actor, now);
        publishJoinClubEvent(target.getMemberId(), club, target);
    }

    private void rejectJoin(Club club, ClubMember actor, ClubMember target) {
        club.validateJoinRejection(actor, target);
        clubMemberRepository.delete(target);
    }

    private void transferOwner(Club club, ClubMember actor, ClubMember target) {
        if (club.transferOwnerBy(actor, target)) {
            log.info("{} Club 개설자 권한 이전: originalOwnerId={}, newOwnerId={}",
                    club.getName(), actor.getId(), target.getId());
        }
    }

    private void kickMember(Club club, ClubMember actor, ClubMember target, LocalDateTime now) {
        target.kickBy(actor, now);
        log.info("{} Club 멤버 강제 탈퇴: actorId={}, targetId={}", club.getName(), actor.getId(), target.getId());
    }

    private void publishJoinClubEvent(String memberId, Club club, ClubMember clubMember) {
        JoinClubEvent joinClubEvent = new JoinClubEvent(clubMember.getId(), memberId, club.getId(), club.getName());
        eventPublisher.publishEvent(joinClubEvent);
    }
}
