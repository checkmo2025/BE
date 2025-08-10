package checkmo.domain.member.service.s3;

import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.apiPayload.exception.GeneralException;
import checkmo.config.properties.S3Properties;
import checkmo.domain.member.converter.MemberConverter;
import checkmo.domain.member.web.dto.MemberResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;
    private final S3Properties s3Properties;

    // 파일 업로드를 위한 presigned URL 생성
    public MemberResponseDTO.PresignedUrlDTO generatePresignedUploadUrl(String fileName, String contentType) {
        // S3에 저장될 파일의 고유 경로(key) 생성
        String key = generateUniqueKey(fileName);

        // 생성된 key를 기반으로 presigned url 생성
        String presignedUrl = generatePresignedUrl(key, contentType);

        // S3에 저장될 image url 생성
        String imageUrl = String.format("https://%s.s3.%s.amazonaws.com/%s",
                s3Properties.getS3().getBucket(),
                s3Properties.getRegion().getName(),
                key);

        return MemberConverter.toPresignedUrlDTO(presignedUrl, imageUrl);
    }

    // 파일 삭제 - 이건 우리가 직접 수행
    public void deleteImage(String key) {
        // key가 null이거나 비어있으면 예외 발생 -> 프론트에서 잘못 전달한 상황
        if (key == null || key.trim().isEmpty()) {
            throw new GeneralException(ErrorStatus.S3_FILE_DELETE_FAILED);
        }

        try {
            // S3에 파일 삭제 요청 객체 생성
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(s3Properties.getS3().getBucket())
                    .key(key)
                    .build();

            // S3에 파일 삭제 요청
            s3Client.deleteObject(deleteRequest);
        } catch (Exception e) {
            log.error("S3 파일 삭제 실패: {}", key, e);
            throw new GeneralException(ErrorStatus.S3_FILE_DELETE_FAILED, e.getMessage());
        }
    }

    // url에서 key 추출
    public String extractKeyFromUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }

        // S3에 저장된 것이 맞는지 확읺
        if (!url.contains("amazonaws.com/") || !url.startsWith("https://")) {
            return null;
        }

        // amazonaws.com/ 기준으로 분할 -> 이 바로 다음 부분이 key
        String[] parts = url.split("amazonaws.com/");
        if (parts.length <= 1) {
            return null;
        }

        String key = parts[1];

        // 만약 key 뒤에 쿼리 스트링이 있을 경우 제거
        int queryIndex = key.indexOf('?');
        return queryIndex > 0 ? key.substring(0, queryIndex) : key;
    }

    private String generatePresignedUrl(String key, String contentType) {
        try {
            // S3에 파일을 업로드하기 위한 PutObject 요청 객체 생성 - 저장할 때는 Put 방식으로 저장하기 때문
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3Properties.getS3().getBucket())
                    .key(key)
                    .contentType(contentType)
                    .build();

            // Presigned URL 생성을 위한 요청 객체 생성
            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(s3Properties.getS3().getPresignedUrlExpiration())
                    .putObjectRequest(putObjectRequest)
                    .build();

            // S3 Presigner를 통해 실제 presigned URL 생성
            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

            // presigned URL을 문자열로 변환
            String presignedUrl = presignedRequest.url().toString();

            log.info("새로운 key로 presigned url 생성: {}", presignedUrl);
            return presignedUrl;
        } catch (Exception e) {
            log.error("key를 통한 presigned url 생성 실패: {}", key, e);
            throw new GeneralException(ErrorStatus.S3_PRESIGNED_URL_GENERATION_FAILED, e.getMessage());
        }
    }

    private String generateUniqueKey(String originalFileName) {
        // 파일의 확장자 가져오기
        String extension = getFileExtension(originalFileName);

        // 해당 파일의 이름을  UUID로 생성
        String uniqueId = UUID.randomUUID().toString();

        // S3 버킷에 저장될 경로와 파일명을 생성 -> 이게 Key가 됨
        return String.format("profile-images/%s%s", uniqueId, extension);
    }

    private String getFileExtension(String fileName) {
        // 파일의 확장자가 제대로 명시되지 않은 경우
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }

        // 파일의 확장자 반환
        return fileName.substring(fileName.lastIndexOf("."));
    }
}
