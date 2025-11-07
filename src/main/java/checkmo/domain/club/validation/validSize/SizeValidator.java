package checkmo.domain.club.validation.validSize;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

import static checkmo.common.apiPayload.code.status.ErrorStatus.SIZE_POSITIVE;

@Component
public class SizeValidator implements ConstraintValidator<ValidSize, Integer> {
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) return true;

        if (value <= 0) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(SIZE_POSITIVE.name())
                    .addConstraintViolation();
            return false;
        }

        return true;
    }

    @Override
    public void initialize(ValidSize constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }
}
