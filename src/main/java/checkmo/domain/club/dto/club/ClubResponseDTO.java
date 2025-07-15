package checkmo.domain.club.dto.club;

import checkmo.domain.club.dto.meeting.MeetingResponseDTO;
import checkmo.global.dto.BookSharedDTO;
import checkmo.global.dto.MemberSharedDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class ClubResponseDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClubListDTO {
        private List<ClubDetailDTO> clubList; // 모임 목록
        private boolean hasNext; // 다음 페이지 존재 여부
        private Long nextCursor; // 다음 페이지 커서 (마지막 항목의 ID)
        private int pageSize; // 현재 페이지 크기
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MyClubListDTO {
        private List<ClubInfoDTO> clubList; // 모임 목록
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClubInfoDTO {
        private Long clubId;         // 모임 ID
        private String clubName;     // 모임 이름, joinClub의 반환값에서 사용될 때는 null
        private Boolean isOpen;      // 모임 공개 여부 (true: 공개, false: 비공개), MyClubListDTO-ClubInfoDTO에서 사용될 때는 null
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClubDetailDTO {
        private Long clubId;         // 모임 ID
        private String profileImgUrl;// 모임 프로필 이미지 URL
        private String clubName;     // 모임 이름
        private String description;  // 모임 소개
        private String participants; // 모임 대상
        private List<String> category;     // 모임 카테고리
        private String region;       // 모임 장소
        private String insta;        // 모임 인스타그램 URL
        private String kakao;        // 모임 카카오톡 URL
        private boolean isOpen;      // 모임 공개 여부 (true: 공개, false: 비공개)
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClubMemberListDTO {
        private List<ClubMemberDTO> clubMembers; // 모임 회원 목록
        private boolean hasNext; // 다음 페이지 존재 여부
        private Long nextCursor; // 다음 페이지 커서 (마지막 항목의 ID)
        private int pageSize; // 현재 페이지 크기
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClubMemberDTO {
        private String nickname; // 회원의 닉네임
        private String profileImgUrl; // 회원의 프로필 이미지 URL
        private String joinMessage; // 회원의 가입 메시지, ClubMemberStatus가 PENDING인 경우에만 사용됨
        private String clubMemberStatus; // 회원의 상태 (예: "MEMBER", "STAFF", "PENDING", "BLOCKED")
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
    }

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
    public static final class PureNoticeDTO implements NoticeItem {
        private Long id; // 공지사항 ID
        private String title; // 공지사항 제목
        private String content; // 공지사항 내용
        private boolean important; // 중요 공지 여부 (true: 중요, false: 일반)
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
        private String tag = "모임"; // 공지사항 태그 (예: "공지", "이벤트")
        private MeetingResponseDTO.MeetingInfoDTO meetingInfoDTO; // 모임 정보 DTO
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static final class VoteDTO implements NoticeItem {
        private Long id; // 투표 ID
        private String title; // 공지사항 제목
        private boolean important; // 중요 공지 여부 (true: 중요, false: 일반)
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
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClubNoticeDetailDTO {
        private NoticeItem noticeItem; // 공지사항 아이템 (PureNoticeDTO, MeetingNoticeDTO, VoteDTO 중 하나)
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookRecommendListDTO {
        private List<BookRecommendDetailDTO> bookRecommendList; // 추천 책 목록
        private boolean hasNext; // 다음 페이지 존재 여부
        private Long nextCursor; // 다음 페이지 커서 (마지막 항목의 ID)
        private int pageSize; // 현재 페이지 크기
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookRecommendDetailDTO {
        private Long id; // 추천 책 ID
        private String content; // 추천 내용
        private double rate; // 평점
        private String tag; // 추천 태그
        private BookSharedDTO.BasicInfoDTO bookInfo; // 책 정보 - 공용 DTO 사용
        private MemberSharedDTO.BasicInfoDTO authorInfo; // 추천책 작성한 회원 정보 - 공용 DTO 사용
    }
}
