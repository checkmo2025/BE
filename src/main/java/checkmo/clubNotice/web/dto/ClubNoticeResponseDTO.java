package checkmo.clubNotice.web.dto;

import checkmo.clubMeeting.ClubMeetingExternalDTO.DetailInfo;
import checkmo.clubNotice.internal.entity.NoticeTag;
import checkmo.member.MemberExternalDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ClubNoticeResponseDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClubNoticePreviewPage {
        private List<ClubNoticePreview> pinnedNotices;
        private NormalNoticePreviewPage normalNotices;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NormalNoticePreviewPage {
        private List<ClubNoticePreview> notices;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean hasNext;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClubNoticePreview {
        private Long id;
        private String title;
        @Getter(AccessLevel.NONE)
        private boolean pinned;
        private ClubNoticeTagItem tagItem;
        private LocalDateTime createdAt;

        @JsonProperty("isPinned")
        public boolean isPinned() {
            return pinned;
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClubNoticeTagItem {
        private String code;
        private String description;

        public static ClubNoticeTagItem from(NoticeTag noticeTag) {
            return ClubNoticeTagItem.builder()
                    .code(noticeTag.name())
                    .description(noticeTag.getDescription())
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static final class ClubNoticeDetail {
        private Long id;
        private String title;
        private String content;
        @Getter(AccessLevel.NONE)
        private boolean pinned;
        private ClubNoticeTagItem tag;
        private List<String> imageUrls;
        private LocalDateTime createdAt;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private DetailInfo meetingDetail; // 모임 공지인 경우에만 포함
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private VoteDetail voteDetail; // 투표 공지인 경우에만 포함

        @JsonProperty("isPinned")
        public boolean isPinned() {
            return pinned;
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VoteDetail {
        private Long id;
        private String title;
        private String content;
        private boolean anonymity;
        private boolean duplication;
        private LocalDateTime startTime;
        private LocalDateTime deadline;
        private List<EachItem> items; // 투표 항목 리스트
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EachItem {
        private int itemNumber; // 항목 번호 (1~6)
        private String item;
        @Getter(AccessLevel.NONE)
        private boolean selected; // 현재 로그인한 멤버가 해당 항목에 투표했는지 여부
        private int voteCount;
        private List<MemberExternalDTO.BasicInfo> votedMembers; // 해당 항목에 투표한 멤버 닉네임과 프로필 사진 url

        @JsonProperty("isSelected")
        public boolean isSelected() {
            return selected;
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NoticeCommentList {
        private List<NoticeComment> comments;
        private boolean hasNext;
        private Long nextCursor;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NoticeComment {
        private Long commentId;
        private MemberExternalDTO.BasicInfo authorInfo;
        private String content;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
