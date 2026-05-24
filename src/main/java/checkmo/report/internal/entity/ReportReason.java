package checkmo.report.internal.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportReason {

    GENERAL("일반"),
    INSULT("욕설/비방"),
    INAPPROPRIATE_CONTENT("음란/부적절"),
    SPAM("홍보/도배");

    private final String description;
}