package checkmo.common.validation;

import java.text.Normalizer;
import java.util.Locale;

public final class NicknamePolicy {

    public static final int MAX_LENGTH = 20;
    public static final String ALLOWED_SPECIAL_CHARACTERS = "!@#$%^&*()_+-=[]{};':\"\\|,.<>/?";

    private NicknamePolicy() {
    }

    public static String normalize(String nickname) {
        if (nickname == null) {
            return null;
        }
        return Normalizer.normalize(nickname, Normalizer.Form.NFC);
    }

    public static String comparisonKey(String nickname) {
        String normalized = normalize(nickname);
        if (normalized == null) {
            return null;
        }
        return normalized.toLowerCase(Locale.ROOT);
    }

    public static boolean isSameIdentity(String first, String second) {
        String firstKey = comparisonKey(first);
        return firstKey != null && !firstKey.isEmpty() && firstKey.equals(comparisonKey(second));
    }

    public static ValidationError validate(String nickname, boolean required) {
        if (nickname == null || nickname.isEmpty()) {
            return required ? ValidationError.REQUIRED : ValidationError.NONE;
        }

        String normalized = normalize(nickname);
        if (normalized.codePoints().anyMatch(NicknamePolicy::isWhitespace)) {
            return ValidationError.WHITESPACE;
        }
        if (normalized.codePointCount(0, normalized.length()) > MAX_LENGTH) {
            return ValidationError.TOO_LONG;
        }
        if (!normalized.codePoints().allMatch(NicknamePolicy::isAllowed)) {
            return ValidationError.INVALID_CHARACTER;
        }
        return ValidationError.NONE;
    }

    private static boolean isWhitespace(int codePoint) {
        return Character.isWhitespace(codePoint) || Character.isSpaceChar(codePoint);
    }

    private static boolean isAllowed(int codePoint) {
        return isHangulSyllable(codePoint)
                || isHangulCompatibilityJamo(codePoint)
                || isAsciiLetter(codePoint)
                || isAsciiDigit(codePoint)
                || ALLOWED_SPECIAL_CHARACTERS.indexOf(codePoint) >= 0;
    }

    private static boolean isHangulSyllable(int codePoint) {
        return codePoint >= '\uAC00' && codePoint <= '\uD7A3';
    }

    private static boolean isHangulCompatibilityJamo(int codePoint) {
        return (codePoint >= '\u3131' && codePoint <= '\u314E')
                || (codePoint >= '\u314F' && codePoint <= '\u3163');
    }

    private static boolean isAsciiLetter(int codePoint) {
        return (codePoint >= 'A' && codePoint <= 'Z')
                || (codePoint >= 'a' && codePoint <= 'z');
    }

    private static boolean isAsciiDigit(int codePoint) {
        return codePoint >= '0' && codePoint <= '9';
    }

    public enum ValidationError {
        NONE(""),
        REQUIRED("닉네임은 필수입니다."),
        WHITESPACE("닉네임에는 공백을 사용할 수 없습니다."),
        TOO_LONG("닉네임은 최대 20자까지 가능합니다."),
        INVALID_CHARACTER("닉네임은 한글, 영문, 숫자, 허용된 특수문자만 사용할 수 있습니다.");

        private final String message;

        ValidationError(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
