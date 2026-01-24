package checkmo.clubMeeting.internal.validation.validTeamManage;

import checkmo.clubMeeting.web.dto.meeting.MeetingRequestDTO.TeamManage;
import checkmo.clubMeeting.web.dto.meeting.MeetingRequestDTO.TeamMember;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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
        Set<Long> seenClubMemberIds = new HashSet<>();

        for (int i = 0; i < teamMemberList.size(); i++) {
            TeamMember dto = teamMemberList.get(i);
            if (dto == null) {
                continue;
            }

            Integer teamNum = dto.getTeamNumber();
            if (teamNum != null) {
                // 1) 팀 번호 범위 검사
                if (teamNum < 1 || teamNum > MAX_TEAM_NUMBER) {
                    success = false;
                    addViolation(context,
                            String.format("요청 teamNumber'%d'는 %d~%d 범위여야 합니다.", teamNum, 1, MAX_TEAM_NUMBER),
                            "teamMemberList[" + i + "].teamNumber");
                }
                // 2) 팀 번호 중복 검사
                if (!seenTeamNumbers.add(teamNum)) {
                    success = false;
                    addViolation(context,
                            String.format("요청 teamNumber'%d'가 중복되었습니다.", teamNum),
                            "teamMemberList[" + i + "].teamNumber");
                }
                /*
                // TODO: ConstraintViolationException의 PropertyPath를 포함해서 에러를 응답하도록 Handler 수정 필요
                context.buildConstraintViolationWithTemplate(
                            String.format("팀 번호 '%d'가 중복되었습니다.", teamNum))
                    .addPropertyNode("teamMemberList").inIterable().atIndex(i)
                    .addPropertyNode("teamNumber")
                    .addConstraintViolation();
                */
            }

            List<Long> clubMemberIds = dto.getClubMemberIds();
            if (clubMemberIds == null) {
                continue;
            }

            // 3) 조 당 최대 인원 수
            List<Long> distinct = clubMemberIds.stream().filter(Objects::nonNull).distinct().toList();
            if (distinct.size() > MAX_MEMBERS_PER_TEAM) {
                success = false;
                addViolation(context,
                        String.format("팀 번호 '%d'의 클럽멤버는 최대 %d명까지 허용됩니다.", teamNum, MAX_MEMBERS_PER_TEAM),
                        "teamMemberList[" + i + "].clubMemberIds"
                );
            }
            for (int j = 0; j < distinct.size(); j++) {
                Long clubMemberId = distinct.get(j);
                if (!seenClubMemberIds.add(clubMemberId)) {
                    success = false;
                    addViolation(
                            context,
                            String.format("클럽멤버 ID '%d'가 중복되었습니다.", clubMemberId),
                            "teamMemberList[" + i + "].clubMemberIds[" + j + "]"
                    );
                        /*
                        context.buildConstraintViolationWithTemplate(
                                        String.format("닉네임 '%s'이 여러 개 포함되었습니다.", nick))
                                .addPropertyNode("teamMemberList").inIterable().atIndex(i)
                                .addPropertyNode("nicknameList").inIterable().atIndex(j)
                                .addConstraintViolation();
                         */
                }
            }
        }
        return success;
    }

    private void addViolation(ConstraintValidatorContext context, String message, String propertyPath) {
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(propertyPath)
                .addConstraintViolation();
    }
}