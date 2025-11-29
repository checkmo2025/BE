package checkmo.clubManagement.internal.service.command;

import checkmo.clubManagement.ClubManagementEvent.JoinClubEvent;
import checkmo.clubManagement.internal.entity.Club;
import checkmo.clubManagement.internal.entity.ClubMember;
import checkmo.clubManagement.internal.excepetion.ClubManagementErrorStatus;
import checkmo.clubManagement.internal.excepetion.ClubManagementException;
import checkmo.clubManagement.internal.repository.ClubMemberRepository;
import checkmo.clubManagement.internal.service.query.ClubManagementQueryService;
import checkmo.clubManagement.internal.service.query.ClubMemberQueryService;
import checkmo.clubManagement.web.dto.ClubRequestDTO.JoinClub;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClubMemberCommandService {

    private final ClubManagementQueryService clubManagementQueryService;
    private final ClubMemberQueryService clubMemberQueryService;

    private final ClubMemberRepository clubMemberRepository;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ClubMember joinClub(Long clubId, String memberId, JoinClub request) {
        Club club = clubManagementQueryService.validateClub(clubId);

        // 2. 이미 신청 또는 가입되어 있는 경우
        clubMemberRepository.findByClubIdAndMemberId(club.getId(), memberId)
                .ifPresent(cm -> {
                    throw new ClubManagementException(ClubManagementErrorStatus.CLUB_MEMBER_ALREADY_EXISTS);
                });

        // 3. 클럽 오픈 여부에 따른 사용자 상태 설정
        ClubMember.ClubMemberStatus status = club.isOpen()
                ? ClubMember.ClubMemberStatus.MEMBER
                : ClubMember.ClubMemberStatus.PENDING;

        // 4. ClubMember 생성
        ClubMember clubMember = ClubMember.builder()
                .clubMemberStatus(status)
                .joinMessage(request.getJoinMessage())
                .memberId(memberId)
                .build();
        club.addClubMember(clubMember);
        clubMemberRepository.save(clubMember);

        // 공개 클럽이면 즉시 가입 완료 이벤트 발행
        if (club.isOpen()) {
            JoinClubEvent joinClubEvent = new JoinClubEvent(clubMember.getId(), memberId, club.getId(), club.getName());
            eventPublisher.publishEvent(joinClubEvent);
        }

        return clubMember;
    }

    @Transactional
    public ClubMember updateClubMemberStatus(Long clubId, String actorId, Long targetClubMemberId, String status) {
        Club club = clubManagementQueryService.validateClub(clubId);
        ClubMember actor = clubMemberQueryService.validateClubMember(clubId, actorId);
        if (!actor.isStaff()) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_STAFF_ONLY);
        }

        // 수정 대상 회원 존재 여부 확인
        ClubMember targetClubMember = clubMemberRepository.findByClubIdAndId(club.getId(), targetClubMemberId)
                .orElseThrow(() -> new ClubManagementException(ClubManagementErrorStatus.CLUB_MEMBER_NOT_FOUND));

        // 상태 문자열 → Enum 변환
        // TODO: 해당 변환은 DTO 레이어에서 처리하는 것이 더 적절할 수 있음
        ClubMember.ClubMemberStatus newStatus;
        try {
            newStatus = ClubMember.ClubMemberStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_MEMBER_INVALID_STATUS);
        }

        ClubMember.ClubMemberStatus oldStatus = targetClubMember.getClubMemberStatus();
        targetClubMember.updateStatus(newStatus);

        // PENDING → MEMBER로 변경되면 가입 완료 이벤트 발행
        if (oldStatus == ClubMember.ClubMemberStatus.PENDING && newStatus == ClubMember.ClubMemberStatus.MEMBER) {
            JoinClubEvent joinClubEvent = new JoinClubEvent(targetClubMember.getId(), targetClubMember.getMemberId(),
                    club.getId(), club.getName());
            eventPublisher.publishEvent(joinClubEvent);
        }

        return targetClubMember;
    }

    @Transactional
    public void leaveClub(Long clubId, String memberId) {
        clubManagementQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        // 2. 운영진(STAFF)은 탈퇴 불가
        if (clubMember.isStaff()) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_STAFF_CANNOT_LEAVE);
        }

        // 3. 탈퇴 처리
        // TODO: 탈퇴 시 BLOCKED 상태로 변경하는 것으로 알고 있는데... 확인해보아야 함
        // TODO: 추가적으로, 탈퇴 시 연관된 엔티티(BookReview, ClubMemberTeam, Topic)를 어떻게 처리할지 결정 필요(PM) -2025.11.14-
        clubMemberRepository.delete(clubMember);
    }

}
