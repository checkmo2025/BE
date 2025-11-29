package checkmo.infra.s3.internal.config;

import checkmo.infra.s3.internal.config.properties.S3Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(S3Properties.class)
public class S3Config {

    private final S3Properties s3Properties;

    @Bean
    public AwsCredentialsProvider awsCredentialsProvider() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(
                s3Properties.getCredentials().getAccessKey(),
                s3Properties.getCredentials().getSecretKey()
        );
        return StaticCredentialsProvider.create(awsCredentials);
    }

    @Bean
    public S3Client s3Client(AwsCredentialsProvider credentialsProvider) {
        return S3Client.builder()
                .region(Region.of(s3Properties.getRegion().getName()))
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Bean
    public S3Presigner s3Presigner(AwsCredentialsProvider credentialsProvider) {
        return S3Presigner.builder()
                .region(Region.of(s3Properties.getRegion().getName()))
                .credentialsProvider(credentialsProvider)
                .build();
    }
}
