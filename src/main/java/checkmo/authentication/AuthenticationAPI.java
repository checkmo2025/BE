package checkmo.authentication;

public interface AuthenticationAPI {

    /**
     * 현재 로그인 중인 AuthUser의 프로일을 완성 처리합니다.
     *
     * @param memberId 현재 로그인 중인 memberId
     */
    void completeProfile(String memberId);

    /**
     * 특정 회원의 인증 관련 데이터(계정 정보, 권한, 토큰 등)를 완전히 삭제합니다.
     */
    void deleteAuthData(String memberId);
}
