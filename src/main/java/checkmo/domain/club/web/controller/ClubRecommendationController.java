package checkmo.domain.club.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping()
@RequiredArgsConstructor
@Tag(name = "모임 추천 책", description = "독서 모임 내 책 추천 및 관리 API")
public class ClubRecommendationController {

    // 추천 책 관리
    // POST /api/clubs/{clubId}/recommendations - 추천 책 작성
    // PATCH /api/clubs/{clubId}/recommendations/{recommendId} - 추천 책 수정
    // DELETE /api/clubs/{clubId}/recommendations/{recommendId} - 추천 책 삭제
    // GET /api/clubs/{clubId}/recommendations - 추천 책 전체 조회
    // GET /api/clubs/{clubId}/recommendations/{recommendId} - 추천 책 상세 조회
}
