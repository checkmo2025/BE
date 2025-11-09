package checkmo.clubMeeting.validation.validTeamManage;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TeamManageValidator.class)
public @interface ValidTeamManage {
    String message() default "잘못된 팀 배정 요청입니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
