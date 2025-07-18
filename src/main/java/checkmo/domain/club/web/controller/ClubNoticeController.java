package checkmo.domain.club.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping()
@RequiredArgsConstructor
@Tag(name = "모임 공지사항", description = "독서 모임 공지사항, 투표 생성 및 관리 API")
public class ClubNoticeController {

    // 공지사항 관리
    // POST /api/clubs/{clubId}/notices - 공지사항 생성 (Vote, Notice, Meeting 전부 포함)
    // DELETE /api/clubs/{clubId}/notices/{noticeId} - 공지사항 삭제하기
    // GET /api/clubs/{clubId}/notices?type=important - 중요 공지사항 보기
    // GET /api/clubs/{clubId}/notices?type=all - 공지사항 전체 보기
    // GET /api/clubs/{clubId}/notices/{noticeId} - 공지사항 상세보기

    // 투표 관리
    // GET /api/clubs/{clubId}/votes/{voteId}/results - 공지사항 투표 결과 조회
    // POST /api/clubs/{clubId}/votes/{voteId}/participate - 공지사항 투표하기
}
