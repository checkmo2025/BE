package checkmo.clubMeeting.internal.validation.validSize;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = SizeValidator.class)
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSize {
    String message() default "SIZE_POSITIVE";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
