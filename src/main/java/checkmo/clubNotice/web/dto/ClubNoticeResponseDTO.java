package checkmo.clubNotice.web.dto;

import checkmo.clubMeeting.ClubMeetingExternalDTO.DetailInfo;
import checkmo.member.MemberExternalDTO;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ClubNoticeResponseDTO {

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.EXISTING_PROPERTY,
            property = "tag",
            visible = true)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = MeetingNotice.class, name = "모임"),
            @JsonSubTypes.Type(value = VoteNotice.class, name = "투표"),
            @JsonSubTypes.Type(value = PureNotice.class, name = "공지")
    })
    public sealed interface NoticeItem
            permits PureNotice, MeetingNotice, VoteNotice {

        Long getId();

        String getTitle();

        boolean isImportant();

        String getTag();
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClubNoticeList {
        List<NoticeItem> noticeList; // 꼭 PureNoticeDTO, MeetingNoticeDTO, VoteDTO만 담아야 합니다!!
        private boolean hasNext;
        private Long nextCursor;
        private int pageSize;
        private boolean isStaff;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static final class PureNotice implements NoticeItem {
        private Long id;
        private String title;
        private String content;
        private boolean important;

        @Builder.Default
        private String tag = "공지";
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static final class MeetingNotice implements NoticeItem {
        private Long id;
        private String title;
        private String content;
        private boolean important;

        @Builder.Default
        private String tag = "모임";
        private DetailInfo detailInfoDTO; // 모임 정보 DTO
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static final class VoteNotice implements NoticeItem {
        private Long id;
        private String title;
        private String content;
        private boolean important;
        private boolean anonymity;
        private boolean duplication;

        private LocalDateTime startTime;
        private LocalDateTime deadline;

        @Builder.Default
        private String tag = "투표";
        private List<EachItem> items; // 투표 항목 목록
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EachItem {
        private String item;
        private boolean isSelected;
        private int voteCount; // 투표한 사람 수
        private List<MemberExternalDTO.BasicInfo> votedMembers; // 해당 항목에 투표한 멤버 닉네임과 프로필 사진 url
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClubNoticeDetail {
        private boolean isStaff;
        private NoticeItem noticeItem; // 공지사항 아이템 (PureNoticeDTO, MeetingNoticeDTO, VoteDTO 중 하나)
    }
}
