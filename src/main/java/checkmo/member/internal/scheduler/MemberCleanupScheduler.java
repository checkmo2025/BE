package checkmo.member.internal.scheduler;

import checkmo.authentication.AuthenticationAPI;
import checkmo.member.internal.entity.Member;
import checkmo.member.internal.repository.MemberRepository;
import checkmo.member.internal.service.command.MemberCommandService;
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
    private final MemberCommandService memberCommandService;

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

            List<String> ghostMemberIds = ghostMembers.stream()
                                                      .map(Member::getId)
                                                      .toList();

            for (String id : ghostMemberIds) {
                authenticationAPI.deleteAuthData(id);
            }

            memberRepository.deleteAllByIdInBatch(ghostMemberIds);

            log.info("유령 회원 삭제 완료");
        }
    }

    /**
     * 탈퇴 후 1년이 지난 회원 계정 하드 삭제 스케줄러
     * 매일 새벽 3시에 실행
     */
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void cleanupExpiredDeactivatedMembers() {
        LocalDateTime threshold = LocalDateTime.now().minusYears(1);
        List<Member> expiredMembers = memberRepository.findAllByDeactivatedAtBefore(threshold);

        if (expiredMembers.isEmpty()) {
            return;
        }

        log.info("탈퇴 1년 경과 회원 삭제 시작: {}명", expiredMembers.size());

        List<String> expiredMemberIds = expiredMembers.stream()
                .map(Member::getId)
                .toList();

        for (String memberId : expiredMemberIds) {
            try {
                memberCommandService.deleteMember(memberId);
            } catch (Exception e) {
                log.error("탈퇴 1년 경과 회원 삭제 실패. memberId={}", memberId, e);
            }
        }

        log.info("탈퇴 1년 경과 회원 삭제 완료");
    }
}
