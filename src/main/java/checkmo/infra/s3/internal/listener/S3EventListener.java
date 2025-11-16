package checkmo.infra.s3.internal.listener;

import checkmo.infra.s3.internal.service.S3Service;
import checkmo.member.MemberEvent;
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
        try {
            String imageKey = s3Service.extractKeyFromUrl(event.imageUrl());

            if (imageKey == null) {
                log.warn("유효하지 않은 S3 URL: {}", event.imageUrl());
                return;
            }

            s3Service.deleteImage(imageKey);
        } catch (Exception e) {
            log.error("프로필 이미지 삭제 실패, event: {}", event, e);
            throw e;
        }
    }
}