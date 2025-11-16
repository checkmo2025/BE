package checkmo.infra.s3.web.controller;

import checkmo.common.apiPayload.ApiResponse;
import checkmo.infra.s3.internal.service.S3Service;
import checkmo.infra.s3.web.dto.S3RequestDTO;
import checkmo.infra.s3.web.dto.S3ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/s3")
@RequiredArgsConstructor
@Tag(name = "S3 Image", description = "S3에 저장할 이미지에 대해서 presigned URL을 발급하는 API")
public class S3Controller {

    private final S3Service s3Service;

    @Operation(summary = "이미지용 presigned URL 발급 API", description = "프로필 이미지/독서 클럽의 이미지 업로드를 위한 S3 presigned URL을 발급합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요한 서비스 입니다.")
    })
    @PostMapping("/image/upload-url")
    public ApiResponse<S3ResponseDTO.PresignedUrl> getProfileImageUploadUrl(
            @Valid @RequestBody S3RequestDTO.ImageUpload request
    ) {
        return ApiResponse.onSuccess(s3Service.generatePresignedUploadUrl(request.getOriginalFileName(), request.getContentType()));
    }
}
