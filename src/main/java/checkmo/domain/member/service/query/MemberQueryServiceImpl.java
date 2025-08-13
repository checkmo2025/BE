package checkmo.domain.member.service.query;

import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.category.facade.CategoryQueryFacade;
import checkmo.domain.member.converter.MemberConverter;
import checkmo.domain.member.entity.Member;
import checkmo.domain.member.repository.FollowRepository;
import checkmo.domain.member.repository.MemberRepository;
import checkmo.domain.member.web.dto.MemberResponseDTO;
import checkmo.global.dto.CategorySharedDTO;
import checkmo.global.dto.MemberSharedDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberQueryServiceImpl implements MemberQueryService {

    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
    private final CategoryQueryFacade categoryQueryFacade;
    private final MemberFollowQueryService memberFollowQueryService;

    @Override
    public boolean isNicknameDuplicated(String nickname) {
        return memberRepository.existsByNickName(nickname);
    }

    /**
     * 회원 기본 정보 조회
     *
     * @param memberId 회원 ID
     * @return 회원 기본 정보 DTO
     */
    @Override
    public MemberResponseDTO.MemberProfileResponseDTO getMemberBasicInfo(String memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        return MemberConverter.toMemberProfileResponseDTO(member);
    }

    @Override
    public MemberResponseDTO.MemberProfileWithCategoryResponseDTO getMemberProfile(String memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        List<CategorySharedDTO.CategoryInfo> categories = categoryQueryFacade.getCategoriesByMemberForShare(memberId).getCategoryList();

        return MemberConverter.toMemberProfileWithCategoryResponseDTO(member, categories);
    }

    @Override
    public Map<String, MemberSharedDTO.BasicInfoDTO> getMemberBasicInfoMapForShare(List<String> memberIds) {
        // 1. Repository를 통해 IN 쿼리로 모든 회원 정보 조회
        // [0] memberId, [1] nickname, [2] profileImageUrl
        List<Object[]> results = memberRepository.findIdNicknameAndImgUrlByIdIn(memberIds);

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

    @Override
    public MemberResponseDTO.otherProfileResponseDTO getOtherProfile(String targetMemberNickname, String memberId) {

        Member targetMember = memberRepository.findByNickName(targetMemberNickname)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        boolean isFollowing = memberFollowQueryService.isFollowing(memberId, targetMember.getId());

        List<CategorySharedDTO.CategoryInfo> categories = categoryQueryFacade.getCategoriesByMemberForShare(targetMember.getId()).getCategoryList();

        return MemberConverter.toOtherProfileResponseDTO(targetMember, isFollowing, categories);

    }

    @Override
    public String getMemberIdByNickname(String nickname) {
        return memberRepository.findIdByNickName(nickname)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
    }

    @Override
    public Map<String, String> getMemberIdsByNicknames(List<String> nicknames) {
        List<Object[]> results = memberRepository.findNicknameAndIdByNicknameIn(nicknames);
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0], // key: nickname
                        row -> (String) row[1]  // value: memberId
                ));
    }

    @Override
    public String getMemberNicknameById(String memberId) {
        return memberRepository.findNicknameById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
    }

    @Override
    public Map<String, String> getMemberNicknamesByMemberIds(List<String> memberIds) {
        var results = memberRepository.findIdAndNicknameByIdIn(memberIds);
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0], // memberId
                        row -> (String) row[1]  // nickname
                ));
    }

    @Override
    public Map<String, MemberSharedDTO.WithFollowStatusDTO> getMemberNicknamesAndProfileImagesByMemberIds(String memberId, List<String> memberIds) {
        // 1. 배치로 회원 기본 정보 조회 (1번의 쿼리)
        var results = memberRepository.findIdNicknameAndImgUrlByIdIn(memberIds);

        // 2. 배치처리로 팔로잉 중인 회원의 id 목록 전부 조회 (2번의 쿼리)
        Set<String> followingIds = followRepository.findFollowingIdsByFollowerId(memberId, memberIds);

        // 3. DTO 생성
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0], // targetMemberId
                        row -> {
                            String targetMemberId = (String) row[0];
                            boolean isFollowing = targetMemberId.equals(memberId) || followingIds.contains(targetMemberId); // 본인인 경우 true, 그 외에는 팔로잉 여부 확인
                            return MemberConverter.toWithFollowStatusDTO(row, isFollowing);
                        }
                ));
    }
}
