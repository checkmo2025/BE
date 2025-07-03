package checkmo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CheckmoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CheckmoApplication.class, args);
    }

}
