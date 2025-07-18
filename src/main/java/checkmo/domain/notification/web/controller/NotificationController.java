package checkmo.domain.notification.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping()
@RequiredArgsConstructor
@Tag(name = "알림", description = "알림 조회, 읽음 처리, 알림 설정 관련 API")
public class NotificationController {

    // 알림 전체 조회
    // GET /api/notifications?&isRead=false - 알림 전체 조회
    // PATCH /api/notifications/{notificationId}/read - 알림 읽음 처리
    // TODO:알림 설정은 현재 MemberController에 포함되어 있음 -> 상의 필요
}
