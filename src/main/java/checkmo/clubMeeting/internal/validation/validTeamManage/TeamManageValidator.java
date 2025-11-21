package checkmo.clubMeeting.internal.validation.validTeamManage;

import checkmo.clubMeeting.web.dto.meeting.MeetingRequestDTO.TeamManage;
import checkmo.clubMeeting.web.dto.meeting.MeetingRequestDTO.TeamMember;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TeamManageValidator implements ConstraintValidator<ValidTeamManage, TeamManage> {
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

            if (teamNum != null && !seenTeamNumbers.add(teamNum)) {
                success = false;
                addViolation(context,
                        String.format("요청 teamNumber'%d'가 중복되었습니다.", teamNum),
                        "teamMemberDTOList[" + i + "].teamNumber");
                /*
                // TODO: ConstraintViolationException의 PropertyPath를 포함해서 에러를 응답하도록 Handler 수정 필요
                context.buildConstraintViolationWithTemplate(
                            String.format("팀 번호 '%d'가 중복되었습니다.", teamNum))
                    .addPropertyNode("teamMemberDTOList").inIterable().atIndex(i)
                    .addPropertyNode("teamNumber")
                    .addConstraintViolation();
                */
            }

            List<Long> clubMemberIds = dto.getClubMemberIds();
            if (clubMemberIds != null) {
                for (int j = 0; j < clubMemberIds.size(); j++) {
                    Long clubMemberId = clubMemberIds.get(j);
                    if (clubMemberId != null && !seenClubMemberIds.add(clubMemberId)) {
                        success = false;
                        addViolation(
                                context,
                                String.format("클럽멤버 ID '%d'가 중복되었습니다.", clubMemberId),
                                "teamMemberDTOList[" + i + "].clubMemberIds[" + j + "]"
                        );
                        /*
                        context.buildConstraintViolationWithTemplate(
                                        String.format("닉네임 '%s'이 여러 개 포함되었습니다.", nick))
                                .addPropertyNode("teamMemberDTOList").inIterable().atIndex(i)
                                .addPropertyNode("nicknameList").inIterable().atIndex(j)
                                .addConstraintViolation();
                         */
                    }
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