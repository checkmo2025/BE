package checkmo.domain.member.service.command;

import checkmo.domain.member.web.dto.MemberRequestDTO;
import checkmo.domain.member.web.dto.MemberResponseDTO;

/**
 * 회원 프로필 정보 수정 서비스
 *
 * 기존 회원의 개인정보 변경, 프로필 이미지 업로드, 관심 도서 분야 설정 등을 담당
 * 닉네임 변경, 간단 소개 수정, 비밀번호 변경(일반 회원만), 계정 탈퇴 처리 포함
 */
public interface MemberProfileCommandService {

    /**
     * 회원 프로필 정보 수정
     *
     * @param memberId 수정할 회원의 ID
     * @param request 수정할 프로필 정보 DTO - 프로필 이미지, 간단 소개, 관심 카테고리 (닉네임은 변경 불가!!)
     * @return 수정된 회원 프로필 정보 DTO - 이때는 관심 카테고리 정보 DTO에 포함 X , -> 반드시 CategoryQueryFacade를 통해 조회해야 함
     */
    MemberResponseDTO.MemberProfileResponseDTO updateMemberProfile(
            String memberId, MemberRequestDTO.MemberProfileUpdateRequestDTO request
    );

    /**
     * 회원 비밀번호 변경 (일반 회원만)
     *
     * @param memberId 비밀번호를 변경할 회원의 ID
     * @param request 비밀번호 변경 정보 DTO (현재 비밀번호, 새 비밀번호, 새 비밀번호 확인)
     */
    void updatePassword(
            String memberId, MemberRequestDTO.PasswordUpdateRequestDTO request
    );

    /**
     * 회원 계정 삭제 (soft delete)
     *
     * @param memberId 비활성화할 회원의 ID
     */
    void deactivateMember(String memberId);

    /**
     * 회원 계정 완전 삭제 (hard delete)
     *
     * @param memberId 삭제할 회원의 ID
     */
    void deleteMember(String memberId);
}