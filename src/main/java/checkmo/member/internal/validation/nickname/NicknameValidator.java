package checkmo.member.internal.validation.nickname;

import checkmo.common.nickname.NicknamePolicy;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NicknameValidator implements ConstraintValidator<ValidNickname, String> {

    private boolean required;

    @Override
    public void initialize(ValidNickname constraintAnnotation) {
        required = constraintAnnotation.required();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        NicknamePolicy.ValidationError validationError = NicknamePolicy.validate(value, required);
        if (validationError == NicknamePolicy.ValidationError.NONE) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(validationError.getMessage())
                .addConstraintViolation();
        return false;
    }
}
