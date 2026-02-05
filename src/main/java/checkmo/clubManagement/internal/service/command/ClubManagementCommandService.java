package checkmo.clubManagement.internal.service.command;

import checkmo.clubManagement.ClubManagementEvent;
import checkmo.clubManagement.internal.converter.ClubManagementConverter;
import checkmo.clubManagement.internal.entity.Club;
import checkmo.clubManagement.internal.entity.ClubMember;
import checkmo.clubManagement.internal.excepetion.ClubManagementErrorStatus;
import checkmo.clubManagement.internal.excepetion.ClubManagementException;
import checkmo.clubManagement.internal.repository.ClubRepository;
import checkmo.clubManagement.internal.service.query.ClubManagementQueryService;
import checkmo.clubManagement.internal.service.query.ClubMemberQueryService;
import checkmo.clubManagement.web.dto.ClubRequestDTO.ClubDetail;
import java.time.LocalDateTime;
import java.util.HashSet;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ClubManagementCommandService {

    private final ClubManagementQueryService clubManagementQueryService;
    private final ClubMemberQueryService clubMemberQueryService;

    private final ClubRepository clubRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    public void createClub(String memberId, ClubDetail request) {
        if (clubManagementQueryService.isDuplicateClubName(request.getName().trim())) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_DUPLICATED_NAME);
        }

        Club club = ClubManagementConverter.toClub(request);
        club.initializeLastActivityAt(LocalDateTime.now());
        club.updateInterestCategories(new HashSet<>(request.getCategory()));
        club.addOwner(memberId, LocalDateTime.now());

        clubRepository.save(club);
    }

    public void updateClub(Long clubId, String memberId, ClubDetail request) {
        Club club = clubManagementQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);
        if (!clubMember.isStaff()) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_STAFF_ONLY);
        }

        validateClubName(request, club);

        String oldImageUrl = club.getProfileImgUrl();
        if (oldImageUrl != null && !oldImageUrl.equals(request.getProfileImageUrl())) {
            applicationEventPublisher.publishEvent(
                    ClubManagementEvent.DeleteClubImage.builder()
                            .imageUrl(oldImageUrl)
                            .build()
            );
        }

        club.updateField(
                request.getName(),
                request.getDescription(),
                request.getProfileImageUrl(),
                request.isOpen(),
                request.getRegion(),
                request.getParticipantTypes(),
                ClubManagementConverter.toClubContacts(request.getLinks())
        );
        club.updateInterestCategories(new HashSet<>(request.getCategory()));
    }

    public void updateLastActivityTime(Long clubId, LocalDateTime lastActivityTime) {
        clubRepository.updateLastActivityTime(clubId, lastActivityTime);
    }

    private void validateClubName(ClubDetail request, Club club) throws ClubManagementException {
        if (club.isDifferent(request.getName())
                && clubManagementQueryService.isDuplicateClubName(request.getName().trim())) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_DUPLICATED_NAME);
        }
    }

    // TODO: 클럽이 삭제될 때, 이벤트 발행
    public void deleteClub(Long clubId, String memberId) {
        // Club을 삭제함으로써 Cascade.REMOVE가 동작되어 ClubManagement 모듈 내 모든 엔티티(클럽 멤버, 책 추천, 클럽 카테고리) 제거
        // Meeting을 삭제함으로써 Cascade.REMOVE가 동작되어 ClubMeeting 모듈 내 모든 엔티티(토픽, 팀, 팀 토픽, 멤터 팀, 한줄평) 제거
        // Notice를 삭제함으로써 Cascade.REMOVE가 동작되어 ClubNotice 모듈 내 모든 엔티티(투표, 회원 투표) 제거 (단, 비즈니스 요구사항 변경에 따라 Notice와 Vote는 연관관계 수정되어야 함 -2025.11.12 기준-)
    }
}