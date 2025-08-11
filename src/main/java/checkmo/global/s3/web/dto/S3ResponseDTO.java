package checkmo.global.s3.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class S3ResponseDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PresignedUrlResponse {
        private String presignedUrl; //프론트가 사용할 URL
        private String imageUrl; //우리 백엔드에 저장할 URL
    }
}
