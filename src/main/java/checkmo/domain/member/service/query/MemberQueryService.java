package checkmo.domain.member.service.query;

import checkmo.domain.member.dto.MemberResponseDTO;

/**
 * 회원 기본 정보 조회 서비스
 *
 * 회원과 관련된 모든 조회 서비스 여기서 처리
 */
public interface MemberQueryService {

    /**
     * 닉네임 중복 확인
     *
     * @param nickname 확인할 닉네임
     * @return 중복 여부 (true: 중복됨, false: 사용 가능)
     */
    boolean isNicknameDuplicated(String nickname);

    /**
     * 회원 기본 정보 조회
     *
     * @param memberId 회원 ID
     * @return 회원 기본 정보 DTO
     */
    MemberResponseDTO.MemberProfileResponseDTO getMemberBasicInfo(String memberId);

    /**
     * 다른 사람 프로필 조회
     *
     * @param targetMemberNickname 조회 대상 회원 닉네임
     * @param memberId 조회하는 회원 ID (팔로우 여부 확인용)
     * @return targetMember의 프로필 정보 DTO - 이때는 관심 카테고리 정보 DTO에 포함 X , -> 반드시 CategoryQueryFacade를 통해 조회해야 함
     */
    MemberResponseDTO.otherProfileResponseDTO getOtherProfile(String targetMemberNickname, String memberId);

    /**
     * 닉네임으로 회원 ID 조회
     *
     * @param nickname 닉네임
     * @return 회원 ID
     */
    String getMemberIdByNickname(String nickname);
}