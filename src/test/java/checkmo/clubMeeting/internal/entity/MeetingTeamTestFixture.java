package checkmo.clubMeeting.internal.entity;

import java.util.Arrays;
import java.util.List;

public final class MeetingTeamTestFixture {

    private MeetingTeamTestFixture() {
    }

    public static Team addTeam(Meeting meeting, int teamNumber, Long... clubMemberIds) {
        Team team = Team.builder().teamNumber(teamNumber).build();
        team.setMeeting(meeting);
        team.replaceMembers(Arrays.asList(clubMemberIds));
        return team;
    }

    public static List<Team> teamsOf(Meeting meeting) {
        return List.copyOf(meeting.getTeams());
    }

    public static Team findTeam(Meeting meeting, int teamNumber) {
        return meeting.getTeams().stream()
                .filter(team -> team.getTeamNumber() == teamNumber)
                .findFirst()
                .orElseThrow();
    }
}
