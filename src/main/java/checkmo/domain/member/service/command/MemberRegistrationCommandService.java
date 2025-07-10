package checkmo.domain.member.service.command;

/**
 * 회원 가입 서비스
 *
 * 새로운 회원의 계정 생성, 이메일 인증, 초기 설정 등을 담당 (이건 일반 회원만)
 * 추가 정보 입력 프로세스 모두 포함 (닉네임, 프로필 이미지, 관심 도서 분야 등) <- 이건 일반회원/소셜 회원 공동 사용
 */
public interface MemberRegistrationCommandService {

    // 이메일 중복 확인 메소드 만들기

    // 이메일 인증 요청 메소드 만들기

    // 이메일 인증번호 확인 메소드 만들기

    // 회원 가입 메소드 만들기 (닉네임, 프로필 이미지, 관심 도서 분야 등) -> 얘는 소셜 로그인도 사용
}
