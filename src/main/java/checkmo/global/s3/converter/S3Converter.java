package checkmo.global.s3.converter;

import checkmo.global.s3.web.dto.S3ResponseDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class S3Converter {

    // =====================================================
    // 값들 ↔ DTO 변환
    // =====================================================

    /**
     * String들 -> MemberResponseDTO.PresignedUrlDTO 변환
     */
    public static S3ResponseDTO.PresignedUrlResponse toPresignedUrlDTO(
            String presignedUrl,
            String imageUrl
    ) {
        return S3ResponseDTO.PresignedUrlResponse.builder()
                .presignedUrl(presignedUrl)
                .imageUrl(imageUrl)
                .build();
    }
}
