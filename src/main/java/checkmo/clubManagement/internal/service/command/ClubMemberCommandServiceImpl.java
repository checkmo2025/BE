package checkmo.clubManagement.internal.service.command;

import checkmo.clubManagement.ClubManagementEvent.JoinClubEvent;
import checkmo.clubManagement.internal.entity.Club;
import checkmo.clubManagement.internal.entity.ClubMember;
import checkmo.clubManagement.internal.repository.ClubMemberRepository;
import checkmo.clubManagement.internal.service.query.ClubMemberQueryService;
import checkmo.clubManagement.internal.service.query.ClubQueryService;
import checkmo.clubManagement.web.dto.ClubRequestDTO.JoinClub;
import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClubMemberCommandServiceImpl implements ClubMemberCommandService {

    // 자신의 Service
    private final ClubQueryService clubQueryService;
    private final ClubMemberQueryService clubMemberQueryService;

    // 자신의 Repository
    private final ClubMemberRepository clubMemberRepository;

    // 이벤트 발행을 위한 ApplicationEventPublisher
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ClubMember joinClub(Long clubId, String memberId, JoinClub request) {
        // 1. 유효성 검증(club)
        Club club = clubQueryService.validateClub(clubId);

        // 2. 이미 신청 또는 가입되어 있는 경우
        clubMemberRepository.findByClubIdAndMemberId(club.getId(), memberId)
                .ifPresent(cm -> {
                    throw new GeneralException(ErrorStatus.CLUB_MEMBER_ALREADY_EXISTS);
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

        // 5. 양방향 연관관계 설정 및 저장
        club.addClubMember(clubMember);
        clubMemberRepository.save(clubMember);

        // 6. 공개 클럽이면 즉시 가입 완료 이벤트 발행
        if (club.isOpen()) {
            JoinClubEvent joinClubEvent = new JoinClubEvent(clubMember.getId(), memberId, club.getId(), club.getName());
            eventPublisher.publishEvent(joinClubEvent);
        }

        return clubMember;
    }

    @Override
    @Transactional
    public ClubMember updateClubMemberStatus(Long clubId, String actorId, Long targetClubMemberId, String status) {
        // 1. 유효성 검증(club, clubMember)
        Club club = clubQueryService.validateClub(clubId);
        ClubMember actor = clubMemberQueryService.validateClubMember(clubId, actorId);

        // 2. 요청자 운영진 여부 확인
        if (!actor.isStaff()) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }

        // 3. 수정 대상 회원 존재 여부 확인
        ClubMember targetClubMember = clubMemberRepository.findByClubIdAndId(club.getId(), targetClubMemberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.CLUB_MEMBER_NOT_FOUND));

        // 4. 상태 문자열 → Enum 변환
        // TODO: 해당 변환은 DTO 레이어에서 처리하는 것이 더 적절할 수 있음
        ClubMember.ClubMemberStatus newStatus;
        try {
            newStatus = ClubMember.ClubMemberStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new GeneralException(ErrorStatus.CLUB_MEMBER_INVALID_STATUS);
        }

        // 5. 상태 변경
        ClubMember.ClubMemberStatus oldStatus = targetClubMember.getClubMemberStatus();
        targetClubMember.updateStatus(newStatus);

        // 6. PENDING → MEMBER로 변경되면 가입 완료 이벤트 발행
        if (oldStatus == ClubMember.ClubMemberStatus.PENDING && newStatus == ClubMember.ClubMemberStatus.MEMBER) {
            JoinClubEvent joinClubEvent = new JoinClubEvent(targetClubMember.getId(), targetClubMember.getMemberId(),
                    club.getId(), club.getName());
            eventPublisher.publishEvent(joinClubEvent);
        }

        return targetClubMember;
    }

    @Override
    @Transactional
    public void leaveClub(Long clubId, String memberId) {
        // 1. 유효성 검증(club, clubMember)
        Club club = clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        // 2. 운영진(STAFF)은 탈퇴 불가
        if (clubMember.isStaff()) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_CANNOT_LEAVE);
        }

        // 3. 탈퇴 처리
        // TODO: 탈퇴 시 BLOCKED 상태로 변경하는 것으로 알고 있는데... 확인해보아야 함
        // TODO: 추가적으로, 탈퇴 시 연관된 엔티티(BookReview, ClubMemberTeam, Topic)를 어떻게 처리할지 결정 필요(PM) -2025.11.14-
        clubMemberRepository.delete(clubMember);
    }

}