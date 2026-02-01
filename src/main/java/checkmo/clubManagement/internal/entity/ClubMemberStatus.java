package checkmo.clubManagement.internal.entity;

import java.util.EnumSet;

public enum ClubMemberStatus {
    MEMBER, STAFF, OWNER,
    PENDING,
    WITHDRAWN, KICKED;

    public static EnumSet<ClubMemberStatus> activeStatuses() {
        return EnumSet.of(MEMBER, STAFF, OWNER);
    }

    public boolean isActive() {
        return this == MEMBER || this == STAFF || this == OWNER;
    }

    public boolean isStaff() {
        return this == STAFF || this == OWNER;
    }

    public boolean isOwner() {
        return this == OWNER;
    }

    public boolean isJoinInProgress() {
        return this == PENDING;
    }

}
