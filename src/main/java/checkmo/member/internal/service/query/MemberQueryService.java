package checkmo.member.internal.service.query;

import checkmo.member.internal.entity.Member;
import checkmo.member.internal.entity.MemberInterestCategory;
import checkmo.member.internal.exception.MemberErrorStatus;
import checkmo.member.internal.exception.MemberException;
import checkmo.member.internal.repository.MemberRepository;
import checkmo.member.internal.repository.projection.MemberBasicInfoProjection;
import checkmo.member.internal.repository.projection.MemberIdAndNicknameProjection;
import checkmo.member.web.dto.MemberRequestDTO;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryService {

    private final MemberRepository memberRepository;

    /**
     * 닉네임 중복 확인
     *
     * @param nickname 확인할 닉네임
     * @return 중복 여부 (true: 중복됨, false: 사용 가능)
     */
    public boolean isNicknameDuplicated(String nickname) {
        return memberRepository.existsByNickNameAndDeactivatedAtIsNull(nickname);
    }

    /**
     * 회원 기본 정보 조회
     *
     * @param memberId 회원 ID
     * @return 회원 엔티티
     */
    public Member retrieveMember(String memberId) {
        return memberRepository.findByIdAndDeactivatedAtIsNull(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorStatus.MEMBER_NOT_FOUND));
    }

    /**
     * 회원 기본 정보 목록 조회
     *
     * @param memberIds 회원 ID 목록
     * @return 회원 엔티티 리스트
     */
    public List<Member> retrieveMemberById(List<String> memberIds) {
        return memberRepository.findAllByIdInAndDeactivatedAtIsNull(memberIds);
    }

    /**
     * 회원 기본 정보 조회
     *
     * @param targetMemberNickname 조회 대상 회원 닉네임
     * @return targetMember의 프로필 정보 DTO - 이때는 관심 카테고리 정보 DTO에 포함 X
     */
    public Member retrieveMemberByNickname(String targetMemberNickname) {
        return memberRepository.findByNickName(targetMemberNickname)
                .orElseThrow(() -> new MemberException(MemberErrorStatus.MEMBER_NOT_FOUND));
    }

    public List<MemberBasicInfoProjection> retrieveActiveMemberBasicInfos(List<String> memberIds) {
        return memberRepository.findActiveIdNicknameAndImgUrlByIdIn(memberIds);
    }

    /**
     * 닉네임으로 회원 ID 조회
     *
     * @param nickname 닉네임
     * @return 회원 ID
     */
    public String retrieveMemberId(String nickname) {
        return memberRepository.findIdByNickName(nickname)
                .orElseThrow(() -> new MemberException(MemberErrorStatus.MEMBER_NOT_FOUND));
    }

    public Optional<String> retrieveActiveMemberNickname(String memberId) {
        return memberRepository.findActiveNicknameById(memberId);
    }

    public Map<String, String> retrieveActiveMemberNicknameByMemberIds(List<String> memberIds) {
        List<MemberIdAndNicknameProjection> results = memberRepository.findActiveIdAndNicknameByIdIn(memberIds);
        return results.stream()
                .collect(Collectors.toMap(
                        MemberIdAndNicknameProjection::getId,
                        MemberIdAndNicknameProjection::getNickName
                ));
    }

    /**
     * 회원 이름과 전화번호로 가입한 이메일 찾기
     */
    public String retrieveMemberEmail(MemberRequestDTO.FindEmail request) {
        List<Member> members = memberRepository.findAllByNameAndPhoneNumber(request.getName(),
                request.getPhoneNumber());

        // 계정이 없는 경우
        if (members.isEmpty()) {
            throw new MemberException(MemberErrorStatus.MEMBER_NOT_FOUND);
        }

        // 계정이 2개 이상인 경우
        if (members.size() > 1) {
            throw new MemberException(MemberErrorStatus.MULTIPLE_ACCOUNTS_FOUND);
        }

        // 1개인 경우에만 이메일 반환
        return members.get(0).getEmail();
    }

    public List<Member> retrieveRecommendedMembers(
            String memberId,
            List<MemberInterestCategory> myInterests,
            List<String> excludedMemberIds,
            int limit
    ) {
        return memberRepository.findRecommendMembers(memberId, myInterests, excludedMemberIds, limit);
    }

    public List<String> retrieveActiveEmailsByKeyword(String keyword, int limit) {
        String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        int boundedLimit = Math.min(Math.max(limit, 1), 100);
        return memberRepository.findActiveEmailsByKeyword(normalizedKeyword, PageRequest.of(0, boundedLimit));
    }

    public Page<Member> retrieveMembersForAdmin(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        if (keyword == null || keyword.isBlank()) {
            return memberRepository.findAll(pageable);
        }

        return memberRepository.findByIdContainingIgnoreCaseOrEmailContainingIgnoreCase(
                keyword,
                keyword,
                pageable
        );
    }

}
