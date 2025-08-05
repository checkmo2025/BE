package checkmo.domain.club.service.command;

import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.club.converter.ClubConverter;
import checkmo.domain.club.entity.Club;
import checkmo.domain.club.entity.ClubMember;
import checkmo.domain.club.repository.ClubMemberRepository;
import checkmo.domain.club.repository.ClubRepository;
import checkmo.domain.club.web.dto.club.ClubRequestDTO;
import checkmo.domain.club.web.dto.club.ClubResponseDTO.ClubInfoDTO;
import checkmo.domain.member.entity.Member;
import checkmo.domain.member.facade.MemberQueryFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClubMembershipCommandServiceImpl implements ClubMembershipCommandService {

    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;

    private final MemberQueryFacade memberQueryFacade;

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
    public ClubInfoDTO joinClub(Long clubId, String memberId, ClubRequestDTO.ClubMemberJoinDTO request) {

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.CLUB_NOT_FOUND));

        // 이미 신청 또는 가입되어 있는 경우
        clubMemberRepository.findByClubIdAndMemberId(clubId, memberId)
                .ifPresent(cm -> {
                    throw new GeneralException(ErrorStatus.CLUB_MEMBER_ALREADY_EXISTS);
                });

        // 프록시로 멤버 참조
        Member proxyMember = memberQueryFacade.findMemberReferenceById(memberId);

        // 가입 상태 설정
        ClubMember.ClubMemberStatus status = club.isOpen()
                ? ClubMember.ClubMemberStatus.MEMBER
                : ClubMember.ClubMemberStatus.PENDING;

        // ClubMember 엔티티 생성 및 저장
        ClubMember clubMember = ClubConverter.toClubMemberEntity(club, proxyMember, status, request.getJoinMessage());
        clubMemberRepository.save(clubMember);

        return new ClubInfoDTO(clubId, null, club.isOpen());
    }

    @Override
    @Transactional
    public void approveJoinRequest(Long clubId, String operatorId, Long clubMemberId) {
    }

}