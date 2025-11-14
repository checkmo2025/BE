package checkmo.member.internal.service.command;

import checkmo.member.web.dto.MemberRequestDTO;

/**
 * 회원 가입 서비스
 * <p>
 * 새로운 회원의 계정 생성, 이메일 인증, 초기 설정 등을 담당 (이건 일반 회원만) 추가 정보 입력 프로세스 모두 포함 (닉네임, 프로필 이미지, 관심 도서 분야 등) <- 이건 일반회원/소셜 회원 공동 사용
 */
public interface MemberRegistrationCommandService {

    void createMember(String memberId, String email);

    /**
     * 회원 추가 정보 입력
     *
     * @param request 추가 정보 DTO (닉네임, 프로필 이미지, 관심 카테고리)
     * @return void -> 어차피 회원 프로필 정보 완료 후에는 메인 화면에 로그인된 상태로 리다이렉트
     */
    void addAdditionalInfo(String memberId, MemberRequestDTO.AdditionalInfo request);
}