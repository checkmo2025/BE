package checkmo.clubManagement.internal.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import checkmo.clubManagement.internal.excepetion.ClubManagementErrorStatus;
import checkmo.clubManagement.internal.excepetion.ClubManagementException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ClubTest {

    private static final LocalDateTime APPLIED_AT = LocalDateTime.of(2026, 1, 10, 9, 0);

    @Test
    void 클럽장을_추가하면_클럽을_참조하는_클럽장_회원이_생성된다() {
        Club club = club(1L, true);

        ClubMember owner = club.createOwnerMember("owner-1", APPLIED_AT);

        assertSoftly(softly -> {
            softly.assertThat(owner.getClubMemberStatus()).isEqualTo(ClubMemberStatus.OWNER);
            softly.assertThat(owner.getJoinedAt()).isEqualTo(APPLIED_AT);
            softly.assertThat(owner.getClub()).isSameAs(club);
        });
    }

    @Test
    void 공개_클럽에_가입을_신청하면_활성_회원으로_추가된다() {
        Club club = club(1L, true);

        ClubMember clubMember = club.applyForMembership("member-1", "join", APPLIED_AT);

        assertSoftly(softly -> {
            softly.assertThat(clubMember.getClubMemberStatus()).isEqualTo(ClubMemberStatus.MEMBER);
            softly.assertThat(clubMember.getJoinedAt()).isEqualTo(APPLIED_AT);
            softly.assertThat(clubMember.getClub()).isSameAs(club);
        });
    }

    @Test
    void 비공개_클럽에_가입을_신청하면_대기_회원으로_추가된다() {
        Club club = club(1L, false);

        ClubMember clubMember = club.applyForMembership("member-1", "join", APPLIED_AT);

        assertSoftly(softly -> {
            softly.assertThat(clubMember.getClubMemberStatus()).isEqualTo(ClubMemberStatus.PENDING);
            softly.assertThat(clubMember.getJoinedAt()).isNull();
            softly.assertThat(clubMember.getClub()).isSameAs(club);
        });
    }

    @Test
    void 대기_회원의_가입을_거절할_수_있다() {
        Club club = club(1L, false);
        ClubMember actor = clubMember(club, 99L, ClubMemberStatus.STAFF);
        ClubMember clubMember = club.applyForMembership("member-1", "join", APPLIED_AT);

        assertThatCode(() -> club.validateJoinRejection(actor, clubMember))
                .doesNotThrowAnyException();
    }

    @Test
    void 이미_가입된_회원은_가입을_거절할_수_없다() {
        Club club = club(1L, true);
        ClubMember actor = clubMember(club, 99L, ClubMemberStatus.STAFF);
        ClubMember clubMember = club.applyForMembership("member-1", "join", APPLIED_AT);

        assertThatThrownBy(() -> club.validateJoinRejection(actor, clubMember))
                .isInstanceOfSatisfying(ClubManagementException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClubManagementErrorStatus.CLUB_MEMBER_INVALID_STATUS)
                );
    }

    @Test
    void 비운영진은_가입을_거절할_수_없다() {
        Club club = club(1L, true);
        ClubMember actor = clubMember(club, 99L, ClubMemberStatus.MEMBER);
        ClubMember clubMember = club.applyForMembership("member-1", "join", APPLIED_AT);

        assertThatThrownBy(() -> club.validateJoinRejection(actor, clubMember))
                .isInstanceOfSatisfying(ClubManagementException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClubManagementErrorStatus.CLUB_STAFF_ONLY)
                );
    }

    @Test
    void 다른_클럽의_가입_요청은_거절할_수_없다() {
        Club club = club(1L, true);
        ClubMember actor = clubMember(club, 99L, ClubMemberStatus.STAFF);
        Club anotherClub = club(2L, false);
        ClubMember clubMember = anotherClub.applyForMembership("member-1", "join", APPLIED_AT);

        assertThatThrownBy(() -> club.validateJoinRejection(actor, clubMember))
                .isInstanceOfSatisfying(ClubManagementException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClubManagementErrorStatus.CLUB_MEMBER_NOT_IN_CLUB)
                );
    }

    @Test
    void 다른_클럽의_회원은_재신청할_수_없다() {
        Club club = club(1L, true);
        Club anotherClub = club(2L, true);
        ClubMember clubMember = anotherClub.applyForMembership("member-1", "join", APPLIED_AT);
        clubMember.leave(APPLIED_AT);

        assertThatThrownBy(() -> club.reApplyMember(clubMember, "again", APPLIED_AT))
                .isInstanceOfSatisfying(ClubManagementException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClubManagementErrorStatus.CLUB_MEMBER_NOT_IN_CLUB)
                );
    }

    @Test
    void 운영진이어도_클럽장이_아니면_소유권을_이전할_수_없다() {
        Club club = club(1L, true);
        ClubMember actor = clubMember(club, 1L, ClubMemberStatus.STAFF);
        ClubMember target = clubMember(club, 2L, ClubMemberStatus.MEMBER);

        assertThatThrownBy(() -> club.transferOwnerBy(actor, target))
                .isInstanceOfSatisfying(ClubManagementException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClubManagementErrorStatus.CLUB_OWNER_ONLY)
                );
    }

    @Test
    void 클럽장이_아닌_회원은_소유권을_이전할_수_없다() {
        Club club = club(1L, true);
        ClubMember actor = clubMember(club, 1L, ClubMemberStatus.MEMBER);
        ClubMember target = clubMember(club, 2L, ClubMemberStatus.WITHDRAWN);

        assertThatThrownBy(() -> club.transferOwnerBy(actor, target))
                .isInstanceOfSatisfying(ClubManagementException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClubManagementErrorStatus.CLUB_OWNER_ONLY)
                );
    }

    @Test
    void 비활성_회원에게_소유권을_이전할_수_없다() {
        Club club = club(1L, true);
        ClubMember actor = clubMember(club, 1L, ClubMemberStatus.OWNER);
        ClubMember target = clubMember(club, 2L, ClubMemberStatus.WITHDRAWN);

        assertThatThrownBy(() -> club.transferOwnerBy(actor, target))
                .isInstanceOfSatisfying(ClubManagementException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClubManagementErrorStatus.CLUB_MEMBER_IS_NOT_ACTIVE)
                );
    }

    @Test
    void 대상이_이미_클럽장이면_소유권_이전은_아무것도_하지_않는다() {
        Club club = club(1L, true);
        ClubMember actor = clubMember(club, 1L, ClubMemberStatus.OWNER);
        ClubMember target = clubMember(club, 2L, ClubMemberStatus.OWNER);

        boolean transferred = club.transferOwnerBy(actor, target);

        assertSoftly(softly -> {
            softly.assertThat(transferred).isFalse();
            softly.assertThat(actor.getClubMemberStatus()).isEqualTo(ClubMemberStatus.OWNER);
            softly.assertThat(target.getClubMemberStatus()).isEqualTo(ClubMemberStatus.OWNER);
        });
    }

    @Test
    void 클럽장이_활성_회원에게_소유권을_이전할_수_있다() {
        Club club = club(1L, true);
        ClubMember actor = clubMember(club, 1L, ClubMemberStatus.OWNER);
        ClubMember target = clubMember(club, 2L, ClubMemberStatus.MEMBER);

        boolean transferred = club.transferOwnerBy(actor, target);

        assertSoftly(softly -> {
            softly.assertThat(transferred).isTrue();
            softly.assertThat(actor.getClubMemberStatus()).isEqualTo(ClubMemberStatus.STAFF);
            softly.assertThat(target.getClubMemberStatus()).isEqualTo(ClubMemberStatus.OWNER);
        });
    }

    private Club club(Long id, boolean open) {
        return Club.builder()
                .id(id)
                .name("club-" + id)
                .isOpen(open)
                .build();
    }

    private ClubMember clubMember(Club club, Long id, ClubMemberStatus status) {
        return ClubMember.builder()
                .id(id)
                .club(club)
                .memberId("member-" + id)
                .clubMemberStatus(status)
                .appliedAt(APPLIED_AT)
                .joinedAt(status.isActive() ? APPLIED_AT : null)
                .build();
    }
}
