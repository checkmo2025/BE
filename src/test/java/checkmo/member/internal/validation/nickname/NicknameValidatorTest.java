package checkmo.member.internal.validation.nickname;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

class NicknameValidatorTest {

    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = VALIDATOR_FACTORY.getValidator();

    @AfterAll
    static void closeValidatorFactory() {
        VALIDATOR_FACTORY.close();
    }

    @Test
    void 닉네임_제약조건은_정책별_오류_문구를_반환한다() {
        assertThat(VALIDATOR.validate(new RequiredNickname("책 모")))
                .singleElement()
                .extracting(violation -> violation.getMessage())
                .isEqualTo("닉네임에는 공백을 사용할 수 없습니다.");
        assertThat(VALIDATOR.validate(new RequiredNickname("")))
                .singleElement()
                .extracting(violation -> violation.getMessage())
                .isEqualTo("닉네임은 필수입니다.");
    }

    private record RequiredNickname(@ValidNickname(required = true) String nickname) {
    }
}
