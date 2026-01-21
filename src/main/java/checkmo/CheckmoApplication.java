package checkmo;

import io.awspring.cloud.autoconfigure.s3.S3AutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;



@EnableAsync
@EnableRetry
@EnableScheduling
@EnableJpaAuditing
@SpringBootApplication(
        exclude = {
                RedisRepositoriesAutoConfiguration.class,
                S3AutoConfiguration.class
        }
)
public class CheckmoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CheckmoApplication.class, args);
    }

}
