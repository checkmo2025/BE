package checkmo.domain.member.facade;

import checkmo.domain.member.converter.MemberConverter;
import checkmo.domain.member.entity.Follow;
import checkmo.domain.member.entity.Member;
import checkmo.domain.member.entity.MemberCategory;
import checkmo.domain.member.repository.MemberRepository;
import checkmo.domain.member.service.query.MemberCategoryQueryService;
import checkmo.domain.member.service.query.MemberFollowQueryService;
import checkmo.domain.member.service.query.MemberQueryService;
import checkmo.domain.member.web.dto.MemberResponseDTO;
import checkmo.global.dto.CategorySharedDTO;
import checkmo.global.dto.MemberSharedDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryFacadeImpl implements MemberQueryFacade {

    // 페이징 기본 크기 상수
    public static final int DEFAULT_PAGE_SIZE = 20;

    // 자신의 QueryService
    private final MemberQueryService memberQueryService;
    private final MemberFollowQueryService memberFollowQueryService;
    private final MemberCategoryQueryService memberCategoryQueryService;

    // 자신의 Repository (프록시용, TODO: 해결 불가한가?)
    private final MemberRepository memberRepository;

    @Override
    public boolean isNicknameDuplicated(String nickname) {
        return memberQueryService.isNicknameDuplicated(nickname);
    }

    @Override
    public MemberResponseDTO.MemberProfileResponseDTO getMemberBasicInfo(String memberId) {
        Member member = memberQueryService.getMemberBasicInfo(memberId);
        return MemberConverter.toMemberProfileResponseDTO(member);
    }

    @Override
    public MemberResponseDTO.MemberProfileWithCategoryResponseDTO getMemberProfile(String memberId) {
        Member member = memberQueryService.getMemberProfile(memberId);

        List<MemberCategory> memberCategories = memberCategoryQueryService.findCategoriesByMember(memberId);
        List<CategorySharedDTO.CategoryInfo> categories = MemberConverter.fromMemberCategoriesToCategoryInfoList(memberCategories);

        return MemberConverter.toMemberProfileWithCategoryResponseDTO(member, categories);
    }

    @Override
    public MemberResponseDTO.otherProfileResponseDTO getOtherProfile(String targetMemberNickname, String memberId) {
        Member targetMember = memberQueryService.getOtherProfile(targetMemberNickname, memberId);
        boolean isFollowing = memberFollowQueryService.isFollowing(memberId, targetMember.getId());

        List<MemberCategory> targetMemberCategories = memberCategoryQueryService.findCategoriesByMember(targetMember.getId());
        List<CategorySharedDTO.CategoryInfo> categories = MemberConverter.fromMemberCategoriesToCategoryInfoList(targetMemberCategories);

        return MemberConverter.toOtherProfileResponseDTO(targetMember, isFollowing, categories);
    }

    @Override
    public MemberResponseDTO.FollowList getFollowerList(String memberId, Long cursorId) {
        // 1. 팔로워 목록 조회
        List<Follow> followerList = memberFollowQueryService.getFollowerList(memberId, cursorId, DEFAULT_PAGE_SIZE + 1);

        // 2. 커서 기반 페이징 처리
        boolean hasNext = followerList.size() > DEFAULT_PAGE_SIZE;
        Long nextCursor = null;
        if (hasNext) {
            followerList.removeLast();
            nextCursor = followerList.getLast().getId();
        }

        // 3. 팔로워 목록의 닉네임, 프로필 이미지 배치 조회
        List<String> followerIdList = followerList.stream()
                .map(Follow::getFollowerId)
                .distinct()
                .toList();

        var followerMap = memberQueryService.getMemberNicknamesAndProfileImagesByMemberIds(memberId, followerIdList);

        List<MemberSharedDTO.WithFollowStatusDTO> followerDTOList = new ArrayList<>(followerMap.values());

        // 4. DTO 변환
        return MemberConverter.toFollowList(followerDTOList, hasNext, nextCursor);
    }

    @Override
    public MemberResponseDTO.FollowList getFollowingList(String memberId, Long cursorId) {
        // 1. 팔로잉 목록 조회
        List<Follow> followingList = memberFollowQueryService.getFollowingList(memberId, cursorId, DEFAULT_PAGE_SIZE + 1);

        // 2. 커서 기반 페이징 처리
        boolean hasNext = followingList.size() > DEFAULT_PAGE_SIZE;
        Long nextCursor = null;
        if (hasNext) {
            followingList.removeLast();
            nextCursor = followingList.getLast().getId();
        }

        // 3. 팔로잉 목록의 닉네임, 프로필 이미지 배치 조회
        List<String> followingIdList = followingList.stream()
                .map(Follow::getFollowingId)
                .distinct()
                .toList();

        var followingMap = memberQueryService.getMemberNicknamesAndProfileImagesByMemberIds(memberId, followingIdList);

        List<MemberSharedDTO.WithFollowStatusDTO> followingDTOList = new ArrayList<>(followingMap.values());

        // 4. DTO 변환
        return MemberConverter.toFollowList(followingDTOList, hasNext, nextCursor);
    }

    @Override
    public MemberResponseDTO.FollowPreviewList getFollowers(String memberId, int size) {
        // 1. 팔로워 목록 size 개수만큼 조회
        List<Follow> followerList = memberFollowQueryService.getFollowers(memberId, size);

        // 2. 팔로워 목록의 닉네임, 프로필 이미지 배치 조회
        List<String> followerIdList = followerList.stream()
                .map(Follow::getFollowerId)
                .distinct()
                .toList();

        var followerMap = memberQueryService.getMemberNicknamesAndProfileImagesByMemberIds(memberId, followerIdList);

        List<MemberSharedDTO.WithFollowStatusDTO> followerDTOList = new ArrayList<>(followerMap.values());

        // 3. DTO 변환
        return MemberConverter.toFollowPreviewList(followerDTOList);
    }

    @Override
    public MemberResponseDTO.FollowPreviewList getFollowings(String memberId, int size) {
        // 1. 팔로잉 목록 size 개수만큼 조회
        List<Follow> followingList = memberFollowQueryService.getFollowings(memberId, size);

        // 2. 팔로잉 목록의 닉네임, 프로필 이미지 배치 조회
        List<String> followingIdList = followingList.stream()
                .map(Follow::getFollowingId)
                .distinct()
                .toList();

        var followingMap = memberQueryService.getMemberNicknamesAndProfileImagesByMemberIds(memberId, followingIdList);

        List<MemberSharedDTO.WithFollowStatusDTO> followingDTOList = new ArrayList<>(followingMap.values());

        // 3. DTO 변환
        return MemberConverter.toFollowPreviewList(followingDTOList);
    }

    @Override
    public String getMemberIdByNickname(String nickname) {
        return memberQueryService.getMemberIdByNickname(nickname);
    }

    @Override
    public Map<String, String> getMemberIdsByNicknames(List<String> nicknames) {
        if (nicknames == null || nicknames.isEmpty()) {
            return Map.of();
        }
        return memberQueryService.getMemberIdsByNicknames(nicknames);
    }

    @Override
    public boolean isFollowing(String memberId, String targetMemberNickname) {
        String targetMemberId = memberQueryService.getMemberIdByNickname(targetMemberNickname);
        return memberFollowQueryService.isFollowing(memberId, targetMemberId);
    }

    /**
     * 공유용 기본 회원 정보 조회 (외부용)
     *
     * @param memberId 조회할 회원 ID
     * @return MemberSharedDTO.BasicInfo
     */
    @Override
    public MemberSharedDTO.BasicInfoDTO getMemberBasicInfoForShare(String memberId) {
        Member member = memberQueryService.getMemberBasicInfo(memberId);
        MemberResponseDTO.MemberProfileResponseDTO profileDTO = MemberConverter.toMemberProfileResponseDTO(member);

        return MemberConverter.toBasicInfoDTO(profileDTO);
    }

    @Override
    public Map<String, MemberSharedDTO.BasicInfoDTO> getMemberBasicInfoMapForShare(List<String> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return Map.of();
        }

        // 1. Repository를 통해 IN 쿼리로 모든 회원 정보 조회
        // [0] memberId, [1] nickname, [2] profileImageUrl
        List<Object[]> results = memberQueryService.getMemberBasicInfoMapForShare(memberIds);

        // 2. 조회된 엔티티 리스트를 Map으로 변환
        // memberId를 key로, BasicInfoDTO를 value로 사용
        return results.stream()
                      .collect(Collectors.toMap(
                          row -> (String) row[0], // memberId
                          row -> MemberSharedDTO.BasicInfoDTO.builder()
                                                             .nickname((String) row[1]) // nickname
                                                             .profileImageUrl((String) row[2]) // profileImageUrl
                                                             .build()
                      ));
    }

    /**
     * 공유용 기본 회원 정보 + 팔로우 상태 조회 (외부용)
     *
     * @param targetMemberId 조회 대상 회원 ID
     * @param currentMemberId 현재 로그인한 회원 ID
     * @return MemberSharedDTO.WithFollowStatusDTO
     */
    @Override
    public MemberSharedDTO.WithFollowStatusDTO getMemberWithFollowStatusForShare(String targetMemberId, String currentMemberId) {
        // 팔로우 상태를 조회
        boolean isFollowing = memberFollowQueryService.isFollowing(currentMemberId, targetMemberId);

        var basicInfoDTO = getMemberBasicInfoForShare(targetMemberId);
        return MemberConverter.toWithFollowStatusDTO(basicInfoDTO, isFollowing);
    }

    @Override
    public Map<String, MemberSharedDTO.WithFollowStatusDTO> getMemberWithFollowStatusMapForShare(List<String> targetMemberIds, String currentMemberId) {
        if (targetMemberIds == null || targetMemberIds.isEmpty()) {
            return Map.of();
        }

        // 회원 ID 목록으로 회원 닉네임과 프로필 이미지 배치 조회하기
        return memberQueryService.getMemberNicknamesAndProfileImagesByMemberIds(currentMemberId, targetMemberIds);
    }

    @Override
    public Member findMemberReferenceById(String memberId) {
        return memberRepository.getReferenceById(memberId);
    }

    @Override
    public String getMemberNicknameById(String memberId) {
        return memberQueryService.getMemberNicknameById(memberId);
    }

    @Override
    public Map<String, String> getMemberNicknamesByMemberIds(List<String> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return Map.of();
        }

        return memberQueryService.getMemberNicknamesByMemberIds(memberIds);
    }
}
