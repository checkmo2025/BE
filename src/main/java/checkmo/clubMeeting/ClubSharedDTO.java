package checkmo.clubMeeting;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 클럽(독서 모임) 도메인과 관련된 공유 DTO 클래스
 * 다른 도메인에서 클럽 정보를 참조할 때 사용
 */
public class ClubSharedDTO {

    /**
     * 내가 참여한 클럽 목록 응답 DTO
     * 사용자가 가입한 클럽들의 목록을 반환할 때 사용
     * 마이페이지에서 사용
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MyClubList {
        private List<MyClubInfo> clubList;
    }

    /**
     * 내 클럽 기본 정보 DTO
     * 클럽의 기본적인 정보(ID, 이름)를 포함
     * 사용자가 참여한 클럽을 간략히 표시할 때 사용
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MyClubInfo {
        private Long clubId;     // 모임 ID
        private String clubName; // 모임 이름
    }

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.EXISTING_PROPERTY,
            property = "tag",
            visible = true)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = MeetingNoticePreview.class, name = "모임"),
            @JsonSubTypes.Type(value = VotePreview.class, name = "투표"),
            @JsonSubTypes.Type(value = PureNoticePreview.class, name = "공지")
    })
    public sealed interface ClubUpdatePreview
            permits MeetingNoticePreview, VotePreview, PureNoticePreview {
        Long getId();
        Long getClubId();
        String getClubName();
        String getTitle();
        String getTag();
    }

    @Getter
    public static final class MeetingNoticePreview implements ClubUpdatePreview {
        private final Long id;
        private final Long clubId;
        private final String clubName;
        private final String title;
        private final String tag = "모임";

        // 모임과 연계된 공지 전용 필드
        private final LocalDateTime nextMeetingDate;
        private final String nextBookTitle;
        private final String bookImageUrl;

        @Builder
        public MeetingNoticePreview(Long id, Long clubId, String clubName, String title, LocalDateTime nextMeetingDate, String nextBookTitle, String bookImageUrl) {
            this.id = id;
            this.clubId = clubId;
            this.clubName = clubName;
            this.title = title;
            this.nextMeetingDate = nextMeetingDate;
            this.nextBookTitle = nextBookTitle;
            this.bookImageUrl = bookImageUrl;
        }
    }

    @Getter
    public static final class VotePreview implements ClubUpdatePreview {
        private final Long id; // 해당 클럽의 공지사항 ID
        private final Long clubId; // 해당 클럽의 ID
        private final String clubName;
        private final String title;
        private final String tag = "투표";

        // 투표 공지 전용 필드
        private final LocalDateTime meetingDate;
        private final String location;
        private final String details; // "뒷풀이 장소" 같은 추가 정보
        private final List<String> voteItems;

        @Builder
        public VotePreview(Long id, Long clubId, String clubName, String title, LocalDateTime meetingDate, String location, String details, List<String> voteItems) {
            this.id = id;
            this.clubId = clubId;
            this.clubName = clubName;
            this.title = title;
            this.meetingDate = meetingDate;
            this.location = location;
            this.details = details;
            this.voteItems = voteItems;
        }
    }

    @Getter
    public static final class PureNoticePreview implements ClubUpdatePreview {
        private final Long id;
        private final Long clubId;
        private final String clubName;
        private final String title;
        private final String tag = "공지";

        // 일반 공지 전용 필드
        private final String content;

        @Builder
        public PureNoticePreview(Long id, Long clubId, String clubName, String title, String content) {
            this.id = id;
            this.clubId = clubId;
            this.clubName = clubName;
            this.title = title;
            this.content = content;
        }
    }
}