package checkmo.clubManagement.internal.service.command;

import checkmo.clubManagement.ClubManagementEvent.DeleteClubImageEvent;
import checkmo.clubManagement.ClubManagementEvent.DeletedClubEvent;
import checkmo.clubManagement.internal.converter.ClubManagementConverter;
import checkmo.clubManagement.internal.entity.Club;
import checkmo.clubManagement.internal.entity.ClubMember;
import checkmo.clubManagement.internal.excepetion.ClubManagementErrorStatus;
import checkmo.clubManagement.internal.excepetion.ClubManagementException;
import checkmo.clubManagement.internal.repository.ClubMemberRepository;
import checkmo.clubManagement.internal.repository.ClubRepository;
import checkmo.clubManagement.internal.service.query.ClubManagementQueryService;
import checkmo.clubManagement.internal.service.query.ClubMemberQueryService;
import checkmo.clubManagement.web.dto.ClubRequestDTO.ClubDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ClubManagementCommandService {

    private final ClubManagementQueryService clubManagementQueryService;
    private final ClubMemberQueryService clubMemberQueryService;

    private final ClubRepository clubRepository;

    private final ClubMemberRepository clubMemberRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    public void createClub(String memberId, ClubDetail request) {
        if (clubManagementQueryService.isDuplicateClubName(request.getName().trim())) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_DUPLICATED_NAME);
        }

        LocalDateTime now = LocalDateTime.now();
        Club club = ClubManagementConverter.toClub(request);
        club.initializeForCreation(new HashSet<>(request.getCategory()), now);

        clubRepository.save(club);
        ClubMember owner = club.createOwnerMember(memberId, now);
        clubMemberRepository.save(owner);
    }

    public void updateClub(Long clubId, String memberId, ClubDetail request) {
        Club club = clubManagementQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);
        if (!clubMember.isStaff()) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_STAFF_ONLY);
        }

        validateClubName(club, request);

        applyClubUpdate(club, request);
    }

    public void updateLastActivityTime(Long clubId, LocalDateTime lastActivityTime) {
        clubRepository.updateLastActivityTime(clubId, lastActivityTime);
    }

    private void validateClubName(Club club, ClubDetail request) throws ClubManagementException {
        if (club.isDifferent(request.getName())
                && clubManagementQueryService.isDuplicateClubName(request.getName().trim())) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_DUPLICATED_NAME);
        }
    }

    public void deleteClub(Long clubId, String memberId) {
        Club club = clubManagementQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);
        if (!clubMember.isOwner()) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_OWNER_ONLY);
        }

        String profileImgUrl = club.getProfileImgUrl();
        if (profileImgUrl != null) {
            publishDeletedClubImageEvent(profileImgUrl);
        }

        log.info("클럽 삭제 시작: clubId={}, memberId={}, deletedAt={}", clubId, memberId, LocalDateTime.now());
        publishDeletedClubEvent(club.getId());
        clubMemberRepository.deleteByClubId(clubId);
        clubRepository.delete(club);
    }

    private void publishDeletedClubEvent(Long clubId) {
        applicationEventPublisher.publishEvent(
                DeletedClubEvent.builder()
                        .clubId(clubId)
                        .build()
        );
    }

    private void publishDeletedClubImageEvent(String oldImageUrl) {
        applicationEventPublisher.publishEvent(
                DeleteClubImageEvent.builder()
                        .imageUrl(oldImageUrl)
                        .build()
        );
    }

    public void updateClubByAdmin(Long clubId, ClubDetail request) {
        Club club = clubManagementQueryService.validateClub(clubId);

        validateClubName(club, request);

        applyClubUpdate(club, request);
    }

    private void applyClubUpdate(Club club, ClubDetail request) {
        String oldImageUrl = club.getProfileImgUrl();
        if (oldImageUrl != null && !oldImageUrl.equals(request.getProfileImageUrl())) {
            publishDeletedClubImageEvent(oldImageUrl);
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
}
