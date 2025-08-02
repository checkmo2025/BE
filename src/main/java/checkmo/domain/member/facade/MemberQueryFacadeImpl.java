package checkmo.domain.member.facade;

import checkmo.domain.member.converter.MemberConverter;
import checkmo.domain.member.entity.Follow;
import checkmo.domain.member.entity.Member;
import checkmo.domain.member.repository.MemberRepository;
import checkmo.domain.member.service.query.MemberFollowQueryService;
import checkmo.domain.member.service.query.MemberQueryService;
import checkmo.domain.member.web.dto.MemberResponseDTO;
import checkmo.global.dto.MemberSharedDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryFacadeImpl implements MemberQueryFacade {

    public static final int DEFAULT_PAGE_SIZE = 20;

    private final MemberRepository memberRepository; // 프록시용
    private final MemberQueryService memberQueryService;
    private final MemberFollowQueryService memberFollowQueryService;

    @Override
    public boolean isNicknameDuplicated(String nickname) {
        return memberQueryService.isNicknameDuplicated(nickname);
    }

    @Override
    public MemberResponseDTO.MemberProfileResponseDTO getMemberBasicInfo(String memberId) {
        return null;
    }

    @Override
    public MemberResponseDTO.otherProfileResponseDTO getOtherProfile(String targetMemberNickname, String memberId) {
        return null;
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

        List<MemberResponseDTO.FollowResponse> followerDTOList = new ArrayList<>(followerMap.values());

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

        List<MemberResponseDTO.FollowResponse> followingDTOList = new ArrayList<>(followingMap.values());

        // 4. DTO 변환
        return MemberConverter.toFollowList(followingDTOList, hasNext, nextCursor);
    }

    @Override
    public MemberResponseDTO.FollowList getFollowers(Long memberId, int size) {
        return null;
    }

    @Override
    public MemberResponseDTO.FollowList getFollowings(Long memberId, int size) {
        return null;
    }

    @Override
    public String getMemberIdByNickname(String nickname) {
        return "";
    }

    @Override
    public boolean isFollowing(String memberId, String targetMemberNickname) {
        return false;
    }

    /**
     * 공유용 기본 회원 정보 조회 (외부용)
     * @param memberId 조회할 회원 ID
     * @return MemberSharedDTO.BasicInfo
     */
    @Override
    public MemberSharedDTO.BasicInfoDTO getMemberBasicInfoForShare(String memberId) {
        var profile = memberQueryService.getMemberBasicInfo(memberId);
        return MemberConverter.toBasicInfoDTO(profile);
    }

    /**
     * 공유용 기본 회원 정보 + 팔로우 상태 조회 (외부용)
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
    public Member findMemberReferenceById(String memberId) {
        return memberRepository.getReferenceById(memberId);
    }

    @Override
    public String getMemberNicknameById(String memberId) {
        return memberQueryService.getMemberNicknameById(memberId);
    }

    @Override
    public Map<String, String> getMemberNicknamesByMemberIds(List<String> memberIds) {
        return memberQueryService.getMemberNicknamesByMemberIds(memberIds);
    }
}
