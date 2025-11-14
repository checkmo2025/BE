package checkmo.clubNotice.web.dto;

import checkmo.clubMeeting.ClubMeetingExternalDTO;
import checkmo.clubNotice.ClubNoticeExternalDTO;
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
            @JsonSubTypes.Type(value = ClubNoticeExternalDTO.MeetingNoticePreview.class, name = "모임"),
            @JsonSubTypes.Type(value = ClubNoticeExternalDTO.VotePreview.class, name = "투표"),
            @JsonSubTypes.Type(value = ClubNoticeExternalDTO.PureNoticePreview.class, name = "공지")
    })
    public sealed interface NoticeItem
            permits PureNoticeDTO, MeetingNoticeDTO, VoteDTO {

        Long getId();

        String getTitle();

        boolean isImportant();

        String getTag();
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClubNoticeListDTO {
        List<NoticeItem> noticeList; // 꼭 PureNoticeDTO, MeetingNoticeDTO, VoteDTO만 담아야 합니다!!
        private boolean hasNext; // 다음 페이지 존재 여부
        private Long nextCursor; // 다음 페이지 커서 (마지막 항목의 ID)
        private int pageSize; // 현재 페이지 크기
        private boolean isStaff; // 본인이 모임의 스탭인지 여부 (true: 스탭, false: 일반 회원)
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MemberNoticeListDTO {
        List<ClubNoticeWithClubDTO> noticeList;
        private boolean hasNext;
        private Long nextCursor;
        private int pageSize;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClubNoticeWithClubDTO {
        private Long clubId;
        private String clubName;
        private NoticeItem notice; // 기존 NoticeItem 유지
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static final class PureNoticeDTO implements NoticeItem {
        private Long id; // 공지사항 ID
        private String title; // 공지사항 제목
        private String content; // 공지사항 내용
        private boolean important; // 중요 공지 여부 (true: 중요, false: 일반)

        @Builder.Default
        private String tag = "공지";
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static final class MeetingNoticeDTO implements NoticeItem {
        private Long id; // 공지사항 ID
        private String title; // 공지사항 제목
        private String content; // 공지사항 내용
        private boolean important; // 중요 공지 여부 (true: 중요, false: 일반)

        @Builder.Default
        private String tag = "모임"; // 공지사항 태그 (예: "공지", "이벤트")
        private ClubMeetingExternalDTO.MeetingInfo meetingInfoDTO; // 모임 정보 DTO
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static final class VoteDTO implements NoticeItem {
        private Long id; // 투표 ID
        private String title; // 공지사항 제목
        private String content; // 내용
        private boolean important; // 중요 공지 여부 (true: 중요, false: 일반)
        private boolean anonymity; // 익명 여부
        private boolean duplication; // 중복 여부

        private LocalDateTime startTime; // 시작 시간
        private LocalDateTime deadline; // 종료 시간

        @Builder.Default
        private String tag = "투표";
        private List<EachItemDTO> items; // 투표 항목 목록
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EachItemDTO {
        private String item;
        private boolean isSelected;
        private int voteCount; // 투표한 사람 수
        private List<MemberExternalDTO.BasicInfo> votedMembers; // 해당 항목에 투표한 멤버 닉네임과 프로필 사진 url
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClubNoticeDetailDTO {
        private boolean isStaff;
        private NoticeItem noticeItem; // 공지사항 아이템 (PureNoticeDTO, MeetingNoticeDTO, VoteDTO 중 하나)
    }
}
