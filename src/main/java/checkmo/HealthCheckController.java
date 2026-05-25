package checkmo;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "헬스체크용", description = "헬스체크용 테스트용 사용 X")
public class HealthCheckController {

    @GetMapping("/health")
    public String home() {
        return "헬스체크 확인용 입니다.";
    }

}
