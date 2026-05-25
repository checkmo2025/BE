package checkmo.report.web.dto;

import checkmo.report.internal.entity.ReportReason;
import checkmo.report.internal.entity.ReportTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ReportRequestDTO {

    @Getter
    @NoArgsConstructor
    public static class Create {

        @NotNull(message = "신고 대상 타입은 필수입니다.")
        @Schema(
                description = """
                        신고 대상 타입입니다.
                        
                        가능한 값:
                        - MEMBER: 사용자
                        - CLUB: 독서모임
                        - BOOK_STORY: 책 이야기
                        - BOOK_STORY_COMMENT: 책 이야기 댓글
                        - CLUB_NOTICE: 독서모임 공지사항
                        - CLUB_NOTICE_COMMENT: 독서모임 공지사항 댓글
                        - CLUB_TOPIC: 독서모임 발제
                        - CLUB_BOOK_REVIEW: 독서모임 한줄평
                        - CHAT: 채팅
                        """,
                example = "BOOK_STORY_COMMENT"
        )
        private ReportTargetType targetType;

        @NotBlank(message = "신고 대상 ID는 필수입니다.")
        @Schema(
                description = """
                        신고 대상 식별자입니다.
                        
                        targetType별 입력 규칙:
                        - MEMBER: 회원 ID가 아니라 회원 닉네임을 입력합니다. 예: tester1
                        - CLUB: 독서모임 ID를 문자열로 입력합니다. 예: "1"
                        - BOOK_STORY: 책 이야기 ID를 문자열로 입력합니다. 예: "10"
                        - BOOK_STORY_COMMENT: 책 이야기 댓글 ID를 문자열로 입력합니다. 예: "15"
                        - CLUB_NOTICE: 공지사항 ID를 문자열로 입력합니다. 예: "20"
                        - CLUB_NOTICE_COMMENT: 공지사항 댓글 ID를 문자열로 입력합니다. 예: "25"
                        - CLUB_TOPIC: 발제 ID를 문자열로 입력합니다. 예: "30"
                        - CLUB_BOOK_REVIEW: 한줄평 ID를 문자열로 입력합니다. 예: "35"
                        - CHAT: 채팅 메시지 ID를 문자열로 입력합니다. 예: "40"
                        
                        MEMBER를 제외한 모든 값은 Long 타입 ID를 문자열 형태로 전달합니다.
                        """,
                example = "15"
        )
        private String targetId;

        @NotNull(message = "신고 종류는 필수입니다.")
        @Schema(
                description = """
                        신고 종류입니다.
                        
                        가능한 값:
                        - GENERAL: 일반
                        - INSULT: 욕설/비방
                        - INAPPROPRIATE_CONTENT: 음란/부적절
                        - SPAM: 홍보/도배
                        """,
                example = "INSULT"
        )
        private ReportReason reason;

        @Size(max = 500, message = "신고 내용은 500자 이하여야 합니다.")
        private String content;
    }
}