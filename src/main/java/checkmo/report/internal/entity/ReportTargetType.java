package checkmo.report.internal.entity;

import checkmo.report.internal.exception.ReportErrorStatus;
import checkmo.report.internal.exception.ReportException;
import lombok.Getter;

import java.util.Map;

@Getter
public enum ReportTargetType {

    MEMBER("사용자") {
        @Override
        public String createRedirectUrl(String targetId, Map<String, String> context) {
            return "/profile/" + targetId;
        }
    },

    CLUB("독서모임") {
        @Override
        public String createRedirectUrl(String targetId, Map<String, String> context) {
            return "/groups/" + targetId;
        }
    },

    BOOK_STORY("책 이야기") {
        @Override
        public String createRedirectUrl(String targetId, Map<String, String> context) {
            return "/stories/" + targetId;
        }
    },

    BOOK_STORY_COMMENT("책 이야기 댓글") {
        @Override
        public String createRedirectUrl(String targetId, Map<String, String> context) {
            return "/stories/"
                    + require(context, "bookStoryId")
                    + "?commentId="
                    + targetId;
        }
    },

    CLUB_NOTICE("독서모임 공지사항") {
        @Override
        public String createRedirectUrl(String targetId, Map<String, String> context) {
            return "/groups/"
                    + require(context, "clubId")
                    + "/notice/"
                    + targetId;
        }
    },

    CLUB_NOTICE_COMMENT("공지사항 댓글") {
        @Override
        public String createRedirectUrl(String targetId, Map<String, String> context) {
            return "/groups/"
                    + require(context, "clubId")
                    + "/notice/"
                    + require(context, "noticeId")
                    + "?commentId="
                    + targetId;
        }
    },

    CLUB_TOPIC("독서모임 발제") {
        @Override
        public String createRedirectUrl(String targetId, Map<String, String> context) {
            return "/groups/"
                    + require(context, "clubId")
                    + "/bookcase/"
                    + require(context, "meetingId")
                    + "?tab=topic&topicId="
                    + targetId;
        }
    },

    CLUB_BOOK_REVIEW("독서모임 한줄평") {
        @Override
        public String createRedirectUrl(String targetId, Map<String, String> context) {
            return "/groups/"
                    + require(context, "clubId")
                    + "/bookcase/"
                    + require(context, "meetingId")
                    + "?tab=review&reviewId="
                    + targetId;
        }
    },

    CHAT("채팅방") {
        @Override
        public String createRedirectUrl(String targetId, Map<String, String> context) {
            return "/groups/"
                    + require(context, "clubId")
                    + "/bookcase/"
                    + require(context, "meetingId")
                    + "/meeting?teamId="
                    + require(context, "teamId")
                    + "&messageId="
                    + targetId;
        }
    };

    private final String description;

    ReportTargetType(String description) {
        this.description = description;
    }

    private static String require(Map<String, String> context, String key) {
        if (context == null || !context.containsKey(key) || context.get(key) == null || context.get(key).isBlank()) {
            throw new ReportException(ReportErrorStatus.INVALID_REPORT_TARGET_ID);
        }

        return context.get(key);
    }

    public abstract String createRedirectUrl(String targetId, Map<String, String> context);
}
