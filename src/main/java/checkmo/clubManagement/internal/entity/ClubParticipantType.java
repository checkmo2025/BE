package checkmo.clubManagement.internal.entity;

public enum ClubParticipantType {
    STUDENT("대학생"),
    WORKER("직장인"),
    ONLINE("온라인"),
    CLUB("동아리"),
    MEETING("모임"),
    OFFLINE("대면");

    private final String description;

    ClubParticipantType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
