package checkmo.authentication;

public interface AuthenticationAPI {

    /**
     * 현재 로그인 중인 AuthUser의 프로일을 완성 처리합니다.
     *
     * @param memberId 현재 로그인 중인 memberId
     */
    void completeProfile(String memberId);
}
