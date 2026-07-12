package checkmo.clubMeeting.internal.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MeetingTest {

    @Test
    void 요청한_팀번호에_맞춰_기존_팀을_유지하고_새_팀을_추가하며_빠진_팀을_제거한다() {
        Meeting meeting = Meeting.builder().clubId(1L).bookId("book").build();
        Team removed = team(meeting, 1, 10L);
        Team retained = team(meeting, 2, 20L);
        Map<Integer, List<Long>> requestedMembersByTeamNumber = new LinkedHashMap<>();
        requestedMembersByTeamNumber.put(2, List.of(21L, 22L));
        requestedMembersByTeamNumber.put(3, List.of(31L));

        meeting.reconfigureTeams(List.of(removed, retained), requestedMembersByTeamNumber);

        Team created = findTeam(meeting, 3);
        assertSoftly(softly -> {
            softly.assertThat(meeting.getTeams()).containsExactlyInAnyOrder(retained, created);
            softly.assertThat(findTeam(meeting, 2)).isSameAs(retained);
            softly.assertThat(removed.getMeeting()).isNull();
            softly.assertThat(created.getMeeting()).isSameAs(meeting);
            softly.assertThat(retained.getClubMemberTeams())
                    .extracting(ClubMemberTeam::getClubMemberId)
                    .containsExactly(21L, 22L);
            softly.assertThat(created.getClubMemberTeams())
                    .extracting(ClubMemberTeam::getClubMemberId)
                    .containsExactly(31L);
        });
        assertThat(retained.getClubMemberTeams())
                .allSatisfy(member -> assertThat(member.getTeam()).isSameAs(retained));
        assertThat(created.getClubMemberTeams())
                .allSatisfy(member -> assertThat(member.getTeam()).isSameAs(created));
    }

    private Team team(Meeting meeting, int teamNumber, Long... clubMemberIds) {
        Team team = Team.builder().teamNumber(teamNumber).build();
        meeting.addTeam(team);
        team.replaceMembers(List.of(clubMemberIds));
        return team;
    }

    private Team findTeam(Meeting meeting, int teamNumber) {
        return meeting.getTeams().stream()
                .filter(team -> team.getTeamNumber() == teamNumber)
                .findFirst()
                .orElseThrow();
    }
}
