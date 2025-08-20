package checkmo.domain.club.service.command;

import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.club.converter.ClubConverter;
import checkmo.domain.club.entity.Club;
import checkmo.domain.club.entity.ClubMember;
import checkmo.domain.club.repository.ClubMemberRepository;
import checkmo.domain.club.repository.ClubRepository;
import checkmo.domain.club.service.query.ClubMemberQueryService;
import checkmo.domain.club.service.query.ClubQueryService;
import checkmo.domain.club.web.dto.club.ClubRequestDTO;
import checkmo.domain.member.entity.Member;
import checkmo.domain.member.facade.MemberQueryFacade;
import checkmo.event.JoinClubEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClubMembershipCommandServiceImpl implements ClubMembershipCommandService {

    // Domain level 2
    private final MemberQueryFacade memberQueryFacade;

    // 자신의 QueryService
    private final ClubMemberQueryService clubMemberQueryService;
    private final ClubQueryService clubQueryService;

    // 자신의 Repository
    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;

    // 이벤트 발행을 위한 ApplicationEventPublisher
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 독서모임에 가입 신청을 합니다.
     *
     * 피그마 참고 페이지 : #독서모임 - 모임 검색하기 - 특정 모임 가입 신청하기 클릭시
     *
     * @param clubId 독서모임 ID
     * @param memberId 회원 ID -> 로그인한 회원의 ID를 사용
     * @param request 가입 신청 메시지 DTO
     * @return 가입 신청 후의 독서모임 정보 DTO : 공개인 경우는 ID 사용해 프론트에서 리다이렉트.
     */
    @Override
    @Transactional
    public Club joinClub(Long clubId, String memberId, ClubRequestDTO.ClubMemberJoinDTO request) {

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.CLUB_NOT_FOUND));

        // 이미 신청 또는 가입되어 있는 경우
        clubMemberRepository.findByClubIdAndMemberId(clubId, memberId)
                .ifPresent(cm -> {
                    throw new GeneralException(ErrorStatus.CLUB_MEMBER_ALREADY_EXISTS);
                });

        // 가입 상태 설정
        ClubMember.ClubMemberStatus status = club.isOpen()
                ? ClubMember.ClubMemberStatus.MEMBER
                : ClubMember.ClubMemberStatus.PENDING;

        // 공개 클럽이면 즉시 가입 완료 이벤트 발행
        if (club.isOpen()) {
            JoinClubEvent joinClubEvent = new JoinClubEvent(memberId, clubId, club.getName());
            eventPublisher.publishEvent(joinClubEvent);
        }

        // ClubMember 생성 및 연관관계 설정
        Member proxyMember = memberQueryFacade.findMemberReferenceById(memberId);
        ClubMember clubMember = ClubConverter.toClubMemberEntity(club, proxyMember, status, request.getJoinMessage());
        club.addClubMember(clubMember);

        return club;
    }

    /**
     * ClubMembershipCommandService
     * 독서 모임 회원의 등급(상태/역할)을 수정합니다.
     *
     * @param clubId          독서 모임 ID
     * @param targetClubMemberId  수정 대상 회원 ID
     * @param currentMemberId 요청자(운영진) 회원 ID
     * @param status 수정할 등급 (MEMBER, STAFF, PENDING, BLOCKED 중 선택)
     * @return 수정된 회원의 응답 DTO
     */
    @Override
    @Transactional
    public ClubMember updateClubMemberStatus(Long clubId, Long targetClubMemberId, String currentMemberId, String status) {

        // 1. 클럽 유효성 검증
        Club club = clubQueryService.validateClub(clubId);
        ClubMember requester = clubMemberQueryService.validateClubMember(clubId, currentMemberId);
        if (!requester.isStaff()) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }

        // 2. 수정 대상 회원 존재 여부 확인
        ClubMember targetMember = clubMemberRepository.findByClubIdAndId(clubId, targetClubMemberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.CLUB_MEMBER_NOT_FOUND));

        // 3. 상태 문자열 → Enum 변환
        ClubMember.ClubMemberStatus newStatus;
        try {
            newStatus = ClubMember.ClubMemberStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new GeneralException(ErrorStatus.CLUB_MEMBER_INVALID_STATUS);
        }

        // 4. 상태 변경 및 이벤트 발행
        ClubMember.ClubMemberStatus oldStatus = targetMember.getClubMemberStatus();
        targetMember.updateStatus(newStatus);

        // PENDING → MEMBER로 변경되면 가입 완료 이벤트 발행
        if (oldStatus == ClubMember.ClubMemberStatus.PENDING && newStatus == ClubMember.ClubMemberStatus.MEMBER) {
            JoinClubEvent joinClubEvent = new JoinClubEvent(targetMember.getMemberId(), clubId, club.getName());
            eventPublisher.publishEvent(joinClubEvent);
        }

        // 5. 엔티티 반환
        return targetMember;
    }

    /**
     * 독서 모임에서 탈퇴합니다.
     *
     * @param clubId   독서 모임 ID
     * @param memberId 탈퇴할 회원 ID (본인)
     */
    @Override
    @Transactional
    public void leaveClub(Long clubId, String memberId) {

        // 1. 클럽 유효성 검증
        clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        // 2. 운영진(STAFF)은 탈퇴 불가
        if (clubMember.isStaff()) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_CANNOT_LEAVE);
        }

        // 3. 탈퇴 처리
        clubMemberRepository.delete(clubMember);
    }

}