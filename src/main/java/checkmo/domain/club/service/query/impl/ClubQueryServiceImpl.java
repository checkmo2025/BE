package checkmo.domain.club.service.query.impl;

import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.club.converter.ClubConverter;
import checkmo.domain.club.entity.Club;
import checkmo.domain.club.entity.ClubMember;
import checkmo.domain.club.repository.ClubMemberRepository;
import checkmo.domain.club.repository.ClubRepository;
import checkmo.domain.club.service.query.ClubQueryService;
import checkmo.domain.club.web.dto.club.ClubResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClubQueryServiceImpl implements ClubQueryService {

    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;

    @Override
    public ClubResponseDTO.ClubListDTO getClubList(String keyword, int region, int participants, Long cursorId) {
        return null;
    }

    @Override
    public ClubResponseDTO.MyClubListDTO getMyClubList(String memberId) {
        return null;
    }

    @Override
    public ClubResponseDTO.MyClubListDTO getMyClubList(String memberId, int size) {
        return null;
    }

    @Override
    public ClubResponseDTO.ClubMemberListDTO getClubMemberListByStatus(Long clubId, String memberId, String clubMemberStatus, Long cursorId) {
        return null;
    }

    @Override
    public ClubResponseDTO.ClubDetailDTO getClubInfo(Long clubId, String memberId) {
        // 1. 클럽 존재 여부 확인
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.CLUB_NOT_FOUND));

        // 2. 운영진 권한 체크
        boolean isStaff = clubMemberRepository.existsByClubIdAndMemberIdAndClubMemberStatus(
                clubId,
                memberId,
                ClubMember.ClubMemberStatus.STAFF
        );

        if (!isStaff) {
            throw new GeneralException(ErrorStatus.CLUB_MEMBER_FORBIDDEN);
        }

        return ClubConverter.toClubDetailDTO(club);
    }

    @Override
    public boolean isDuplicateClubName(String clubName) {
        return false;
    }

    @Override
    public ClubResponseDTO.ClubNoticeListDTO getLatestNotices(Long clubId, String memberId, int size) {
        return null;
    }

    @Override
    public ClubResponseDTO.ClubNoticeListDTO getLatestNotices(Long clubId, String memberId, Long cursorId) {
        return null;
    }

    @Override
    public ClubResponseDTO.ClubNoticeDetailDTO getNoticeDetail(Long clubId, Long noticeId) {
        return null;
    }
}
