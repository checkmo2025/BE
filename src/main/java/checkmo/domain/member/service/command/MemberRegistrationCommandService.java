package checkmo.domain.member.service.command;

import checkmo.domain.member.web.dto.MemberRequestDTO;
import checkmo.domain.member.web.dto.MemberResponseDTO;

/**
 * 회원 가입 서비스
 *
 * 새로운 회원의 계정 생성, 이메일 인증, 초기 설정 등을 담당 (이건 일반 회원만)
 * 추가 정보 입력 프로세스 모두 포함 (닉네임, 프로필 이미지, 관심 도서 분야 등) <- 이건 일반회원/소셜 회원 공동 사용
 */
public interface MemberRegistrationCommandService {

    /**
     * Redis Hash 구조 사용해서
     * key : kiroro0814@naver.com
     * field :
     *      code : [인증 코드]
     *      verified : true/false
     * 이렇게 1개의 이메일 키 값에 두개의 값 저장
     */

    /**
     * 이메일 인증 요청
     *
     * @param email 인증할 이메일
     */
    void sendEmailVerification(String email);

    /**
     * 이메일 인증번호 확인
     *
     * @param request 이메일 인증 요청 DTO
     * @return 인증 성공 여부
     */
    boolean verifyEmailCode(
            MemberRequestDTO.EmailVerificationRequestDTO request
    );

    /**
     * 회원 가입 - 이 정보를 전달받으면 Access Token, RefreshToken 생성
     *  - 추가 정보 입력 안받으면 정식 회원 아님
     *
     * @param request id,pw DTO
     * @return 회원 가입 응답 DTO
     */
    MemberResponseDTO.SignUpResponseDTO signUp(
            MemberRequestDTO.SignUpRequestDTO request
    );

    /**
     * 회원 추가 정보 입력
     *
     * @param request 추가 정보 DTO (닉네임, 프로필 이미지, 관심 카테고리)
     * @return void -> 어차피 회원 프로필 정보 완료 후에는 메인 화면에 로그인된 상태로 리다이렉트
     */
    void addAdditionalInfo(
            MemberRequestDTO.AdditionalInfoDTO request
    );
}