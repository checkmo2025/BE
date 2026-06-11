package checkmo.common.monitoring;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SentrySmokeController {

    @PostMapping("/internal/monitoring/sentry-smoke/9f3c2b8e7a6d4c11")
    public void smoke() {
        throw new IllegalStateException("Sentry smoke test");
    }
}
