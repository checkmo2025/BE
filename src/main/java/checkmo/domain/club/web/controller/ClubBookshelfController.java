package checkmo.domain.club.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping()
@RequiredArgsConstructor
@Tag(name = "모임 책장", description = "독서 모임 책장, 한줄평 관리 API")
public class ClubBookshelfController {

    // 책장 조회 (Meeting 기반)
    // GET /api/clubs/{clubId}/meetings - 책장(책장이 곧 Meeting) 전체 조회 ← 필터 적용 가능
    // GET /api/meetings/{meetingId} - 책장(책장이 곧 Meeting) 상세 화면 (책 정보, 발제들, 한줄평)

    // 한줄평(BookReview) 관리
    // GET /api/meetings/{meetingId}/reviews - 책(Meeting)에 대한 BookReview 전체 조회
    // POST /api/meetings/{meetingId}/reviews - 한줄평(Book Review) 등록
    // PATCH /api/meetings/{meetingId}/reviews/{reviewId} - 한줄평(Book Review) 수정
    // DELETE /api/meetings/{meetingId}/reviews/{reviewId} - 한줄평(Book Review) 삭제
}
