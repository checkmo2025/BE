package checkmo.domain.club.service.query;

import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.club.converter.ClubConverter;
import checkmo.domain.club.entity.Club;
import checkmo.domain.club.entity.ClubCategory;
import checkmo.domain.club.entity.ClubMember;
import checkmo.domain.club.repository.ClubMemberRepository;
import checkmo.domain.club.web.dto.club.ClubResponseDTO;
import checkmo.domain.member.facade.MemberQueryFacade;
import checkmo.global.dto.CategorySharedDTO;
import checkmo.global.dto.ClubSharedDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubMemberQueryServiceImpl implements ClubMemberQueryService {

    // Domain level 2
    private final MemberQueryFacade memberQueryFacade;

    // 자신의 QueryService
    private final ClubCategoryQueryService clubCategoryQueryService;


    // 자신의 Repository
    private final ClubMemberRepository clubMemberRepository;

    @Override
    public ClubMember validateClubMember(Long clubId, String memberId) throws GeneralException {
        return clubMemberRepository.findByClubIdAndMemberId(clubId, memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.CLUB_MEMBER_ONLY));
    }

    @Override
    public ClubSharedDTO.MyClubList getMyClubList(String memberId) {

        // 회원ID를 통해 JPQL로 클럽 ID와 이름을 조회하고 DTO로 변환
        var clubIdAndNameByMemberId = clubMemberRepository.findClubIdAndNameByMemberId(memberId);

        // Object[] -> ClubSharedDTO.MyClubInfo 변환
        var myClubInfoList = clubIdAndNameByMemberId.stream()
                .map(row -> new ClubSharedDTO.MyClubInfo((Long) row[0], (String) row[1]))
                .toList();

        return ClubConverter.fromClubInfoListToMyClubList(myClubInfoList);
    }

    /**
     * 특정 회원이 가입한 모임 목록을 조회합니다. (내부용)
     *
     * 피그마 참고 페이지 : #마이페이지
     *
     * @param memberId 회원 ID -> 로그인한 회원의 ID를 사용
     * @return 내가 가입한 독서 클럽 목록 DTO
     */
    @Override
    public ClubResponseDTO.MyPageClubListDTO getMyPageClubList(String memberId) {

        // 1. ClubMember와 Club 엔티티 fetch join으로 조회
        List<ClubMember> clubMembers = clubMemberRepository.findByMemberIdWithClub(memberId);

        // 2. 모든 클럽 ID 수집
        List<Long> clubIds = clubMembers.stream()
                .map(cm -> cm.getClub().getId())
                .toList();

        // 3. 모든 클럽의 카테고리를 한 번에 조회
        List<ClubCategory> allClubCategories = clubCategoryQueryService.findCategoriesByClubIds(clubIds);

        var clubCategoriesMap = ClubConverter.fromClubCategoriesToCategoryInfoListMap(allClubCategories);

        // 4. DTO 변환
        List<ClubResponseDTO.ClubDetailResponseDTO> responseList = clubMembers.stream()
                .map(cm -> {
                    Club c = cm.getClub();
                    List<CategorySharedDTO.CategoryInfo> categories = clubCategoriesMap.getOrDefault(c.getId(), Collections.emptyList());
                    return ClubConverter.fromClubToResponseDTO(c, categories, cm.isStaff());
                })
                .toList();

        // 5. MyPageClubListDTO로 감싸서 반환
        return ClubResponseDTO.MyPageClubListDTO.builder()
                .clubList(responseList)
                .build();
    }

    /**
     * 특정 회원이 해당 클럽에서 어떤 상태(등급)인지 조회합니다.
     *
     * @param memberId 회원 ID
     * @param clubId   클럽 ID
     * @return ClubMemberStatus (MEMBER, STAFF 등) 또는 null (회원 아님)
     */
    @Override
    public ClubMember.ClubMemberStatus getMemberStatusInClub(String memberId, Long clubId) {
        return clubMemberRepository.findByClubIdAndMemberId(clubId, memberId)
                .map(ClubMember::getClubMemberStatus)
                .orElse(null); // 존재하지 않으면 null 반환
    }

    /**
     * 특정 회원이 여러 클럽에서의 상태를 한꺼번에 조회합니다.
     *
     * @param memberId 회원 ID
     * @param clubIds 클럽 ID 리스트
     * @return 클럽 ID별 회원 상태 맵
     */
    @Override
    public Map<Long, ClubMember.ClubMemberStatus> getMemberStatuses(String memberId, List<Long> clubIds) {

        // clubMemberRepository에서 clubId IN :clubIds AND memberId = :memberId 조건으로 여러 상태를 한 번에 조회
        List<ClubMember> members = clubMemberRepository.findAllByMemberIdAndClubIdIn(memberId, clubIds);

        // Map<clubId, ClubMemberStatus> 형태로 변환 후 반환
        return members.stream()
                .collect(Collectors.toMap(ClubMember::getClubId, ClubMember::getClubMemberStatus));
    }

    /**
     * 특정 상태의 모임 회원 목록을 조회합니다.
     *
     * @param clubId 모임 ID
     * @param status 조회할 상태 ("MEMBER", "STAFF", "PENDING", "BLOCKED", "ALL", *"ACTIVE"* 중 하나)
     * @param cursorId 페이징 커서 ID (null이면 처음부터 조회)
     * @param size 조회할 개수 (null이면 전체 조회)
     * @return ClubMember 엔티티 리스트
     */
    @Override
    public List<ClubMember> getClubMemberListByStatus(Long clubId, String status, Long cursorId, Integer size) {
        List<ClubMember.ClubMemberStatus> clubMemberStatus;
        if ("ALL".equalsIgnoreCase(status)) {
            clubMemberStatus = null; // ALL
        } else if ("ACTIVE".equalsIgnoreCase(status)) { // ACTIVE는 내부적으로 사용할 예정
            clubMemberStatus = List.of(ClubMember.ClubMemberStatus.MEMBER, ClubMember.ClubMemberStatus.STAFF);
        } else {
            try {
                clubMemberStatus = List.of(ClubMember.ClubMemberStatus.valueOf(status.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new GeneralException(ErrorStatus.CLUB_MEMBER_INVALID_STATUS);
            }
        }

        return clubMemberRepository.findClubMembersByClubIdInClubMemberStatusOrderByIdDesc(
                clubId,
                clubMemberStatus,
                cursorId,
                size
        );
    }

    @Override
    public Map<String, ClubMember> getNicknameToClubMember(Long clubId, List<String> nicknames) throws GeneralException {
        if (nicknames == null || nicknames.isEmpty()) {
            return Map.of();
        }

        Map<String, String> nicknameToMemberId = memberQueryFacade.getMemberIdsByNicknames(nicknames);
        Map<String, ClubMember> memberIdToClubMember = getMemberIdToClubMember(clubId, nicknameToMemberId.values().stream().toList());
        Map<String, ClubMember> nicknameToClubMember = new HashMap<>(nicknameToMemberId.size());
        List<String> missing = new ArrayList<>();
        for (String nickname : nicknames) {
            String memberId = nicknameToMemberId.get(nickname);
            ClubMember clubMember = (memberId == null) ? null : memberIdToClubMember.get(memberId);
            if (clubMember == null) missing.add(nickname);
            else nicknameToClubMember.put(nickname, clubMember);
        }
        if (!missing.isEmpty()) {
            throw new GeneralException(ErrorStatus.CLUB_MEMBER_NOT_FOUND, missing.toString()); // TODO: 잘못에러 메시지 확인
        }
        return nicknameToClubMember;
    }

    private Map<String, ClubMember> getMemberIdToClubMember(Long clubId, List<String> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return Map.of();
        }
        List<ClubMember> results = clubMemberRepository.findClubMembersByClubIdAndMemberIdIn(clubId, memberIds);
        return results.stream()
                .collect(Collectors.toMap(
                        ClubMember::getMemberId,
                        cm -> cm)
                );
    }
}
