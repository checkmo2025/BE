package checkmo.clubManagement.internal.service.command;

import checkmo.clubManagement.internal.converter.ClubManagementConverter;
import checkmo.clubManagement.internal.entity.Club;
import checkmo.clubManagement.internal.entity.ClubMember;
import checkmo.clubManagement.internal.repository.ClubRepository;
import checkmo.clubManagement.internal.service.query.ClubMemberQueryService;
import checkmo.clubManagement.internal.service.query.ClubQueryService;
import checkmo.clubManagement.web.dto.ClubRequestDTO.ClubDetail;
import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
import java.util.HashSet;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class ClubManagementCommandServiceImpl implements ClubManagementCommandService {

    // 자신의 QueryService
    private final ClubQueryService clubQueryService;
    private final ClubMemberQueryService clubMemberQueryService;

    // 자신의 Repository
    private final ClubRepository clubRepository;

    // Event Publisher
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public Long createClub(String memberId, ClubDetail request) {
        // 1. 클럽 이름 중복 검사
        if (clubQueryService.isDuplicateClubName(request.getName())) {
            throw new GeneralException(ErrorStatus.CLUB_DUPLICATED_NAME);
        }

        // 2. 클럽 엔티티 생성
        Club club = ClubManagementConverter.toClub(request);

        // 3. 카테고리 연관관계 설정
        club.updateInterestCategories(new HashSet<>(request.getCategory()));

        // 4. 운영진 멤버 생성
        ClubMember clubMember = ClubMember.builder()
                .memberId(memberId)
                .clubMemberStatus(ClubMember.ClubMemberStatus.STAFF)
                .build();

        // 5. 양방향 연관관계 설정
        club.addClubMember(clubMember);

        // 6. 클럽 저장
        clubRepository.save(club);

        // 7. 생성된 클럽의 ID 반환
        return club.getId();
    }

    @Override
    public Long updateClub(Long clubId, String memberId, ClubDetail request) {
        // 1. 유효성 검증(club, clubMember)
        Club club = clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        // 2. 운영진 여부 검증
        if (!clubMember.isStaff()) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }

        // 3. 클럽 이름 중복 검사 (단, 기존 이름과 다를 때만)
        if (!club.getName().equals(request.getName()) &&
                clubQueryService.isDuplicateClubName(request.getName())) {
            throw new GeneralException(ErrorStatus.CLUB_DUPLICATED_NAME);
        }

        // 4. 엔티티 필드 수정
        club.updateField(request.getName(),
                request.getDescription(),
                request.getProfileImageUrl(),
                request.getParticipantTypes(),
                request.getRegion(),
                request.getInsta(),
                request.getKakao());

        // 5. 카테고리 수정
        club.updateInterestCategories(new HashSet<>(request.getCategory()));

        return club.getId();
    }

    @Override
    // TODO: 클럽이 삭제될 때, 이벤트 발행
    public void deleteClub(Long clubId, String memberId) {
        // Club을 삭제함으로써 Cascade.REMOVE가 동작되어 ClubManagement 모듈 내 모든 엔티티(클럽 멤버, 책 추천, 클럽 카테고리) 제거
        // Meeting을 삭제함으로써 Cascade.REMOVE가 동작되어 ClubMeeting 모듈 내 모든 엔티티(토픽, 팀, 팀 토픽, 멤터 팀, 한줄평) 제거
        // Notice를 삭제함으로써 Cascade.REMOVE가 동작되어 ClubNotice 모듈 내 모든 엔티티(투표, 회원 투표) 제거 (단, 비즈니스 요구사항 변경에 따라 Notice와 Vote는 연관관계 수정되어야 함 -2025.11.12 기준-)
    }
}
