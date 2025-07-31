package checkmo;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/")
@Slf4j
@Tag(name = "Home", description = "헬스체크용 테스트 API")
public class HealthCheckController {

    @GetMapping
    public String home() {
        return "로드밸런서 사용 중인 홈 화면입니다. auto scaling 제대로 삭제되고 잘 되나 확인용.";
    }
}