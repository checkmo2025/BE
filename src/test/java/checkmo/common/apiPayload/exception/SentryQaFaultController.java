package checkmo.common.apiPayload.exception;

import checkmo.common.apiPayload.code.status.ErrorStatus;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Profile("sentry-qa")
@RestController
@RequestMapping("/api/v1/test/sentry")
public class SentryQaFaultController {

    private final SentryQaCaptureState captureState;

    public SentryQaFaultController(SentryQaCaptureState captureState) {
        this.captureState = captureState;
    }

    @GetMapping("/unexpected")
    public void unexpected(@RequestParam(defaultValue = "unexpected") String message) {
        throw new RuntimeException(message);
    }

    @GetMapping("/bad-request")
    public void badRequest() {
        throw new IllegalArgumentException("bad request");
    }

    @GetMapping("/domain")
    public void domain() {
        throw new GeneralException(ErrorStatus._BAD_REQUEST);
    }

    @GetMapping("/domain-server-error")
    public void domainServerError() {
        throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/captures/count")
    public String captureCount() {
        return String.valueOf(captureState.count());
    }
}
