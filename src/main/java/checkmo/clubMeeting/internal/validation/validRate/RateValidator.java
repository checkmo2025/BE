package checkmo.clubMeeting.internal.validation.validRate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class RateValidator implements ConstraintValidator<ValidRate, Double> {
    @Override
    public boolean isValid(Double value, ConstraintValidatorContext context) {
        if (value == null) return true; // null은 @NotNull이 따로 처리

        // 1. 범위 검증
        if (value < 1.0 || value > 5.0) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("평점은 1.0 이상 5.0 이하만 가능합니다.")
                    .addConstraintViolation();
            return false;
        }

        // 2. 0.5 단위 검증
        if ((value * 10) % 5 != 0) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("평점은 0.5 단위로만 입력 가능합니다.")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }

    @Override
    public void initialize(ValidRate constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }
}
