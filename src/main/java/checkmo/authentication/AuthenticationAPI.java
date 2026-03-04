package checkmo.authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthenticationAPI {

    /**
     * 현재 로그인 중인 AuthUser의 프로일을 완성 처리합니다.
     *
     * @param memberId 현재 로그인 중인 memberId
     */
    void completeProfile(String memberId);

    /**
     * 특정 회원의 인증 계정을 비활성화하고 세션을 만료시킵니다.
     */
    void deactivateMember(String memberId, HttpServletRequest request, HttpServletResponse response);

    /**
     * 특정 회원의 인증 관련 데이터(계정 정보, 권한, 토큰 등)를 완전히 삭제합니다.
     */
    void deleteAuthData(String memberId);

    /**
     * 추가정보 입력 시 특정 회원의 닉네임을 저장합니다.
     * @param memberId
     * @param nickname
     */
    void updateNickname(String memberId, String nickname);

    /**
     *
     * @param memberId 현재 로그인 중인 memberId
     * @param currentPassword 기존 비밀번호
     * @param newPassword 새 비밀번호
     * @return 비밀번호 변경 성공 여부
     */
    boolean updatePassword(String memberId, String currentPassword, String newPassword);

    void updateEmail(String memberId, String currentEmail, String newEmail, String verificationCode);
}
