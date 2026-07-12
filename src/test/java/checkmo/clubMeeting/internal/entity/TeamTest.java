package checkmo.clubMeeting.internal.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.hibernate.annotations.BatchSize;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TeamTest {

    @ParameterizedTest
    @ValueSource(strings = {"clubMemberTeams", "teamTopics"})
    void 자식_컬렉션은_팀_최대_개수_단위로_batch_fetch한다(String fieldName) throws NoSuchFieldException {
        BatchSize batchSize = Team.class.getDeclaredField(fieldName).getAnnotation(BatchSize.class);

        assertThat(batchSize).isNotNull();
        assertThat(batchSize.size()).isEqualTo(12);
    }

    @Test
    void 기존_팀원을_요청_순서대로_새_팀원으로_교체하고_역방향_연관을_연결한다() {
        Team team = Team.builder().teamNumber(1).build();
        team.replaceMembers(List.of(10L, 20L));
        ClubMemberTeam removedFirst = team.getClubMemberTeams().get(0);
        ClubMemberTeam removedSecond = team.getClubMemberTeams().get(1);

        team.replaceMembers(List.of(30L, 40L));

        assertThat(team.getClubMemberTeams())
                .extracting(ClubMemberTeam::getClubMemberId)
                .containsExactly(30L, 40L);
        assertThat(team.getClubMemberTeams())
                .doesNotContain(removedFirst, removedSecond)
                .allSatisfy(member -> assertThat(member.getTeam()).isSameAs(team));
    }

    @Test
    void 빈_목록으로_교체하면_모든_팀원을_제거한다() {
        Team team = Team.builder().teamNumber(1).build();
        team.replaceMembers(List.of(10L, 20L));

        team.replaceMembers(List.of());

        assertThat(team.getClubMemberTeams()).isEmpty();
    }

    @Test
    void 같은_팀원_ID를_다른_순서로_요청하면_기존_객체와_목록_순서를_유지한다() {
        Team team = Team.builder().teamNumber(1).build();
        team.replaceMembers(List.of(10L, 20L));
        ClubMemberTeam first = team.getClubMemberTeams().get(0);
        ClubMemberTeam second = team.getClubMemberTeams().get(1);

        team.replaceMembers(List.of(20L, 10L));

        assertThat(team.getClubMemberTeams()).containsExactly(first, second);
        assertThat(team.getClubMemberTeams().get(0)).isSameAs(first);
        assertThat(team.getClubMemberTeams().get(1)).isSameAs(second);
    }

    @Test
    void 중복_ID의_개수가_다르면_같은_팀원으로_판단하지_않는다() {
        Team team = Team.builder().teamNumber(1).build();
        team.replaceMembers(List.of(10L, 10L));
        ClubMemberTeam duplicateFirst = team.getClubMemberTeams().get(0);
        ClubMemberTeam duplicateSecond = team.getClubMemberTeams().get(1);

        team.replaceMembers(List.of(10L, 20L));

        assertThat(team.getClubMemberTeams())
                .extracting(ClubMemberTeam::getClubMemberId)
                .containsExactly(10L, 20L);
        assertThat(team.getClubMemberTeams()).doesNotContain(duplicateFirst, duplicateSecond);
    }
}
