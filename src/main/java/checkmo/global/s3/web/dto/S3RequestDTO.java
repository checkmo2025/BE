package checkmo.global.s3.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class S3RequestDTO {

    @Getter
    @NoArgsConstructor
    public static class ImageUploadRequest {
        @NotBlank(message = "파일명은 필수입니다")
        @Schema(description = "업로드할 파일명", example = "profile.jpg")
        private String originalFileName;

        @NotBlank(message = "콘텐츠 타입은 필수입니다")
        @Schema(description = "파일의 콘텐츠 타입", example = "image/jpeg")
        private String contentType;
    }
}
