package checkmo.report.internal.entity;

import lombok.Getter;

import java.util.Map;

@Getter
public enum ReportTargetType {

    MEMBER("사용자") {
        @Override
        public String createRedirectUrl(String targetId, Map<String, String> context) {
            return "/api/members/" + targetId;
        }
    },

    CLUB("독서모임") {
        @Override
        public String createRedirectUrl(String targetId, Map<String, String> context) {
            return "/api/clubs/" + targetId + "/home";
        }
    },

    BOOK_STORY("책 이야기") {
        @Override
        public String createRedirectUrl(String targetId, Map<String, String> context) {
            return "/api/book-stories/" + targetId;
        }
    },

    BOOK_STORY_COMMENT("책 이야기 댓글") {
        @Override
        public String createRedirectUrl(String targetId, Map<String, String> context) {
            return "/api/book-stories/" + context.get("bookStoryId");
        }
    },

    CLUB_NOTICE("독서모임 공지사항") {
        @Override
        public String createRedirectUrl(String targetId, Map<String, String> context) {
            return "/api/clubs/" + context.get("clubId") + "/notices/" + targetId;
        }
    },

    CLUB_NOTICE_COMMENT("독서모임 공지사항 댓글") {
        @Override
        public String createRedirectUrl(String targetId, Map<String, String> context) {
            return "/api/clubs/" + context.get("clubId") + "/notices/" + context.get("noticeId");
        }
    },

    CLUB_TOPIC("독서모임 발제") {
        @Override
        public String createRedirectUrl(String targetId, Map<String, String> context) {
            return "/api/clubs/" + context.get("clubId") + "/bookshelves/" + context.get("meetingId");
        }
    },

    CLUB_BOOK_REVIEW("독서모임 한줄평") {
        @Override
        public String createRedirectUrl(String targetId, Map<String, String> context) {
            return "/api/clubs/" + context.get("clubId") + "/bookshelves/" + context.get("meetingId");
        }
    },

    CHAT("채팅") {
        @Override
        public String createRedirectUrl(String targetId, Map<String, String> context) {
            return "/api/clubs/" + context.get("clubId") + "/bookshelves/" + context.get("meetingId");
        }
    };

    private final String description;

    ReportTargetType(String description) {
        this.description = description;
    }

    public abstract String createRedirectUrl(String targetId, Map<String, String> context);
}