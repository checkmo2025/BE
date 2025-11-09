package checkmo.clubManagement.internal.service.command;

import checkmo.clubManagement.internal.converter.ClubManagementConverter;
import checkmo.clubManagement.internal.entity.Club;
import checkmo.clubManagement.internal.entity.ClubMember;
import checkmo.clubManagement.internal.repository.ClubMemberRepository;
import checkmo.clubManagement.web.dto.ClubRequestDTO.ClubMemberJoinDTO;
import checkmo.clubMeeting.JoinClubEvent;
import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
import checkmo.member.internal.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClubMemberCommandServiceImpl implements ClubMemberCommandService {

    // 자신의 Repository
    private final ClubMemberRepository clubMemberRepository;

    // 이벤트 발행을 위한 ApplicationEventPublisher
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ClubMember joinClub(Club club, Member proxyMember, ClubMemberJoinDTO request) {

        // 1. 이미 신청 또는 가입되어 있는 경우
        clubMemberRepository.findByClubIdAndMemberId(club.getId(), proxyMember.getId())
                .ifPresent(cm -> {
                    throw new GeneralException(ErrorStatus.CLUB_MEMBER_ALREADY_EXISTS);
                });

        // 2. 클럽 오픈 여부에 따른 사용자 상태 설정
        ClubMember.ClubMemberStatus status = club.isOpen()
                ? ClubMember.ClubMemberStatus.MEMBER
                : ClubMember.ClubMemberStatus.PENDING;

        // 3. 공개 클럽이면 즉시 가입 완료 이벤트 발행
        if (club.isOpen()) {
            JoinClubEvent joinClubEvent = new JoinClubEvent(proxyMember.getId(), club.getId(), club.getName());
            eventPublisher.publishEvent(joinClubEvent);
        }

        // 4. ClubMember 생성 및 연관관계 설정
        ClubMember clubMember = ClubManagementConverter.toClubMemberEntity(club, proxyMember, status,
                request.getJoinMessage());
        club.addClubMember(clubMember);

        return clubMember;
    }

    @Override
    @Transactional
    public ClubMember updateClubMemberStatus(Club club, ClubMember actor, Long targetClubMemberId, String status) {

        // 1. 운영진 여부 확인
        if (!actor.isStaff()) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }

        // 2. 수정 대상 회원 존재 여부 확인
        ClubMember targetClubMember = clubMemberRepository.findByClubIdAndId(club.getId(), targetClubMemberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.CLUB_MEMBER_NOT_FOUND));

        // 3. 상태 문자열 → Enum 변환
        // TODO: 해당 변환은 DTO 레이어에서 처리하는 것이 더 적절할 수 있음
        ClubMember.ClubMemberStatus newStatus;
        try {
            newStatus = ClubMember.ClubMemberStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new GeneralException(ErrorStatus.CLUB_MEMBER_INVALID_STATUS);
        }

        // 4. 상태 변경
        ClubMember.ClubMemberStatus oldStatus = targetClubMember.getClubMemberStatus();
        targetClubMember.updateStatus(newStatus);

        // 5. PENDING → MEMBER로 변경되면 가입 완료 이벤트 발행
        if (oldStatus == ClubMember.ClubMemberStatus.PENDING && newStatus == ClubMember.ClubMemberStatus.MEMBER) {
            JoinClubEvent joinClubEvent = new JoinClubEvent(targetClubMember.getMemberId(), club.getId(),
                    club.getName());
            eventPublisher.publishEvent(joinClubEvent);
        }

        return targetClubMember;
    }

    @Override
    @Transactional
    public void leaveClub(Club club, ClubMember clubMember) {
        // 1. 운영진(STAFF)은 탈퇴 불가
        if (clubMember.isStaff()) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_CANNOT_LEAVE);
        }

        // 2. 탈퇴 처리
        clubMemberRepository.delete(clubMember);
    }

}