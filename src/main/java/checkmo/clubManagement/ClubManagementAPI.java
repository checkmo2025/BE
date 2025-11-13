package checkmo.clubManagement;

public interface ClubManagementAPI {

    /**
     * 특정 회원이 가입한 모임 목록을 조회합니다. (외부용) 마이페이지 등 다른 서비스에서 사용됩니다.
     *
     * @param memberId 회원 ID
     * @return 회원이 가입한 모임의 간략한 정보 목록 DTO
     */
    ClubManagementExternalDTO.MyClubList getMyClubListForShare(String memberId);

}
