package checkmo.infra.s3.internal.listener;

import checkmo.clubNotice.ClubNoticeEvent;
import checkmo.infra.s3.internal.service.S3Service;
import checkmo.member.MemberEvent;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class S3EventListener {

    private final S3Service s3Service;

    @ApplicationModuleListener
    public void handleDeleteProfileImageEvent(MemberEvent.DeleteProfileImage event) {
        deleteSingleUrl(event.imageUrl(), "프로필 이미지", event);
    }

    @ApplicationModuleListener
    public void handleDeleteNoticeImageEvent(ClubNoticeEvent.DeleteNoticeImage event) {
        List<String> imageUrls = (event.imageUrls() == null) ? List.of() : event.imageUrls();
        if (imageUrls.isEmpty()) {
            return;
        }
        imageUrls.stream()
                .filter(Objects::nonNull)
                .forEach(imageUrl -> deleteSingleUrl(imageUrl, "공지사항 이미지", event));
    }

    // URL이 유효하지 않거나 S3 삭제 실패해도 로그만 남기고 무시
    private void deleteSingleUrl(String imageUrl, String label, Object event) {
        try {
            String imageKey = s3Service.extractKeyFromUrl(imageUrl);
            if (imageKey == null) {
                log.warn("유효하지 않은 S3 URL: label={}, url={}, event={}", label, imageUrl, event);
                return;
            }
            s3Service.deleteImage(imageKey);
        } catch (Exception e) {
            log.error("{} 삭제 실패, url={}, event={}", label, imageUrl, event, e);
        }
    }
}