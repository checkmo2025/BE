package checkmo.domain.club.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping()
@RequiredArgsConstructor
@Tag(name = "모임 토론", description = "독서 모임 미팅, 발제, 토론조 관리 API")
public class ClubMeetingController {

    // 미팅 관리
    // POST /api/clubs/{clubId}/meetings - Meeting 생성
    // GET /api/clubs/{clubId}/meetings - Meeting 전체 보기
    // GET /api/meetings/{meetingId} - Meeting 상세 보기

    // 토론조 관리
    // GET /api/meetings/{meetingId}/teams - Meeting 참여 인원 전체 조회
    // POST /api/meetings/{meetingId}/teams - 토론조 생성
    // GET api/meetings/{meetingId}?teamNumber=1 - Team에 속한 인원 전체보기

    // 발제(Topic) 관리
    // POST /api/meetings/{meetingId}/topics - Topic 등록
    // PATCH /api/meetings/{meetingId}/topics/{topicId} - Topic 수정
    // DELETE /api/meetings/{meetingId}/topics/{topicId} - Topic 삭제
    // GET /api/meetings/{meetingId}/topics - Meeting에 대한 Topic 전체보기

    // 팀-발제 연결 관리
    // POST /api/meetings/{meetingId}/teams/{teamId}/topics/{topicId}/select - Team에서 Topic 선택하기
    // GET /api/meetings/{meetingId}/teams/{teamId}/topics - Team별로 선택된 Topic 보기
}
