package checkmo.domain.member.service.query;

import checkmo.domain.member.entity.Member;
import checkmo.global.dto.MemberSharedDTO;
import java.util.List;
import java.util.Map;

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
    Member getMemberBasicInfo(String memberId);

    /**
     * 회원 프로필 정보 (카테고리 포함) 조회
     *
     * @param memberId 회원 ID
     * @return 회원 프로필 정보 DTO
     */
    Member getMemberProfile(String memberId);

    /**
     * 회원 ID 목록으로 회원 기본 정보 배치 조회
     *
     * @param memberIds 회원 ID 목록
     * @return 회원 ID와 기본 정보 DTO의 매핑
     */
    List<Object[]> getMemberBasicInfoMapForShare(List<String> memberIds);

    /**
     * 다른 사람 프로필 조회
     *
     * @param targetMemberNickname 조회 대상 회원 닉네임
     * @return targetMember의 프로필 정보 DTO - 이때는 관심 카테고리 정보 DTO에 포함 X , -> 반드시 CategoryQueryFacade를 통해 조회해야 함
     */
    Member getOtherProfile(String targetMemberNickname);

    /**
     * 닉네임으로 회원 ID 조회
     *
     * @param nickname 닉네임
     * @return 회원 ID
     */
    String getMemberIdByNickname(String nickname);

    /**
     * 닉네임 목록으로 회원 ID 배치 조회
     *
     * @param nicknames 닉네임 목록
     * @return 닉네임과 회원 ID의 매핑 정보
     */
    Map<String, String> getMemberIdsByNicknames(List<String> nicknames);

    /**
     * 회원ID로 회원 닉네임 조회
     *
     * @param memberId 회원 ID
     * @return 회원 닉네임
     */
    String getMemberNicknameById(String memberId);

    /**
     * 회원 ID 목록으로 회원 닉네임 배치 조회
     *
     * @param memberIds 회원 ID 목록
     * @return 회원 ID와 닉네임의 매핑 정보
     */
    Map<String, String> getMemberNicknamesByMemberIds(List<String> memberIds);

    /**
     * 회원 ID 목록으로 회원 닉네임과 프로필 이미지 배치 조회
     *
     * @param memberIds 회원 ID 목록
     * @return 회원 ID와 닉네임, 프로필 이미지, 팔로우 상태 정보의 매핑
     */
    List<Object[]> getMemberNicknamesAndProfileImagesByMemberIds(List<String> memberIds);
}