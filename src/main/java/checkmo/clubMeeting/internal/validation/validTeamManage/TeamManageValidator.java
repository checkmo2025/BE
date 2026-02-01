package checkmo.clubMeeting.internal.validation.validTeamManage;

import checkmo.clubMeeting.web.dto.meeting.MeetingRequestDTO.TeamManage;
import checkmo.clubMeeting.web.dto.meeting.MeetingRequestDTO.TeamMember;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TeamManageValidator implements ConstraintValidator<ValidTeamManage, TeamManage> {
    private static final int MAX_TEAM_NUMBER = 12;
    private static final int MAX_MEMBERS_PER_TEAM = 12;

    @Override
    public void initialize(ValidTeamManage constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(TeamManage value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        List<TeamMember> teamMemberList = value.getTeamMemberList();
        if (teamMemberList == null) {
            return true; // @NotNull로 따로 처리
        }

        boolean success = true;
        context.disableDefaultConstraintViolation();

        Set<Integer> seenTeamNumbers = new HashSet<>();
        Map<Long, IdLocation> firstLocationByClubMemberId = new HashMap<>();

        for (int i = 0; i < teamMemberList.size(); i++) {
            TeamMember dto = teamMemberList.get(i);
            if (dto == null) {
                continue;
            }

            Integer teamNum = dto.getTeamNumber();
            success &= validateTeamNumber(context, seenTeamNumbers, i, teamNum);
            success &= validateClubMemberIds(context, firstLocationByClubMemberId, i, teamNum, dto.getClubMemberIds());

        }
        return success;
    }

    /**
     * 팀 번호 유효성 검사 (범위 및 중복 여부)
     *
     * @param context         ConstraintViolation을 추가하기 위한 컨텍스트
     * @param seenTeamNumbers 이미 등장한 팀 번호를 기록하는 Set
     * @param teamIndex       요청 DTO teamMemberList에서 현재 teamNumber의 인덱스
     * @param teamNumber      검사할 팀 번호
     * @return 유효하면 true, 그렇지 않으면 false
     */
    private boolean validateTeamNumber(
            ConstraintValidatorContext context,
            Set<Integer> seenTeamNumbers,
            int teamIndex,
            Integer teamNumber
    ) {
        if (teamNumber == null) {
            return true;
        }
        boolean ok = true;

        // 1) 팀 번호 범위 검사
        if (teamNumber < 1 || teamNumber > MAX_TEAM_NUMBER) {
            ok = false;
            addViolation(context,
                    String.format("요청 teamNumber '%d'는 %d~%d 범위여야 합니다.", teamNumber, 1, MAX_TEAM_NUMBER),
                    pathTeamNumber(teamIndex));
        }

        // 2) 팀 번호 중복 검사
        if (!seenTeamNumbers.add(teamNumber)) {
            ok = false;
            addViolation(context,
                    String.format("요청 teamNumber '%d'가 중복되었습니다.", teamNumber),
                    pathTeamNumber(teamIndex));
        }
        return ok;
    }

    /**
     * 클럽멤버 ID 유효성 검사 (팀 내 중복, 팀 간 중복, 조당 최대 인원)
     *
     * @param context                     ConstraintViolation을 추가하기 위한 컨텍스트
     * @param firstLocationByClubMemberId 각 클럽멤버 ID의 최초 등장 위치를 기록하는 맵(클럽멤버ID -> 최초 등장 위치)
     * @param teamIndex                   요청 DTO teamMemberList에서 현재 teamNumber의 인덱스
     * @param teamNumber                  현재 팀 번호
     * @param clubMemberIds               검사할 클럽멤버 ID 리스트
     * @return 유효하면 true, 그렇지 않으면 false
     */
    private boolean validateClubMemberIds(
            ConstraintValidatorContext context,
            Map<Long, IdLocation> firstLocationByClubMemberId,
            int teamIndex,
            Integer teamNumber,
            List<Long> clubMemberIds
    ) {
        if (clubMemberIds == null) {
            return true;
        }
        boolean ok = true;

        Set<Long> seenInTeam = new HashSet<>();
        int distinctCount = 0;

        for (int memberIndex = 0; memberIndex < clubMemberIds.size(); memberIndex++) {
            Long clubMemberId = clubMemberIds.get(memberIndex);
            if (clubMemberId == null) {
                continue;
            }

            // 1) 팀 내부 중복: 같은 팀 리스트에서 중복 입력을 "원본 인덱스"로 정확히 찍음
            if (!seenInTeam.add(clubMemberId)) {
                ok = false;
                addViolation(context,
                        String.format("팀 번호 '%d'의 clubMemberIds에 ID '%d'가 중복되었습니다.", teamNumber, clubMemberId),
                        pathClubMemberId(teamIndex, memberIndex));
                continue; // 내부 중복은 아래 distinctCount/팀간중복 체크에서 제외
            }

            // 2) 조당 최대 인원: "중복 제외 기준"으로 세고, 초과한 원본 인덱스에 에러
            distinctCount++;
            if (distinctCount > MAX_MEMBERS_PER_TEAM) {
                ok = false;
                addViolation(context,
                        String.format("팀 번호 '%d'의 클럽멤버는 최대 %d명까지 허용됩니다.", teamNumber, MAX_MEMBERS_PER_TEAM),
                        pathClubMemberIds(teamIndex));
            }

            // 3) 팀 간 중복: 최초 등장 위치를 저장해두고, 재등장한 "현재 위치"를 정확히 찍음
            ok &= validateCrossTeamDuplicate(context, firstLocationByClubMemberId, teamIndex, memberIndex, teamNumber,
                    clubMemberId);
        }
        return ok;
    }

    /**
     * 클럽멤버 ID의 팀 간 중복 검사
     *
     * @param context                     ConstraintViolation을 추가하기 위한 컨텍스트
     * @param firstLocationByClubMemberId 각 클럽멤버 ID의 최초 등장 위치를 기록하는 맵(클럽멤버ID -> 최초 등장 위치)
     * @param teamIndex                   요청 DTO teamMemberList에서 현재 teamNumber의 인덱스
     * @param memberIndex                 요청 DTO teamMemberList[teamIndex]에서 현재 clubMemberId의 인덱스
     * @param teamNumber                  현재 팀 번호
     * @param clubMemberId                검사할 클럽멤버 ID
     * @return 유효하면 true, 그렇지 않으면 false
     */
    private boolean validateCrossTeamDuplicate(
            ConstraintValidatorContext context,
            Map<Long, IdLocation> firstLocationByClubMemberId,
            int teamIndex,
            int memberIndex,
            Integer teamNumber,
            Long clubMemberId
    ) {
        IdLocation first = firstLocationByClubMemberId.get(clubMemberId);
        if (first == null) {
            firstLocationByClubMemberId.put(clubMemberId, new IdLocation(teamIndex, memberIndex, teamNumber));
            return true;
        }

        // 같은 팀에서의 중복은 이미 팀 내부 중복에서 잡았으므로, 여기서는 팀이 다를 때만 처리
        if (first.teamIndex() == teamIndex) {
            return true;
        }

        addViolation(context,
                String.format("클럽멤버 ID '%d'가 여러 팀에 중복되었습니다. (처음: %s팀, 현재: %s팀)",
                        clubMemberId, teamLabel(first.teamNumber()), teamLabel(teamNumber)),
                pathClubMemberId(teamIndex, memberIndex));
        return false;
    }

    private String pathTeamNumber(int teamIndex) {
        return "teamMemberList[" + teamIndex + "].teamNumber";
    }

    private String pathClubMemberIds(int teamIndex) {
        return "teamMemberList[" + teamIndex + "].clubMemberIds";
    }

    private String pathClubMemberId(int teamIndex, int memberIndex) {
        return "teamMemberList[" + teamIndex + "].clubMemberIds[" + memberIndex + "]";
    }

    private String teamLabel(Integer teamNum) {
        return teamNum == null ? "알 수 없음" : String.valueOf(teamNum);
    }

    private void addViolation(ConstraintValidatorContext context, String message, String propertyPath) {
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(propertyPath)
                .addConstraintViolation();
    }

    private record IdLocation(int teamIndex, int memberIndex, Integer teamNumber) {
    }
}