package checkmo.member.internal.scheduler;

import checkmo.authentication.AuthenticationAPI;
import checkmo.member.internal.entity.Member;
import checkmo.member.internal.repository.MemberRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberCleanupScheduler {

    private final MemberRepository memberRepository;
    private final AuthenticationAPI authenticationAPI;

    /**
     * 프로필 미완료(유령) 회원 삭제 스케줄러
     * 15분마다 실행
     */
    @Scheduled(fixedRate = 900000)
    @Transactional
    public void cleanupGhostMembers() {
        // 현재 시간으로부터 15분 전 시점 계산
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(15);

        // 15분 전에 생성되었으나 아직 닉네임이 없는(가입 절차를 마치지 않은) 유저 조회
        List<Member> ghostMembers = memberRepository.findAllGhostMembers(threshold);

        if (!ghostMembers.isEmpty()) {
            log.info("유령 회원 삭제 시작: {}명", ghostMembers.size());

            for (Member ghost : ghostMembers) {
                // 인증 데이터 삭제 (Authentication 모듈 API 호출)
                authenticationAPI.deleteAuthData(ghost.getId());

                // 회원 삭제
                memberRepository.deleteById(ghost.getId());
            }
            memberRepository.flush();

            log.info("유령 회원 삭제 완료");
        }
    }
}
