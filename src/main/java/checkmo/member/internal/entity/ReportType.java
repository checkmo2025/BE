package checkmo.member.internal.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportType {

    GENERAL("일반"),
    CLUB_MEETING("모임내부"),
    BOOK_STORY("책이야기"),
    COMMENT("댓글");

    private final String description;
}