package checkmo.clubNotice.internal.entity;

public enum NoticeTag {
    GENERAL("일반"),
    MEETING("모임"),
    VOTE("투표"),
    VOTE_MEETING("투표와 모임"),
    ;

    private final String description;

    NoticeTag(String description) {
        this.description = description;
    }

    public static NoticeTag decideTag(boolean hasVote, boolean hasMeeting) {
        if (hasVote && hasMeeting) {
            return VOTE_MEETING;
        }
        if (hasVote) {
            return VOTE;
        }
        if (hasMeeting) {
            return MEETING;
        }
        return GENERAL;
    }

    public String getDescription() {
        return description;
    }

    public boolean isNotice() {
        return this == GENERAL;
    }

    public boolean isMeeting() {
        return this == MEETING || this == VOTE_MEETING;
    }

    public boolean isVote() {
        return this == VOTE || this == VOTE_MEETING;
    }
}