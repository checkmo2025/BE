package checkmo.clubManagement.internal.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import checkmo.clubManagement.internal.excepetion.ClubManagementErrorStatus;
import checkmo.clubManagement.internal.excepetion.ClubManagementException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ClubTest {

    private static final LocalDateTime APPLIED_AT = LocalDateTime.of(2026, 1, 10, 9, 0);

    @Test
    void 공개_클럽에_가입을_신청하면_활성_회원으로_추가된다() {
        Club club = club(1L, true);

        ClubMember clubMember = club.applyMember("member-1", "join", APPLIED_AT);

        assertSoftly(softly -> {
            softly.assertThat(clubMember.getClubMemberStatus()).isEqualTo(ClubMemberStatus.MEMBER);
            softly.assertThat(clubMember.getJoinedAt()).isEqualTo(APPLIED_AT);
            softly.assertThat(clubMember.getClub()).isSameAs(club);
            softly.assertThat(club.getClubMembers()).containsExactly(clubMember);
        });
    }

    @Test
    void 비공개_클럽에_가입을_신청하면_대기_회원으로_추가된다() {
        Club club = club(1L, false);

        ClubMember clubMember = club.applyMember("member-1", "join", APPLIED_AT);

        assertSoftly(softly -> {
            softly.assertThat(clubMember.getClubMemberStatus()).isEqualTo(ClubMemberStatus.PENDING);
            softly.assertThat(clubMember.getJoinedAt()).isNull();
            softly.assertThat(clubMember.getClub()).isSameAs(club);
            softly.assertThat(club.getClubMembers()).containsExactly(clubMember);
        });
    }

    @Test
    void 다른_클럽의_회원은_제거할_수_없다() {
        Club club = club(1L, true);
        Club anotherClub = club(2L, true);
        ClubMember clubMember = anotherClub.applyMember("member-1", "join", APPLIED_AT);

        assertThatThrownBy(() -> club.removeMember(clubMember))
                .isInstanceOfSatisfying(ClubManagementException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClubManagementErrorStatus.CLUB_MEMBER_NOT_IN_CLUB)
                );
    }

    @Test
    void 다른_클럽의_회원은_재신청할_수_없다() {
        Club club = club(1L, true);
        Club anotherClub = club(2L, true);
        ClubMember clubMember = anotherClub.applyMember("member-1", "join", APPLIED_AT);
        clubMember.leave(APPLIED_AT);

        assertThatThrownBy(() -> club.reApplyMember(clubMember, "again", APPLIED_AT))
                .isInstanceOfSatisfying(ClubManagementException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClubManagementErrorStatus.CLUB_MEMBER_NOT_IN_CLUB)
                );
    }

    private Club club(Long id, boolean open) {
        return Club.builder()
                .id(id)
                .name("club-" + id)
                .isOpen(open)
                .build();
    }
}
