package checkmo.clubMeeting.internal.validation.validCursor;

import checkmo.clubMeeting.internal.exception.ClubMeetingErrorStatus;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class CursorValidator implements ConstraintValidator<ValidCursor, Long> {
    @Override
    public boolean isValid(Long value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        if (value <= 0) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(ClubMeetingErrorStatus.CURSOR_ID_POSITIVE.name())
                    .addConstraintViolation();
            return false;
        }

        return true;
    }

    @Override
    public void initialize(ValidCursor constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }
}
