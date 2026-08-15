package checkmo.infra.s3.internal.listener;

import checkmo.bookStory.BookStoryEvent;
import checkmo.clubManagement.ClubManagementEvent.DeleteClubImageEvent;
import checkmo.clubNotice.ClubNoticeEvent;
import checkmo.common.monitoring.SentryCaptureClient;
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
    private final SentryCaptureClient sentryCaptureClient;

    @ApplicationModuleListener
    public void handleDeleteProfileImageEvent(MemberEvent.DeleteProfileImage event) {
        deleteSingleUrl(event.imageUrl(), "프로필 이미지", event);
    }

    @ApplicationModuleListener
    public void handleDeleteClubImageEvent(DeleteClubImageEvent event) {
        deleteSingleUrl(event.imageUrl(), "클럽 이미지", event);
    }

    @ApplicationModuleListener
    public void handleDeleteNoticeImageEvent(ClubNoticeEvent.DeleteNoticeImage event) {
        deleteUrls(event.imageUrls(), "공지사항 이미지", event);
    }

    @ApplicationModuleListener
    public void handleDeleteNoticeCommentImageEvent(ClubNoticeEvent.DeleteNoticeCommentImage event) {
        deleteUrls(event.imageUrls(), "공지사항 댓글 이미지", event);
    }

    @ApplicationModuleListener
    public void handleDeleteBookStoryImageEvent(BookStoryEvent.DeleteBookStoryImage event) {
        deleteUrls(event.imageUrls(), "책 이야기 이미지", event);
    }

    private void deleteUrls(List<String> urls, String label, Object event) {
        List<String> imageUrls = (urls == null) ? List.of() : urls;
        if (imageUrls.isEmpty()) {
            return;
        }
        imageUrls.stream()
                .filter(Objects::nonNull)
                .forEach(imageUrl -> deleteSingleUrl(imageUrl, label, event));
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
            sentryCaptureClient.captureException(e);
        }
    }
}
