package checkmo.domain.member.service.authenticate;

import checkmo.domain.member.dto.MemberResponseDTO;

/**
 * 로그인/로그아웃 로직
 *
 * JWT 기반 로그인 담당하는 서비스
 * 사용자가 제출한 로그인 정보를 검증하고 JWT 토큰 생성/쿠키 설정/SecurityContext 저장까지의 전체 과정 관리
 * 로그아웃 시 토큰 무효화 및 쿠키 삭제
 */
public interface MemberAuthenticationService {

    /**
     * 로그인 처리
     *
     * @param email 사용자의 이메일
     * @param password 사용자의 비밀번호
     */
    void login(String email, String password);

    /**
     * 로그아웃 처리 - JWT 토큰을 무효화하고 쿠키 삭제
     * 계정 복구를 원치 않는 경우에도 다시 사용하기
     * -> NO를 누름 -> AccessToken, RefreshToken 모두 삭제하고 탈퇴 상태 유지(아무것도 안해줘도 됨)
     *
     * @param token 로그아웃할 JWT 토큰 -> redis Blacklist에 저장하여 무효화
     */
    void logout(String token);

    /**
     * 회원 계정 복구 (비활성화된 계정 재활성화)
     */
    void reactivateMember();

    /**
     * 탈퇴한 사용자가 login()을 누름 -> 서버로 요청 1 = 서버에 요청이 전달되는 순간 AccessToken, RefreshToken 생성
     * -> 이제 내부에서 해당 회원이 탈퇴 상태인지 확인 -> 탈퇴일 경우 바로 반환
     * -> 사용자가 복구 할지 여부를 묻는 창을 보게 됨
     * -> OK를 누름 -> 서버로 요청 2
     * -> reactivateMember() 호출해서 회원 계정 복구
     *
     * 사용자 입장 : 로그인 버튼 누름 -> 계정 복구 OK 누름 -> 바로 메인 홈화면
     * 사용자 입장 : 로그인 버튼 누름 -> 계정 복구 OK 누름 -> 다시 로그인 화면으로 돌아가서 id, pw 입력하고 로그인 버튼 누름
     *
     *
     */
}
