package checkmo.clubMeeting.internal.validation.validTeamManage;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TeamManageValidator.class)
public @interface ValidTeamManage {
    String message() default "잘못된 팀 배정 요청입니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}