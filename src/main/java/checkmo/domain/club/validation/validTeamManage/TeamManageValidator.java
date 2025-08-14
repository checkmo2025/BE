package checkmo.domain.club.validation.validTeamManage;

import checkmo.domain.club.web.dto.meeting.MeetingRequestDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TeamManageValidator implements ConstraintValidator<ValidTeamManage, MeetingRequestDTO.TeamManageDTO> {
    @Override
    public void initialize(ValidTeamManage constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(MeetingRequestDTO.TeamManageDTO value, ConstraintValidatorContext context) {
        if (value == null) return true; // @NotNull로 따로 처리

        List<MeetingRequestDTO.TeamMemberDTO> teamMemberDTOList = value.getTeamMemberDTOList();
        if (teamMemberDTOList == null) return true; // @NotNull로 따로 처리

        boolean success = true;

        Set<Integer> seenTeamNumbers = new HashSet<>(); // 팀 번호 중복 체크용
        Set<String> seenNicknames = new HashSet<>();

        for (int i = 0; i < teamMemberDTOList.size(); i++) {
            MeetingRequestDTO.TeamMemberDTO dto = teamMemberDTOList.get(i);
            if (dto == null) continue;

            Integer teamNum = dto.getTeamNumber();

            if (teamNum != null && !seenTeamNumbers.add(teamNum)) {
                success = false;
                addViolation(context,
                        String.format("요청 teamNumber'%d'가 중복되었습니다.", teamNum),
                        "teamMemberDTOList[" + i + "].teamNumber");
            }

            List<String> nicknameList = dto.getNicknameList();
            if (nicknameList != null) {
                for (int j = 0; j < nicknameList.size(); j++) {
                    String nick = (nicknameList.get(j) == null) ? null : nicknameList.get(j).trim(); // 필드 레벨 @NotBlank/@Pattern로 1차 검증됨
                    if (nick != null && !seenNicknames.add(nick)) {
                        success = false;
                        addViolation(
                                context,
                                String.format("닉네임 '%s'이 여러 개 포함되었습니다.", nick),
                                "teamMemberDTOList[" + i + "].nicknameList[" + j + "]"
                        );
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

