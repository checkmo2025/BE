package checkmo.book.internal;

import java.util.regex.Pattern;

public final class RecommendationLogSanitizer {

    private static final int LOG_MESSAGE_MAX_LENGTH = 500;
    private static final Pattern URL_QUERY_PARAMETER_PATTERN = Pattern.compile("([?&][^=&#\\s]+)=([^&#\\s]*)");
    private static final String SENSITIVE_FIELD_NAMES =
            "ttbkey|query|keyword|auth|authorization|cookie|jwt|accessToken|access_token|refreshToken|"
                    + "refresh_token|key|secret|password|verification(?:[-_]?|\\s+)code";
    private static final Pattern QUOTED_SENSITIVE_LOG_FIELD_PATTERN = Pattern.compile(
            "(?i)(\"?\\b(?:" + SENSITIVE_FIELD_NAMES + ")\\b\"?\\s*[:=]\\s*\")(?:Bearer\\s+)?[^\"]*(\")"
    );
    private static final Pattern UNTERMINATED_QUOTED_SENSITIVE_LOG_FIELD_PATTERN = Pattern.compile(
            "(?i)(\"?\\b(?:" + SENSITIVE_FIELD_NAMES + ")\\b\"?\\s*[:=]\\s*\")(?:Bearer\\s+)?[^\"\\r\\n]*?(?=(?:[,;]\\s*[^=:\\s]+\\s*[=:])|$)"
    );
    private static final Pattern SENSITIVE_LOG_FIELD_PATTERN = Pattern.compile(
            "(?i)(\"?\\b(?:" + SENSITIVE_FIELD_NAMES + ")\\b\"?\\s*[:=]\\s*)(?:Bearer\\s+)?[^\"&;,\\s]+"
    );

    private RecommendationLogSanitizer() {
    }

    public static String sanitize(String value) {
        if (value == null) {
            return null;
        }

        String sanitizedUrlParameters = URL_QUERY_PARAMETER_PATTERN.matcher(value).replaceAll("$1=***");
        String sanitizedQuotedFields = QUOTED_SENSITIVE_LOG_FIELD_PATTERN.matcher(sanitizedUrlParameters)
                .replaceAll("$1***$2");
        String sanitizedUnterminatedQuotedFields =
                UNTERMINATED_QUOTED_SENSITIVE_LOG_FIELD_PATTERN.matcher(sanitizedQuotedFields).replaceAll("$1***");
        String sanitizedFields = SENSITIVE_LOG_FIELD_PATTERN.matcher(sanitizedUnterminatedQuotedFields)
                .replaceAll("$1***");
        if (sanitizedFields.length() <= LOG_MESSAGE_MAX_LENGTH) {
            return sanitizedFields;
        }

        return sanitizedFields.substring(0, LOG_MESSAGE_MAX_LENGTH) + "...";
    }
}
