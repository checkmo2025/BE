package checkmo.common.nickname;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;

class NicknamePolicyTest {

    @Test
    void 한글_영문_대소문자_숫자와_허용된_특수문자를_사용할_수_있다() {
        assertThat(NicknamePolicy.validate("책모", true)).isEqualTo(NicknamePolicy.ValidationError.NONE);
        assertThat(NicknamePolicy.validate("ㅋㅋ책모", true)).isEqualTo(NicknamePolicy.ValidationError.NONE);
        assertThat(NicknamePolicy.validate("BookMo123", true)).isEqualTo(NicknamePolicy.ValidationError.NONE);
        assertThat(NicknamePolicy.ALLOWED_SPECIAL_CHARACTERS.codePoints()
                .mapToObj(codePoint -> Character.toString(codePoint))
                .toList())
                .allSatisfy(specialCharacter -> assertThat(NicknamePolicy.validate(specialCharacter, true))
                        .isEqualTo(NicknamePolicy.ValidationError.NONE));
    }

    @Test
    void 백틱과_물결표_이모지와_그_밖의_문자를_거부한다() {
        assertThat(NicknamePolicy.validate("책`모", true))
                .isEqualTo(NicknamePolicy.ValidationError.INVALID_CHARACTER);
        assertThat(NicknamePolicy.validate("책~모", true))
                .isEqualTo(NicknamePolicy.ValidationError.INVALID_CHARACTER);
        assertThat(NicknamePolicy.validate("책📚모", true))
                .isEqualTo(NicknamePolicy.ValidationError.INVALID_CHARACTER);
        assertThat(NicknamePolicy.validate("책モ", true))
                .isEqualTo(NicknamePolicy.ValidationError.INVALID_CHARACTER);
    }

    @Test
    void 모든_위치와_종류의_공백을_거부한다() {
        Set<String> nicknamesWithWhitespace = Set.of(
                " 책모",
                "책모 ",
                "책 모",
                "책\t모",
                "책\n모",
                "책\u00A0모"
        );

        assertThat(nicknamesWithWhitespace)
                .allSatisfy(nickname -> assertThat(NicknamePolicy.validate(nickname, true))
                        .isEqualTo(NicknamePolicy.ValidationError.WHITESPACE));
    }

    @Test
    void NFC_정규화한_결과를_기준으로_길이와_문자를_검증한다() {
        String decomposedHangul = "\u1100\u1161".repeat(20);

        assertThat(decomposedHangul).hasSize(40);
        assertThat(NicknamePolicy.normalize(decomposedHangul)).isEqualTo("가".repeat(20));
        assertThat(NicknamePolicy.validate(decomposedHangul, true))
                .isEqualTo(NicknamePolicy.ValidationError.NONE);
        assertThat(NicknamePolicy.validate("가".repeat(21), true))
                .isEqualTo(NicknamePolicy.ValidationError.TOO_LONG);
    }

    @Test
    void 비교값은_NFC_정규화하고_영문_대소문자를_구분하지_않는다() {
        assertThat(NicknamePolicy.comparisonKey("BookMo")).isEqualTo("bookmo");
        assertThat(NicknamePolicy.comparisonKey("책모ABC")).isEqualTo("책모abc");
        assertThat(NicknamePolicy.isSameIdentity("BookMo", "bookMO")).isTrue();
        assertThat(NicknamePolicy.isSameIdentity("\u1100\u1161", "가")).isTrue();
        assertThat(NicknamePolicy.isSameIdentity(null, null)).isFalse();
        assertThat(NicknamePolicy.isSameIdentity("", "")).isFalse();
    }

    @Test
    void 선택값은_null과_빈문자열을_허용하고_필수값은_거부한다() {
        assertThat(NicknamePolicy.validate(null, false)).isEqualTo(NicknamePolicy.ValidationError.NONE);
        assertThat(NicknamePolicy.validate("", false)).isEqualTo(NicknamePolicy.ValidationError.NONE);
        assertThat(NicknamePolicy.validate(null, true)).isEqualTo(NicknamePolicy.ValidationError.REQUIRED);
        assertThat(NicknamePolicy.validate("", true)).isEqualTo(NicknamePolicy.ValidationError.REQUIRED);
    }
}
