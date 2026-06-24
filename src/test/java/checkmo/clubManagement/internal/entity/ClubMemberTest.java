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
        ClubMember clubMember = ClubMember.apply(
                "member-1",
                ClubMemberStatus.MEMBER,
                "join",
                APPLIED_AT
        );

        assertThatThrownBy(() -> clubMember.reApply(ClubMemberStatus.MEMBER, "again", CHANGED_AT))
                .isInstanceOfSatisfying(ClubManagementException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClubManagementErrorStatus.CLUB_MEMBER_ALREADY_JOINED)
                );
    }

    @Test
    void 대기_회원은_가입_승인시_활성_회원이_된다() {
        ClubMember clubMember = ClubMember.apply(
                "member-1",
                ClubMemberStatus.PENDING,
                "join",
                APPLIED_AT
        );

        clubMember.join(CHANGED_AT);

        assertSoftly(softly -> {
            softly.assertThat(clubMember.getClubMemberStatus()).isEqualTo(ClubMemberStatus.MEMBER);
            softly.assertThat(clubMember.getJoinedAt()).isEqualTo(CHANGED_AT);
        });
    }

    @Test
    void 대기_상태가_아닌_회원은_가입_승인할_수_없다() {
        ClubMember clubMember = ClubMember.apply(
                "member-1",
                ClubMemberStatus.MEMBER,
                "join",
                APPLIED_AT
        );

        assertThatThrownBy(() -> clubMember.join(CHANGED_AT))
                .isInstanceOfSatisfying(ClubManagementException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClubManagementErrorStatus.CLUB_MEMBER_INVALID_STATUS)
                );
    }

    @Test
    void 클럽장은_탈퇴할_수_없다() {
        ClubMember clubMember = ClubMember.ownerOf("owner-1", APPLIED_AT);

        assertThatThrownBy(() -> clubMember.leave(CHANGED_AT))
                .isInstanceOfSatisfying(ClubManagementException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClubManagementErrorStatus.CLUB_OWNER_CANNOT_LEAVE)
                );
    }

    @Test
    void 비활성_회원은_탈퇴할_수_없다() {
        ClubMember clubMember = ClubMember.apply(
                "member-1",
                ClubMemberStatus.WITHDRAWN,
                "join",
                APPLIED_AT
        );

        assertThatThrownBy(() -> clubMember.leave(CHANGED_AT))
                .isInstanceOfSatisfying(ClubManagementException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClubManagementErrorStatus.CLUB_MEMBER_INVALID_STATUS)
                );
    }

    @Test
    void 클럽장은_강퇴할_수_없다() {
        ClubMember clubMember = ClubMember.ownerOf("owner-1", APPLIED_AT);

        assertThatThrownBy(() -> clubMember.kick(CHANGED_AT))
                .isInstanceOfSatisfying(ClubManagementException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClubManagementErrorStatus.CLUB_OWNER_CANNOT_BE_KICKED)
                );
    }

    @Test
    void 비활성_회원은_강퇴할_수_없다() {
        ClubMember clubMember = ClubMember.apply(
                "member-1",
                ClubMemberStatus.KICKED,
                "join",
                APPLIED_AT
        );

        assertThatThrownBy(() -> clubMember.kick(CHANGED_AT))
                .isInstanceOfSatisfying(ClubManagementException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClubManagementErrorStatus.CLUB_MEMBER_INVALID_STATUS)
                );
    }

    @Test
    void 자신의_역할은_변경할_수_없다() {
        ClubMember actor = clubMember(1L, ClubMemberStatus.STAFF);

        assertThatThrownBy(() -> actor.changeRoleBy(actor, ClubMemberStatus.MEMBER))
                .isInstanceOfSatisfying(ClubManagementException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClubManagementErrorStatus.CLUB_MEMBER_CANNOT_CHANGE_OWN_ROLE)
                );
    }

    @Test
    void 비활성_회원의_역할은_변경할_수_없다() {
        ClubMember actor = clubMember(1L, ClubMemberStatus.STAFF);
        ClubMember target = clubMember(2L, ClubMemberStatus.WITHDRAWN);

        assertThatThrownBy(() -> target.changeRoleBy(actor, ClubMemberStatus.STAFF))
                .isInstanceOfSatisfying(ClubManagementException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClubManagementErrorStatus.CLUB_MEMBER_IS_NOT_ACTIVE)
                );
    }

    @Test
    void 클럽장의_역할은_변경할_수_없다() {
        ClubMember actor = clubMember(1L, ClubMemberStatus.STAFF);
        ClubMember target = clubMember(2L, ClubMemberStatus.OWNER);

        assertThatThrownBy(() -> target.changeRoleBy(actor, ClubMemberStatus.STAFF))
                .isInstanceOfSatisfying(ClubManagementException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClubManagementErrorStatus.CLUB_OWNER_ROLE_CHANGE_NOT_ALLOWED)
                );
    }

    @Test
    void 활성_회원의_역할을_변경할_수_있다() {
        ClubMember actor = clubMember(1L, ClubMemberStatus.STAFF);
        ClubMember target = clubMember(2L, ClubMemberStatus.MEMBER);

        target.changeRoleBy(actor, ClubMemberStatus.STAFF);

        assertThat(target.getClubMemberStatus()).isEqualTo(ClubMemberStatus.STAFF);
    }

    private ClubMember clubMember(Long id, ClubMemberStatus status) {
        return ClubMember.builder()
                .id(id)
                .memberId("member-" + id)
                .clubMemberStatus(status)
                .appliedAt(APPLIED_AT)
                .joinedAt(status.isActive() ? APPLIED_AT : null)
                .build();
    }
}
