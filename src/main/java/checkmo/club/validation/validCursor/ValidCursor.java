package checkmo.club.validation.validCursor;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CursorValidator.class)
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCursor {
    String message() default "CURSOR_ID_POSITIVE";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
