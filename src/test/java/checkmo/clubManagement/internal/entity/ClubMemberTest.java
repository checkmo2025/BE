package checkmo.clubManagement.internal.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import checkmo.clubManagement.internal.excepetion.ClubManagementErrorStatus;
import checkmo.clubManagement.internal.excepetion.ClubManagementException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ClubMemberTest {

    private static final LocalDateTime APPLIED_AT = LocalDateTime.of(2026, 1, 10, 9, 0);
    private static final LocalDateTime CHANGED_AT = LocalDateTime.of(2026, 1, 11, 10, 30);

    @Test
    void 이미_가입한_회원은_재신청할_수_없다() {
        ClubMember clubMember = clubMember(1L, ClubMemberStatus.MEMBER);

        assertThatThrownBy(() -> clubMember.reApply(ClubMemberStatus.MEMBER, "again", CHANGED_AT))
                .isInstanceOfSatisfying(ClubManagementException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClubManagementErrorStatus.CLUB_MEMBER_ALREADY_JOINED)
                );
    }

    @Test
    void 대기_회원은_가입_승인시_활성_회원이_된다() {
        ClubMember actor = clubMember(99L, ClubMemberStatus.STAFF);
        ClubMember clubMember = clubMember(1L, ClubMemberStatus.PENDING);

        clubMember.approveJoinBy(actor, CHANGED_AT);

        assertSoftly(softly -> {
            softly.assertThat(clubMember.getClubMemberStatus()).isEqualTo(ClubMemberStatus.MEMBER);
            softly.assertThat(clubMember.getJoinedAt()).isEqualTo(CHANGED_AT);
        });
    }

    @Test
    void 대기_상태가_아닌_회원은_가입_승인할_수_없다() {
        ClubMember actor = clubMember(99L, ClubMemberStatus.STAFF);
        ClubMember clubMember = clubMember(1L, ClubMemberStatus.MEMBER);

        assertThatThrownBy(() -> clubMember.approveJoinBy(actor, CHANGED_AT))
                .isInstanceOfSatisfying(ClubManagementException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClubManagementErrorStatus.CLUB_MEMBER_INVALID_STATUS)
                );
    }

    @Test
    void 비운영진은_가입을_승인할_수_없다() {
        ClubMember actor = clubMember(99L, ClubMemberStatus.MEMBER);
        ClubMember clubMember = clubMember(1L, ClubMemberStatus.MEMBER);

        assertThatThrownBy(() -> clubMember.approveJoinBy(actor, CHANGED_AT))
                .isInstanceOfSatisfying(ClubManagementException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClubManagementErrorStatus.CLUB_STAFF_ONLY)
                );
    }

    @Test
    void 클럽장은_탈퇴할_수_없다() {
        ClubMember clubMember = clubMember(1L, ClubMemberStatus.OWNER);

        assertThatThrownBy(() -> clubMember.leave(CHANGED_AT))
                .isInstanceOfSatisfying(ClubManagementException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClubManagementErrorStatus.CLUB_OWNER_CANNOT_LEAVE)
                );
    }

    @Test
    void 비활성_회원은_탈퇴할_수_없다() {
        ClubMember clubMember = clubMember(1L, ClubMemberStatus.WITHDRAWN);

        assertThatThrownBy(() -> clubMember.leave(CHANGED_AT))
                .isInstanceOfSatisfying(ClubManagementException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClubManagementErrorStatus.CLUB_MEMBER_INVALID_STATUS)
                );
    }

    @Test
    void 클럽장은_강퇴할_수_없다() {
        ClubMember actor = clubMember(99L, ClubMemberStatus.STAFF);
        ClubMember clubMember = clubMember(1L, ClubMemberStatus.OWNER);

        assertThatThrownBy(() -> clubMember.kickBy(actor, CHANGED_AT))
                .isInstanceOfSatisfying(ClubManagementException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClubManagementErrorStatus.CLUB_OWNER_CANNOT_BE_KICKED)
                );
    }

    @Test
    void 비활성_회원은_강퇴할_수_없다() {
        ClubMember actor = clubMember(99L, ClubMemberStatus.STAFF);
        ClubMember clubMember = clubMember(1L, ClubMemberStatus.KICKED);

        assertThatThrownBy(() -> clubMember.kickBy(actor, CHANGED_AT))
                .isInstanceOfSatisfying(ClubManagementException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClubManagementErrorStatus.CLUB_MEMBER_INVALID_STATUS)
                );
    }

    @Test
    void 비운영진은_회원을_강퇴할_수_없다() {
        ClubMember actor = clubMember(99L, ClubMemberStatus.MEMBER);
        ClubMember clubMember = clubMember(1L, ClubMemberStatus.OWNER);

        assertThatThrownBy(() -> clubMember.kickBy(actor, CHANGED_AT))
                .isInstanceOfSatisfying(ClubManagementException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClubManagementErrorStatus.CLUB_STAFF_ONLY)
                );
    }

    private ClubMember clubMember(Long id, ClubMemberStatus status) {
        return ClubMember.builder()
                .id(id)
                .memberId("member-" + id)
                .club(club(1L))
                .clubMemberStatus(status)
                .appliedAt(APPLIED_AT)
                .joinedAt(status.isActive() ? APPLIED_AT : null)
                .build();
    }

    private Club club(Long id) {
        return Club.builder()
                .id(id)
                .name("club-" + id)
                .build();
    }
}
