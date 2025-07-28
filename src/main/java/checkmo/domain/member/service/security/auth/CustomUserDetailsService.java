package checkmo.domain.member.service.security.auth;

import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.member.entity.Member;
import checkmo.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security UserDetailsService 구현체
 *
 * 일반 로그인 시 id로 회원을 조회하여 UserDetails 객체로 변환
 * 비활성화된 계정, 탈퇴한 계정 등의 상태 검증도 여기서 처리
 * loadUserByUsername 메소드 단순하게 오버라이딩 하면 됨
 */

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email)
                                        .orElseThrow(() -> new UsernameNotFoundException(
                                            "해당 이메일을 가진 사용자를 찾을 수 없습니다: " + email));

        // 비활성화된 계정, 탈퇴한 계정 등의 상태 검증
        if (member.getDeactivated() != null) {
            throw new GeneralException(ErrorStatus.MEMBER_INACTIVE);
        }

        return new PrincipalDetails(member);
    }

    public UserDetails loadUserById(String id) {
        Member member = memberRepository.findById(id)
                                        .orElseThrow(() -> new GeneralException(
                                            ErrorStatus.MEMBER_NOT_FOUND));

        validateMemberStatus(member);
        return new PrincipalDetails(member);
    }

    private void validateMemberStatus(Member member) {
        if (member.getDeactivated() != null) {
            throw new GeneralException(ErrorStatus.MEMBER_INACTIVE);
        }
    }
}
