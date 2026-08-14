package checkmo.infra.s3.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import checkmo.infra.s3.internal.config.properties.S3Properties;
import checkmo.infra.s3.internal.entity.FileUploadType;
import java.net.MalformedURLException;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

class S3ServiceTest {

    private S3Service s3Service;

    @BeforeEach
    void setUp() throws MalformedURLException {
        S3Presigner presigner = mock(S3Presigner.class);
        PresignedPutObjectRequest presignedRequest = mock(PresignedPutObjectRequest.class);
        when(presignedRequest.url()).thenReturn(URI.create("https://upload.example.com").toURL());
        when(presigner.presignPutObject(any(software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest.class)))
                .thenReturn(presignedRequest);

        S3Properties properties = new S3Properties();
        properties.getS3().setBucket("test-bucket");
        properties.getRegion().setName("ap-northeast-2");
        s3Service = new S3Service(presigner, mock(S3Client.class), properties);
    }

    @Test
    void generatedUploadUrlContainsAuthenticatedMemberPath() {
        var response = s3Service.generatePresignedUploadUrl(
                "story.jpg",
                "image/jpeg",
                FileUploadType.BOOK_STORY,
                42L
        );

        assertThat(response.getImageUrl())
                .startsWith("https://test-bucket.s3.ap-northeast-2.amazonaws.com/images/book-stories/42/")
                .endsWith(".jpg");
    }

    @Test
    void extractsKeyOnlyFromCurrentBucketUrl() {
        String validUrl = "https://test-bucket.s3.ap-northeast-2.amazonaws.com/images/book-stories/42/file.jpg";

        assertThat(s3Service.extractKeyFromUrl(validUrl))
                .isEqualTo("images/book-stories/42/file.jpg");
        assertThat(s3Service.extractKeyFromUrl(validUrl.replace("test-bucket", "foreign-bucket"))).isNull();
        assertThat(s3Service.extractKeyFromUrl(validUrl.replace("https://", "http://"))).isNull();
        assertThat(s3Service.extractKeyFromUrl(validUrl.replace("images/", "images%2F"))).isNull();
    }
}
