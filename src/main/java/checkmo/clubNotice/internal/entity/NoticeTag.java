package checkmo.clubNotice.internal.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NoticeTag {
    NOTICE("공지"),
    MEETING("모임"),
    VOTE("투표");

    private final String displayName;

    public boolean isNotice() {
        return this == NOTICE;
    }

    public boolean isMeeting() {
        return this == MEETING;
    }

    public boolean isVote() {
        return this == VOTE;
    }
}