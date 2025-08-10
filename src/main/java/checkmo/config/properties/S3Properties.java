package checkmo.config.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "cloud.aws")
public class S3Properties {

    @Valid
    private Credentials credentials = new Credentials();

    @Valid
    private Region region = new Region();

    @Valid
    private S3 s3 = new S3();

    @Getter
    @Setter
    public static class Credentials {
        @NotBlank(message = "S3 Access Key는 필수입니다")
        private String accessKey;

        @NotBlank(message = "S3 Secret Key는 필수입니다")
        private String secretKey;
    }

    @Getter
    @Setter
    public static class Region {
        @NotBlank(message = "S3 Region은 필수입니다")
        private String name;
    }

    @Getter
    @Setter
    public static class S3{
        @NotBlank(message = "S3 Bucket 이름은 필수입니다")
        private String bucket;

        @NotNull(message = "Presigned URL 만료시간은 필수입니다")
        private Duration presignedUrlExpiration = Duration.ofMinutes(10);
    }
}
